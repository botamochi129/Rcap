package com.botamochi.rcap.passenger;

import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PassengerManager {

    private static final List<Passenger> PASSENGERS = new ArrayList<>();

    /** 毎Tickまたは定期的に呼び出し、乗客の行動を更新 */
    public static void tick(MinecraftServer server) {
        long now = System.currentTimeMillis();

        Iterator<Passenger> it = PASSENGERS.iterator();
        while (it.hasNext()) {
            Passenger p = it.next();

            if (now < p.nextActionTime) continue;

            // 状態による処理例
            switch (p.state) {
                case AT_HOME:
                    // 出勤準備・オフィス決定・ルート検索など
                    p.state = Passenger.State.TO_OFFICE_WALKING;
                    p.nextActionTime = now + 10_000; // 仮：徒歩移動時間
                    break;

                case TO_OFFICE_WALKING:
                    // 駅に到着した想定で次状態へ
                    p.state = Passenger.State.TO_OFFICE_STATION;
                    p.nextActionTime = now + 5_000;
                    break;

                case TO_OFFICE_STATION:
                    // 電車へ乗車準備
                    p.state = Passenger.State.TO_OFFICE_ON_TRAIN;
                    p.nextActionTime = now + 60_000; // 電車移動時間など
                    break;

                case TO_OFFICE_ON_TRAIN:
                    p.state = Passenger.State.AT_OFFICE;
                    p.nextActionTime = now + 1800_000; // 職場滞在時間（30分など）
                    break;

                case AT_OFFICE:
                    // 夕方帰宅処理へ
                    p.state = Passenger.State.TO_HOME_WALKING;
                    p.nextActionTime = now + 10_000;
                    break;

                case TO_HOME_WALKING:
                    p.state = Passenger.State.TO_HOME_STATION;
                    p.nextActionTime = now + 5_000;
                    break;

                case TO_HOME_STATION:
                    p.state = Passenger.State.TO_HOME_ON_TRAIN;
                    p.nextActionTime = now + 60_000;
                    break;

                case TO_HOME_ON_TRAIN:
                    p.state = Passenger.State.AT_HOME_RETURNED;
                    p.nextActionTime = now + 0;
                    break;

                case AT_HOME_RETURNED:
                    // 家に到着。リストから消す、描画消す等
                    it.remove();
                    break;
            }
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
