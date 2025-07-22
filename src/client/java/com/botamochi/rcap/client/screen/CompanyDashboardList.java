package com.botamochi.rcap.client.screen;

import com.botamochi.rcap.data.Company;
import com.botamochi.rcap.data.CompanyListEntry;
import com.botamochi.rcap.data.CompanyManager;
import mtr.data.NameColorDataBase;
import mtr.screen.DashboardList;
import net.minecraft.client.MinecraftClient;

import java.util.List;
import java.util.stream.Collectors;

public class CompanyDashboardList extends DashboardList {
    public CompanyDashboardList() {
        super(
                (data, idx) -> {},                     // onFind
                null,                                  // onDrawArea
                (data, idx) -> {                       // onEdit
                    if (data instanceof CompanyListEntry entry) {
                        MinecraftClient.getInstance().setScreen(new EditCompanyScreen(entry.company));
                    }
                },
                null,                                  // onSort
                (data, idx) -> {                       // onAdd
                    MinecraftClient.getInstance().setScreen(new EditCompanyScreen(null));
                },
                (data, idx) -> {                       // onDelete
                    if (data instanceof CompanyListEntry entry) {
                        CompanyManager.COMPANY_LIST.remove(entry.company);
                        // サーバー同期が必要ならここでパケット送信
                    }
                },
                CompanyDashboardList::getCompanyList,
                () -> "", // 検索取得
                s -> {}   // 検索セット
        );
    }

    private static List<NameColorDataBase> getCompanyList() {
        return CompanyManager.COMPANY_LIST.stream().map(CompanyListEntry::new).collect(Collectors.toList());
    }
}