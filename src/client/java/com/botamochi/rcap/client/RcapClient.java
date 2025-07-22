package com.botamochi.rcap.client;

import com.botamochi.rcap.client.screen.HousingBlockScreen;
import com.botamochi.rcap.client.screen.OfficeBlockScreen;
import com.botamochi.rcap.screen.ModScreens;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

public class RcapClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(ModScreens.HOUSING_SCREEN_HANDLER, HousingBlockScreen::new);
        ScreenRegistry.register(ModScreens.OFFICE_SCREEN_HANDLER, OfficeBlockScreen::new);
    }
}
