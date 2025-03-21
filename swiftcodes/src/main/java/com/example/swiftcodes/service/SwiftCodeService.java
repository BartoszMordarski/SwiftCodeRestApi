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
