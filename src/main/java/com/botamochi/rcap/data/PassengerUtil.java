package com.botamochi.rcap.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;

public class PassengerUtil {
    public static List<PassengerRenderData> convertToRenderData(List<PassengerData> passengers) {
        List<PassengerRenderData> result = new ArrayList<>();
        for (PassengerData p : passengers) {
            Vec3d pos = Vec3d.ZERO;
            Vec3d prevPos = Vec3d.ZERO;
            int idx = p.routeIndex;

            // 今のBlockPosを後ろに遡って探す
            BlockPos from = null;
            for (int i = idx; i >= 0; i--) {
                if (p.route.get(i) instanceof BlockPos bp) {
                    from = bp;
                    break;
                }
            }
            // 次のBlockPosを前方に探す
            BlockPos to = null;
            for (int i = idx + 1; i < p.route.size(); i++) {
                if (p.route.get(i) instanceof BlockPos bp) {
                    to = bp;
                    break;
                }
            }

            if (from != null && to != null) {
                pos = new Vec3d(
                        lerp(p.routeProgress, from.getX() + 0.5, to.getX() + 0.5),
                        lerp(p.routeProgress, from.getY(), to.getY()),
                        lerp(p.routeProgress, from.getZ() + 0.5, to.getZ() + 0.5)
                );
                prevPos = new Vec3d(
                        lerp(Math.max(0, p.routeProgress - 0.1), from.getX() + 0.5, to.getX() + 0.5),
                        lerp(Math.max(0, p.routeProgress - 0.1), from.getY(), to.getY()),
                        lerp(Math.max(0, p.routeProgress - 0.1), from.getZ() + 0.5, to.getZ() + 0.5)
                );
            } else if (from != null) {
                pos = new Vec3d(from.getX() + 0.5, from.getY(), from.getZ() + 0.5);
                prevPos = pos;
            } else {
                pos = Vec3d.ZERO;
                prevPos = Vec3d.ZERO;
            }

            String skinName = "default";
            result.add(new PassengerRenderData(p.id, pos, prevPos, skinName, p.motionState));
        }
        return result;
    }

    private static double lerp(double t, double a, double b) {
        return a + (b - a) * t;
    }
}