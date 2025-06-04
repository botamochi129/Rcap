package com.botamochi.rcap.block;

import com.botamochi.rcap.Rcap;
import com.botamochi.rcap.data.ExitManager;
import com.botamochi.rcap.data.PassengerData;
import com.botamochi.rcap.route.PassengerRouteUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class EntranceBlock extends Block {
    public EntranceBlock() {
        super(Settings.of(Material.METAL).strength(3.0F));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand,
                              net.minecraft.util.hit.BlockHitResult hit) {
        if (!world.isClient) {
            ServerWorld serverWorld = (ServerWorld) world;
            BlockPos exit = ExitManager.getRandomExit(serverWorld);
            if (exit == null) {
                player.sendMessage(Text.literal("出口がありません！"), false);
                return ActionResult.SUCCESS;
            }

            List<Object> route = PassengerRouteUtil.findFullRoute(serverWorld, pos.up(), exit.up());
            if (route == null || route.isEmpty()) {
                player.sendMessage(Text.literal("ルートが見つかりません！"), false);
                return ActionResult.SUCCESS;
            }

            PassengerData passenger = new PassengerData(route);
            Rcap.passengerManager.addPassenger(passenger);
            Rcap.passengerManager.sendSyncPacketToAll(serverWorld);
        }
        return ActionResult.SUCCESS;
    }
}