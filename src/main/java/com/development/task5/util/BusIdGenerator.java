package com.development.task5.util;

public class BusIdGenerator {
    private static long counter;

    private BusIdGenerator(){

    }

    public static long generateId(){
        return ++counter;
    }
}
