package com.jpmorgan.processor.models;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BankCodeWithUniqueCustomer {

    public static final String HEADER_TEMPLATE =
            "Country\t\t\t\tBank Name\t\t\t\t\t\t\t\tUnique Customer Count";

    public static final String CONTENT_TEMPLATE = "%s\t\t\t%s\t\t%s";

    public static final String UNIQUE_CUST_COUNT = "uniqueCustomerCount";

    private static Comparator<BankCodeWithUniqueCustomer> byUnqiueCustomer =
            new Comparator<BankCodeWithUniqueCustomer>() {
                @Override
                public int compare(final BankCodeWithUniqueCustomer o1,
                        final BankCodeWithUniqueCustomer o2) {
                    return o2.getUniqueCustomerCount().compareTo(o1.getUniqueCustomerCount());
                }
            };

    private static Comparator<BankCodeWithUniqueCustomer> byCountry =
            new Comparator<BankCodeWithUniqueCustomer>() {
                @Override
                public int compare(final BankCodeWithUniqueCustomer o1,
                        final BankCodeWithUniqueCustomer o2) {
                    return o2.getCountry().compareTo(o1.getCountry());
                }
            };

    public static final Map<String, Comparator<BankCodeWithUniqueCustomer>> CUSTOM_SORT =
            new HashMap<>();
    static {
        CUSTOM_SORT.put(UNIQUE_CUST_COUNT, byUnqiueCustomer.thenComparing(byCountry));
    }

    String country;

    String bankName;

    String uniqueCustomerCount;

}
