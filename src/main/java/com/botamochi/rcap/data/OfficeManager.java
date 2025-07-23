package com.botamochi.rcap.data;

import com.botamochi.rcap.block.entity.OfficeBlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OfficeManager {
    private static final List<OfficeBlockEntity> offices = new ArrayList<>();

    public static OfficeBlockEntity getRandomAvailableOffice() {
        List<OfficeBlockEntity> available = offices.stream().filter(OfficeBlockEntity::hasRoom).toList();
        if (available.isEmpty()) return null;
        return available.get(new Random().nextInt(available.size()));
    }

    // 初期化や管理用メソッドも追加
}
