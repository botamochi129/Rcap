package com.botamochi.rcap.passenger;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class Passenger {
    public long id;
    public String name;
    public double x, y, z;
    public int color;
    public int skinIndex;

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

    public static final Identifier[] SKINS = {
            new Identifier("rcap", "textures/entity/passenger/custom_skin.png"),
            new Identifier("rcap", "textures/entity/passenger/custom_skin_2.png")
    };
    // ワールドID（Dimensionの名前など）
    public String worldId;

    // クライアントが利用するための（オプショナル）列車参照ID（クライアント側で埋める）
    public Long currentTrainId = null;
    // クライアントで検出した「何号車か」を保持する（サーバ側では基本的に決められないのでクライアントのみで保持）
    public int currentCarIndex = -1;

    // --- 追加フィールド（サーバがスケジュール情報からセットする） ---
    // 乗車開始推定時刻（millis）。
    public long boardingTimeMillis = -1L;
    // 降車推定時刻（millis）。
    public long alightTimeMillis = -1L;
    // 乗車したプラットフォームID
    public long boardingPlatformId = -1L;
    // 降車予定のプラットフォームID
    public long alightingPlatformId = -1L;
    // スケジュールから得られた routeId
    public long scheduledRouteId = -1L;

    // 追加: サーバが計算したプラットフォーム／オフィス座標（クライアントが platformId を持っていない場合のフォールバック）
    public double boardingX = Double.NaN, boardingY = Double.NaN, boardingZ = Double.NaN;
    public double alightX = Double.NaN, alightY = Double.NaN, alightZ = Double.NaN;

    // 追加: 最終目的地（オフィス）座標。WALKING_TO_DESTINATION で使用
    public double destinationX = Double.NaN, destinationY = Double.NaN, destinationZ = Double.NaN;

    public Passenger(long id, String name, double x, double y, double z, int color, String worldId) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
        this.skinIndex = (int) (Math.random() * SKINS.length);
        this.worldId = worldId;
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
        tag.putString("worldId", worldId);

        // ルート保存
        NbtList listTag = new NbtList();
        for (Long platformId : route) {
            listTag.add(NbtLong.of(platformId));
        }
        tag.put("route", listTag);

        tag.putInt("routeTargetIndex", routeTargetIndex);
        tag.putInt("moveState", moveState.ordinal());
        tag.putInt("skinIndex", skinIndex);

        // 追加フィールド
        tag.putLong("boardingTimeMillis", boardingTimeMillis);
        tag.putLong("alightTimeMillis", alightTimeMillis);
        tag.putLong("boardingPlatformId", boardingPlatformId);
        tag.putLong("alightingPlatformId", alightingPlatformId);
        tag.putLong("scheduledRouteId", scheduledRouteId);

        // 座標保存（DoubleをNBTに）
        tag.putDouble("boardingX", boardingX);
        tag.putDouble("boardingY", boardingY);
        tag.putDouble("boardingZ", boardingZ);
        tag.putDouble("alightX", alightX);
        tag.putDouble("alightY", alightY);
        tag.putDouble("alightZ", alightZ);

        // 目的地（オフィス）座標保存
        tag.putDouble("destinationX", destinationX);
        tag.putDouble("destinationY", destinationY);
        tag.putDouble("destinationZ", destinationZ);

        // currentCarIndex はクライアントのみで検出・使用する想定のため NBT に含めない（必要なら追加可能）

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
                tag.getInt("color"),
                tag.contains("worldId") ? tag.getString("worldId") : "minecraft:overworld"
        );

        p.route.clear();
        if (tag.contains("route")) {
            NbtList routeList = tag.getList("route", NbtElement.LONG_TYPE);
            for (int i = 0; i < routeList.size(); i++) {
                var element = routeList.get(i);
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
        p.skinIndex = tag.contains("skinIndex") ? tag.getInt("skinIndex") : (int)(Math.random() * SKINS.length);

        p.boardingTimeMillis = tag.contains("boardingTimeMillis") ? tag.getLong("boardingTimeMillis") : -1L;
        p.alightTimeMillis = tag.contains("alightTimeMillis") ? tag.getLong("alightTimeMillis") : -1L;
        p.boardingPlatformId = tag.contains("boardingPlatformId") ? tag.getLong("boardingPlatformId") : -1L;
        p.alightingPlatformId = tag.contains("alightingPlatformId") ? tag.getLong("alightingPlatformId") : -1L;
        p.scheduledRouteId = tag.contains("scheduledRouteId") ? tag.getLong("scheduledRouteId") : -1L;

        p.boardingX = tag.contains("boardingX") ? tag.getDouble("boardingX") : Double.NaN;
        p.boardingY = tag.contains("boardingY") ? tag.getDouble("boardingY") : Double.NaN;
        p.boardingZ = tag.contains("boardingZ") ? tag.getDouble("boardingZ") : Double.NaN;
        p.alightX = tag.contains("alightX") ? tag.getDouble("alightX") : Double.NaN;
        p.alightY = tag.contains("alightY") ? tag.getDouble("alightY") : Double.NaN;
        p.alightZ = tag.contains("alightZ") ? tag.getDouble("alightZ") : Double.NaN;

        p.destinationX = tag.contains("destinationX") ? tag.getDouble("destinationX") : Double.NaN;
        p.destinationY = tag.contains("destinationY") ? tag.getDouble("destinationY") : Double.NaN;
        p.destinationZ = tag.contains("destinationZ") ? tag.getDouble("destinationZ") : Double.NaN;

        // currentTrainId / currentCarIndex はクライアント側で検出・埋めるため NBT では保持しない
        p.currentTrainId = null;
        p.currentCarIndex = -1;

        return p;
    }
}