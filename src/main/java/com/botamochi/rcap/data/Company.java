package com.botamochi.rcap.data;


import net.minecraft.nbt.NbtCompound;

public class Company {

    public final long id;
    public String name;
    public int color;

    public Company(long id, String name) {
        this.id = id;
        this.name = name;
        this.color = 0xFFFFFF; // 初期色（白など）
    }

    // NBTから読み込み
    public static Company fromTag(NbtCompound tag) {
        long id = tag.getLong("id");
        String name = tag.getString("name");
        int color = tag.getInt("color");
        Company company = new Company(id, name);
        company.color = color;
        return company;
    }

    // NBTに書き込み
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putLong("id", id);
        tag.putString("name", name);
        tag.putInt("color", color);
        return tag;
    }
}
