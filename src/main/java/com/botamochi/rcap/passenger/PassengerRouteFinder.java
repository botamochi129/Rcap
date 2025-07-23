package com.botamochi.rcap.passenger;

import java.util.List;

public class PassengerRouteFinder {

    /**
     * 自宅の最寄駅から職場の最寄駅までのプラットフォームIDルート取得（MTR連携）
     * @param homeBlockPos 自宅BlockPosのLong値
     * @param officeBlockPos 職場BlockPosのLong値
     * @return プラットフォームIDの経路リスト(例)
     */
    public static List<Long> findRoute(long homeBlockPos, long officeBlockPos) {
        // TODO: MTR APIを使い、最寄駅検出→ルート検索

        // 仮戻り値: home => office
        return List.of(homeBlockPos, officeBlockPos);
    }
}
