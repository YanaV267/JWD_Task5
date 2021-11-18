package com.development.task5.reader;

import com.development.task5.exception.BusException;

import java.util.List;

public interface BusReader {
    List<String> readBusData(String filename) throws BusException;
}
