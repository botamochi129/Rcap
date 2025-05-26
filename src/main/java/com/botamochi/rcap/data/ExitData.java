package com.botamochi.rcap.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.List;

public class ExitData extends PersistentState {
    public static final String NAME = "rcap_exit_data";
    public List<BlockPos> exits = new ArrayList<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        for (BlockPos pos : exits) {
            NbtCompound posNbt = new NbtCompound();
            posNbt.putInt("x", pos.getX());
            posNbt.putInt("y", pos.getY());
            posNbt.putInt("z", pos.getZ());
            list.add(posNbt);
        }
        nbt.put("Exits", list);
        return nbt;
    }

    public static ExitData createFromNbt(NbtCompound nbt) {
        ExitData data = new ExitData();
        NbtList list = nbt.getList("Exits", 10); // 10 = NbtCompound
        for (int i = 0; i < list.size(); i++) {
            NbtCompound posNbt = list.getCompound(i);
            BlockPos pos = new BlockPos(posNbt.getInt("x"), posNbt.getInt("y"), posNbt.getInt("z"));
            data.exits.add(pos);
        }
        return data;
    }
}