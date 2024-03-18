package com.jpmorgan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.jpmorgan.processor.dto.FileDto;
import com.jpmorgan.processor.handlers.FileUploadHandler;

@ExtendWith(SpringExtension.class)
public class FileUploadHandlerTest {

    @InjectMocks
    FileUploadHandler fileUploadHandler;

    @Test
    public void testHandleRequest() throws IOException {

        FileDto fileDtoMock = mock(FileDto.class);
        MultipartFile multipartFileMock = mock(MultipartFile.class);

        when(fileDtoMock.getRequestId()).thenReturn("SOME_STRING");
        when(fileDtoMock.getUserFile()).thenReturn(multipartFileMock);
        try (MockedStatic<FileCopyUtils> mockStatic = mockStatic(FileCopyUtils.class)) {
            FileCopyUtils.copy(any(InputStream.class), any(OutputStream.class));
        }

        fileUploadHandler.handleRequest(fileDtoMock);
        verify(fileDtoMock, times(1)).getUserFile();
        verify(fileDtoMock, times(1)).getRequestId();
    }

    @Test
    public void testNullPointerExceptionIsThrown() {

        FileDto fileDtoMock = mock(FileDto.class);

        try {
            fileUploadHandler.handleRequest(fileDtoMock);
            fail();
        } catch (ResponseStatusException ex) {
            assertEquals("NullPointerException occured during uploadHandler",
                    ex.getBody().getDetail());
        }
    }

    @Test
    public void testIllegalStateExceptionIsThrown() {

        FileDto fileDtoMock = mock(FileDto.class);
        MultipartFile multipartFileMock = mock(MultipartFile.class);

        when(fileDtoMock.getRequestId()).thenReturn("SOME_STRING");
        when(fileDtoMock.getUserFile()).thenReturn(multipartFileMock);

        try (MockedStatic<FileCopyUtils> mockStatic = mockStatic(FileCopyUtils.class)) {
            mockStatic
                    .when(() -> FileCopyUtils.copy(any(InputStream.class), any(OutputStream.class)))
                    .thenThrow(IllegalStateException.class);
        }

        try {
            fileUploadHandler.handleRequest(fileDtoMock);
        } catch (ResponseStatusException ex) {
            assertEquals("IllegalStateException occured during uploadHandler",
                    ex.getBody().getDetail());
        }
    }

    @Test
    public void testIOExceptionIsThrown() {

        FileDto fileDtoMock = mock(FileDto.class);
        MultipartFile multipartFileMock = mock(MultipartFile.class);

        when(fileDtoMock.getRequestId()).thenReturn("SOME_STRING");
        when(fileDtoMock.getUserFile()).thenReturn(multipartFileMock);

        try (MockedStatic<FileCopyUtils> mockStatic = mockStatic(FileCopyUtils.class)) {
            mockStatic
                    .when(() -> FileCopyUtils.copy(any(InputStream.class), any(OutputStream.class)))
                    .thenThrow(IOException.class);
        }

        try {
            fileUploadHandler.handleRequest(fileDtoMock);
        } catch (ResponseStatusException ex) {
            assertEquals("IOException occured during uploadHandler", ex.getBody().getDetail());
        }
    }
}
