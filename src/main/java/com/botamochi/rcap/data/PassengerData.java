package com.botamochi.rcap.data;

import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.UUID;

public class PassengerData {
    public final UUID id;
    public BlockPos currentBlockPos;
    public final List<Object> route;
    public int routeIndex;
    public double routeProgress; // 0.0 ~ 1.0
    public MotionState motionState = MotionState.WALKING_TO_PLATFORM;
    public long platformId;
    public long destinationPlatformId;
    public int ticksUntilTrain = -1;

    public enum MotionState {
        WALKING_TO_PLATFORM,
        WAITING_FOR_TRAIN,
        ON_TRAIN,
        WALKING_TO_EXIT,
        IDLE
    }

    public PassengerData(List<Object> route) {
        this.id = UUID.randomUUID();
        this.route = route;
        this.routeIndex = 0;
    }
}