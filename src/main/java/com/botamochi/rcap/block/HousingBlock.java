package com.botamochi.rcap.block;

import com.botamochi.rcap.Rcap;
import com.botamochi.rcap.block.entity.HousingBlockEntity;

import com.botamochi.rcap.passenger.Passenger;
import com.botamochi.rcap.passenger.PassengerManager;
import com.botamochi.rcap.passenger.PassengerRouteFinder;
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

import java.util.List;

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
                player.openHandledScreen((HousingBlockEntity) be);
            }

            // ここで適当なオフィスを探す（例としてOfficeManagerなどを使ってください）
            com.botamochi.rcap.block.entity.OfficeBlockEntity office = com.botamochi.rcap.data.OfficeManager.getRandomAvailableOffice();
            if (office == null) {
                player.sendMessage(Text.literal("利用可能なオフィスが見つかりません。"), false);
                return ActionResult.SUCCESS;
            }

            long newId = System.currentTimeMillis();
            String name = "テスト乗客";

            // 住宅・オフィスのBlockPosのlong値
            long homeLong = pos.asLong();
            long officeLong = office.getPos().asLong();

            // ルート検索
            List<Long> route = PassengerRouteFinder.findRoute(world, homeLong, officeLong);
            System.out.println("[Debug] 生成乗客ID=" + newId + " ルートの長さ=" + route.size());

            // 初期座標はルートの最初のプラットフォームの座標か住宅の中央に設定
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + 0.5;

            if (!route.isEmpty()) {
                var railwayData = mtr.data.RailwayData.getInstance(world);
                if (railwayData != null && railwayData.dataCache.platformIdMap.containsKey(route.get(0))) {
                    var platform = railwayData.dataCache.platformIdMap.get(route.get(0));
                    BlockPos platPos = platform.getMidPos();
                    x = platPos.getX() + 0.5;
                    y = platPos.getY();
                    z = platPos.getZ() + 0.5;
                }
            }

            // 乗客生成
            Passenger passenger = new Passenger(newId, name, x, y, z, 0xFFFFFF);

            // ルートと状態をセット
            passenger.route = route;
            passenger.routeTargetIndex = 0;
            passenger.moveState = Passenger.MoveState.WALKING_TO_PLATFORM;

            PassengerManager.PASSENGER_LIST.add(passenger);
            PassengerManager.save();

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
