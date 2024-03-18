package com.jpmorgan.processor.handlers;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.jpmorgan.processor.dto.FileDto;

@Component
public abstract class FileProcessorHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileProcessorHandler.class);

    public FileProcessorHandler nextHandler = null;

    public abstract void handleRequest(FileDto fileDto);

    public abstract void validateAndProcessNextChaining(FileDto fileDto);

    public static FileProcessorHandler chain(List<FileProcessorHandler> chain) {
        FileProcessorHandler handler = null;
        FileProcessorHandler start = null;
        
        for (int i = 0; i < chain.size(); i++) {
            if (i == 0) {
                start = chain.get(i);
                handler = start;
            } else {
                handler.nextHandler = chain.get(i);
                handler = handler.nextHandler;
            }
        }

        return start;
    }

    protected void processNextChain(FileDto fileDto) {
        if (validateNextChain()) {
            nextHandler.handleRequest(fileDto);
        } else {
            logger.info("No chaining is present");
        }
    }

    protected boolean validateNextChain() {
        return nextHandler != null;
    }

}
