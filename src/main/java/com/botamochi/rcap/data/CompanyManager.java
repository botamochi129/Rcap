package com.botamochi.rcap.data;

import java.util.ArrayList;
import java.util.List;

public class CompanyManager {
    public static final List<Company> COMPANY_LIST = new ArrayList<>();
    private static long nextId = 1;

    public static long getNextId() {
        return nextId++;
    }

    public static Company getById(long id) {
        for (Company company : COMPANY_LIST) {
            if (company.id == id) {
                return company;
            }
        }
        return null;
    }

    public static void removeById(long id) {
        COMPANY_LIST.removeIf(company -> company.id == id);
    }
}
