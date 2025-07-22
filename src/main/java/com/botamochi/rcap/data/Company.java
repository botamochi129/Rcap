package com.botamochi.rcap.data;

public class Company {
    public long id;
    public String name;
    public int color;

    public Company(long id, String name) {
        this.id = id;
        this.name = name;
        this.color = 0xFFFFFF; // 初期白
    }
}
