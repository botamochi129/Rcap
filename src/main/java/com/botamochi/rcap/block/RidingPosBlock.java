package com.botamochi.rcap.block;

import com.botamochi.rcap.block.entity.RidingPosBlockEntity;
import com.botamochi.rcap.passenger.PassengerManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.mtr.core.data.Data;
import org.mtr.core.data.Platform;
import org.mtr.mod.client.MinecraftClientData;
import org.mtr.mod.block.BlockPlatform; // MTRのプラットフォームブロックをインポート

import java.util.Collection; // Collectionをインポート

public class RidingPosBlock extends BlockWithEntity {
    public RidingPosBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient) {
                // プラットフォームブロックの場所が見つかったら、そこに対応するPlatform IDを探す
                Data data = MinecraftClientData.getInstance(); // サーバーサイドでもクライアントデータを使用
                Platform foundPlatform = null;
                // MTRのプラットフォームはブロックに直接紐付かない場合があるため、座標から最も近いプラットフォームを探す
                // または、PlatformBlockのNBT情報からPlatform IDを取得できるかMTRのソースを確認する
                // 現状、最も近いプラットフォームを探す
                Collection<Platform> platforms = data.platforms;
                double minDist = Double.MAX_VALUE;

                for (Platform platform : platforms) {
                    // プラットフォームのMidPosがこのブロックの近くにあるか
                    Vec3d platformMidPos = new Vec3d(platform.getMidPosition().getX(), platform.getMidPosition().getY(), platform.getMidPosition().getZ());
                    double dist = platformMidPos.distanceTo(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                    if (dist < minDist) {
                        minDist = dist;
                        foundPlatform = platform;
                    }
                }

                if (foundPlatform != null && minDist < 10) { // 十分に近いプラットフォームを見つけた場合
                    PassengerManager.registerRidingPos(foundPlatform.getId(), pos);
                } else {
                    System.out.println("Could not find a nearby MTR Platform for RidingPosBlock at " + pos);
                }
        }
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RidingPosBlockEntity(pos, state);
    }
}