package com.botamochi.rcap.block;

import com.botamochi.rcap.Rcap;
import com.botamochi.rcap.block.entity.HousingBlockEntity;

import com.botamochi.rcap.passenger.Passenger;
import com.botamochi.rcap.passenger.PassengerManager;
import com.botamochi.rcap.screen.HousingBlockScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

public class HousingBlock extends BlockWithEntity {
    public HousingBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new HousingBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof HousingBlockEntity) {
                player.openHandledScreen((HousingBlockEntity) be); // ← これだけでOK
            }

            // 乗客の一例追加
            long newId = System.currentTimeMillis();
            String name = "テスト乗客"; // 任意の名前

            // 座標はブロック中央 + 少し浮上（y + 1.0）
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + 0.5;

            int color = 0xFFFFFF; // 色は白

            Passenger passenger = new Passenger(newId, name, x, y, z, color);
            PassengerManager.PASSENGER_LIST.add(passenger);
            PassengerManager.save(); // PersistentStateに保存フラグ

            PassengerManager.broadcastToAllPlayers(((ServerWorld)world).getServer());

            player.sendMessage(Text.literal("乗客を召喚しました！"), false);
        }
        return ActionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
                                                                  BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, Rcap.HOUSING_BLOCK_ENTITY, HousingBlockEntity::tick);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL ; }
}
