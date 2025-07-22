package com.botamochi.rcap.client.screen;

import mtr.data.NameColorDataBase;
import net.minecraft.text.Text;
import com.botamochi.rcap.data.Company;

public class CompanyEntry implements NameColorDataBase {
    public final Company company;

    public CompanyEntry(Company company) {
        this.company = company;
    }

    @Override
    public String getName() {
        return company.name;
    }

    @Override
    public int getColor() {
        return company.color;
    }

    @Override
    public long getId() {
        return company.id;
    }

    @Override
    public Text getNameText() {
        return Text.literal(company.name);
    }
}
