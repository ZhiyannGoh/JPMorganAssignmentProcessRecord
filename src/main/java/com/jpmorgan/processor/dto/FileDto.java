package com.jpmorgan.processor.dto;

import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import com.jpmorgan.processor.constants.FileHeader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDto {


    private MultipartFile userFile;

    private String requestId;

    private Map<FileHeader, Integer> headerMetadata;

    private Map<String, Long> resultMap;

}
