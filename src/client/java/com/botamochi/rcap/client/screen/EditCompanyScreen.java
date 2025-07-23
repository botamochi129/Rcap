package com.botamochi.rcap.client.screen;

import com.botamochi.rcap.data.Company;
import mtr.mappings.Text;
import mtr.screen.DashboardScreen;
import mtr.screen.EditNameColorScreenBase;

public class EditCompanyScreen extends EditNameColorScreenBase<Company> {
    public EditCompanyScreen(Company company, DashboardScreen parent) {
        super(company, parent, "gui.rcap.company", "gui.mtr.color");
    }
    // saveDataのオーバーライド不要。本家の流れに任せる
}