package com.botamochi.rcap.client.screen;

import com.botamochi.rcap.data.Company;
import com.botamochi.rcap.data.CompanyManager;
import mtr.data.NameColorDataBase;
import mtr.screen.DashboardList;
import mtr.screen.DashboardScreen;
import net.minecraft.client.MinecraftClient;

import java.util.List;
import java.util.stream.Collectors;

public class CompanyDashboardList extends DashboardList {

    public CompanyDashboardList(DashboardScreen parent) {
        super(
                null,
                null,
                (data, id) -> {
                    if (data instanceof Company company) {
                        MinecraftClient.getInstance().setScreen(new EditCompanyScreen(company, parent));
                    }
                },
                null,
                null,
                (data, id) -> {
                    if (data instanceof Company company) {
                        CompanyManager.removeCompany(company);
                    }
                },
                () -> CompanyManager.COMPANY_LIST.stream().map(c -> (NameColorDataBase) c).collect(Collectors.toList()),
                () -> "会社一覧",
                s -> {}
        );
    }
}
