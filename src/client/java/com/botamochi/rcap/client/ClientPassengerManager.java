// ClientPassengerManager.java
package com.botamochi.rcap.client;

import net.minecraft.util.math.Vec3d;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientPassengerManager {
    private static final Map<Long, ClientPassenger> passengers = new ConcurrentHashMap<>();

    public static void updatePassenger(long id, Vec3d pos, boolean isRemoved) {
        if (isRemoved) {
            passengers.remove(id);
        } else {
            passengers.compute(id, (k, v) -> {
                if (v == null) v = new ClientPassenger(id, pos);
                else v.pos = pos;
                return v;
            });
        }
    }

    public static Iterable<ClientPassenger> getPassengers() {
        return passengers.values();
    }
}
