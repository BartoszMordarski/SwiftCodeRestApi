package com.example.swiftcodes.util;

import com.example.swiftcodes.service.SwiftCodeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class DataLoader {
    private static final String DATA_FILE_PATH = "data/swift_codes.tsv";
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);


    private final SwiftCodeParser swiftCodeParser;

    public DataLoader(SwiftCodeParser swiftCodeParser) {
        this.swiftCodeParser = swiftCodeParser;
    }

    @Bean
    @Profile("!test")
    public CommandLineRunner loadData() {
        return args -> {
            try {
                Resource resource = new ClassPathResource(DATA_FILE_PATH);
                if (resource.exists()) {
                    try (InputStream inputStream = resource.getInputStream()) {
                        swiftCodeParser.parseTsv(inputStream);
                    }
                }
            } catch (IOException e) {
                logger.error("Error loading data file: {}", e.getMessage(), e);
            }
        };
    }
}
