package com.botamochi.rcap.client;

import com.botamochi.rcap.block.entity.RidingPosBlockEntity;
import com.botamochi.rcap.client.network.RcapClientPackets;
import com.botamochi.rcap.client.screen.HousingBlockScreen;
import com.botamochi.rcap.client.screen.OfficeBlockScreen;
import com.botamochi.rcap.client.screen.RidingPosScreen;
import com.botamochi.rcap.data.CompanyManager;
import com.botamochi.rcap.screen.ModScreens;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;

public class RcapClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RcapClientPackets.register();

        ScreenRegistry.register(ModScreens.HOUSING_SCREEN_HANDLER, HousingBlockScreen::new);
        ScreenRegistry.register(ModScreens.OFFICE_SCREEN_HANDLER, OfficeBlockScreen::new);
    }
}
