package com.jpmorgan.processor.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.jpmorgan.processor.dto.FileDto;
import com.jpmorgan.processor.utilities.FileSystemUtils;

@Component
public class RequestProcessorHandler {

        private static final Logger logger = LoggerFactory.getLogger(RequestProcessorHandler.class);

        @Autowired
        FileUploadHandler fileUploadHandler;

        @Autowired
        FileHeaderHandler fileHeaderHandler;

        @Autowired
        FileAggregatorHandler fileAggregatorHandler;

        @Autowired
        FilePostHandler filePostHandler;

        public ResponseEntity<String> getAggregatedData(MultipartFile file) {

                String baseFilename = FilenameUtils.getBaseName(file.getOriginalFilename());
                String requestId = baseFilename + "_" + System.currentTimeMillis();

                FileDto fileDto = FileDto.builder().requestId(requestId).userFile(file).build();

                fileUploadHandler.nextHandler = FileProcessorHandler.chain(Arrays
                                .asList(fileHeaderHandler, fileAggregatorHandler, filePostHandler));
                fileUploadHandler.handleRequest(fileDto);

                File outputFile = new File(FileSystemUtils.FILE_COMPLETED_DIR + requestId);

                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.add("Content-Type", "text/plain; charset=utf-8");

                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new FileReader(outputFile))) {
                        String record;
                        String newLine = "";
                        while ((record = br.readLine()) != null) {
                                sb.append(newLine);
                                sb.append(record);
                                newLine = "\n";
                        }
                        br.close();
                } catch (Exception e) {
                        logger.error("Exception has occured", e);
                }
                return new ResponseEntity<>(sb.toString(), responseHeaders, HttpStatus.OK);
        }

}
