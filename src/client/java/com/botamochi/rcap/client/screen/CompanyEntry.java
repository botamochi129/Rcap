package com.botamochi.rcap.client.screen;

import com.botamochi.rcap.data.Company;
import mtr.data.NameColorDataBase;
import mtr.data.TransportMode;

public class CompanyEntry extends NameColorDataBase {

    public final Company company;

    public CompanyEntry(Company company) {
        super(company.id, TransportMode.TRAIN); // ← テキトウな TransportMode を渡す
        this.company = company;
        this.name = company.name;
        this.color = company.color;
    }

    @Override
    protected boolean hasTransportMode() {
        return false;
    }
}
