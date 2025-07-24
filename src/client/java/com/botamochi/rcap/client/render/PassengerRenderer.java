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

import java.util.ArrayList;
import java.util.List;

/**
 * 乗客を プレイヤーモデル + カスタムスキン画像 で描画するクラス（Entity使用なし）
 */
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

            for (Passenger passenger : passengers) {
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

                // skinIndexが範囲内かチェックし安全に取り出す
                int skinIndex = passenger.skinIndex;
                if (skinIndex < 0 || skinIndex >= Passenger.SKINS.length) {
                    skinIndex = 0; // デフォルト
                }
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