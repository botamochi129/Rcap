package com.botamochi.rcap.screen;

import com.botamochi.rcap.Rcap;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RcapScreens {
    public static ScreenHandlerType<HousingBlockScreenHandler> HOUSING_SCREEN_HANDLER;
    public static ScreenHandlerType<OfficeBlockScreenHandler> OFFICE_SCREEN_HANDLER;
    public static ScreenHandlerType<RidingPosBlockScreenHandler> RIDING_POS_SCREEN_HANDLER;

    public static void register() {
        HOUSING_SCREEN_HANDLER = Registry.register(
                Registry.SCREEN_HANDLER,
                new Identifier(Rcap.MOD_ID, "housing_screen_handler"),
                new ScreenHandlerType<>((syncId, inv) -> new HousingBlockScreenHandler(syncId, inv, net.minecraft.screen.ScreenHandlerContext.EMPTY))
        );
        OFFICE_SCREEN_HANDLER = Registry.register(
                Registry.SCREEN_HANDLER,
                new Identifier(Rcap.MOD_ID, "office_screen_handler"),
                new ScreenHandlerType<>((syncId, inv) -> new OfficeBlockScreenHandler(syncId, inv, net.minecraft.screen.ScreenHandlerContext.EMPTY))
        );
        RIDING_POS_SCREEN_HANDLER = Registry.register(
                Registry.SCREEN_HANDLER,
                new Identifier(Rcap.MOD_ID, "riding_pos_screen_handler"),
                new ScreenHandlerType<>((syncId, inv) -> new RidingPosBlockScreenHandler(syncId, inv, net.minecraft.screen.ScreenHandlerContext.EMPTY))
        );
    }
}