package com.example.swiftcodes.service;

import com.example.swiftcodes.model.Country;
import com.example.swiftcodes.model.SwiftCode;
import com.example.swiftcodes.model.dto.CountrySwiftCodesDto;
import com.example.swiftcodes.model.dto.MessageResponseDto;
import com.example.swiftcodes.model.dto.SwiftCodeDto;
import com.example.swiftcodes.model.dto.SwiftCodeWithBranchesDto;
import com.example.swiftcodes.repository.CountryRepository;
import com.example.swiftcodes.repository.SwiftCodeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class SwiftCodeService {

    private final SwiftCodeRepository swiftCodeRepository;
    private final CountryRepository countryRepository;

    public SwiftCodeService(SwiftCodeRepository swiftCodeRepository, CountryRepository countryRepository) {
        this.swiftCodeRepository = swiftCodeRepository;
        this.countryRepository = countryRepository;
    }

    public SwiftCodeWithBranchesDto getSwiftCodeDetails(String swiftCode) {
        SwiftCode code = swiftCodeRepository.findBySwiftCode(swiftCode)
                .orElseThrow(() -> new EntityNotFoundException("Swift code not found: " + swiftCode));

        List<SwiftCodeDto> branches = Collections.emptyList();
        if (code.getIsHeadquarter()) {
            branches = swiftCodeRepository.findByHeadquarter(code).stream()
                    .map(this::convertToSwiftCodeDto)
                    .toList();
        }

        return SwiftCodeWithBranchesDto.builder()
                .swiftCode(code.getSwiftCode())
                .bankName(code.getBankName())
                .address(code.getAddress())
                .countryISO2(code.getCountry().getIso2Code())
                .countryName(code.getCountry().getName())
                .isHeadquarter(code.getIsHeadquarter())
                .branches(branches.isEmpty() ? null : branches)
                .build();
    }

    public CountrySwiftCodesDto getSwiftCodesByCountry(String countryIso2Code) {
        Country country = countryRepository.findByIso2Code(countryIso2Code.toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("Country not found: " + countryIso2Code));

        List<SwiftCodeDto> swiftCodes = swiftCodeRepository.findByCountryIso2Code(countryIso2Code.toUpperCase()).stream()
                .map(this::convertToSwiftCodeDto)
                .toList();

        return CountrySwiftCodesDto.builder()
                .countryISO2(country.getIso2Code())
                .countryName(country.getName())
                .swiftCodes(swiftCodes)
                .build();
    }

    @Transactional
    public MessageResponseDto deleteSwiftCode(String swiftCode) {
        SwiftCode codeToDelete = swiftCodeRepository.findBySwiftCode(swiftCode)
                .orElseThrow(() -> new EntityNotFoundException("Swift code not found: " + swiftCode));

        if (codeToDelete.getIsHeadquarter()) {
            List<SwiftCode> branches = swiftCodeRepository.findByHeadquarter(codeToDelete);

            for (SwiftCode branch : branches) {
                branch.setHeadquarter(null);
                swiftCodeRepository.save(branch);
            }
        }

        swiftCodeRepository.delete(codeToDelete);
        return new MessageResponseDto("Swift code " + codeToDelete.getSwiftCode() + " deleted successfully");
    }

    @Transactional
    public MessageResponseDto addSwiftCode(SwiftCodeDto swiftCodeDto) {

        validateSwiftCode(swiftCodeDto);

        Country country = countryRepository.findByIso2Code(swiftCodeDto.getCountryISO2().toUpperCase())
                .orElseGet(() -> {
                    Country newCountry = Country.builder()
                            .iso2Code(swiftCodeDto.getCountryISO2().toUpperCase())
                            .name(swiftCodeDto.getCountryName().toUpperCase())
                            .build();
                    return countryRepository.save(newCountry);
                });

        SwiftCode swiftCode = SwiftCode.builder()
                .swiftCode(swiftCodeDto.getSwiftCode())
                .bankName(swiftCodeDto.getBankName())
                .address(swiftCodeDto.getAddress())
                .isHeadquarter(swiftCodeDto.getIsHeadquarter())
                .country(country)
                .build();

        linkToHeadquarter(swiftCode);

        swiftCodeRepository.save(swiftCode);
        return new MessageResponseDto("Swift code added successfully!");
    }

    private void linkToHeadquarter(SwiftCode swiftCode) {
        if(!swiftCode.getIsHeadquarter() && swiftCode.getSwiftCode().length() >= 8) {
            String bankIdentifier = swiftCode.getSwiftCode().substring(0, 8);
            String headquarterCode = bankIdentifier + "XXX";
            swiftCodeRepository.findBySwiftCode(headquarterCode)
                    .ifPresent(swiftCode::setHeadquarter);
        }
    }

    private void validateSwiftCode(SwiftCodeDto swiftCodeDto) {
        if(swiftCodeRepository.findBySwiftCode(swiftCodeDto.getSwiftCode()).isPresent()) {
            throw new IllegalArgumentException("Swift code already exists: " + swiftCodeDto.getSwiftCode());
        }

        if (swiftCodeDto.getSwiftCode().length() == 11 &&
                "XXX".equals(swiftCodeDto.getSwiftCode().substring(8)) &&
                !swiftCodeDto.getIsHeadquarter()) {
            throw new IllegalArgumentException("Swift code ending with XXX has to be a headquarter");
        }

        if (swiftCodeDto.getSwiftCode().length() == 11 &&
                !"XXX".equals(swiftCodeDto.getSwiftCode().substring(8)) &&
                swiftCodeDto.getIsHeadquarter()) {
            throw new IllegalArgumentException("Headquarter swift code must end with XXX");
        }

        if (!swiftCodeDto.getSwiftCode().substring(4, 6).equals(swiftCodeDto.getCountryISO2())) {
            throw new IllegalArgumentException("Characters 5-6 of swift code must match the country ISO code: "
                    + swiftCodeDto.getCountryISO2());
        }
    }


    private SwiftCodeDto convertToSwiftCodeDto(SwiftCode swiftCode) {
        return SwiftCodeDto.builder()
                .swiftCode(swiftCode.getSwiftCode())
                .bankName(swiftCode.getBankName())
                .address(swiftCode.getAddress())
                .countryISO2(swiftCode.getCountry().getIso2Code())
                .isHeadquarter(swiftCode.getIsHeadquarter())
                .build();
    }

}
