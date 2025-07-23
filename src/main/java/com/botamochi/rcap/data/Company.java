package com.botamochi.rcap.data;

import mtr.data.NameColorDataBase;
import net.minecraft.nbt.NbtCompound;

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

    public NbtCompound toNBT() {
        NbtCompound tag = new NbtCompound();
        tag.putLong("id", id);
        tag.putString("name", name);
        tag.putInt("color", color);
        return tag;
    }

    public static Company fromNBT(NbtCompound tag) {
        long id = tag.getLong("id");
        String name = tag.getString("name");
        int color = tag.getInt("color");
        return new Company(id, name, color);
    }
}
