package com.jpmorgan;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.jpmorgan.processor.handlers.FileAggregatorHandler;
import com.jpmorgan.processor.handlers.FileHeaderHandler;
import com.jpmorgan.processor.handlers.FilePostHandler;
import com.jpmorgan.processor.handlers.FileProcessorHandler;

@ExtendWith(SpringExtension.class)
public class FileProcessorHandlerTest {

    @Mock
    FileHeaderHandler fileHeaderHandlerMock;

    @Mock
    FileAggregatorHandler fileAggregatorHandlerMock;

    @Mock
    FilePostHandler filePostHandlerMock;

    @Test
    public void testChaining() {
        try (MockedStatic<FileProcessorHandler> mockStatic =
                mockStatic(FileProcessorHandler.class)) {
        }
        FileProcessorHandler
                .chain(Lists.list(fileHeaderHandlerMock, fileAggregatorHandlerMock, filePostHandlerMock));

        assertTrue(fileHeaderHandlerMock.nextHandler == fileAggregatorHandlerMock);
        assertTrue(fileAggregatorHandlerMock.nextHandler == filePostHandlerMock);
        assertTrue(filePostHandlerMock.nextHandler == null);
    }
}
