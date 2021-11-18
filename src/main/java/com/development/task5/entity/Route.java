package com.development.task5.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Route {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String RESOURCE_FILE_NAME = "data/routeData.properties";
    private static final String BUS_STOP_AMOUNT_PROPERTY = "bus_stop_amount";
    private static final String BUS_STOP_CAPACITY_PROPERTY = "bus_stop_capacity";
    private static final AtomicBoolean isInitialised = new AtomicBoolean(false);
    private final Lock lock = new ReentrantLock();
    private final Map<Long, Condition> conditions = new HashMap<>();
    private final List<BusStop> availableBusStops = new ArrayList<>();
    private final List<BusStop> occupiedBusStops = new ArrayList<>();
    private final int BUS_STOP_AMOUNT;
    private static Route instance;

    public Route() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(RESOURCE_FILE_NAME));
        } catch (IOException exception) {
            LOGGER.error("Error was occurred while reading file \"{}\"", RESOURCE_FILE_NAME);
        }
        BUS_STOP_AMOUNT = Integer.parseInt(properties.getProperty(BUS_STOP_AMOUNT_PROPERTY));
        final int busStopCapacity = Integer.parseInt(properties.getProperty(BUS_STOP_CAPACITY_PROPERTY));
        for (int i = 0; i < BUS_STOP_AMOUNT; i++) {
            BusStop busStop = new BusStop(busStopCapacity);
            availableBusStops.add(busStop);
            conditions.put(busStop.getBusStopId(), lock.newCondition());
        }
    }

    public static Route getInstance() {
        while (instance == null) {
            if (isInitialised.compareAndSet(false, true)) {
                instance = new Route();
            }
        }
        return instance;
    }

    public BusStop obtainBusStop(long busStopNumber) {
        try {
            lock.lock();
            BusStop busStop = availableBusStops.stream()
                    .filter(s -> s.getBusStopId() == busStopNumber)
                    .findFirst()
                    .orElse(null);
            try {
                if (busStop == null) {
                    LOGGER.info("Bus stop {} is currently occupied", busStopNumber);
                    conditions.get(busStopNumber).await();
                    busStop = availableBusStops.stream()
                            .filter(s -> s.getBusStopId() == busStopNumber)
                            .findFirst()
                            .get();
                }
            } catch (InterruptedException exception) {
                LOGGER.error("Error was found while processing a bus route: " + exception);
                Thread.currentThread().interrupt();
            }
            busStop.setCurrentCapacity(busStop.getCurrentCapacity() + 1);
            availableBusStops.set(availableBusStops.indexOf(busStop), busStop);
            if (busStop.getCurrentCapacity() >= busStop.getMaxCapacity()) {
                availableBusStops.remove(busStop);
                occupiedBusStops.add(busStop);
            }
            return busStop;
        } finally {
            lock.unlock();
        }
    }

    public void releaseBusStop(BusStop busStop) {
        try {
            lock.lock();
            if (availableBusStops.contains(busStop)) {
                busStop.setCurrentCapacity(busStop.getCurrentCapacity() - 1);
                availableBusStops.set(availableBusStops.indexOf(busStop), busStop);
            } else {
                occupiedBusStops.remove(busStop);
                availableBusStops.add(busStop);
                conditions.get(busStop.getBusStopId()).signal();
                LOGGER.info("Bus stop {} is available now", busStop.getBusStopId());
            }
        } finally {
            lock.unlock();
        }
    }

    public void getPeopleOffBus(Bus bus) {
        int decreasedPeopleAmount = new Random().nextInt(bus.getCurrentPeopleAmount() - 1);
        bus.setCurrentPeopleAmount(bus.getCurrentPeopleAmount() - decreasedPeopleAmount);
        LOGGER.info("{} people got off the bus {}", decreasedPeopleAmount, bus.getBusId());
    }

    public void getPeopleOnBus(Bus bus) {
        int availableSeatsAmount = bus.getMaxCapacity() - bus.getCurrentPeopleAmount();
        int increasedPeopleAmount = new Random().nextInt(availableSeatsAmount - 1);
        bus.setCurrentPeopleAmount(bus.getCurrentPeopleAmount() + increasedPeopleAmount);
        LOGGER.info("{} people got on the bus {}", increasedPeopleAmount, bus.getBusId());
    }
}
