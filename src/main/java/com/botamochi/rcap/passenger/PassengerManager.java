package com.botamochi.rcap.passenger;

import com.botamochi.rcap.block.entity.HousingBlockEntity;
import com.botamochi.rcap.data.HousingManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PassengerManager {

    private static final List<Passenger> PASSENGERS = new ArrayList<>();
    private static final long WALK_TIME = 10_000L; // 10秒
    private static int tickCounter = 0;

    public static void tick(MinecraftServer server) {
        long now = System.currentTimeMillis();
        int hour = java.time.LocalTime.now().getHour(); // 0〜23

        tickCounter++;
        if (tickCounter % 100 == 0) { // 約5秒に1回 ※20TPSとして
            for (ServerWorld world : server.getWorlds()) {
                if (hour >= 6 && hour < 8) { // 朝
                    for (HousingBlockEntity housing : HousingManager.getAll(world)) {
                        housing.spawnPassengersIfTime(now);
                    }
                }
            }
        }

        // --- パッセンジャー処理 ---
        List<Passenger> copy = new ArrayList<>(PASSENGERS);
        for (Passenger p : copy) {
            if (now >= p.getNextActionTime()) {
                processPassenger(p, now);
            }
        }
    }

    private static void processPassenger(Passenger p, long now) {
        switch (p.getState()) {
            case AT_HOME -> {
                p.setState(Passenger.State.TO_OFFICE_WALKING);
                p.setNextActionTime(now + WALK_TIME);
            }
            // ... 他のステートもここに処理を追加
        }
    }

    public static void addPassenger(Passenger p) {
        PASSENGERS.add(p);
    }

    public static List<Passenger> getPassengers() {
        return List.copyOf(PASSENGERS);
    }

    public static void save() {}
    public static void load() {}
}
