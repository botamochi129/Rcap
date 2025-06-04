package com.botamochi.rcap.data;

import net.minecraft.util.math.Vec3d;
import java.util.UUID;

public class PassengerRenderData {
    public UUID id;
    public String skinName;
    public Vec3d pos;
    public Vec3d prevPos;
    public PassengerData.MotionState motionState;

    public PassengerRenderData(UUID id, Vec3d pos, Vec3d prevPos, String skinName, PassengerData.MotionState motionState) {
        this.id = id;
        this.pos = pos;
        this.prevPos = prevPos;
        this.skinName = skinName;
        this.motionState = motionState;
    }
}
