package com.jpmorgan.processor.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MorganFile {

    private String custName;
    private String custID;
    private String countryCode;
    private String bankCode;

}
