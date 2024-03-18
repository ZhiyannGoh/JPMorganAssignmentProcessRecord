package com.jpmorgan.processor.constants;

import java.util.Optional;
import org.apache.commons.lang3.EnumUtils;
import lombok.Getter;

@Getter
public enum FileHeader {

    // This was created because of the requirement that the input is not guaranteed
    // Assumed that not guaranteed means columns can be in any sequence

    CUSTOMER_NAME("Customer Name"), CUSTOMER_ID("Customer ID"), COUNTRY_CODE(
            "Country Code"), BANK_CODE("Bank Code");

    String headerInFile;

    private FileHeader(String headerInFile) {
        this.headerInFile = headerInFile;
    }

    public static Optional<FileHeader> getFileHeaderEnumByValue(String fileHeader) {
        return EnumUtils.getEnumList(FileHeader.class).stream().filter(
                enumFileHeader -> enumFileHeader.getHeaderInFile().equalsIgnoreCase(fileHeader))
                .findFirst();
    }

}
