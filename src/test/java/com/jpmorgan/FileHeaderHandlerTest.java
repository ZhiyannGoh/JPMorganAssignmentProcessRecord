package com.jpmorgan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.jpmorgan.processor.constants.FileHeader;
import com.jpmorgan.processor.dto.FileDto;
import com.jpmorgan.processor.handlers.FileHeaderHandler;
import com.jpmorgan.processor.utilities.FileSystemUtils;

@ExtendWith(SpringExtension.class)
public class FileHeaderHandlerTest {

    @InjectMocks
    FileHeaderHandler fileHeaderHandler;

    @Mock
    FileSystemUtils fileSystemUtilMock;

    @Test
    public void testHandleRequestFileIsMissing() {
        FileDto fileDtoMock = mock(FileDto.class);

        try {
            fileHeaderHandler.handleRequest(fileDtoMock);
        } catch (ResponseStatusException ex) {
            assertEquals(ex.getBody().getDetail(), "MultipartFile is missing");
        }

    }

    @Test
    public void testHandleRequestWholeFileRejected() throws IOException {
        FileDto fileDtoMock = mock(FileDto.class);

        InputStream inputStreamMock = IOUtils.toInputStream("12", Charset.defaultCharset());
        MultipartFile multipartFileMock = mock(MultipartFile.class);
        when(fileDtoMock.getUserFile()).thenReturn(multipartFileMock);
        when(multipartFileMock.getInputStream()).thenReturn(inputStreamMock);
        doNothing().when(fileSystemUtilMock).moveFileToNewState(nullable(String.class),
                nullable(String.class), nullable(String.class));

        try {
            fileHeaderHandler.handleRequest(fileDtoMock);
        } catch (ResponseStatusException ex) {
            assertEquals(ex.getBody().getDetail(),
                    "Whole file was rejected because none of the header was found");
        }

        verify(fileSystemUtilMock, atMostOnce()).moveFileToNewState(nullable(String.class),
                nullable(String.class), nullable(String.class));
        inputStreamMock.close();
    }

    @Test
    public void testHandleRequestHeaderOrder() throws IOException {
        FileDto fileDtoSpy =
                Mockito.mock(FileDto.class, AdditionalAnswers.delegatesTo(new FileDto()));

        InputStream order1 = IOUtils.toInputStream("Customer Name,Bank Code,Country Code",
                Charset.defaultCharset());

        InputStream order2 = IOUtils.toInputStream("Bank Code,Country Code,Customer Name",
                Charset.defaultCharset());

        MultipartFile multipartFileMock = mock(MultipartFile.class);
        when(fileDtoSpy.getUserFile()).thenReturn(multipartFileMock);
        when(multipartFileMock.getInputStream()).thenReturn(order1).thenReturn(order2);

        //Order 1
        fileHeaderHandler.handleRequest(fileDtoSpy);
        assertEquals(3, fileDtoSpy.getHeaderMetadata().size());
        assertEquals(0, fileDtoSpy.getHeaderMetadata().get(FileHeader.CUSTOMER_NAME));
        assertEquals(1, fileDtoSpy.getHeaderMetadata().get(FileHeader.BANK_CODE));
        assertEquals(2, fileDtoSpy.getHeaderMetadata().get(FileHeader.COUNTRY_CODE));

        //Order 2
        fileHeaderHandler.handleRequest(fileDtoSpy);
        assertEquals(3, fileDtoSpy.getHeaderMetadata().size());
        assertEquals(0, fileDtoSpy.getHeaderMetadata().get(FileHeader.BANK_CODE));
        assertEquals(1, fileDtoSpy.getHeaderMetadata().get(FileHeader.COUNTRY_CODE));
        assertEquals(2, fileDtoSpy.getHeaderMetadata().get(FileHeader.CUSTOMER_NAME));
        order1.close();
        order2.close();
    }

    @Test
    public void testHandleRequestSkipUnknownHeaderOrder() throws IOException {
        FileDto fileDtoSpy =
                Mockito.mock(FileDto.class, AdditionalAnswers.delegatesTo(new FileDto()));

        InputStream order1 = IOUtils.toInputStream("Customer Name,Bank Code,Hack,Country Code",
                Charset.defaultCharset());

        InputStream order2 = IOUtils.toInputStream("Bank Code,Country Code,Customer Name,Hack",
                Charset.defaultCharset());

        MultipartFile multipartFileMock = mock(MultipartFile.class);
        when(fileDtoSpy.getUserFile()).thenReturn(multipartFileMock);
        when(multipartFileMock.getInputStream()).thenReturn(order1).thenReturn(order2);

        // Order 1
        fileHeaderHandler.handleRequest(fileDtoSpy);
        assertEquals(3, fileDtoSpy.getHeaderMetadata().size());
        assertEquals(0, fileDtoSpy.getHeaderMetadata().get(FileHeader.CUSTOMER_NAME));
        assertEquals(1, fileDtoSpy.getHeaderMetadata().get(FileHeader.BANK_CODE));
        assertEquals(3, fileDtoSpy.getHeaderMetadata().get(FileHeader.COUNTRY_CODE));

        // Order 2
        fileHeaderHandler.handleRequest(fileDtoSpy);
        assertEquals(3, fileDtoSpy.getHeaderMetadata().size());
        assertEquals(0, fileDtoSpy.getHeaderMetadata().get(FileHeader.BANK_CODE));
        assertEquals(1, fileDtoSpy.getHeaderMetadata().get(FileHeader.COUNTRY_CODE));
        assertEquals(2, fileDtoSpy.getHeaderMetadata().get(FileHeader.CUSTOMER_NAME));
        order1.close();
        order2.close();
    }
}
