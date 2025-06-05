package com.botamochi.rcap.passenger;

import net.minecraft.data.DataCache;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.mtr.core.data.Data;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PassengerManager {
    private static final List<Passenger> passengers = new CopyOnWriteArrayList<>();

    public static void addPassenger(Vec3d pos) {
        passengers.add(new Passenger(pos));
    }

    public static List<Passenger> getPassengers() {
        return passengers;
    }

    public static void clearAll() {
        passengers.clear();
    }

    public static void tickAll(ServerWorld world, Data data) {
        for (Passenger passenger : passengers) {
            passenger.tick(world, data);
        }
    }
}
