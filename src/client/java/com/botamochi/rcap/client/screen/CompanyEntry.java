package com.botamochi.rcap.client.screen;

import mtr.data.NameColorDataBase;
import com.botamochi.rcap.data.Company;

public class CompanyEntry extends NameColorDataBase {

    private final Company company;

    public CompanyEntry(Company company) {
        super(company.id, company.name, company.color);
        this.company = company;
    }

    // 必要なら変更時に書き戻すメソッドを追加可能
    public Company getCompany() {
        return company;
    }

    @Override
    protected boolean hasTransportMode() {
        return false;
    }
}
