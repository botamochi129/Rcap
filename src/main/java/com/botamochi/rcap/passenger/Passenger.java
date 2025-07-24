package com.botamochi.rcap.passenger;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;

import java.util.ArrayList;
import java.util.List;

public class Passenger {
    public long id;
    public String name;
    public double x, y, z;
    public int color;

    // --- 追加部分 ---
    /** 移動ルートとしてのプラットフォームIDリスト */
    public List<Long> route = new ArrayList<>();

    /** 現在目標としているルートのインデックス */
    public int routeTargetIndex = 0;

    /** 移動状態 */
    public enum MoveState {
        WALKING_TO_PLATFORM,
        WAITING_FOR_TRAIN,
        ON_TRAIN,
        WALKING_TO_DESTINATION,
        IDLE
    }
    public MoveState moveState = MoveState.IDLE;

    // --- コンストラクタ・NBT変換は既存コード＋追加分 ---

    public Passenger(long id, String name, double x, double y, double z, int color) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
    }

    /** NBTシリアライズ */
    public NbtCompound toNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putLong("id", id);
        tag.putString("name", name);
        tag.putDouble("x", x);
        tag.putDouble("y", y);
        tag.putDouble("z", z);
        tag.putInt("color", color);

        // ルート保存
        NbtList listTag = new NbtList();
        for (Long platformId : route) {
            listTag.add(NbtLong.of(platformId));
        }
        tag.put("route", listTag);

        tag.putInt("routeTargetIndex", routeTargetIndex);
        tag.putInt("moveState", moveState.ordinal());

        return tag;
    }

    /** NBTデシリアライズ */
    public static Passenger fromNbt(NbtCompound tag) {
        Passenger p = new Passenger(
                tag.getLong("id"),
                tag.getString("name"),
                tag.getDouble("x"),
                tag.getDouble("y"),
                tag.getDouble("z"),
                tag.getInt("color")
        );

        p.route.clear();
        if (tag.contains("route")) {
            NbtList routeList = tag.getList("route", NbtElement.LONG_TYPE); // TYPE=4はNbtLong

            for (int i = 0; i < routeList.size(); i++) {
                NbtElement element = routeList.get(i);
                if (element instanceof NbtLong nbtLong) {
                    p.route.add(nbtLong.longValue());
                }
            }
        }

        p.routeTargetIndex = tag.getInt("routeTargetIndex");

        int moveStateOrdinal = tag.getInt("moveState");
        if (moveStateOrdinal >= 0 && moveStateOrdinal < MoveState.values().length) {
            p.moveState = MoveState.values()[moveStateOrdinal];
        } else {
            p.moveState = MoveState.IDLE;
        }
        return p;
    }
}
