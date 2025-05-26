package com.botamochi.rcap.data;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentStateManager;

import java.util.ArrayList;
import java.util.List;

public class ExitManager {
    // 例：ワールドに保存する
    public static ExitData get(ServerWorld world) {
        PersistentStateManager manager = world.getPersistentStateManager();
        return manager.getOrCreate(
                ExitData::createFromNbt,
                ExitData::new,
                ExitData.NAME
        );
    }

    // 出口追加
    public static void addExit(ServerWorld world, BlockPos pos) {
        ExitData data = get(world);
        if (!data.exits.contains(pos)) {
            data.exits.add(pos);
            data.markDirty();
        }
    }

    // 出口削除
    public static void removeExit(ServerWorld world, BlockPos pos) {
        ExitData data = get(world);
        if (data.exits.remove(pos)) {
            data.markDirty();
        }
    }

    // ランダム取得
    public static BlockPos getRandomExit(ServerWorld world) {
        ExitData data = get(world);
        if (data.exits.isEmpty()) return null;
        return data.exits.get(world.getRandom().nextInt(data.exits.size()));
    }

    // 全取得
    public static List<BlockPos> getAllExits(ServerWorld world) {
        return new ArrayList<>(get(world).exits);
    }
}
