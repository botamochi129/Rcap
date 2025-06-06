// ClientPassenger.java
package com.botamochi.rcap.client;

import net.minecraft.util.math.Vec3d;

public class ClientPassenger {
    public final long id;
    public Vec3d pos;

    public ClientPassenger(long id, Vec3d pos) {
        this.id = id;
        this.pos = pos;
    }
}
