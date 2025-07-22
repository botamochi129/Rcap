package com.botamochi.rcap.data;

import java.util.ArrayList;
import java.util.List;

public class CompanyManager {
    public static final List<Company> COMPANY_LIST = new ArrayList<>();

    private static long nextId = 1;

    public static long getNextId() {
        return nextId++;
    }
}
