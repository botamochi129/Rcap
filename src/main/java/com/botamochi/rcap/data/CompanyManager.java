package com.botamochi.rcap.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CompanyManager {

    public static final List<Company> COMPANY_LIST = new ArrayList<>();

    public static long getNextId() {
        return COMPANY_LIST.stream().mapToLong(c -> c.id).max().orElse(0) + 1;
    }

    public static void save(File file) {
        try {
            NbtCompound tag = new NbtCompound();
            NbtList list = new NbtList();
            for (Company company : COMPANY_LIST) {
                list.add(company.toNbt());
            }
            tag.put("companies", list);
            NbtIo.writeCompressed(tag, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load(File file) {
        try {
            if (!file.exists()) return;
            NbtCompound tag = NbtIo.readCompressed(file);
            NbtList list = tag.getList("companies", NbtElement.COMPOUND_TYPE);
            COMPANY_LIST.clear();
            for (int i = 0; i < list.size(); i++) {
                COMPANY_LIST.add(new Company(list.getCompound(i)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
