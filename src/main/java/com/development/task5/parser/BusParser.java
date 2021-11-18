package com.development.task5.parser;

import com.development.task5.entity.Bus;

import java.util.List;

public interface BusParser {
    List<Bus> parseBuses(List<String> busesData);
}
