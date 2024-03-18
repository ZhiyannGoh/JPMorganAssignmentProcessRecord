package com.jpmorgan.processor.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import com.jpmorgan.processor.dto.FileDto;
import com.jpmorgan.processor.models.BankCodeWithUniqueCustomer;

@Component
public class FileSystemUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemUtils.class);

    public static final String FILE_COMPLETED_DIR = "src/main/resources/completed/";
    public static final String FILE_UPLOAD_DIR = "src/main/resources/upload/";
    public static final String FILE_FAILED_DIR = "src/main/resources/failed/";


    public void moveFileToNewState(String src, String dest, String filename) {
        File srcFile = new File(src + filename);
        File destFile = new File(dest);
        try {
            org.apache.commons.io.FileUtils.moveToDirectory(srcFile, destFile, true);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Unable to move file from src: %s, to dest: %s", src, dest));
        }
    }

    public void writeAggregatedData(List<BankCodeWithUniqueCustomer> outputList, FileDto fileDto) {
        File f = new File(FILE_COMPLETED_DIR + fileDto.getRequestId());
        try {
            f.createNewFile();
        } catch (IOException e) {
            logger.error("Unable to create file", e);
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
            bw.write(BankCodeWithUniqueCustomer.HEADER_TEMPLATE);
            for (BankCodeWithUniqueCustomer bank : outputList) {
                bw.newLine();
                bw.write(String.format(BankCodeWithUniqueCustomer.CONTENT_TEMPLATE,
                        bank.getCountry(), bank.getBankName(), bank.getUniqueCustomerCount()));
            }
            bw.close();
            FileUtils.delete(new File(FILE_UPLOAD_DIR + fileDto.getRequestId()));
        } catch (IOException ex) {
            logger.error("Aggregated Data write error occur", ex);
            ex.printStackTrace();
        }
    }

}
