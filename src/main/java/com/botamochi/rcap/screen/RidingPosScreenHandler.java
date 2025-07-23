package com.botamochi.rcap.screen;

import com.botamochi.rcap.block.entity.RidingPosBlockEntity;
import com.botamochi.rcap.screen.ModScreens;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class RidingPosScreenHandler extends ScreenHandler {

    public RidingPosBlockEntity blockEntity;

    public RidingPosScreenHandler(int syncId, PlayerInventory inventory, RidingPosBlockEntity entity) {
        super(ModScreens.RIDING_POS_SCREEN_HANDLER_TYPE, syncId);
        this.blockEntity = entity;
    }

    public long getPlatformId() {
        return blockEntity.getPlatformId();
    }

    public void setPlatformId(long id) {
        blockEntity.setPlatformId(id);
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
