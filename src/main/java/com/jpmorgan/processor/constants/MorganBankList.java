package com.jpmorgan.processor.constants;

import lombok.Getter;

@Getter
public enum MorganBankList {

    // Design consideration
    // Preferred to have a configuration-based for Banks configuration
    // To model a more realistic scenario, where only registered banks will send in the input

    DBXSG01("DBS Pte Ltd", "Singapore"),
    OCBCSG01("Overseaas Chinese Bank", "Singapore"),
    UOBSG01("United Overseas Bank", "Singapore"),
    UOBMY01("United Overseas Bank", "Malaysia");

    String bankName;
    String country;

    private MorganBankList(String bankName, String country) {
        this.bankName = bankName;
        this.country = country;
    }

}
