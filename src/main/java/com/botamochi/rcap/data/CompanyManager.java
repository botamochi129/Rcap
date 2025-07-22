package com.botamochi.rcap.data;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CompanyManager {

    // 会社リスト(シングルトン的にアクセス)
    public static final List<Company> COMPANY_LIST = new ArrayList<>();

    // IDの最大値から次を得る（単純増分例）
    public static long getNextId() {
        return COMPANY_LIST.stream().mapToLong(c -> c.id).max().orElse(0) + 1;
    }

    // DashboardList表示用に変換
    public static List<CompanyEntry> getDashboardEntries() {
        return COMPANY_LIST.stream().map(CompanyEntry::new).collect(Collectors.toList());
    }
}
