package com.jpmorgan.processor.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import com.jpmorgan.processor.constants.FileHeader;
import com.jpmorgan.processor.dto.FileDto;
import com.jpmorgan.processor.models.MorganFile;
import com.jpmorgan.processor.utilities.RecordConsumer;
import com.jpmorgan.processor.utilities.RecordProducer;

@Component
public class FileAggregatorHandler extends FileProcessorHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileAggregatorHandler.class);

    private static final List<FileHeader> mandatoryColumn = new ArrayList<>();
    static {
        mandatoryColumn.add(FileHeader.BANK_CODE);
        mandatoryColumn.add(FileHeader.CUSTOMER_ID);
    }

    @Value("${EXECUTE_SYNC_CALL}")
    private boolean syncCallEnabled;

    @Value("${MAX_THREAD_POOL}")
    private int numberOfThreads;

    @Override
    public void handleRequest(FileDto fileDto) {
        preProcess(fileDto);

        getAsyncCallResultMap(fileDto);

        // This was used to compare the difference between
        // processing line-by-line and multi-line
        if (syncCallEnabled) {
            getSyncCallResultMap(fileDto);
        }

        /**
         * Observation: nThread have correlation between input
         *
         * 1) No point having 3 records with 3 thread -> thread overhead might make it slower than
         * sequence call
         *
         * (Threaded) Start of aggregating: 1710736839688 (Threaded) End of aggregating:
         * 1710736839722 (Single) Start of aggregating: 1710736839722 (Single) End of aggregating:
         * 1710736839754
         *
         * (Threaded) Start of aggregating: 1710736843641 (Threaded) End of aggregating:
         * 1710736843658 (Single) Start of aggregating: 1710736843658 (Single) End of aggregating:
         * 1710736843690
         *
         * (Threaded) Start of aggregating: 1710736843933 (Threaded) End of aggregating:
         * 1710736843946 (Single) Start of aggregating: 1710736843946 (Single) End of aggregating:
         * 1710736843964
         *
         * (Threaded) Start of aggregating: 1710736844297 (Threaded) End of aggregating:
         * 1710736844306 (Single) Start of aggregating: 1710736844306 (Single) End of aggregating:
         * 1710736844318
         */

        validateAndProcessNextChaining(fileDto);
    }

    private void getAsyncCallResultMap(FileDto fileDto) {
        BlockingQueue<String> recordQueue = new LinkedBlockingQueue<>();

        logger.info("(Threaded) Start of aggregating: " + System.currentTimeMillis());

        String POISON_PILL = "EOP";

        // Alternatively we can use MQ
        ExecutorService pExecutorService = Executors.newSingleThreadExecutor();
        ExecutorService cExecutorService = Executors.newFixedThreadPool(numberOfThreads);

        try {
            pExecutorService.submit(new RecordProducer(recordQueue,
                    fileDto.getUserFile().getInputStream(), POISON_PILL, numberOfThreads));
            pExecutorService.shutdown();
        } catch (IOException e) {
            logger.error("InputStream error has occurred");
            e.printStackTrace();
        }

        logger.info("Pool Size: " + numberOfThreads);

        List<CompletableFuture<List<MorganFile>>> aFutures = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            aFutures.add(
                    CompletableFuture
                            .supplyAsync(
                                    () -> new RecordConsumer(recordQueue,
                                            fileDto.getHeaderMetadata(), POISON_PILL).runTask(),
                                    cExecutorService));
        }

        Map<String, Map<String, Long>> asyncCallWithBankCodeAndCustIdMap = new HashMap<>();
        List<MorganFile> asyncCallMorganFiles = new ArrayList<>();
        for (int j = 0; j < aFutures.size(); j++) {
            aFutures.parallelStream().forEach(futures -> {
                try {
                    asyncCallMorganFiles.addAll(futures.get());
                } catch (InterruptedException | ExecutionException e) {
                    logger.info("Exception called when calling completable future");
                    e.printStackTrace();
                }

            });
        }
        asyncCallWithBankCodeAndCustIdMap =
                asyncCallMorganFiles.stream().collect(Collectors.groupingBy(MorganFile::getBankCode,
                        Collectors.groupingBy(MorganFile::getCustID, Collectors.counting())));
        Map<String, Long> asyncCallResultMap = new HashMap<>();
        for (String bankCode : asyncCallWithBankCodeAndCustIdMap.keySet()) {
            asyncCallResultMap.put(bankCode,
                    Long.valueOf(asyncCallWithBankCodeAndCustIdMap.get(bankCode).size()));
        }


        logger.info("(Threaded) End of aggregating: " + System.currentTimeMillis());
        logger.info(asyncCallResultMap.toString());
        fileDto.setResultMap(asyncCallResultMap);
        terminateConsumerExecutorService(pExecutorService, cExecutorService);
    }

    private void getSyncCallResultMap(FileDto fileDto) {
        List<MorganFile> syncCallMorganFiles = new ArrayList<>();
        logger.info("(Single) Start of aggregating: " + System.currentTimeMillis());

        try (BufferedReader br =
                new BufferedReader(new InputStreamReader(fileDto.getUserFile().getInputStream()))) {

            // Skip header row
            br.readLine();

            String record = null;
            Map<FileHeader, Integer> headerMetadata = fileDto.getHeaderMetadata();
            while ((record = br.readLine()) != null) {
                String[] colArr = record.split(",");
                MorganFile morganFile = MorganFile.builder()
                        .custID(getColumnValue(colArr, FileHeader.CUSTOMER_ID, headerMetadata))
                        .bankCode(getColumnValue(colArr, FileHeader.BANK_CODE, headerMetadata))
                        .build();
                syncCallMorganFiles.add(morganFile);
            }
        } catch (IOException e) {
            logger.info("IOException when getting InputStream from DTO in synchronus call");
            e.printStackTrace();
        }

        Map<String, Long> syncCallResultMap = syncCallMorganFiles.stream().distinct()
                .collect(Collectors.groupingBy(MorganFile::getBankCode, Collectors.counting()));

        logger.info("(Single) End of aggregating: " + System.currentTimeMillis());
        logger.info(syncCallResultMap.toString());
        fileDto.setResultMap(syncCallResultMap);
    }

    private void terminateConsumerExecutorService(ExecutorService producerThread,
            ExecutorService executorService) {
        if (producerThread.isShutdown()) {
            executorService.shutdown();
        }
    }

    private String getColumnValue(String[] colArr, FileHeader column,
            Map<FileHeader, Integer> headerMetadata) {
        return colArr[headerMetadata.get(column)].trim();
    }


    @Override
    public void validateAndProcessNextChaining(FileDto fileDto) {
        processNextChain(fileDto);
    }

    private void preProcess(FileDto fileDto) {
        mandatoryColumn.stream().forEach(column -> {
            if (!fileDto.getHeaderMetadata().containsKey(column)) {

                String message =
                        String.format("Request Id: %s, Column: %s is missing, MANDATORY_FIELDS: %s",
                                fileDto.getRequestId(), column.name(), mandatoryColumn.toString());

                logger.error(message);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message");
            }
        });
    }
}
