package com.botamochi.rcap.passenger;

import net.minecraft.server.MinecraftServer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PassengerManager {

    private static final List<Passenger> PASSENGERS = new ArrayList<>();

    // 保存ファイルパスや形式は環境に合わせて調整してください
    private static final File SAVE_FILE = new File("rcap_data/passengers.dat");

    /** サーバー側tickで呼び出す */
    public static void tick(MinecraftServer server) {
        long now = System.currentTimeMillis();

        for (Passenger p : new ArrayList<>(PASSENGERS)) {
            if (now >= p.getNextActionTime()) {
                switch (p.getState()) {
                    case AT_HOME -> {
                        // 自宅から出勤開始
                        p.setState(Passenger.State.TO_OFFICE);
                        // 任意で現在位置変更など
                        p.setNextActionTime(now + 20_000); // 20秒後着くなど
                    }
                    case TO_OFFICE -> {
                        p.setState(Passenger.State.AT_OFFICE);
                        p.setCurrentPos(p.getOfficePos());
                        p.setNextActionTime(now + 60_000); // 職場に1分いるなど
                    }
                    case AT_OFFICE -> {
                        // 夕方帰宅
                        p.setState(Passenger.State.TO_HOME);
                        p.setNextActionTime(now + 20_000);
                    }
                    case TO_HOME -> {
                        p.setState(Passenger.State.AT_HOME);
                        p.setCurrentPos(p.getHomePos());
                        p.setNextActionTime(now + 300_000); // 次の日まで自宅など
                    }
                }
            }
        }
    }

    public static void addPassenger(Passenger p) {
        PASSENGERS.add(p);
    }

    public static void removePassenger(UUID uuid) {
        PASSENGERS.removeIf(p -> p.uuid.equals(uuid));
    }

    public static List<Passenger> getPassengers() {
        return Collections.unmodifiableList(PASSENGERS);
    }

    // 保存
    public static void save() {
        // NBTやJSON形式など環境にあわせて実装してください
        // 省略
    }

    // 読み込み
    public static void load() {
        // 省略
    }
}
