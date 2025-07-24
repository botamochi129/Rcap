package com.botamochi.rcap.client;

import com.botamochi.rcap.Rcap;
import com.botamochi.rcap.client.network.RcapClientPackets;
import com.botamochi.rcap.client.render.PassengerRenderer;
import com.botamochi.rcap.client.screen.HousingBlockScreen;
import com.botamochi.rcap.client.screen.OfficeBlockScreen;
import com.botamochi.rcap.screen.ModScreens;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

public class RcapClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RcapClientPackets.register();

        ScreenRegistry.register(ModScreens.HOUSING_SCREEN_HANDLER, HousingBlockScreen::new);
        ScreenRegistry.register(ModScreens.OFFICE_SCREEN_HANDLER, OfficeBlockScreen::new);

        PassengerRenderer.register();
    }
}
