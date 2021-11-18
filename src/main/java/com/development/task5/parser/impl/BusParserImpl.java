package com.development.task5.parser.impl;

import com.development.task5.entity.Bus;
import com.development.task5.parser.BusParser;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class BusParserImpl implements BusParser {
    private static final String DELIMITER_REGEX = "/";
    private static final String BUS_STOP_DELIMITER_REGEX = "-";

    @Override
    public List<Bus> parseBuses(List<String> busesData) {
        return busesData.stream()
                .map(d -> Stream.of(d.split(DELIMITER_REGEX))
                        .map(String::toString)
                        .toList())
                .map(b -> new Bus(Integer.parseInt(b.get(0)), Integer.parseInt(b.get(1)),
                        Arrays.stream(b.get(2).split(BUS_STOP_DELIMITER_REGEX))
                                .map(Long::parseLong)
                                .toList()))
                .toList();
    }
}
