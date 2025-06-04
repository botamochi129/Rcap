package com.botamochi.rcap.data;

import com.botamochi.rcap.network.PassengerSyncS2CPacket;
import mtr.data.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class PassengerManager {
    public static final Identifier SYNC_PACKET_ID = new Identifier("rcap", "passenger_sync");
    private final List<PassengerData> passengers = new ArrayList<>();

    public void addPassenger(PassengerData passenger) {
        passengers.add(passenger);
    }

    public void tick(ServerWorld world) {
        Iterator<PassengerData> it = passengers.iterator();
        while (it.hasNext()) {
            PassengerData p = it.next();
            switch (p.motionState) {
                case WALKING_TO_PLATFORM:
                    if (isAtPlatform(p, world)) {
                        p.motionState = PassengerData.MotionState.WAITING_FOR_TRAIN;
                        p.platformId = getPlatformIdAt(p.currentBlockPos, world);
                        p.ticksUntilTrain = getTicksUntilNextTrain(p.platformId, world);
                    }
                    break;
                case WAITING_FOR_TRAIN:
                    if (p.ticksUntilTrain > 0) {
                        p.ticksUntilTrain--;
                    } else {
                        // スケジュールを使って本当に電車が到着したかを判定
                        if (isTrainArrivedBySchedule(p.platformId, world)) {
                            p.motionState = PassengerData.MotionState.ON_TRAIN;
                            passengerBoard(p, world);
                        } else {
                            // 本当に電車が到着していなければ再度少し待つ
                            p.ticksUntilTrain = 20; // 1秒だけ追加で待つ
                        }
                    }
                    break;
                // ...（省略）
            }
        }
    }

    private boolean isAtPlatform(PassengerData p, ServerWorld world) {
        Platform platform = getPlatformByPos(p.currentBlockPos, world);
        return platform != null;
    }

    public long getPlatformIdAt(BlockPos pos, ServerWorld world) {
        System.out.println("getPlatformIdAt check for passenger at " + pos);
        Platform platform = getPlatformByPos(pos, world);
        return platform != null ? platform.id : -1;
    }

    // Platformの範囲判定はgetMidPosで近傍チェックを行う
    public Platform getPlatformByPos(BlockPos pos, ServerWorld world) {
        if (pos == null) return null;
        for (Platform platform : RailwayData.getInstance(world).platforms) {
            BlockPos middle = platform.getMidPos(); // getMidPos()はMTRでpublic
            if (middle != null && pos.isWithinDistance(middle, 10)) { // 半径10ブロック以内
                return platform;
            }
        }
        return null;
    }

    /** 指定ホームにn秒以内に次の電車が到着するか */
    public boolean isTrainArrivedBySchedule(long platformId, ServerWorld world) {
        final int ARRIVED_THRESHOLD_TICKS = 10; // 0.5秒以内なら到着とみなす
        List<ScheduleEntry> arrivals = RailwayData.getInstance(world).getSchedulesAtPlatform(platformId);
        long now = System.currentTimeMillis();
        if (arrivals != null) {
            for (ScheduleEntry entry : arrivals) {
                long ticksUntilArrival = (entry.arrivalMillis - now) / 50L;
                if (ticksUntilArrival >= 0 && ticksUntilArrival <= ARRIVED_THRESHOLD_TICKS) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 次の電車まで何tickか */
    public int getTicksUntilNextTrain(long platformId, ServerWorld world) {
        List<ScheduleEntry> arrivals = RailwayData.getInstance(world).getSchedulesAtPlatform(platformId);
        long now = System.currentTimeMillis();
        if (arrivals != null && !arrivals.isEmpty()) {
            long minWait = Long.MAX_VALUE;
            for (ScheduleEntry entry : arrivals) {
                long wait = entry.arrivalMillis - now;
                if (wait > 0 && wait < minWait) {
                    minWait = wait;
                }
            }
            if (minWait != Long.MAX_VALUE) {
                int ticks = (int) (minWait / 50);
                return Math.max(ticks, 1);
            }
        }
        return 20 * 10; // デフォルト10秒
    }

    public void passengerBoard(PassengerData p, ServerWorld world) {
        p.motionState = PassengerData.MotionState.ON_TRAIN;
    }

    // 目的地プラットフォームにスケジュール上で到着した電車があるか
    public boolean hasTrainArrivedAtDestination(PassengerData p, ServerWorld world) {
        return isTrainArrivedBySchedule(p.destinationPlatformId, world);
    }

    public void sendSyncPacketToAll(ServerWorld world) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        List<PassengerRenderData> renderList = PassengerUtil.convertToRenderData(passengers);
        PassengerSyncS2CPacket.write(buf, renderList);
        for (ServerPlayerEntity player : world.getPlayers()) {
            ServerPlayNetworking.send(player, SYNC_PACKET_ID, new PacketByteBuf(buf.copy()));
        }
    }
}