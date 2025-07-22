package com.botamochi.rcap.data;

import java.util.ArrayList;
import java.util.List;

public class Company {
    public long id;
    public String name;
    public int color;
    public List<Long> ownedRoutes = new ArrayList<>();
    public List<Long> ownedDepots = new ArrayList<>();

    public Company(long id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }
}