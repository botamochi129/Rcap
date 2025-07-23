package com.botamochi.rcap.data;

import mtr.data.NameColorDataBase;
import java.util.ArrayList;
import java.util.List;

public class Company extends NameColorDataBase {
    public List<Long> ownedRoutes = new ArrayList<>();
    public List<Long> ownedDepots = new ArrayList<>();

    public Company(long id, String name, int color) {
        super(id);
        this.name = name;
        this.color = color;
    }

    @Override
    protected boolean hasTransportMode() {
        return false;
    }
}