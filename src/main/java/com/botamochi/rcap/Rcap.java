package com.botamochi.rcap;

import com.botamochi.rcap.network.RcapNetworking;
import com.botamochi.rcap.registry.RcapBlocks;
import com.botamochi.rcap.registry.RcapBlockEntities;
import com.botamochi.rcap.screen.RcapScreens;
import net.fabricmc.api.ModInitializer;

public class Rcap implements ModInitializer {
    public static final String MOD_ID = "rcap";

    @Override
    public void onInitialize() {
        RcapBlocks.register();
        RcapBlockEntities.register();
        RcapScreens.register();
        RcapNetworking.registerServerReceivers();
    }
}