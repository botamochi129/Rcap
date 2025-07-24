package com.botamochi.rcap.passenger;

import com.botamochi.rcap.network.RcapServerPackets;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentStateManager;
import java.util.ArrayList;
import java.util.List;

// サーバーもクライアントも同じこのクラスを利用（クライアントは一部だけ使う）
public class PassengerManager {
    public static List<Passenger> PASSENGER_LIST = new ArrayList<>();
    private static PassengerState passengerState;

    public static void init(ServerWorld world) {
        PersistentStateManager manager = world.getPersistentStateManager();
        passengerState = manager.getOrCreate(
                PassengerState::createFromNbt, PassengerState::new, PassengerState.KEY
        );
        PASSENGER_LIST = passengerState.passengerList;
    }

    public static void save() {
        if (passengerState != null) passengerState.markDirty();
    }

    public static void broadcastToAllPlayers(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            RcapServerPackets.sendPassengerList(player);
        }
    }
}
