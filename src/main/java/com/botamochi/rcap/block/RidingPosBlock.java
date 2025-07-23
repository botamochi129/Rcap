package com.botamochi.rcap.block;

import com.botamochi.rcap.block.entity.RidingPosBlockEntity;
import com.botamochi.rcap.network.RcapServerPackets;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RidingPosBlock extends BlockWithEntity {
    public RidingPosBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RidingPosBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof RidingPosBlockEntity) {
                RcapServerPackets.sendOpenGui(serverPlayer, pos); // <- ここでパケット送信！
            }
        }
        return ActionResult.SUCCESS;
    }
}
