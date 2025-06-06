package com.botamochi.rcap.block;

import com.botamochi.rcap.block.entity.OfficeBlockEntity;
import com.botamochi.rcap.passenger.PassengerManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class OfficeBlock extends BlockWithEntity {

    public OfficeBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient) {
            // 職場ブロックを設置したら座標を PassengerManager に登録
            PassengerManager.registerOffice(pos);
        }
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new OfficeBlockEntity(pos, state);
    }
}
