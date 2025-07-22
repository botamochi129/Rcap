package com.botamochi.rcap.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CompanyManager {
    public static final List<Company> COMPANY_LIST = new ArrayList<>();

    // 会社IDから会社を検索
    public static Company getById(long id) {
        for (Company company : COMPANY_LIST) {
            if (company.id == id) {
                return company;
            }
        }
        return null;
    }

    // 会社IDで削除
    public static void removeById(long id) {
        Iterator<Company> iterator = COMPANY_LIST.iterator();
        while (iterator.hasNext()) {
            Company company = iterator.next();
            if (company.id == id) {
                iterator.remove();
                return;
            }
        }
    }
}