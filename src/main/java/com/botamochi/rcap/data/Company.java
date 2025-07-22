package com.botamochi.rcap.data;

import net.minecraft.nbt.*;

import java.util.ArrayList;
import java.util.List;

public class Company {
    public long id;
    public String name;
    public int color; // ARGB
    public List<Long> routeIds = new ArrayList<>();

    public Company(long id, String name) {
        this.id = id;
        this.name = name;
        this.color = 0xFFAAAAAA;
    }

    public Company(NbtCompound tag) {
        this.id = tag.getLong("id");
        this.name = tag.getString("name");
        this.color = tag.getInt("color");
        this.routeIds = new ArrayList<>();

        NbtList list = tag.getList("routeIds", NbtElement.LONG_TYPE); // NbtElement.LONG_TYPE == 4

        for (int i = 0; i < list.size(); i++) {
            NbtElement element = list.get(i);
            if (element instanceof NbtLong nbtLong) {
                this.routeIds.add(nbtLong.longValue());
            }
        }
    }

    public NbtCompound toNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putLong("id", id);
        tag.putString("name", name);
        tag.putInt("color", color);
        NbtList list = new NbtList();
        for (Long routeId : routeIds) {
            list.add(NbtLong.of(routeId));
        }
        tag.put("routeIds", list);
        return tag;
    }
}
