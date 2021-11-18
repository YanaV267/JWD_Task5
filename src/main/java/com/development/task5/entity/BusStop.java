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
    private final int MAX_CAPACITY;
    private final long busStopId;
    private int currentCapacity;

    public BusStop(int maxCapacity) {
        MAX_CAPACITY = maxCapacity;
        busStopId = BusStopIdGenerator.generateId();
    }

    public long getBusStopId() {
        return busStopId;
    }

    public int getMaxCapacity() {
        return MAX_CAPACITY;
    }

    public int getCurrentCapacity() {
        return currentCapacity;
    }

    public void setCurrentCapacity(int currentCapacity) {
        this.currentCapacity = currentCapacity;
    }

    public void processBus(Bus bus) {
        LOGGER.info("Bus stop {} is processing by bus {}", busStopId, bus.getBusId());
        int timeout = new Random().nextInt(MAX_TIMEOUT - MIN_TIMEOUT) + MIN_TIMEOUT;
        try {
            TimeUnit.SECONDS.sleep(timeout);
        } catch (InterruptedException exception) {
            LOGGER.error("Error was found while processing bus {} on the bus stop {} : {}", bus.getBusId(), busStopId, exception);
            Thread.currentThread().interrupt();
        }
        Route route = Route.getInstance();
        route.getPeopleOffBus(bus);
        route.getPeopleOnBus(bus);
        LOGGER.info("Bus stop {} finished processing by bus {}", busStopId, bus.getBusId());
    }
}
