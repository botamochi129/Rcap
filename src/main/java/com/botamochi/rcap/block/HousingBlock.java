package com.botamochi.rcap.block;

import com.botamochi.rcap.block.entity.HousingBlockEntity;
import com.botamochi.rcap.block.entity.RcapBlockEntity;
import com.botamochi.rcap.passenger.Passenger;
import com.botamochi.rcap.passenger.PassengerManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.mtr.mod.client.MinecraftClientData;

public class HousingBlock extends BlockWithEntity {
    public HousingBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient) {
            // 住宅ブロックを設置したら座標を PassengerManager に登録
            PassengerManager.registerHousing(pos);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        // サーバーサイドのみで登録。クライアント側は描画しか行わないので無視する
        if (!world.isClient) {
            // Y 座標をプレイヤーの足元に合わせる (0.5 足すと地面から少し上がる)
            BlockPos spawnPos = new BlockPos(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);

            // PassengerManager に登録
            PassengerManager.addPassenger(MinecraftClientData.getInstance(), world);

            // プレイヤーにフィードバック (チャットで知らせる)
            player.sendMessage(Text.literal("乗客を生成しました: " + spawnPos.getX() + ", " + spawnPos.getY() + ", " + spawnPos.getZ()), false);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new HousingBlockEntity(pos, state);
    }
}
