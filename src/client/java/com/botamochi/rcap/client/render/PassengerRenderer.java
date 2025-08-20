package com.botamochi.rcap.client.render;

import com.botamochi.rcap.mixin.TrainAccessor;
import com.botamochi.rcap.passenger.Passenger;
import com.botamochi.rcap.passenger.PassengerManager;
import mtr.client.ClientData;
import mtr.data.Platform;
import mtr.data.TrainClient;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class PassengerRenderer {
    private static PassengerModel playerModel = null;

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null) return;

            if (playerModel == null) {
                playerModel = new PassengerModel(
                        client.getEntityModelLoader().getModelPart(EntityModelLayers.PLAYER),
                        false
                );
            }

            MatrixStack matrices = context.matrixStack();
            VertexConsumerProvider consumers = context.consumers();
            var camera = context.camera();

            List<Passenger> passengers;
            synchronized (PassengerManager.PASSENGER_LIST) {
                passengers = new ArrayList<>(PassengerManager.PASSENGER_LIST);
            }

            long now = System.currentTimeMillis();

            for (Passenger passenger : passengers) {
                // ON_TRAIN の場合、まず TrainClient が見つかればそれに追従する
                if (passenger.moveState == Passenger.MoveState.ON_TRAIN && passenger.currentTrainId != null) {
                    TrainClient matchedTrain = null;
                    for (TrainClient trainClient : ClientData.TRAINS) {
                        if (trainClient != null && trainClient.id == passenger.currentTrainId) {
                            matchedTrain = trainClient;
                            break;
                        }
                    }
                    if (matchedTrain != null) {
                        Vec3d posRaw = ((TrainAccessor) matchedTrain).callGetRoutePosition(0, matchedTrain.spacing);
                        passenger.x = posRaw.x;
                        passenger.y = posRaw.y + 1.1;
                        passenger.z = posRaw.z;
                    }
                }

                // currentTrainId がない / 見つからない場合は、サーバが保存した boarding/alight 時刻と座標で補間して表示する
                if (passenger.moveState == Passenger.MoveState.ON_TRAIN && passenger.currentTrainId == null) {
                    long bt = passenger.boardingTimeMillis;
                    long at = passenger.alightTimeMillis;
                    double bx = passenger.boardingX;
                    double by = passenger.boardingY;
                    double bz = passenger.boardingZ;
                    double ax = passenger.alightX;
                    double ay = passenger.alightY;
                    double az = passenger.alightZ;

                    boolean usedInterpolation = false;

                    // 1) 優先：サーバが与えた座標で線形補間
                    if (!Double.isNaN(bx) && !Double.isNaN(ax) && at > bt && bt > 0) {
                        double t = Math.min(1.0, Math.max(0.0, (double)(now - bt) / (double)(at - bt)));
                        double ix = bx + (ax - bx) * t;
                        double iy = by + (ay - by) * t;
                        double iz = bz + (az - bz) * t;
                        passenger.x = ix;
                        passenger.y = iy;
                        passenger.z = iz;
                        usedInterpolation = true;
                    } else {
                        // 2) 次に platformId から座標を取得して補間
                        long bpid = passenger.boardingPlatformId;
                        long apid = passenger.alightingPlatformId;

                        if (bpid != -1 && apid != -1 && bt > 0 && at > bt) {
                            Platform bp = ClientData.DATA_CACHE.platformIdMap.get(bpid);
                            Platform ap = ClientData.DATA_CACHE.platformIdMap.get(apid);
                            if (bp != null && ap != null) {
                                Vec3d bpPos = new Vec3d(bp.getMidPos().getX() + 0.5, bp.getMidPos().getY() + 1.0, bp.getMidPos().getZ() + 0.5);
                                Vec3d apPos = new Vec3d(ap.getMidPos().getX() + 0.5, ap.getMidPos().getY() + 1.0, ap.getMidPos().getZ() + 0.5);
                                double t = Math.min(1.0, Math.max(0.0, (double)(now - bt) / (double)(at - bt)));
                                double ix = bpPos.x + (apPos.x - bpPos.x) * t;
                                double iy = bpPos.y + (apPos.y - bpPos.y) * t;
                                double iz = bpPos.z + (apPos.z - bpPos.z) * t;
                                passenger.x = ix;
                                passenger.y = iy;
                                passenger.z = iz;
                                usedInterpolation = true;
                            }
                        }
                    }

                    if (!usedInterpolation) {
                        // 3) フォールバック: 近傍の TrainClient を探して currentTrainId を一時的に埋める（描画のみ）
                        TrainClient found = null;
                        double best = Double.MAX_VALUE;
                        for (TrainClient trainClient : ClientData.TRAINS) {
                            if (trainClient == null) continue;
                            Vec3d posRaw = ((TrainAccessor) trainClient).callGetRoutePosition(0, trainClient.spacing);
                            double dx = posRaw.x - passenger.x;
                            double dy = posRaw.y - passenger.y;
                            double dz = posRaw.z - passenger.z;
                            double d2 = dx*dx + dy*dy + dz*dz;
                            if (d2 < best && d2 < 64*64) {
                                best = d2;
                                found = trainClient;
                            }
                        }
                        if (found != null) {
                            passenger.currentTrainId = found.id;
                        } else {
                            // 4) 最後に：TrainClient が全く見つからない（電車が描画されていない）場合は
                            //    プラットフォームにTP（視覚的にプラットフォーム上に配置）
                            //    - 可能なら boarding / alight のプラットフォーム座標を使う
                            //    - どちらも無ければ近いプラットフォーム(midPos)を探してTP
                            boolean teleported = false;
                            // prefer server-given boarding/alight coords
                            if (!Double.isNaN(bx) && !Double.isNaN(by) && !Double.isNaN(bz) &&
                                    !Double.isNaN(ax) && !Double.isNaN(ay) && !Double.isNaN(az) && at > bt && bt > 0) {
                                // 期間の前半なら boarding に、後半なら alight に切り替えて見た目を安定化
                                double t = Math.min(1.0, Math.max(0.0, (double)(now - bt) / (double)(at - bt)));
                                if (t < 0.5) {
                                    passenger.x = bx; passenger.y = by; passenger.z = bz;
                                } else {
                                    passenger.x = ax; passenger.y = ay; passenger.z = az;
                                }
                                teleported = true;
                            } else {
                                // try platform ids
                                long bpid = passenger.boardingPlatformId;
                                long apid = passenger.alightingPlatformId;
                                Platform bp = null;
                                Platform ap = null;
                                if (bpid != -1) bp = ClientData.DATA_CACHE.platformIdMap.get(bpid);
                                if (apid != -1) ap = ClientData.DATA_CACHE.platformIdMap.get(apid);

                                if (bp != null && ap != null && at > bt && bt > 0) {
                                    double t = Math.min(1.0, Math.max(0.0, (double)(now - bt) / (double)(at - bt)));
                                    if (t < 0.5) {
                                        var mid = bp.getMidPos();
                                        passenger.x = mid.getX() + 0.5; passenger.y = mid.getY() + 1.0; passenger.z = mid.getZ() + 0.5;
                                    } else {
                                        var mid = ap.getMidPos();
                                        passenger.x = mid.getX() + 0.5; passenger.y = mid.getY() + 1.0; passenger.z = mid.getZ() + 0.5;
                                    }
                                    teleported = true;
                                } else if (bp != null) {
                                    var mid = bp.getMidPos();
                                    passenger.x = mid.getX() + 0.5; passenger.y = mid.getY() + 1.0; passenger.z = mid.getZ() + 0.5;
                                    teleported = true;
                                } else if (ap != null) {
                                    var mid = ap.getMidPos();
                                    passenger.x = mid.getX() + 0.5; passenger.y = mid.getY() + 1.0; passenger.z = mid.getZ() + 0.5;
                                    teleported = true;
                                } else {
                                    // 最後の手段：ClientData の全プラットフォームを走査して最も近い midPos を選ぶ
                                    long nearestPid = Long.MIN_VALUE;
                                    double bestDist = Double.MAX_VALUE;
                                    for (Platform p : ClientData.DATA_CACHE.platformIdMap.values()) {
                                        var mid = p.getMidPos();
                                        double px = mid.getX() + 0.5;
                                        double py = mid.getY() + 1.0;
                                        double pz = mid.getZ() + 0.5;
                                        double dx = px - passenger.x;
                                        double dy = py - passenger.y;
                                        double dz = pz - passenger.z;
                                        double d2 = dx*dx + dy*dy + dz*dz;
                                        if (d2 < bestDist) {
                                            bestDist = d2;
                                            nearestPid = p.id;
                                        }
                                    }
                                    if (nearestPid != Long.MIN_VALUE) {
                                        Platform nearest = ClientData.DATA_CACHE.platformIdMap.get(nearestPid);
                                        if (nearest != null) {
                                            var mid = nearest.getMidPos();
                                            passenger.x = mid.getX() + 0.5; passenger.y = mid.getY() + 1.0; passenger.z = mid.getZ() + 0.5;
                                            teleported = true;
                                        }
                                    }
                                }
                            }

                            if (teleported) {
                                // optional: mark a small visual offset so passengers don't stack exactly on top of each other
                                passenger.x += (Math.random() - 0.5) * 0.5;
                                passenger.z += (Math.random() - 0.5) * 0.5;
                            }
                        }
                    }
                }

                double dx = passenger.x - camera.getPos().x;
                double dy = passenger.y - camera.getPos().y;
                double dz = passenger.z - camera.getPos().z;

                if (dx * dx + dy * dy + dz * dz > 64 * 64) continue;

                BlockPos pos = new BlockPos(Math.floor(passenger.x), Math.floor(passenger.y), Math.floor(passenger.z));
                int lightLevel = context.world().getLightLevel(pos);
                int light = LightmapTextureManager.pack(lightLevel, 0);

                matrices.push();
                matrices.translate(dx, dy + 1.5, dz);
                matrices.scale(-1f, -1f, 1f);
                playerModel.setAngles(null, 0f, 0f, 0f, 0f, 0f);

                int skinIndex = passenger.skinIndex;
                if (skinIndex < 0 || skinIndex >= Passenger.SKINS.length) skinIndex = 0;
                Identifier skinToUse = Passenger.SKINS[skinIndex];

                playerModel.render(
                        matrices,
                        consumers.getBuffer(RenderLayer.getEntityTranslucentCull(skinToUse)),
                        light,
                        OverlayTexture.DEFAULT_UV,
                        1f, 1f, 1f, 1f
                );
                matrices.pop();
            }
        });
    }
}