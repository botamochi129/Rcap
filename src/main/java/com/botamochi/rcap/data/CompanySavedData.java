package com.botamochi.rcap.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.List;

public class CompanySavedData extends PersistentState {

    public static final String NAME = "rcap_companies";
    public final List<Company> companies = new ArrayList<>();

    public static CompanySavedData createFromNbt(NbtCompound tag) {
        CompanySavedData data = new CompanySavedData();
        NbtList list = tag.getList("companies", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            data.companies.add(new Company(list.getCompound(i)));
        }
        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        NbtList list = new NbtList();
        for (Company c : companies) {
            list.add(c.toNbt());
        }
        tag.put("companies", list);
        return tag;
    }

    public void add(Company c) {
        companies.add(c);
        markDirty();
    }

    public void remove(long id) {
        companies.removeIf(c -> c.id == id);
        markDirty();
    }

    public static CompanySavedData get(net.minecraft.server.world.ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(CompanySavedData::createFromNbt, CompanySavedData::new, NAME);
    }
}
