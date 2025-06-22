package com.botamochi.rcap.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;

public class HousingBlockScreenHandler extends ScreenHandler {
    public final ScreenHandlerContext context;

    public HousingBlockScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(RcapScreens.HOUSING_SCREEN_HANDLER, syncId);
        this.context = context;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}