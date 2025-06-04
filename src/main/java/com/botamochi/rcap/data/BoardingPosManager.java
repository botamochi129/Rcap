package com.botamochi.rcap.data;

import mtr.data.RailwayData;
import mtr.data.Platform;
import mtr.data.Station;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.*;

public class BoardingPosManager extends PersistentState {
    public static final String NAME = "rcap_boarding_pos_data";
    // BlockPos → PlatformId
    private static final Map<BlockPos, Long> boardingPosToPlatform = new HashMap<>();

    public void add(ServerWorld world, BlockPos pos) {
        Platform platform = RailwayData.getPlatformByPos(RailwayData.getInstance(world).platforms, pos);
        if (platform != null) {
            boardingPosToPlatform.put(pos, platform.id);
            markDirty();
        }
    }

    public void remove(ServerWorld world, BlockPos pos) {
        if (boardingPosToPlatform.remove(pos) != null) {
            markDirty();
        }
    }

    public Map<BlockPos, Long> getBoardingPosToPlatform() {
        return Collections.unmodifiableMap(boardingPosToPlatform);
    }

    // 最寄りの乗車位置を取得（同じ駅限定）
    public static BlockPos findNearest(ServerWorld world, Station station, BlockPos from) {
        double minDist = Double.MAX_VALUE;
        BlockPos nearest = null;
        for (Map.Entry<BlockPos, Long> entry : boardingPosToPlatform.entrySet()) {
            Platform platform = RailwayData.getInstance(world).dataCache.platformIdMap.get(entry.getValue());
            if (platform != null && RailwayData.getInstance(world).dataCache.platformIdToStation.get(platform).id == station.id) {
                double dist = entry.getKey().getSquaredDistance(from);
                if (dist < minDist) {
                    minDist = dist;
                    nearest = entry.getKey();
                }
            }
        }
        return nearest;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        for (Map.Entry<BlockPos, Long> entry : boardingPosToPlatform.entrySet()) {
            NbtCompound tag = new NbtCompound();
            BlockPos pos = entry.getKey();
            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            tag.putLong("platformId", entry.getValue());
            list.add(tag);
        }
        nbt.put("BoardingPos", list);
        return nbt;
    }

    public static BoardingPosManager createFromNbt(NbtCompound nbt) {
        BoardingPosManager data = new BoardingPosManager();
        NbtList list = nbt.getList("BoardingPos", 10);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound tag = list.getCompound(i);
            BlockPos pos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
            long platformId = tag.getLong("platformId");
            data.boardingPosToPlatform.put(pos, platformId);
        }
        return data;
    }

    public static BoardingPosManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                BoardingPosManager::createFromNbt,
                BoardingPosManager::new,
                NAME
        );
    }
}