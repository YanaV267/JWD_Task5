package com.development.task5.main;

import com.development.task5.entity.Bus;
import com.development.task5.exception.BusException;
import com.development.task5.parser.BusParser;
import com.development.task5.parser.impl.BusParserImpl;
import com.development.task5.reader.BusReader;
import com.development.task5.reader.impl.BusReaderImpl;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws BusException {
        BusReader busReader = new BusReaderImpl();
        BusParser busParser = new BusParserImpl();
        List<String> readBusData = busReader.readBusData("data/busData.txt");
        List<Bus> buses = busParser.parseBuses(readBusData);

        ExecutorService executorService = Executors.newFixedThreadPool(readBusData.size());
        buses.forEach(executorService::execute);
        executorService.shutdown();
    }
}
