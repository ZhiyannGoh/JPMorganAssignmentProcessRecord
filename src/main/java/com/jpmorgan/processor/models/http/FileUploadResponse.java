package com.jpmorgan.processor.models.http;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileUploadResponse {

    // This class was created to return a more "standard" way of Response
    // Response was later updated to return csv format to "adhere" to the requirements I have intepreted

    private String filename;
    private String uploadRequestId;
    private String message;

}
