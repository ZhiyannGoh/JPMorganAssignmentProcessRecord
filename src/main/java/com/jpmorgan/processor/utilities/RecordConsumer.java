package com.jpmorgan.processor.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.jpmorgan.processor.constants.FileHeader;
import com.jpmorgan.processor.models.MorganFile;

public class RecordConsumer {

    private static final Logger logger = LoggerFactory.getLogger(RecordConsumer.class);

    private final BlockingQueue<String> recordQueue;
    private final Map<FileHeader, Integer> headerMetadata;
    private final String poisonPill;

    public RecordConsumer(BlockingQueue<String> recordQueue,
            Map<FileHeader, Integer> headerMetadata, String poisonPill) {
        this.recordQueue = recordQueue;
        this.headerMetadata = headerMetadata;
        this.poisonPill = poisonPill;
    }

    public List<MorganFile> runTask() {
        List<String> records = new ArrayList<>();
        while (true) {
            try {
                String record = recordQueue.take();
                if (record.equalsIgnoreCase(poisonPill)) {
                    break;
                }
                records.add(record);
                // logger.info(Thread.currentThread().getName() + " consumed: " + record);

            } catch (InterruptedException e) {
                String message = "InterruptedException while consuming record";
                logger.info(message);
                e.printStackTrace();
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
            }
        }
        return getMorganFilesFromRecords(records);
    }

    private List<MorganFile> getMorganFilesFromRecords(List<String> records) {
        List<MorganFile> morganFiles = new ArrayList<>();
        records.stream().distinct().forEach(s -> {

            String[] colArr = s.split(",");
            MorganFile morganRecord =
                    MorganFile.builder().custID(getColumnValue(colArr, FileHeader.CUSTOMER_ID))
                            .custName(getColumnValue(colArr, FileHeader.CUSTOMER_NAME))
                            .countryCode(getColumnValue(colArr, FileHeader.COUNTRY_CODE))
                            .bankCode(getColumnValue(colArr, FileHeader.BANK_CODE)).build();
            morganFiles.add(morganRecord);

        });
        return morganFiles;
    }

    private String getColumnValue(String[] colArr, FileHeader column) {
        return colArr[headerMetadata.get(column)].trim();
    }

}
