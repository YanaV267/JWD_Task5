package com.development.task5.util;

public class BusStopIdGenerator {
    private static long counter;

    private BusStopIdGenerator(){

    }

    public static long generateId(){
        return ++counter;
    }
}
