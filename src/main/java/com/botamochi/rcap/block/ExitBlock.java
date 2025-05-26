package com.botamochi.rcap.block;

import com.botamochi.rcap.data.ExitManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExitBlock extends Block {
    public ExitBlock() {
        super(Block.Settings.of(Material.METAL).strength(3.0F));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity player, net.minecraft.item.ItemStack itemStack) {
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            ExitManager.addExit(serverWorld, pos);
        }
        super.onPlaced(world, pos, state, player, itemStack);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            ExitManager.removeExit(serverWorld, pos);
        }
        super.onBreak(world, pos, state, player);
    }
}