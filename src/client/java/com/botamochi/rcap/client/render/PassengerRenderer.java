package com.botamochi.rcap.client.render;

import com.botamochi.rcap.passenger.Passenger;
import com.botamochi.rcap.passenger.PassengerManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

/**
 * 乗客を プレイヤーモデル + カスタムスキン画像 で描画するクラス（Entity使用なし）
 */
public class PassengerRenderer {
    private static final Identifier SKIN_TEXTURE = new Identifier("rcap", "textures/entity/passenger/custom_skin.png");
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

            for (Passenger passenger : PassengerManager.PASSENGER_LIST) {
                double dx = passenger.x - camera.getPos().x;
                double dy = passenger.y - camera.getPos().y;
                double dz = passenger.z - camera.getPos().z;

                if (dx * dx + dy * dy + dz * dz > 64 * 64) continue;

                matrices.push();

                matrices.translate(dx, dy + 1.5, dz);

                // 頭など部位のアンバランスによる問題を減らすため、Y軸の反転は外し少し低めのマイナスに
                matrices.scale(-1f, -1f, 1f);

                // モデルの角度設定
                playerModel.setAngles(null, 0f, 0f, 0f, 0f, 0f);

                BlockPos pos = new BlockPos(Math.floor(passenger.x), Math.floor(passenger.y), Math.floor(passenger.z));
                int lightLevel = context.world().getLightLevel(pos);

                int light = LightmapTextureManager.pack(lightLevel, 0);  // SkyLight=lightLevel, BlockLight=0 として扱う

                playerModel.render(
                        matrices,
                        consumers.getBuffer(RenderLayer.getEntityTranslucentCull(SKIN_TEXTURE)),
                        light,
                        OverlayTexture.DEFAULT_UV,
                        1f, 1f, 1f, 1f
                );

                matrices.pop();
            }
        });
    }
}
