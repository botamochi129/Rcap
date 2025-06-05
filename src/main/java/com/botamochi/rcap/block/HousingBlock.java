package com.botamochi.rcap.block;

import com.botamochi.rcap.passenger.PassengerManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class HousingBlock extends Block {
    public HousingBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        // サーバーサイドのみで登録。クライアント側は描画しか行わないので無視する
        if (!world.isClient) {
            // Y 座標をプレイヤーの足元に合わせる (0.5 足すと地面から少し上がる)
            Vec3d spawnPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);

            // PassengerManager に登録
            PassengerManager.addPassenger(spawnPos);

            // プレイヤーにフィードバック (チャットで知らせる)
            player.sendMessage(Text.literal("乗客を生成しました: " + spawnPos.x + ", " + spawnPos.y + ", " + spawnPos.z), false);
        }
        return ActionResult.SUCCESS;
    }
}
