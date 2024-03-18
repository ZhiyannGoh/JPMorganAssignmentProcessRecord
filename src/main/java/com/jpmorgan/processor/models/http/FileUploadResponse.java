package com.jpmorgan.processor.models.http;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileUploadResponse {

    private String filename;
    private String uploadRequestId;
    private String message;

}
