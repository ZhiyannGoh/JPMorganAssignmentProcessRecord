package com.jpmorgan.processor.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.jpmorgan.processor.constants.MorganBankList;
import com.jpmorgan.processor.dto.FileDto;
import com.jpmorgan.processor.models.BankCodeWithUniqueCustomer;
import com.jpmorgan.processor.utilities.FileSystemUtils;

@Component
public class FilePostHandler extends FileProcessorHandler {

    private static final Logger logger = LoggerFactory.getLogger(FilePostHandler.class);

    @Autowired
    FileSystemUtils fileSystemUtils;

    @Override
    public void handleRequest(FileDto fileDto) {

        List<BankCodeWithUniqueCustomer> unsortedRecords = new ArrayList<>();
        fileDto.getResultMap().forEach((k, v) -> {
            boolean isValidBank = EnumUtils.isValidEnumIgnoreCase(MorganBankList.class, k);
            if (isValidBank) {
                MorganBankList bank = EnumUtils.getEnumIgnoreCase(MorganBankList.class, k);
                BankCodeWithUniqueCustomer outputRecord = BankCodeWithUniqueCustomer.builder()
                        .bankName(String.format("%-35s", bank.getBankName()))
                        .country(bank.getCountry()).uniqueCustomerCount(String.format("%07d", v))
                        .build();
                unsortedRecords.add(outputRecord);

            } else {
                logger.warn("Bank not recognized: " + k);
            }
        });

        List<BankCodeWithUniqueCustomer> records;
        if (!BankCodeWithUniqueCustomer.CUSTOM_SORT.isEmpty()) {
            records = unsortedRecords.stream()
                    .sorted(BankCodeWithUniqueCustomer.CUSTOM_SORT
                            .get(BankCodeWithUniqueCustomer.UNIQUE_CUST_COUNT))
                    .collect(Collectors.toList());
        } else {
            records = unsortedRecords;
            logger.info("Sort Column is missing");
        }

        fileSystemUtils.writeAggregatedData(records, fileDto);
        validateAndProcessNextChaining(fileDto);
    }

    @Override
    public void validateAndProcessNextChaining(FileDto fileDto) {
        processNextChain(fileDto);
    }

}
