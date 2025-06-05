package com.botamochi.rcap.data;

import com.mojang.datafixers.types.templates.Tag;
import org.mtr.mapping.holder.CompoundTag;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.ArrayList;
import java.util.List;

public class Company {
    private final String id;
    private String name;
    private final List<String> routeIds;

    public Company(String id, String name) {
        this.id = id;
        this.name = name;
        this.routeIds = new ArrayList<>();
    }

    public void addRoute(String routeId) {
        routeIds.add(routeId);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRouteIds() {
        return new ArrayList<>(routeIds);
    }

    public void setRouteIds(List<String> routeIds) {
        this.routeIds.clear();
        this.routeIds.addAll(routeIds);
    }

    public NbtCompound toNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putString("id", id);
        tag.putString("name", name);
        NbtList routeList = new NbtList();
        for (String routeId : routeIds) {
            routeList.add(NbtString.of(routeId));
        }
        tag.put("routeIds", routeList);
        return tag;
    }

    public static Company fromNbt(NbtCompound tag) {
        String id = tag.getString("id");
        String name = tag.getString("name");
        NbtList routeList = tag.getList("routeIds", NbtElement.STRING_TYPE); // NbtElement.STRING_TYPE = 8
        List<String> routeIds = new ArrayList<>();
        for (NbtElement element : routeList) {
            routeIds.add(element.asString());
        }
        Company company = new Company(id, name);
        company.getRouteIds().addAll(routeIds);
        return company;
    }
}
