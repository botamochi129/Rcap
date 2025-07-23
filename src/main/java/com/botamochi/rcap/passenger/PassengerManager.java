package com.botamochi.rcap.passenger;

import com.botamochi.rcap.block.entity.HousingBlockEntity;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PassengerManager {

    private static final List<Passenger> PASSENGERS = new ArrayList<>();
    private static final long WALK_TIME = 10_000L; // 10秒、仮設定

    public static void tick(MinecraftServer server) {
        long now = System.currentTimeMillis();
        int hour = java.time.LocalTime.now().getHour(); // 0〜23

        if (hour >= 6 && hour < 8) { // 朝
            for (HousingBlockEntity housing : HousingBlockEntity.getAllHousingBlocks(server)) {
                housing.spawnPassengersIfTime(now);
            }
        }

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
            // ...ほか同様
        }
    }

    public static void addPassenger(Passenger p) {
        PASSENGERS.add(p);
    }

    public static List<Passenger> getPassengers() {
        return List.copyOf(PASSENGERS);
    }

    // 保存・読み込みCPU負荷や待機時間が多いので別スレッド推奨（中略）
    public static void save() { }
    public static void load() { }
}
