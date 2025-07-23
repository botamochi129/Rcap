package com.botamochi.rcap.data;

import mtr.data.NameColorDataBase;

import java.util.HashSet;
import java.util.Set;

public class Company extends NameColorDataBase {
    public final Set<Long> ownedRoutes = new HashSet<>();
    public final Set<Long> ownedDepots = new HashSet<>();

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
