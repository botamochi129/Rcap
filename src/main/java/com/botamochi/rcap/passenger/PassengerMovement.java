package com.botamochi.rcap.passenger;

import com.botamochi.rcap.block.entity.RidingPosBlockEntity;
import com.botamochi.rcap.data.RidingPosManager;
import mtr.data.Platform;
import mtr.data.RailwayData;
import mtr.data.ScheduleEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
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
        Vec3d targetPos = new Vec3d(targetPosBlock.getX() + 0.5, targetPosBlock.getY() + 1, targetPosBlock.getZ() + 0.5);
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
                // 1. 目標プラットフォームIDを取得
                long waitingPlatformId = route.get(passenger.routeTargetIndex);

                // 2. スケジュールを取得
                List<ScheduleEntry> scheduleList = railwayData.getSchedulesAtPlatform(waitingPlatformId); // scheduleManagerはスケジュール管理クラスの想定

                boolean trainArrived = false;

                if (scheduleList != null && !scheduleList.isEmpty()) {
                    for (ScheduleEntry schedule : scheduleList) {
                        // 3. 電車の現在位置や時間、駅到着判定ロジック
                        if (schedule.arrivalMillis <= System.currentTimeMillis()) {
                            trainArrived = true;
                            break;
                        }
                    }
                }

                if (trainArrived) {
                    // 4. 電車に乗車処理へ遷移
                    passenger.moveState = Passenger.MoveState.ON_TRAIN;
                    LOGGER.info("[Passenger] {} (ID:{}) train arrived at platform {}, switching to ON_TRAIN", passenger.name, passenger.id, waitingPlatformId);

                    // 乗車した電車IDや情報を乗客に紐づける処理があればここに
                    // passenger.currentTrainId = schedule.getTrainId();
                } else {
                    // まだ電車到着していないので待機状態を維持
                    LOGGER.info("[Passenger] {} (ID:{}) waiting for train at platform {}", passenger.name, passenger.id, waitingPlatformId);
                }
                break;
            case ON_TRAIN:
                LOGGER.info("[Passenger] {} (ID:{}) is ON_TRAIN", passenger.name, passenger.id);

                // 1. 今乗っている電車に追従する処理
                // ↓ 前回記録しておいた乗車列車情報（例: routeIdやrouteFinderなど）から、スケジュールEntryやplatformIdに紐づいたpositionを仮に取得する例
                // ※ 実際は、MTR APIでは「列車そのもののサーバ側インスタンス」は直接取得困難
                // なので以下は乗車orダミー位置決定例（例:該当プラットフォームのmidPosで疑似的に動かす）
                long ridingPlatformId = route.get(passenger.routeTargetIndex); // 例：降車駅 or 次駅
                Platform ridingPlatform = railwayData.dataCache.platformIdMap.get(ridingPlatformId);
                if (ridingPlatform != null) {
                    // 今は駅で停車中として、（本来は列車座標を使いたい）
                    BlockPos trainPosBlock = ridingPlatform.getMidPos();
                    Vec3d trainPos = new Vec3d(trainPosBlock.getX() + 0.5, trainPosBlock.getY() + 1, trainPosBlock.getZ() + 0.5);

                    // 乗客の座標を「仮想的に列車に追従」させる
                    passenger.x = trainPos.x;
                    passenger.y = trainPos.y;
                    passenger.z = trainPos.z;

                    LOGGER.info("[Passenger] {} (ID:{}) follows train at platform {} ({}, {}, {})", passenger.name, passenger.id, ridingPlatformId, trainPos.x, trainPos.y, trainPos.z);
                }

                // 2. 降車判定
                // 経路(RouteFinderData)の最後のstationIdsや、次のプラットフォームIDをカルキュレート
                // 今は「この駅に到着している」「ドアが開いた」等は本物の列車インスタンス参照不可なので、scheduleListから判定
                List<ScheduleEntry> scheduleListRiding = railwayData.getSchedulesAtPlatform(ridingPlatformId);
                boolean atArrival = false;
                if (scheduleListRiding != null && !scheduleListRiding.isEmpty()) {
                    for (ScheduleEntry schedule : scheduleListRiding) {
                        // schedule.arrivalMillisが「過ぎて」いて今ここなら「停車中」と仮定
                        if (System.currentTimeMillis() >= schedule.arrivalMillis) {
                            atArrival = true;
                            break;
                        }
                    }
                }

                // 【降りる駅の判定方式】
                // ここでは「次のrouteTargetIndexが存在する」「この駅がstationIdsのラスト駅」などで降車扱いに
                boolean shouldAlight = false;
                if (/* 条件: 例えばrouteの現在targetが降車駅か、ON_TRAIN目的地に到達した判定等 */ atArrival) {
                    shouldAlight = true;
                }

                if (shouldAlight) {
                    // 降車処理: routeTargetIndexを進めて、次の目的地へ徒歩移動を復帰
                    if (passenger.routeTargetIndex + 1 < route.size()) {
                        passenger.routeTargetIndex++;
                        passenger.moveState = Passenger.MoveState.WALKING_TO_PLATFORM;
                        LOGGER.info("[Passenger] {} (ID:{}) arrived at next platform {}, moving to WALKING_TO_PLATFORM", passenger.name, passenger.id, route.get(passenger.routeTargetIndex));
                    } else {
                        // 最終目的地: HOME/DESITNATION等
                        passenger.moveState = Passenger.MoveState.WALKING_TO_DESTINATION;
                        LOGGER.info("[Passenger] {} (ID:{}) reached destination, switching to WALKING_TO_DESTINATION", passenger.name, passenger.id);
                    }
                }
                break;
            case WALKING_TO_DESTINATION:
                // TODO: 最終地点徒歩移動ログなど
                break;
            default:
                break;
        }
    }
}
