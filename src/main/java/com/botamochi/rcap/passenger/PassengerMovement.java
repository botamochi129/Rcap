package com.botamochi.rcap.passenger;

import com.botamochi.rcap.data.RidingPosManager;
import com.botamochi.rcap.network.RcapServerPackets;
import mtr.data.Platform;
import mtr.data.RailwayData;
import mtr.data.ScheduleEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;

public class PassengerMovement {

    private static final Logger LOGGER = LogManager.getLogger();

    // フォールバック移動時間（ms）: スケジュールから降車時刻が推定できない場合
    private static final long DEFAULT_TRAVEL_TIME_MS = 20_000L;

    public static void updatePassenger(ServerWorld world, Passenger passenger) {
        LOGGER.debug("[Passenger] {} updatePassenger called. world={}", passenger.name, world.getRegistryKey().getValue());
        List<Long> route = passenger.route;

        if (route == null || route.isEmpty() || passenger.routeTargetIndex >= route.size()) {
            if (passenger.moveState != Passenger.MoveState.IDLE) {
                passenger.moveState = Passenger.MoveState.IDLE;
            }
            return;
        }

        long targetPlatformId = route.get(passenger.routeTargetIndex);

        RailwayData railwayData = RailwayData.getInstance(world);
        if (railwayData == null || railwayData.dataCache.platformIdMap.isEmpty()) {
            LOGGER.warn("[Passenger] RailwayData not ready for passenger {}", passenger.id);
            return;
        }

        if (!railwayData.dataCache.platformIdMap.containsKey(targetPlatformId)) {
            LOGGER.warn("[Passenger] targetPlatformId {} not found", targetPlatformId);
            passenger.moveState = Passenger.MoveState.IDLE;
            return;
        }
        Platform platform = railwayData.dataCache.platformIdMap.get(targetPlatformId);
        if (platform == null) {
            passenger.moveState = Passenger.MoveState.IDLE;
            return;
        }

        BlockPos targetPosBlock;
        var ridingPositions = RidingPosManager.getRidingPositions(targetPlatformId);
        if (ridingPositions != null && !ridingPositions.isEmpty()) {
            targetPosBlock = ridingPositions.get(new Random().nextInt(ridingPositions.size())).getPos();
        } else {
            targetPosBlock = platform.getMidPos();
        }
        Vec3d targetPos = new Vec3d(targetPosBlock.getX() + 0.5, targetPosBlock.getY() + 1, targetPosBlock.getZ() + 0.5);
        Vec3d currentPos = new Vec3d(passenger.x, passenger.y, passenger.z);
        double distanceSq = currentPos.squaredDistanceTo(targetPos);

        final double speed = 0.25;

        switch (passenger.moveState) {
            case WALKING_TO_PLATFORM:
                // 最終到着（最後の route 要素）に到達したら即時削除＆同期する（ユーザー要求）
                if (passenger.routeTargetIndex == route.size() - 1 && distanceSq < 0.25) {
                    LOGGER.info("[Passenger] {} reached final platform (index {}). Removing and broadcasting.", passenger.id, passenger.routeTargetIndex);
                    PassengerManager.PASSENGER_LIST.remove(passenger);
                    PassengerManager.save();
                    MinecraftServer server = world.getServer();
                    if (server != null) PassengerManager.broadcastToAllPlayers(server);
                    return;
                }

                if (distanceSq < 0.25) {
                    passenger.moveState = Passenger.MoveState.WAITING_FOR_TRAIN;
                    LOGGER.debug("[Passenger] {} reached platform {} -> WAITING", passenger.id, targetPlatformId);
                } else {
                    Vec3d dir = targetPos.subtract(currentPos).normalize().multiply(speed);
                    passenger.x += dir.x;
                    passenger.y += dir.y;
                    passenger.z += dir.z;
                }
                break;

            case WAITING_FOR_TRAIN:
                long now = System.currentTimeMillis();
                List<ScheduleEntry> schedules = railwayData.getSchedulesAtPlatform(targetPlatformId);
                ScheduleEntry matched = null;

                if (schedules != null) {
                    for (ScheduleEntry s : schedules) {
                        try {
                            if (s.arrivalMillis <= now) {
                                matched = s;
                                break;
                            }
                        } catch (Throwable t) {
                            // arrivalMillis が想定外ならスキップ
                        }
                    }
                }

                if (matched != null) {
                    // 乗車：matched の routeId と arrivalMillis を利用して降車時刻を推定する
                    passenger.moveState = Passenger.MoveState.ON_TRAIN;
                    passenger.boardingTimeMillis = matched.arrivalMillis > 0 ? matched.arrivalMillis : now;
                    passenger.boardingPlatformId = targetPlatformId;
                    passenger.scheduledRouteId = matched.routeId;

                    // boarding 座標（サーバで計算）: targetPos
                    passenger.boardingX = targetPos.x;
                    passenger.boardingY = targetPos.y;
                    passenger.boardingZ = targetPos.z;

                    // 次駅（降車）を決める
                    long estimatedAlightTime = -1L;
                    long alightPlatformId = -1L;
                    int alightIndex = passenger.routeTargetIndex + 1;
                    if (alightIndex < passenger.route.size()) {
                        alightPlatformId = passenger.route.get(alightIndex);
                        // alightPlatformId のスケジュールから同じ routeId, arrivalMillis > boarding を探す
                        List<ScheduleEntry> alightSchedules = railwayData.getSchedulesAtPlatform(alightPlatformId);
                        if (alightSchedules != null) {
                            long best = Long.MAX_VALUE;
                            Platform alightPlatform = railwayData.dataCache.platformIdMap.get(alightPlatformId);
                            Vec3d alightPos = null;
                            if (alightPlatform != null) {
                                BlockPos mid = alightPlatform.getMidPos();
                                alightPos = new Vec3d(mid.getX() + 0.5, mid.getY() + 1.0, mid.getZ() + 0.5);
                            }
                            for (ScheduleEntry as : alightSchedules) {
                                try {
                                    if (as.routeId == matched.routeId && as.arrivalMillis > passenger.boardingTimeMillis) {
                                        if (as.arrivalMillis < best) best = as.arrivalMillis;
                                    }
                                } catch (Throwable t) {
                                    // ignore
                                }
                            }
                            if (best != Long.MAX_VALUE) estimatedAlightTime = best;
                            // alight 座標（あれば）を設定（フォールバックで later）
                            if (alightPos != null) {
                                passenger.alightX = alightPos.x;
                                passenger.alightY = alightPos.y;
                                passenger.alightZ = alightPos.z;
                            }
                        }
                    }

                    if (estimatedAlightTime <= 0L) {
                        // フォールバック（固定時間）
                        estimatedAlightTime = passenger.boardingTimeMillis + DEFAULT_TRAVEL_TIME_MS;
                    }

                    passenger.alightTimeMillis = estimatedAlightTime;
                    passenger.alightingPlatformId = alightPlatformId;

                    LOGGER.info("[Passenger] {} boarded: routeId={}, boardingPlatform={}, boardingTime={}, alightPlatform={}, alightTime={}",
                            passenger.id, passenger.scheduledRouteId, passenger.boardingPlatformId, passenger.boardingTimeMillis, passenger.alightingPlatformId, passenger.alightTimeMillis);
                }
                break;

            case ON_TRAIN:
                long cur = System.currentTimeMillis();
                if (passenger.alightTimeMillis > 0 && cur >= passenger.alightTimeMillis) {
                    // 降車処理
                    if (passenger.routeTargetIndex + 1 < passenger.route.size()) {
                        passenger.routeTargetIndex++;
                        passenger.moveState = Passenger.MoveState.WALKING_TO_PLATFORM;
                        // リセット（次の待機/乗車で再計算）
                        passenger.currentTrainId = null;
                        passenger.boardingTimeMillis = -1L;
                        passenger.alightTimeMillis = -1L;
                        passenger.boardingPlatformId = -1L;
                        passenger.alightingPlatformId = -1L;
                        passenger.scheduledRouteId = -1L;
                        passenger.boardingX = passenger.boardingY = passenger.boardingZ = Double.NaN;
                        passenger.alightX = passenger.alightY = passenger.alightZ = Double.NaN;
                        LOGGER.debug("[Passenger] {} alighted and will WALK to next platform (index {}).", passenger.id, passenger.routeTargetIndex);
                    } else {
                        // 最終到着: 削除して保存＆全クライアントに同期（描画消去）
                        LOGGER.info("[Passenger] {} reached final destination -> removing passenger", passenger.id);
                        PassengerManager.PASSENGER_LIST.remove(passenger);
                        PassengerManager.save();
                        MinecraftServer server = world.getServer();
                        if (server != null) PassengerManager.broadcastToAllPlayers(server);
                    }
                }
                break;

            case WALKING_TO_DESTINATION:
                // 将来的にオフィスの座標へ歩かせるならここに実装
                break;

            default:
                break;
        }
    }
}