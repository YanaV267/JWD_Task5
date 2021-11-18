package com.development.task5.reader.impl;

import com.development.task5.exception.BusException;
import com.development.task5.reader.BusReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class BusReaderImpl implements BusReader {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public List<String> readBusData(String filename) throws BusException {
        if (getClass().getClassLoader().getResource(filename) == null) {
            LOGGER.error("File \"{}\" doesn't exist in specified directory.", filename);
            throw new BusException("File \"" + filename + "\" doesn't exist in specified directory.");
        }
        List<String> busesData;
        Stream<String> lines = Stream.<String>builder().build();
        try {
            Path pathToFile = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
            lines = Files.lines(pathToFile);
            busesData = lines.toList();
        } catch (IOException | URISyntaxException exception) {
            LOGGER.error("Error was found while extracting buses' data from the file  \"{}\"", filename);
            throw new BusException("Error was found while extracting buses' data from the file  \"" + filename + "\"", exception);
        } finally {
            lines.close();
        }
        return busesData;
    }
}
