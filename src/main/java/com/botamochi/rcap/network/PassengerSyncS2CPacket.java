package com.botamochi.rcap.network;

import com.botamochi.rcap.data.PassengerData;
import com.botamochi.rcap.data.PassengerRenderData;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.List;

public class PassengerSyncS2CPacket {

    // サーバー→クライアント送信用
    public static void write(PacketByteBuf buf, List<PassengerRenderData> passengers) {
        buf.writeInt(passengers.size());
        for (PassengerRenderData p : passengers) {
            buf.writeUuid(p.id);
            writeVec3d(buf, p.pos);
            writeVec3d(buf, p.prevPos);
            buf.writeString(p.skinName, 32); // Stringは最大長を必ず指定
            buf.writeEnumConstant(p.motionState);
        }
    }

    // クライアント側受信用
    public static void read(PacketByteBuf buf, List<PassengerRenderData> clientList) {
        clientList.clear();
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            PassengerRenderData data = new PassengerRenderData(
                    buf.readUuid(),
                    readVec3d(buf),
                    readVec3d(buf),
                    buf.readString(32),
                    buf.readEnumConstant(PassengerData.MotionState.class)
            );
            clientList.add(data);
        }
    }

    private static void writeVec3d(PacketByteBuf buf, Vec3d vec) {
        buf.writeDouble(vec.x);
        buf.writeDouble(vec.y);
        buf.writeDouble(vec.z);
    }

    private static Vec3d readVec3d(PacketByteBuf buf) {
        return new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }
}