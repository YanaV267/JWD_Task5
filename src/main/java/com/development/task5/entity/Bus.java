package com.development.task5.entity;

import com.development.task5.util.BusIdGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class Bus implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final long busId;
    private final List<Long> busStopNumbers;
    private final int MAX_CAPACITY;
    private int currentPeopleAmount;
    private State state;

    public enum State {
        WAITING, RUNNING, COMPLETED
    }

    public Bus(int maxCapacity, int currentCapacity, List<Long> busStopNumbers) {
        MAX_CAPACITY = maxCapacity;
        this.currentPeopleAmount = currentCapacity;
        this.busStopNumbers = busStopNumbers;
        busId = BusIdGenerator.generateId();
        state = State.WAITING;
    }

    public long getBusId() {
        return busId;
    }

    public int getCurrentPeopleAmount() {
        return currentPeopleAmount;
    }

    public void setCurrentPeopleAmount(int currentPeopleAmount) {
        this.currentPeopleAmount = currentPeopleAmount;
    }

    public int getMaxCapacity() {
        return MAX_CAPACITY;
    }

    @Override
    public void run() {
        LOGGER.info("Bus {} started its route", busId);
        state = State.RUNNING;

        Route route = Route.getInstance();
            BusStop busStop = route.obtainBusStop(busStopNumbers.get(0));
            LOGGER.info("Bus {} arrived to a bus stop {} with {} people", busId, busStop.getBusStopId(), currentPeopleAmount);

            busStop.processBus(this);

            route.releaseBusStop(busStop);
            LOGGER.info("Bus {} drove away from a bus stop {} with {} people", busId, busStop.getBusStopId(), currentPeopleAmount);

        LOGGER.info("Bus {} completed its route", busId);
        state = State.COMPLETED;
    }
}
