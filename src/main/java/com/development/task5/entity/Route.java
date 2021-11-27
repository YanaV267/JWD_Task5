package com.development.task5.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Route {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String RESOURCE_FILE_NAME = "data/routeData.properties";
    private static final String BUS_STOP_AMOUNT_PROPERTY = "bus_stop_amount";
    private static final String BUS_STOP_CAPACITY_PROPERTY = "bus_stop_capacity";
    private static final int DEFAULT_BUS_STOP_AMOUNT = 5;
    private static final int DEFAULT_BUS_STOP_CAPACITY = 2;
    private static final AtomicBoolean isInitialised = new AtomicBoolean(false);
    private static final Lock lock = new ReentrantLock();
    private final Map<Long, Condition> conditions = new HashMap<>();
    private final Map<Long, Semaphore> semaphores = new HashMap<>();
    private final List<BusStop> availableBusStops = new ArrayList<>();
    private final List<BusStop> occupiedBusStops = new ArrayList<>();
    private static Route instance;
    private int busStopAmount;

    private Route() {
        int busStopCapacity = retrieveBusStopProperties();
        for (int i = 0; i < busStopAmount; i++) {
            BusStop busStop = new BusStop(busStopCapacity);
            availableBusStops.add(busStop);
            conditions.put(busStop.getBusStopId(), lock.newCondition());
            semaphores.put(busStop.getBusStopId(), new Semaphore(busStop.getMaxBusCapacity(), true));
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

    public BusStop obtainBusStop(long busStopNumber, Bus bus) {
        try {
            lock.lock();
            bus.setState(Bus.State.WAITING);
            try {
                while (bus.getState().equals(Bus.State.WAITING)) {
                    if (semaphores.get(busStopNumber).tryAcquire()) {
                        bus.setState(Bus.State.RUNNING);
                    } else {
                        LOGGER.info("Bus stop {} is currently occupied", busStopNumber);
                        conditions.get(busStopNumber).await();
                    }
                }
            } catch (InterruptedException exception) {
                LOGGER.error("Error was found while processing a bus route: " + exception);
                Thread.currentThread().interrupt();
            }
            BusStop busStop = availableBusStops.stream()
                    .filter(s -> s.getBusStopId() == busStopNumber)
                    .findFirst()
                    .orElse(new BusStop());
            if (semaphores.get(busStopNumber).availablePermits() == 0) {
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
            semaphores.get(busStop.getBusStopId()).release();
            if (!availableBusStops.contains(busStop)) {
                occupiedBusStops.remove(busStop);
                availableBusStops.add(busStop);
                conditions.get(busStop.getBusStopId()).signalAll();
                LOGGER.info("Bus stop {} is available now", busStop.getBusStopId());
            }
        } finally {
            lock.unlock();
        }
    }

    public int getPeopleOffBus(Bus bus) {
        int decreasedPeopleAmount = new Random().nextInt(bus.getCurrentPeopleAmount() + 1);
        bus.setCurrentPeopleAmount(bus.getCurrentPeopleAmount() - decreasedPeopleAmount);
        LOGGER.info("{} people got off the bus {}", decreasedPeopleAmount, bus.getBusId());
        return decreasedPeopleAmount;
    }

    public int getPeopleOnBus(Bus bus, BusStop busStop) {
        int availableSeatsAmount = bus.getMaxCapacity() - bus.getCurrentPeopleAmount();
        int maxPossiblePeopleAmount = Math.min(busStop.getCurrentPeopleAmount(), availableSeatsAmount);
        int increasedPeopleAmount = new Random().nextInt(maxPossiblePeopleAmount + 1);
        bus.setCurrentPeopleAmount(bus.getCurrentPeopleAmount() + increasedPeopleAmount);
        LOGGER.info("{} people got on the bus {}", increasedPeopleAmount, bus.getBusId());
        return increasedPeopleAmount;
    }

    public int retrieveBusStopProperties() {
        int busStopCapacity;
        try {
            Properties properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream(RESOURCE_FILE_NAME));
            busStopAmount = Integer.parseInt(properties.getProperty(BUS_STOP_AMOUNT_PROPERTY,
                    String.valueOf(DEFAULT_BUS_STOP_AMOUNT)));
            busStopCapacity = Integer.parseInt(properties.getProperty(BUS_STOP_CAPACITY_PROPERTY,
                    String.valueOf(DEFAULT_BUS_STOP_CAPACITY)));
        } catch (IOException exception) {
            LOGGER.error("Error was occurred while reading file \"{}\"", RESOURCE_FILE_NAME);
            LOGGER.warn("Route will be initialised with default values of bus stop amount({}) and bus stop capacity({})",
                    DEFAULT_BUS_STOP_AMOUNT, DEFAULT_BUS_STOP_CAPACITY);
            busStopAmount = DEFAULT_BUS_STOP_AMOUNT;
            busStopCapacity = DEFAULT_BUS_STOP_CAPACITY;
        }
        return busStopCapacity;
    }
}
