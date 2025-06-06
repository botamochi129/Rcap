package com.botamochi.rcap.client;

import com.botamochi.rcap.Rcap;
import com.botamochi.rcap.network.PassengerSyncPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class RcapClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // PassengerModel のレイヤー登録
        EntityModelLayerRegistry.registerModelLayer(
                PassengerModel.LAYER_LOCATION,
                PassengerModel::getTexturedModelData
        );

        // パケット受信登録（クライアント用）
        ClientPlayNetworking.registerGlobalReceiver(PassengerSyncPacket.ID, (client, handler, buf, responseSender) -> {
            long id = buf.readLong();
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            boolean isRemoved = buf.readBoolean();
            client.execute(() -> {
                ClientPassengerManager.updatePassenger(id, new Vec3d(x, y, z), isRemoved);
            });
        });

        // 乗客レンダラーを登録
        PassengerRenderer.register();
    }
}
