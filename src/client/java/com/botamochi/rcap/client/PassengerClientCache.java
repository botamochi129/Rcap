package com.botamochi.rcap.client;

import com.botamochi.rcap.data.PassengerRenderData;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PassengerClientCache {
    public static final List<PassengerRenderData> PASSENGERS = new CopyOnWriteArrayList<>();
}