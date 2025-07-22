package com.botamochi.rcap.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CompanyManager {
    public static final List<Company> COMPANY_LIST = new ArrayList<>();

    public static long getNextId() {
        return COMPANY_LIST.stream().mapToLong(c -> c.id).max().orElse(0) + 1;
    }
}
