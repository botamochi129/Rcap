package com.botamochi.rcap.data;

import mtr.data.NameColorDataBase;

public class CompanyListEntry extends NameColorDataBase {
    public final Company company;

    public CompanyListEntry(Company company) {
        super(company.id);
        this.company = company;
        this.name = company.name;
        this.color = company.color;
    }

    @Override
    protected boolean hasTransportMode() {
        return false;
    }
}