package com.botamochi.rcap.passenger;

import net.minecraft.nbt.NbtCompound;

public class Passenger {
    public long id;
    public String name;
    public double x, y, z;
    public int color;

    public Passenger(long id, String name, double x, double y, double z, int color) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
    }

    // NBT保存用
    public NbtCompound toNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putLong("id", id);
        tag.putString("name", name);
        tag.putDouble("x", x);
        tag.putDouble("y", y);
        tag.putDouble("z", z);
        tag.putInt("color", color);
        return tag;
    }

    public static Passenger fromNbt(NbtCompound tag) {
        return new Passenger(
                tag.getLong("id"),
                tag.getString("name"),
                tag.getDouble("x"),
                tag.getDouble("y"),
                tag.getDouble("z"),
                tag.getInt("color")
        );
    }
}
