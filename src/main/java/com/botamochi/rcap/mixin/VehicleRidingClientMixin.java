package com.botamochi.rcap.mixin;

import mtr.data.VehicleRidingClient;
import org.spongepowered.asm.mixin.Mixin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mixin(VehicleRidingClient.class)
public class VehicleRidingClientMixin {
    // クライアント同期パケットで受け取ったAI乗客UUIDリストを列車ごとに持つ
    private final Set<UUID> rcap_virtualPassengers = new HashSet<>();

    public Set<UUID> rcap_getVirtualPassengers() {
        return rcap_virtualPassengers;
    }

    // クライアント受信時にこのリストを更新
}