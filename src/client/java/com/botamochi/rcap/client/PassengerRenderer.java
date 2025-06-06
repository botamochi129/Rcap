package com.botamochi.rcap.client;

import com.botamochi.rcap.Rcap;
import com.botamochi.rcap.passenger.Passenger;
import com.botamochi.rcap.passenger.PassengerManager;
import com.mojang.authlib.GameProfile;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.OverlayTexture;

public class PassengerRenderer {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    /** Blockbench で使ったテクスチャを指す右辺と一致させる */
    private static final Identifier PASSENGER_TEXTURE =
            new Identifier(Rcap.MOD_ID, "textures/entity/passenger_1.png");

    /**
     * クライアント起動時に一度だけ呼ばれるよう登録する。
     */
    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(PassengerRenderer::onWorldRender);
    }

    private static void onWorldRender(WorldRenderContext context) {
        // レンダラや行列、カメラ位置などを取得
        EntityRenderDispatcher dispatcher = CLIENT.getEntityRenderDispatcher();
        MatrixStack    matrices  = context.matrixStack();
        Camera         camera    = context.camera();
        Vec3d          camPos    = camera.getPos();
        VertexConsumerProvider consumers = context.consumers();

        // ベイク済みの ModelPart を使って PassengerModel インスタンスを生成
        PassengerModel model =
                new PassengerModel(
                        CLIENT.getEntityModelLoader().getModelPart(PassengerModel.LAYER_LOCATION)
                );

        // ワールド時間＋tickDelta で ageInTicks を計算
        double worldTime = CLIENT.world.getTime();
        float tickDelta  = CLIENT.getTickDelta();
        float ageInTicks = (float) worldTime + tickDelta;

        // PassengerManager が管理する全乗客をイテレートして描画
        for (ClientPassenger p : ClientPassengerManager.getPassengers()) {
            Vec3d pos = p.pos;
            double dx = pos.x - camPos.x;
            double dy = pos.y - camPos.y;
            double dz = pos.z - camPos.z;

            // 擬似プレイヤーを生成し、向き・アニメーション用パラメータをコピーするだけ
            AbstractClientPlayerEntity fakePlayer = new AbstractClientPlayerEntity(
                    CLIENT.world,
                    new GameProfile(java.util.UUID.randomUUID(), "Passenger"),
                    (PlayerPublicKey) null
            ) {
                @Override
                protected net.minecraft.client.network.PlayerListEntry getPlayerListEntry() {
                    return null;
                }
            };

            // 座標と向きのセット
            fakePlayer.setPos(pos.x, pos.y, pos.z);

            // ─── 描画処理 ───
            matrices.push();
            // 1. カメラ相対の位置に移動
            matrices.translate(dx, dy, dz);
            matrices.translate(0.0D, 1.375D, 0.0D);
            matrices.scale(1f, -1f, 1f);

            // テクスチャをバインドして VertexConsumer を取得
            VertexConsumer vertexConsumer =
                    consumers.getBuffer(RenderLayer.getEntityCutoutNoCull(PASSENGER_TEXTURE));

            // フルブライトで描画（必要に応じて環境光に合わせてください）
            int light = 0xF000F0;
            model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f);
            matrices.pop();
        }

        // バッファを流し込む
        if (consumers instanceof VertexConsumerProvider.Immediate) {
            ((VertexConsumerProvider.Immediate) consumers).draw();
        }
    }
}
