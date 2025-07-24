package com.botamochi.rcap.passenger;

import mtr.data.Platform;
import mtr.data.RailwayData;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class PassengerMovement {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void updatePassenger(ServerWorld world, Passenger passenger) {
        List<Long> route = passenger.route;

        if (route == null || route.isEmpty() || passenger.routeTargetIndex >= route.size()) {
            if (passenger.moveState != Passenger.MoveState.IDLE) {
                passenger.moveState = Passenger.MoveState.IDLE;
                LOGGER.info("[Passenger] {} (ID:{}) has no route or finished route. Set to IDLE", passenger.name, passenger.id);
            }
            return;
        }

        long targetPlatformId = route.get(passenger.routeTargetIndex);

        RailwayData railwayData = RailwayData.getInstance(world);
        if (railwayData == null) {
            LOGGER.warn("[Passenger] {} (ID:{}) RailwayData instance is null", passenger.name, passenger.id);
            return;
        }

        Platform platform = railwayData.dataCache.platformIdMap.get(targetPlatformId);
        if (platform == null) {
            LOGGER.warn("[Passenger] {} (ID:{}) Target platform ID {} not found", passenger.name, passenger.id, targetPlatformId);
            passenger.moveState = Passenger.MoveState.IDLE;
            return;
        }

        BlockPos targetPosBlock = platform.getMidPos();
        Vec3d targetPos = new Vec3d(targetPosBlock.getX() + 0.5, targetPosBlock.getY(), targetPosBlock.getZ() + 0.5);
        Vec3d currentPos = new Vec3d(passenger.x, passenger.y, passenger.z);
        double distanceSq = currentPos.squaredDistanceTo(targetPos);

        final double speed = 0.05;

        // ログ出力（tick毎の乗客の状態）
        LOGGER.debug("[Passenger] {} (ID:{}) moveState: {}, pos: ({}, {}, {}), targetPlatform: {}, distanceSq: {}",
                passenger.name, passenger.id, passenger.moveState, passenger.x, passenger.y, passenger.z, targetPlatformId, distanceSq);

        switch (passenger.moveState) {
            case WALKING_TO_PLATFORM:
                if (distanceSq < 0.25) {
                    LOGGER.info("[Passenger] {} (ID:{}) reached platform {}, changing state to WAITING_FOR_TRAIN", passenger.name, passenger.id, targetPlatformId);
                    passenger.moveState = Passenger.MoveState.WAITING_FOR_TRAIN;
                } else {
                    Vec3d direction = targetPos.subtract(currentPos).normalize().multiply(speed);
                    passenger.x += direction.x;
                    passenger.y += direction.y;
                    passenger.z += direction.z;
                }
                break;
            case WAITING_FOR_TRAIN:
                // TODO: 電車到着待ちログなど
                break;
            case ON_TRAIN:
                // TODO: 電車内移動ログなど
                break;
            case WALKING_TO_DESTINATION:
                // TODO: 最終地点徒歩移動ログなど
                break;
            default:
                break;
        }
    }
}
