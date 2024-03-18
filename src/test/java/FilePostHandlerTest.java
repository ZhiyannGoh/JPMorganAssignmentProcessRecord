import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.jpmorgan.processor.dto.FileDto;
import com.jpmorgan.processor.handlers.FilePostHandler;
import com.jpmorgan.processor.models.BankCodeWithUniqueCustomer;
import com.jpmorgan.processor.utilities.FileSystemUtils;

@ExtendWith(SpringExtension.class)
public class FilePostHandlerTest {

    @InjectMocks
    FilePostHandler filePostHandler;

    @Mock
    FileSystemUtils fileSystemUtilsMock;

    @Test
    public void testHandleRequestCustomSort() {

        FileDto fileDtoMock = mock(FileDto.class);

        Map<String, Long> resultMapMock = new HashMap<>();
        resultMapMock.put("DBXSG01", Long.valueOf(3));
        resultMapMock.put("UOBMY01", Long.valueOf(1));
        resultMapMock.put("UOBSG01", Long.valueOf(1));
        when(fileDtoMock.getResultMap()).thenReturn(resultMapMock);

        filePostHandler.handleRequest(fileDtoMock);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BankCodeWithUniqueCustomer>> sortedRecordCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(fileSystemUtilsMock, times(1)).writeAggregatedData(sortedRecordCaptor.capture(),
                any());
        List<BankCodeWithUniqueCustomer> capture = sortedRecordCaptor.getValue();
        assertTrue(capture.size() == 3);
        assertTrue(capture.get(0).getBankName().contains("DBS Pte Ltd"));
        assertTrue(capture.get(1).getBankName().contains("United Overseas Bank"));
        assertTrue(capture.get(2).getBankName().contains("United Overseas Bank"));
        assertTrue(capture.get(0).getBankName().length() == 35);
        assertTrue(capture.get(1).getBankName().length() == 35);
        assertTrue(capture.get(2).getBankName().length() == 35);
        assertTrue(capture.get(0).getCountry().equalsIgnoreCase("Singapore"));
        assertTrue(capture.get(1).getCountry().equalsIgnoreCase("Singapore"));
        assertTrue(capture.get(2).getCountry().equalsIgnoreCase("Malaysia"));
        assertTrue(capture.get(0).getUniqueCustomerCount().equalsIgnoreCase("0000003"));
        assertTrue(capture.get(1).getUniqueCustomerCount().equalsIgnoreCase("0000001"));
        assertTrue(capture.get(2).getUniqueCustomerCount().equalsIgnoreCase("0000001"));
    }

    @Test
    public void testHandleRequestResultMapIsEmpty() {

        FileDto fileDtoMock = mock(FileDto.class);
        when(fileDtoMock.getResultMap()).thenReturn(Collections.emptyMap());

        filePostHandler.handleRequest(fileDtoMock);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BankCodeWithUniqueCustomer>> sortedRecordCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(fileSystemUtilsMock, times(1)).writeAggregatedData(any(), any());
    }
}
