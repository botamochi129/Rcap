package com.botamochi.rcap.screen;

import com.botamochi.rcap.Rcap;
import com.botamochi.rcap.screen.HousingBlockScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModScreens {
    public static ScreenHandlerType<HousingBlockScreenHandler> HOUSING_SCREEN_HANDLER;
    public static ScreenHandlerType<OfficeBlockScreenHandler> OFFICE_SCREEN_HANDLER;
    public static ScreenHandlerType<RidingPosScreenHandler> RIDING_POS_SCREEN_HANDLER_TYPE;

    public static void registerScreenHandlers() {
        HOUSING_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(
                new Identifier(Rcap.MOD_ID, "housing_screen"),
                (syncId, playerInventory, buf) ->
                        new HousingBlockScreenHandler(syncId, playerInventory, buf.readBlockPos(), buf.readInt())
        );
        OFFICE_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(
                new Identifier(Rcap.MOD_ID, "office_screen"),
                (syncId, playerInventory, buf) ->
                        new OfficeBlockScreenHandler(syncId, playerInventory, buf.readBlockPos(), buf.readInt())
        );
        RIDING_POS_SCREEN_HANDLER_TYPE = ScreenHandlerRegistry.registerSimple(
                new Identifier(Rcap.MOD_ID, "riding_pos_screen"),
                RidingPosScreenHandler::new
        );
    }
}
