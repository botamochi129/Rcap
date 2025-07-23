package com.botamochi.rcap.passenger;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.UUID;

public class Passenger {

    public final UUID uuid;         // 固有ID

    private long homePos;           // 自宅ブロックの位置（BlockPos.toLong()）
    private long officePos;         // 職場ブロックの位置
    private long currentPos;        // 現在の移動中の位置

    public State state;            // 現時点の状態
    public long nextActionTime;    // 次回行動時間（ミリ秒epoch）

    public Passenger(UUID uuid, long homePos, long officePos) {
        this.uuid = uuid;
        this.homePos = homePos;
        this.officePos = officePos;
        this.currentPos = homePos;
        this.state = State.AT_HOME;
        this.nextActionTime = System.currentTimeMillis() + 60_000; // 初期は1分後に行動開始予定
    }

    public enum State {
        AT_HOME,
        TO_OFFICE_WALKING,
        TO_OFFICE_STATION,
        TO_OFFICE_ON_TRAIN,
        AT_OFFICE,
        TO_HOME_WALKING,
        TO_HOME_STATION,
        TO_HOME_ON_TRAIN,
        AT_HOME_RETURNED
    }

    public void setCurrentPos(BlockPos pos) {
        this.currentPos = pos.asLong();
    }

    public BlockPos getCurrentBlockPos() {
        return BlockPos.fromLong(currentPos);
    }

    //=============================
    // NBT 保存 / 復元
    //=============================

    /** NBT保存用に変換 */
    public NbtCompound writeNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putUuid("UUID", uuid);
        nbt.putLong("HomePos", homePos);
        nbt.putLong("OfficePos", officePos);
        nbt.putLong("CurrentPos", currentPos);
        nbt.putString("State", state.name());
        nbt.putLong("NextActionTime", nextActionTime);
        return nbt;
    }

    /** NBTから読み込みでPassengerを生成 */
    public static Passenger fromNbt(NbtCompound nbt) {
        UUID uuid = nbt.getUuid("UUID");
        long home = nbt.getLong("HomePos");
        long office = nbt.getLong("OfficePos");

        Passenger p = new Passenger(uuid, home, office);
        p.currentPos = nbt.contains("CurrentPos", NbtElement.LONG_TYPE) ? nbt.getLong("CurrentPos") : home;
        p.state = Passenger.State.valueOf(nbt.getString("State"));
        p.nextActionTime = nbt.getLong("NextActionTime");
        return p;
    }

    //=============================
    // ゲッター / セッター
    //=============================

    public long getHomePosLong() {
        return homePos;
    }

    public long getOfficePosLong() {
        return officePos;
    }

    public long getCurrentPosLong() {
        return currentPos;
    }

    public BlockPos getHomePos() {
        return BlockPos.fromLong(homePos);
    }

    public BlockPos getOfficePos() {
        return BlockPos.fromLong(officePos);
    }

    public BlockPos getCurrentPos() {
        return BlockPos.fromLong(currentPos);
    }

    public State getState() {
        return state;
    }

    public void setState(State newState) {
        this.state = newState;
    }

    public long getNextActionTime() {
        return nextActionTime;
    }

    public void setNextActionTime(long nextActionTime) {
        this.nextActionTime = nextActionTime;
    }

    public boolean isAtHome() {
        return state == State.AT_HOME;
    }

    public boolean isAtOffice() {
        return state == State.AT_OFFICE;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Passenger cast && cast.uuid.equals(this.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        return "Passenger{" +
                "uuid=" + uuid +
                ", state=" + state +
                ", currentPos=" + getCurrentPos() +
                ", nextAt=" + nextActionTime +
                '}';
    }
}
