package com.botamochi.rcap.client.network;

import com.botamochi.rcap.block.entity.RidingPosBlockEntity;
import com.botamochi.rcap.client.screen.RidingPosScreen;
import com.botamochi.rcap.data.Company;
import com.botamochi.rcap.data.CompanyManager;
import com.botamochi.rcap.network.RcapServerPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class RcapClientPackets {

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(RcapServerPackets.OPEN_RIDING_POS_GUI, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            long platformId = buf.readLong();

            client.execute(() -> {
                var world = MinecraftClient.getInstance().world;
                if (world == null) return;

                var be = world.getBlockEntity(pos);
                if (be instanceof RidingPosBlockEntity ridingPos) {
                    ridingPos.setPlatformId(platformId);

                    // ✅ GUIを開く
                    client.setScreen(new RidingPosScreen(ridingPos));
                }
            });
        });
    }
}
