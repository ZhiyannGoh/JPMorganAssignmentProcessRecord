package com.jpmorgan.processor.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.jpmorgan.processor.constants.FileHeader;
import com.jpmorgan.processor.dto.FileDto;
import com.jpmorgan.processor.utilities.FileSystemUtils;

@Component
public class FileHeaderHandler extends FileProcessorHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileHeaderHandler.class);

    @Autowired
    FileSystemUtils fileSystemUtils;

    @Override
    public void handleRequest(FileDto fileDto) {

        if (fileDto.getUserFile() == null) {
            String message = "MultipartFile is missing";
            logger.info(message);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        }

        MultipartFile userFile = fileDto.getUserFile();
        try (BufferedReader br =
                new BufferedReader(new InputStreamReader(userFile.getInputStream()))) {

            String headerStr = br.readLine();
            fileDto.setHeaderMetadata(getHeaderColumnOrder(headerStr));

        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Reading error has occurred");
        }

        validateAndProcessNextChaining(fileDto);
    }

    private Map<FileHeader, Integer> getHeaderColumnOrder(String headerStr) {
        Map<FileHeader, Integer> headerOrder = new HashMap<>();
        String[] headerArr = headerStr.split(",");

        for (int i = 0; i < headerArr.length; i++) {

            // Assumption: input file might have header in different order
            // 1. cust, bank, country
            // 2. bank, country, cust

            String header = headerArr[i].trim();
            Optional<FileHeader> fileHeader = FileHeader.getFileHeaderEnumByValue(header);
            if (fileHeader.isPresent()) {
                headerOrder.put(fileHeader.get(), i);
            } else {
                logger.info("Column is not recongized");
            }
        }

        return headerOrder;
    }

    @Override
    public void validateAndProcessNextChaining(FileDto fileDto) {
        if (!fileDto.getHeaderMetadata().isEmpty()) {
            processNextChain(fileDto);
        } else {
            logger.warn("Header Metadata is missing. Aborting");
            fileSystemUtils.moveFileToNewState(FileSystemUtils.FILE_UPLOAD_DIR,
                    FileSystemUtils.FILE_FAILED_DIR, fileDto.getRequestId());
        }
    }

}
