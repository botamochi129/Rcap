package com.botamochi.rcap.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;

public class RidingPosBlockScreenHandler extends ScreenHandler {
    public final ScreenHandlerContext context;

    public RidingPosBlockScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(RcapScreens.RIDING_POS_SCREEN_HANDLER, syncId);
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