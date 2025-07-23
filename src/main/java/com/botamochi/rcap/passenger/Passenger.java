package com.botamochi.rcap.passenger;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.UUID;

public class Passenger {

    /** この乗客固有の UUID（保存・識別） */
    public final UUID uuid;

    /** ブロック位置（BlockPos.toLong() で変換されたもの） */
    private long homePos;
    private long officePos;
    private long currentPos;

    /** 現在の状態（在宅・出勤中・在職・帰宅中） */
    private State state;

    /** 次のアクションを起こす epoch ミリ秒 */
    private long nextActionTime;

    //=============================
    // コンストラクタ
    //=============================

    public Passenger(UUID uuid, long homePos, long officePos) {
        this.uuid = uuid;
        this.homePos = homePos;
        this.officePos = officePos;
        this.currentPos = homePos; // 初期状態では自宅が現在地
        this.state = State.AT_HOME;
        this.nextActionTime = System.currentTimeMillis() + 60_000; // 仮：1分後に出発
    }

    //=============================
    // 状態定義
    //=============================

    /** 乗客の状態をあらわす列挙（今の段階で4種類） */
    public enum State {
        AT_HOME,
        TO_OFFICE,
        AT_OFFICE,
        TO_HOME
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

    public void setCurrentPos(BlockPos pos) {
        this.currentPos = pos.asLong();
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

    public boolean isMoving() {
        return state == State.TO_OFFICE || state == State.TO_HOME;
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
