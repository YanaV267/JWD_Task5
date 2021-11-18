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
    private final Condition condition = lock.newCondition();
    private final Deque<BusStop> availableBusStops = new ArrayDeque<>();
    private final Deque<BusStop> occupiedBusStops = new ArrayDeque<>();
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
            try {
                if (availableBusStops.isEmpty()) {
                    LOGGER.info("All bus stops are currently occupied");
                    condition.await();
                }
            } catch (InterruptedException exception) {
                LOGGER.error("Error was found while processing a bus route: " + exception);
                Thread.currentThread().interrupt();
            }
            BusStop busStop = availableBusStops.removeFirst();
            occupiedBusStops.addLast(busStop);
            return busStop;
        } finally {
            lock.unlock();
        }
    }

    public void releaseBusStop(BusStop busStop) {
        try {
            lock.lock();
            occupiedBusStops.remove(busStop);
            availableBusStops.addLast(busStop);
            condition.signal();
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
