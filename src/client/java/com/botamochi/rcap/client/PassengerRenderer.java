package com.botamochi.rcap.client;

import com.botamochi.rcap.data.PassengerData;
import com.botamochi.rcap.data.PassengerRenderData;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PassengerRenderer {
    public static void init() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            ClientWorld world = mc.world;
            if (world == null) return;

            Vec3d cameraPos = context.camera().getPos();
            VertexConsumerProvider vertexConsumers = context.consumers();

            for (PassengerRenderData p : PassengerClientCache.PASSENGERS) {
                // 無効な乗客は描画しない
                if (p.pos == null || p.prevPos == null) continue;
                if (Double.isNaN(p.pos.x) || Double.isNaN(p.pos.y) || Double.isNaN(p.pos.z)) continue;

                GameProfile profile = new GameProfile(p.id, "");
                OtherClientPlayerEntity fakePlayer = new OtherClientPlayerEntity(world, profile, null);

                // ネームタグ非表示
                fakePlayer.setCustomNameVisible(false);
                fakePlayer.setCustomName(null);

                Vec3d pos = p.pos;
                Vec3d prevPos = p.prevPos;

                // 地面補正
                double y = pos.y;
                BlockPos bp = new BlockPos(pos.x, (int) y, pos.z);
                while (y > world.getBottomY() && world.getBlockState(bp.down()).isAir()) {
                    y -= 1;
                    bp = bp.down();
                }
                pos = new Vec3d(pos.x, y, pos.z);
                prevPos = new Vec3d(prevPos.x, y, prevPos.z);

                // 進行方向
                double dx = pos.x - prevPos.x;
                double dz = pos.z - prevPos.z;
                double dist = Math.sqrt(dx * dx + dz * dz);

                float yaw = fakePlayer.getYaw();
                if (dist > 1e-4) {
                    yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                }

                // プレイヤーにより近い挙動
                fakePlayer.setYaw(yaw);
                fakePlayer.setBodyYaw(yaw);
                fakePlayer.setHeadYaw(yaw);

                // 歩行アニメーション
                float limbDistance = (float) dist;
                if ((p.motionState == PassengerData.MotionState.WALKING_TO_EXIT || p.motionState == PassengerData.MotionState.WALKING_TO_PLATFORM) && dist > 1e-4) {
                    fakePlayer.limbDistance = limbDistance;
                    fakePlayer.limbAngle += limbDistance * 4.0f; // 歩幅調整
                } else {
                    fakePlayer.limbDistance = 0.0f;
                }
                fakePlayer.lastLimbDistance = fakePlayer.limbDistance;

                fakePlayer.setPos(pos.x, pos.y, pos.z);
                fakePlayer.prevX = prevPos.x;
                fakePlayer.prevY = prevPos.y;
                fakePlayer.prevZ = prevPos.z;

                context.matrixStack().push();
                mc.getEntityRenderDispatcher().render(
                        fakePlayer,
                        pos.x - cameraPos.x,
                        pos.y - cameraPos.y,
                        pos.z - cameraPos.z,
                        yaw,
                        context.tickDelta(),
                        context.matrixStack(),
                        vertexConsumers,
                        15728880
                );
                context.matrixStack().pop();
            }
        });
    }
}
