package com.botamochi.rcap.client;

import com.botamochi.rcap.screen.RcapScreens;
import com.botamochi.rcap.client.screen.HousingBlockScreen;
import com.botamochi.rcap.client.screen.OfficeBlockScreen;
import com.botamochi.rcap.client.screen.RidingPosBlockScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class RcapClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(RcapScreens.HOUSING_SCREEN_HANDLER, HousingBlockScreen::new);
        HandledScreens.register(RcapScreens.OFFICE_SCREEN_HANDLER, OfficeBlockScreen::new);
        HandledScreens.register(RcapScreens.RIDING_POS_SCREEN_HANDLER, RidingPosBlockScreen::new);
    }
}