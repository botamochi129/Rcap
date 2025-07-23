package com.botamochi.rcap.data;

import java.util.ArrayList;
import java.util.List;

public class CompanyManager {
    public static final List<Company> COMPANY_LIST = new ArrayList<>();

    public static void addCompany(Company company) {
        COMPANY_LIST.add(company);
    }

    public static void removeCompany(Company company) {
        COMPANY_LIST.remove(company);
    }

    public static Company getById(long id) {
        for (Company company : COMPANY_LIST) {
            if (company.id == id) return company;
        }
        return null;
    }
}
