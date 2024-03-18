package com.jpmorgan.processor.apis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.jpmorgan.processor.handlers.RequestProcessorHandler;

@RestController
public class UploadFileApi {

    @Autowired
    RequestProcessorHandler requestProcessorHandler;

    @PostMapping("/upload-and-process-file")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file)
            throws Exception {

        if (file.getSize() == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File should not be empty");
        }

        return requestProcessorHandler.getAggregatedData(file);
    }

}
