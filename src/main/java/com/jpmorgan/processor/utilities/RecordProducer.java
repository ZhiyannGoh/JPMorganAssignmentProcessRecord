package com.jpmorgan.processor.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class RecordProducer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RecordConsumer.class);

    private final BlockingQueue<String> recordQueue;
    private final InputStream inputStream;
    private final String poisonPill;
    private final int numberOfThreads;

    public RecordProducer(BlockingQueue<String> recordQueue, InputStream inputStream,
            String poisonPill, int numberOfThreads) {
        this.recordQueue = recordQueue;
        this.inputStream = inputStream;
        this.poisonPill = poisonPill;
        this.numberOfThreads = numberOfThreads;
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

            // Skip header row
            br.readLine();

            String record;

            // Instead of reading by row, we can have mutiple producer produce by bytes
            while ((record = br.readLine()) != null) {
                if (StringUtils.isNotBlank(record)) {
                    recordQueue.put(record);
                    // logger.info("Produced: " + record);
                }
            }

            for (int i = 0; i < numberOfThreads; i++) {
                recordQueue.put(poisonPill);
            }

            // logger.info("Stopping");
            return;

        } catch (InterruptedException e) {
            logger.info("InterruptedException while producing record");
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Producer error has occurred");
        }
    }

}
