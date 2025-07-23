package com.botamochi.rcap.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentStateManager;

import java.util.List;

public class CompanyManager {

    public static List<Company> COMPANY_LIST;

    private static CompanyState companyState;

    public static void init(ServerWorld world) {
        PersistentStateManager manager = world.getPersistentStateManager();

        companyState = manager.getOrCreate(
                CompanyState::createFromNbt,
                CompanyState::new,
                CompanyState.KEY
        );

        COMPANY_LIST = companyState.companyList;
    }

    public static void save() {
        if (companyState != null) {
            companyState.markDirty(); // 保存を通知する
        }
    }

    public static Company getById(long id) {
        for (Company company : COMPANY_LIST) {
            if (company.id == id) return company;
        }
        return null;
    }
}
