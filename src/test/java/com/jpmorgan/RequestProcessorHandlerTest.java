package com.jpmorgan;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import com.jpmorgan.processor.handlers.FileAggregatorHandler;
import com.jpmorgan.processor.handlers.FileHeaderHandler;
import com.jpmorgan.processor.handlers.FilePostHandler;
import com.jpmorgan.processor.handlers.FileProcessorHandler;
import com.jpmorgan.processor.handlers.FileUploadHandler;
import com.jpmorgan.processor.handlers.RequestProcessorHandler;

@ExtendWith(SpringExtension.class)
public class RequestProcessorHandlerTest {

    @InjectMocks
    RequestProcessorHandler requestProcessorHandler;

    @Mock
    FileUploadHandler fileUploadHandler;

    @Mock
    FileHeaderHandler fileHeaderHandler;

    @Mock
    FileAggregatorHandler fileAggregatorHandler;

    @Mock
    FilePostHandler filePostHandler;


    @Test
    public void testGetAggregatedData() {

        MultipartFile fileMock = mock(MultipartFile.class);

        try (MockedStatic<FileProcessorHandler> mockStatic =
                mockStatic(FileProcessorHandler.class)) {
        }
        FileProcessorHandler.chain(Collections.emptyList());

        requestProcessorHandler.getAggregatedData(fileMock);
        verify(fileUploadHandler, times(1)).handleRequest(any());
    }


}
