package com.botamochi.rcap.screen;

import com.botamochi.rcap.block.entity.RidingPosBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class RidingPosScreenHandler extends ScreenHandler {

    private final RidingPosBlockEntity blockEntity;

    public RidingPosScreenHandler(int syncId, PlayerInventory playerInventory, RidingPosBlockEntity blockEntity) {
        super(ModScreens.RIDING_POS_SCREEN_HANDLER_TYPE, syncId);
        this.blockEntity = blockEntity;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return blockEntity.getPos().isWithinDistance(player.getBlockPos(), 8);
    }

    public long getPlatformId() {
        return blockEntity.getPlatformId();
    }

    public void setPlatformId(long id) {
        blockEntity.setPlatformId(id);
    }
}
