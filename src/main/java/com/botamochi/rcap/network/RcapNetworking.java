package com.botamochi.rcap.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.network.ServerPlayerEntity;
import com.botamochi.rcap.Rcap;

public class RcapNetworking {
    public static final Identifier SET_HOUSING_RESIDENTS = new Identifier(Rcap.MOD_ID, "set_housing_residents");
    public static final Identifier SET_OFFICE_EMPLOYEES = new Identifier(Rcap.MOD_ID, "set_office_employees");
    public static final Identifier SET_RIDING_PLATFORM = new Identifier(Rcap.MOD_ID, "set_riding_platform");

    // サーバーで受信
    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(SET_HOUSING_RESIDENTS, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int val = buf.readInt();
            server.execute(() -> {
                // BlockEntity取得して値セット
                if (player.world.getBlockEntity(pos) instanceof com.botamochi.rcap.block.HousingBlockEntity be) {
                    be.setResidents(val);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(SET_OFFICE_EMPLOYEES, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int val = buf.readInt();
            server.execute(() -> {
                // BlockEntity取得して値セット
                if (player.world.getBlockEntity(pos) instanceof com.botamochi.rcap.block.OfficeBlockEntity be) {
                    be.setEmployees(val);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(SET_RIDING_PLATFORM, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int val = buf.readInt();
            server.execute(() -> {
                // BlockEntity取得して値セット
                if (player.world.getBlockEntity(pos) instanceof com.botamochi.rcap.block.RidingPosBlockEntity be) {
                    be.setPlatformId(val);
                }
            });
        });
    }
}