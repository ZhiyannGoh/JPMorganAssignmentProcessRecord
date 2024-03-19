package com.jpmorgan;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.jpmorgan.processor.apis.UploadFileApi;
import com.jpmorgan.processor.handlers.RequestProcessorHandler;

@ExtendWith(SpringExtension.class)
public class UploadFileApiTest {

    @InjectMocks
    UploadFileApi uploadFileApi;

    @Mock
    RequestProcessorHandler requestProcessorHandler;

    @Test
    public void testFileNotUploaded() throws Exception {

        MultipartFile multipartFileMock = mock(MultipartFile.class);
        when(multipartFileMock.getSize()).thenReturn(Long.valueOf(0));

        try {
            uploadFileApi.uploadFile(multipartFileMock);
        } catch (ResponseStatusException ex) {
            assertSame("File should not be empty", ex.getBody().getDetail());
        }

    }

    @Test
    public void testFileUploaded() throws Exception {

        MultipartFile multipartFileMock = mock(MultipartFile.class);
        when(multipartFileMock.getSize()).thenReturn(Long.valueOf(1));

        ResponseEntity<String> responseEntityMock =
                new ResponseEntity<>("some response body", HttpStatus.OK);

        when(requestProcessorHandler.getAggregatedData(any())).thenReturn(responseEntityMock);

        uploadFileApi.uploadFile(multipartFileMock);

        verify(requestProcessorHandler, atMostOnce()).getAggregatedData(any());

    }

}
