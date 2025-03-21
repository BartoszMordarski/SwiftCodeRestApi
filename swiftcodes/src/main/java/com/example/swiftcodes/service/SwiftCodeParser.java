package com.example.swiftcodes.service;

import com.example.swiftcodes.model.Country;
import com.example.swiftcodes.model.SwiftCode;
import com.example.swiftcodes.repository.CountryRepository;
import com.example.swiftcodes.repository.SwiftCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SwiftCodeParser {
    private static final Logger logger = LoggerFactory.getLogger(SwiftCodeParser.class);

    private final CountryRepository countryRepository;
    private final SwiftCodeRepository swiftCodeRepository;

    public SwiftCodeParser(CountryRepository countryRepository, SwiftCodeRepository swiftCodeRepository) {
        this.countryRepository = countryRepository;
        this.swiftCodeRepository = swiftCodeRepository;
    }

    @Transactional
    public void parseTsv(InputStream inputStream) throws IOException {
        loadAllRecords(inputStream);
        linkBranchesToHeadquarters();
    }

    private void loadAllRecords(InputStream inputStream) throws IOException {
        Map<String, Country> countryCache = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    String[] fields = line.split("\t");

                    if (fields.length < 8) {
                        continue;
                    }

                    String countryIso2 = fields[0].toUpperCase();
                    String swiftCode = fields[1];
                    String bankName = fields[3];
                    String address = fields[4];
                    String countryName = fields[6].toUpperCase();

                    boolean isHeadquarter = swiftCode.endsWith("XXX");

                    Country country = countryCache.computeIfAbsent(countryIso2, keyFromMap -> {
                        return countryRepository.findByIso2Code(keyFromMap)
                                .orElseGet(() -> countryRepository.save(
                                        Country.builder()
                                                .iso2Code(keyFromMap)
                                                .name(countryName)
                                                .build()
                                ));
                    });

                    if (swiftCodeRepository.findBySwiftCode(swiftCode).isPresent()) {
                        continue;
                    }

                    SwiftCode swiftCodeEntity = SwiftCode.builder()
                            .swiftCode(swiftCode)
                            .bankName(bankName)
                            .address(address)
                            .isHeadquarter(isHeadquarter)
                            .country(country)
                            .build();

                    swiftCodeRepository.save(swiftCodeEntity);

                } catch (Exception e) {
                    logger.error("Error while parsing line: {}: {}", lineNumber, e.getMessage());
                }
            }
        }
    }

    @Transactional
    public void linkBranchesToHeadquarters() {
        List<SwiftCode> branches = swiftCodeRepository.findAll().stream()
                .filter(swift -> !swift.getIsHeadquarter())
                .toList();

        for (SwiftCode branch : branches) {
            if (branch.getSwiftCode().length() >= 8) {
                String bankIdentifier = branch.getSwiftCode().substring(0, 8);
                String headquarterCode = bankIdentifier + "XXX";

                SwiftCode headquarter = swiftCodeRepository.findBySwiftCode(headquarterCode).orElse(null);

                if (headquarter != null && !headquarter.equals(branch)) {
                    branch.setHeadquarter(headquarter);
                    swiftCodeRepository.save(branch);
                }
            }
        }
    }
}