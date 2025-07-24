package com.botamochi.rcap;

import com.botamochi.rcap.block.HousingBlock;
import com.botamochi.rcap.block.OfficeBlock;
import com.botamochi.rcap.block.RidingPosBlock;
import com.botamochi.rcap.block.entity.HousingBlockEntity;
import com.botamochi.rcap.block.entity.OfficeBlockEntity;
import com.botamochi.rcap.block.entity.RidingPosBlockEntity;
import com.botamochi.rcap.data.CompanyManager;
import com.botamochi.rcap.data.HousingManager;
import com.botamochi.rcap.network.HousingBlockPacketReceiver;
import com.botamochi.rcap.network.OfficeBlockPacketReceiver;
import com.botamochi.rcap.network.RcapServerPackets;
import com.botamochi.rcap.network.ServerNetworking;
import com.botamochi.rcap.passenger.PassengerManager;
import com.botamochi.rcap.screen.ModScreens;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Rcap implements ModInitializer {
    public static final String MOD_ID = "rcap";
    public static final Block HOUSING_BLOCK = new HousingBlock(FabricBlockSettings.of(Material.STONE).strength(2.0f));
    public static final Block OFFICE_BLOCK = new OfficeBlock(FabricBlockSettings.of(Material.STONE).strength(2.0f));
    public static BlockEntityType<OfficeBlockEntity> OFFICE_BLOCK_ENTITY;
    public static BlockEntityType<HousingBlockEntity> HOUSING_BLOCK_ENTITY;
    public static final Block RIDING_POS_BLOCK = new RidingPosBlock(FabricBlockSettings.of(Material.STONE).strength(2.0f));
    public static BlockEntityType<RidingPosBlockEntity> RIDING_POS_BLOCK_ENTITY;

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "housing_block"), HOUSING_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "housing_block"),
                new BlockItem(HOUSING_BLOCK, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)));
        HOUSING_BLOCK_ENTITY = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                new Identifier(MOD_ID, "housing_block_entity"),
                FabricBlockEntityTypeBuilder.create(HousingBlockEntity::new, HOUSING_BLOCK).build(null)
        );

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "office_block"), OFFICE_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "office_block"), new BlockItem(OFFICE_BLOCK, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)));
        OFFICE_BLOCK_ENTITY = Registry.register(
                Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "office_block_entity"),
                BlockEntityType.Builder.create(OfficeBlockEntity::new, OFFICE_BLOCK).build(null)
        );

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "riding_pos_block"), RIDING_POS_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "riding_pos_block"), new BlockItem(RIDING_POS_BLOCK, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)));
        RIDING_POS_BLOCK_ENTITY = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                new Identifier(MOD_ID, "riding_pos_block_entity"),
                FabricBlockEntityTypeBuilder.create(RidingPosBlockEntity::new, RIDING_POS_BLOCK).build(null)
        );

        ModScreens.registerScreenHandlers();
        RcapServerPackets.register();
        ServerNetworking.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerWorld world = server.getOverworld();
            if (world != null) {
                CompanyManager.init(world);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            RcapServerPackets.sendCompanyList(handler.getPlayer());
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            CompanyManager.save(); // ← 忘れずに追加！
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            HousingManager.clear();
        });

        HousingBlockPacketReceiver.register();
        OfficeBlockPacketReceiver.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerWorld world = server.getOverworld();
            if (world != null) PassengerManager.init(world);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // サンプル: 乗客ランダム移動処理やAI・状態更新
            // for(Passenger p: PassengerManager.PASSENGER_LIST) { ...update p.x, p.y, p.z... }

            // 一定tickごとに全クライアントへ同期
            if (server.getTicks() % 20 == 0) { // 20tick=1秒ごと
                PassengerManager.broadcastToAllPlayers(server);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerWorld w : server.getWorlds()) {
                long time = w.getTime();
                for (HousingBlockEntity house : HousingManager.getAll(w)) {
                    house.spawnPassengersIfTime(w, time);
                }
            }
        });
    }
}
