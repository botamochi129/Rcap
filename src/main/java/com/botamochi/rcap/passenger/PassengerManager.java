package com.botamochi.rcap.passenger;

import com.botamochi.rcap.network.RcapServerPackets;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentStateManager;

import java.util.ArrayList;
import java.util.List;

public class PassengerManager {
    public static List<Passenger> PASSENGER_LIST = new ArrayList<>();
    private static PassengerState passengerState;

    public static final java.util.concurrent.ConcurrentLinkedQueue<Passenger> PENDING_ADD_QUEUE = new java.util.concurrent.ConcurrentLinkedQueue<>();

    // 必要です：ワールドロード時にPersistentStateからPassengerState取得＋PASSENGER_LIST同期
    public static void init(ServerWorld world) {
        PersistentStateManager manager = world.getPersistentStateManager();
        passengerState = manager.getOrCreate(
                PassengerState::createFromNbt, PassengerState::new, PassengerState.KEY
        );
        PASSENGER_LIST = passengerState.passengerList;
    }

    // 変更あり：PersistentStateの変更をMarkし保存トリガーをセット
    public static void save() {
        if (passengerState != null) passengerState.markDirty();
    }

    // クライアントへ乗客リスト全送信（実装済みのパケット送信呼び出しを使用）
    public static void broadcastToAllPlayers(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            RcapServerPackets.sendPassengerList(player);
        }
    }
}
