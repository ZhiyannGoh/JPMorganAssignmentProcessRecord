package com.jpmorgan.processor.handlers;

import java.io.IOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import com.jpmorgan.processor.dto.FileDto;
import com.jpmorgan.processor.utilities.FileSystemUtils;

@Component
public class FileUploadHandler extends FileProcessorHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadHandler.class);

    @Override
    public void handleRequest(FileDto fileDto) {

        Path path = Path.of(FileSystemUtils.FILE_UPLOAD_DIR + fileDto.getRequestId());
        try {
            fileDto.getUserFile().transferTo(path);
        } catch (IllegalStateException e) {
            System.out.println("1");
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "IllegalStateException occured during uploadHandler");
        } catch (IOException e) {
            System.out.println("2");
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "IOException occured during uploadHandler");
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "NullPointerException occured during uploadHandler");
        }

        logger.info("File is saved.");
        validateAndProcessNextChaining(fileDto);
    }

    @Override
    public void validateAndProcessNextChaining(FileDto fileDto) {
        processNextChain(fileDto);
    }

}
