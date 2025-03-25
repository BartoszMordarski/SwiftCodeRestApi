package com.example.swiftcodes.service;

import com.example.swiftcodes.model.Country;
import com.example.swiftcodes.model.SwiftCode;
import com.example.swiftcodes.repository.CountryRepository;
import com.example.swiftcodes.repository.SwiftCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SwiftCodeParserTest {

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private SwiftCodeRepository swiftCodeRepository;

    @InjectMocks
    private SwiftCodeParser swiftCodeParser;

    private Country mockCountry;
    private SwiftCode mockHeadquarter;
    private SwiftCode mockBranch;

    @BeforeEach
    void setUp() {
        mockCountry = Country.builder()
                .id(1L)
                .iso2Code("PL")
                .name("POLAND")
                .build();

        mockHeadquarter = SwiftCode.builder()
                .id(1L)
                .swiftCode("BREXPLPWXXX")
                .bankName("BANK TEST")
                .address("TEST ADDRESS 1")
                .isHeadquarter(true)
                .country(mockCountry)
                .build();

        mockBranch = SwiftCode.builder()
                .id(2L)
                .swiftCode("BREXPLPW001")
                .bankName("BANK TEST BRANCH")
                .address("TEST ADDRESS 2")
                .isHeadquarter(false)
                .country(mockCountry)
                .headquarter(null)
                .build();
    }

    @Test
    void shouldParseValidTsvAndCreateEntities() throws IOException {
        String tsvData = "PL\tBREXPLPWXXX\tBREX\tBANK TEST\tTEST ADDRESS 1\tWARSAW\tPOLAND\t1234\n" +
                "PL\tBREXPLPW001\tBREX\tBANK TEST BRANCH\tTEST ADDRESS 2\tKRAKOW\tPOLAND\t5678";
        InputStream inputStream = new ByteArrayInputStream(tsvData.getBytes(StandardCharsets.UTF_8));

        when(countryRepository.findByIso2Code("PL")).thenReturn(Optional.empty());
        when(countryRepository.save(any(Country.class))).thenReturn(mockCountry);
        when(swiftCodeRepository.findBySwiftCode(anyString())).thenReturn(Optional.empty());
        when(swiftCodeRepository.save(any(SwiftCode.class))).thenReturn(mockHeadquarter, mockBranch);

        swiftCodeParser.parseTsv(inputStream);

        verify(countryRepository).save(any(Country.class));
        verify(swiftCodeRepository, times(2)).save(any(SwiftCode.class));
    }

    @Test
    void shouldSkipInvalidLineAndContinueProcessing() throws IOException {
        String tsvData = "PL\tBREXPLPWXXX\tBREX\tBANK TEST\tTEST ADDRESS 1\tWARSAW\tPOLAND\t1234\n" +
                "INVALID LINE\n" +
                "PL\tBREXPLPW001\tBREX\tBANK TEST BRANCH\tTEST ADDRESS 2\tKRAKOW\tPOLAND\t5678";
        InputStream inputStream = new ByteArrayInputStream(tsvData.getBytes(StandardCharsets.UTF_8));

        when(countryRepository.findByIso2Code("PL")).thenReturn(Optional.of(mockCountry));
        when(swiftCodeRepository.findBySwiftCode(anyString())).thenReturn(Optional.empty());
        when(swiftCodeRepository.save(any(SwiftCode.class))).thenReturn(mockHeadquarter, mockBranch);

        swiftCodeParser.parseTsv(inputStream);

        verify(swiftCodeRepository, times(2)).save(any(SwiftCode.class));
    }

    @Test
    void shouldLinkBranchesToHeadquarters() {
        mockBranch.setHeadquarter(null);

        when(swiftCodeRepository.findAll()).thenReturn(List.of(mockHeadquarter, mockBranch));
        when(swiftCodeRepository.findBySwiftCode("BREXPLPWXXX")).thenReturn(Optional.of(mockHeadquarter));

        swiftCodeParser.linkBranchesToHeadquarters();

        ArgumentCaptor<SwiftCode> swiftCodeCaptor = ArgumentCaptor.forClass(SwiftCode.class);
        verify(swiftCodeRepository).save(swiftCodeCaptor.capture());

        SwiftCode savedBranch = swiftCodeCaptor.getValue();
        assertEquals("BREXPLPW001", savedBranch.getSwiftCode());
        assertNotNull(savedBranch.getHeadquarter());
        assertEquals("BREXPLPWXXX", savedBranch.getHeadquarter().getSwiftCode());
    }
}
