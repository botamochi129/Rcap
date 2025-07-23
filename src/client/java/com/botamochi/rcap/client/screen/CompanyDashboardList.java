package com.botamochi.rcap.client.screen;

import com.botamochi.rcap.data.Company;
import com.botamochi.rcap.data.CompanyManager;
import mtr.data.NameColorDataBase;
import mtr.mappings.UtilitiesClient;
import mtr.screen.DashboardList;
import mtr.screen.DashboardScreen;
import net.minecraft.client.MinecraftClient;

import java.util.List;
import java.util.stream.Collectors;

public class CompanyDashboardList extends DashboardList {
    public CompanyDashboardList(DashboardScreen parentScreen) {
        super(
                (data, idx) -> {}, // onFind
                null,              // onDrawArea
                (data, idx) -> {   // onEdit
                    if (data instanceof Company company) {
                        UtilitiesClient.setScreen(MinecraftClient.getInstance(), new EditCompanyScreen(company, parentScreen));
                    }
                },
                null,              // onSort
                (data, idx) -> {   // onAdd
                    UtilitiesClient.setScreen(MinecraftClient.getInstance(), new EditCompanyScreen(
                            new Company(System.currentTimeMillis(), "", 0xFFFFFF), parentScreen
                    ));
                },
                (data, idx) -> {   // onDelete
                    if (data instanceof Company company) {
                        CompanyManager.COMPANY_LIST.remove(company);
                    }
                },
                CompanyDashboardList::getCompanyList,
                () -> "",
                s -> {}
        );
    }

    private static List<NameColorDataBase> getCompanyList() {
        return CompanyManager.COMPANY_LIST.stream().map(e -> (NameColorDataBase) e).collect(Collectors.toList());
    }
}