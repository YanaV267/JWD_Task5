package com.development.task5.entity;

import com.development.task5.util.BusStopIdGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class BusStop {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int MIN_TIMEOUT = 1;
    private static final int MAX_TIMEOUT = 10;
    private final long busStopId;
    private final int MAX_BUS_CAPACITY;
    private int currentPeopleAmount;

    {
        busStopId = BusStopIdGenerator.generateId();
    }

    public BusStop() {
        MAX_BUS_CAPACITY = 0;
    }

    public BusStop(int maxBusCapacity) {
        MAX_BUS_CAPACITY = maxBusCapacity;
        currentPeopleAmount = new Random().nextInt(40) + 1;
    }

    public long getBusStopId() {
        return busStopId;
    }

    public int getMaxBusCapacity() {
        return MAX_BUS_CAPACITY;
    }

    public int getCurrentPeopleAmount() {
        return currentPeopleAmount;
    }

    public void processBus(Bus bus) {
        LOGGER.info("Bus stop {} is processing by bus {}", busStopId, bus.getBusId());

        Route route = Route.getInstance();
        currentPeopleAmount += route.getPeopleOffBus(bus);
        currentPeopleAmount = new Random().nextInt(currentPeopleAmount + 10 -
                Math.max(currentPeopleAmount - 10, 0)) + Math.max(currentPeopleAmount - 10, 0);
        currentPeopleAmount -= route.getPeopleOnBus(bus, this);

        int timeout = new Random().nextInt(MAX_TIMEOUT - MIN_TIMEOUT) + MIN_TIMEOUT;
        try {
            TimeUnit.SECONDS.sleep(timeout);
        } catch (InterruptedException exception) {
            LOGGER.error("Error was found while processing bus {} on the bus stop {} : {}",
                    bus.getBusId(), busStopId, exception);
            Thread.currentThread().interrupt();
        }
        LOGGER.info("Bus stop {} finished processing by bus {}", busStopId, bus.getBusId());
    }
}
