package com.botamochi.rcap.passenger;

import com.botamochi.rcap.block.entity.RidingPosBlockEntity;
import com.botamochi.rcap.data.RidingPosManager;
import mtr.data.Platform;
import mtr.data.RailwayData;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;

public class PassengerMovement {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void updatePassenger(ServerWorld world, Passenger passenger) {
        LOGGER.info("[Passenger] {} updatePassenger called. World dimension: {}", passenger.name, world.getDimension());
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
        if (railwayData == null || railwayData.dataCache.platformIdMap.isEmpty()) {
            // プラットフォーム情報が未ロードの場合は処理をスキップ
            LOGGER.warn("[Passenger] {} (ID:{}) RailwayData or platformIdMap is not initialized", passenger.name, passenger.id);
            return;
        }

        if (!railwayData.dataCache.platformIdMap.containsKey(targetPlatformId)) {
            LOGGER.warn("[Passenger] {} (ID:{}) Target platform ID {} not found in platformIdMap", passenger.name, passenger.id, targetPlatformId);
            LOGGER.info("Current platformIdMap keys: {}", railwayData.dataCache.platformIdMap.keySet());
            passenger.moveState = Passenger.MoveState.IDLE;
            return;
        }
        Platform platform = railwayData.dataCache.platformIdMap.get(targetPlatformId);
        if (platform == null) {
            LOGGER.warn("[Passenger] {} (ID:{}) Target platform ID {} not found", passenger.name, passenger.id, targetPlatformId);
            passenger.moveState = Passenger.MoveState.IDLE;
            return;
        }

        BlockPos targetPosBlock;
        List<RidingPosBlockEntity> ridingPositions = RidingPosManager.getRidingPositions(targetPlatformId);
        if (ridingPositions != null && !ridingPositions.isEmpty()) {
            Random random = new Random();
            int index = random.nextInt(ridingPositions.size());
            targetPosBlock = ridingPositions.get(index).getPos();
        } else {
            targetPosBlock = platform.getMidPos();
        }
        Vec3d targetPos = new Vec3d(targetPosBlock.getX() + 0.5, targetPosBlock.getY(), targetPosBlock.getZ() + 0.5);
        Vec3d currentPos = new Vec3d(passenger.x, passenger.y, passenger.z);
        double distanceSq = currentPos.squaredDistanceTo(targetPos);

        final double speed = 0.25;

        LOGGER.info("[Passenger] {} (ID:{}) moveState: {}, pos=({}, {}, {}), targetPlatformId: {}, distSq: {}",
                passenger.name, passenger.id, passenger.moveState, passenger.x, passenger.y, passenger.z, targetPlatformId, distanceSq);

        if (passenger.moveState == Passenger.MoveState.WALKING_TO_PLATFORM) {
            if (distanceSq < 0.25) {
                LOGGER.info("[Passenger] {} (ID:{}) reached platform {}, changing to WAITING_FOR_TRAIN", passenger.name, passenger.id, targetPlatformId);
            } else {
                LOGGER.info("[Passenger] {} (ID:{}) moving towards platform {}", passenger.name, passenger.id, targetPlatformId);
            }
        }

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
