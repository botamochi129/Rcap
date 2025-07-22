package com.botamochi.rcap.data;

import net.minecraft.nbt.NbtCompound;

public class Company {
    public long id;
    public String name;
    public int color;

    public Company(long id, String name) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public Company(NbtCompound tag) {
        this.id = tag.getLong("id");
        this.name = tag.getString("name");
    }

    public NbtCompound toNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putLong("id", id);
        tag.putString("name", name);
        return tag;
    }
}
