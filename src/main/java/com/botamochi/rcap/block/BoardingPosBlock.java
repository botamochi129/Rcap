package com.botamochi.rcap.block;

import com.botamochi.rcap.data.BoardingPosManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BoardingPosBlock extends Block {
    public BoardingPosBlock() {
        super(Block.Settings.of(Material.METAL).strength(3.0F));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand,
                              net.minecraft.util.hit.BlockHitResult hit) {
        return ActionResult.SUCCESS;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity player, net.minecraft.item.ItemStack itemStack) {
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            BoardingPosManager.get(serverWorld).add(serverWorld, pos);
        }
        super.onPlaced(world, pos, state, player, itemStack);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            BoardingPosManager.get(serverWorld).remove(serverWorld, pos);
        }
        super.onBreak(world, pos, state, player);
    }
}