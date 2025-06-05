package com.botamochi.rcap.client;

import com.botamochi.rcap.Rcap;
import com.botamochi.rcap.passenger.Passenger;
import com.botamochi.rcap.passenger.PassengerManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class RcapClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // PassengerModel に定義した LAYER_LOCATION を登録し、
        // getTexturedModelData() で生成される LayerDefinition を渡す
        EntityModelLayerRegistry.registerModelLayer(
                PassengerModel.LAYER_LOCATION,
                PassengerModel::getTexturedModelData
        );

        // 乗客レンダラーを登録 (WorldRenderEvents などで毎フレーム描画する想定)
        PassengerRenderer.register();
    }
}