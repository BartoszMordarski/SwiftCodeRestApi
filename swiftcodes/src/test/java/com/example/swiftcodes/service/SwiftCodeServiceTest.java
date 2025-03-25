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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SwiftCodeServiceTest {

    @Mock
    private SwiftCodeRepository swiftCodeRepository;

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private SwiftCodeService swiftCodeService;

    private Country mockCountry;
    private SwiftCode mockHeadquarter;
    private SwiftCode mockBranch;
    private SwiftCodeDto mockSwiftCodeDto;

    @BeforeEach
    void setUp() {
        mockCountry = Country.builder()
                .id(1L)
                .iso2Code("PL")
                .name("POLAND")
                .build();

        mockHeadquarter = SwiftCode.builder()
                .id(1L)
                .swiftCode("TESTPLDEXXX")
                .bankName("BANK TEST")
                .address("TEST ADDRESS 1")
                .isHeadquarter(true)
                .country(mockCountry)
                .build();

        mockBranch = SwiftCode.builder()
                .id(2L)
                .swiftCode("TESTPLDE001")
                .bankName("BANK TEST BRANCH")
                .address("TEST ADDRESS 2")
                .isHeadquarter(false)
                .country(mockCountry)
                .headquarter(mockHeadquarter)
                .build();

        mockSwiftCodeDto = SwiftCodeDto.builder()
                .swiftCode("TESTPLDE002")
                .bankName("BANK TEST BRANCH 2")
                .address("TEST ADDRESS 3")
                .countryISO2("PL")
                .isHeadquarter(false)
                .build();
    }

    @Test
    void getSwiftCodeDetailsWithHeadquarterReturnsBranches() {
        List<SwiftCode> branches = List.of(mockBranch);
        when(swiftCodeRepository.findBySwiftCode("TESTPLDEXXX")).thenReturn(Optional.of(mockHeadquarter));
        when(swiftCodeRepository.findByHeadquarter(mockHeadquarter)).thenReturn(branches);

        SwiftCodeWithBranchesDto result = swiftCodeService.getSwiftCodeDetails("TESTPLDEXXX");

        assertNotNull(result);
        assertEquals("TESTPLDEXXX", result.getSwiftCode());
        assertEquals("BANK TEST", result.getBankName());
        assertEquals(true, result.getIsHeadquarter());
        assertEquals("PL", result.getCountryISO2());
        assertEquals("POLAND", result.getCountryName());
        assertNotNull(result.getBranches());
        assertEquals(1, result.getBranches().size());
        assertEquals("TESTPLDE001", result.getBranches().getFirst().getSwiftCode());

        verify(swiftCodeRepository).findBySwiftCode("TESTPLDEXXX");
        verify(swiftCodeRepository).findByHeadquarter(mockHeadquarter);
    }

    @Test
    void getSwiftCodeDetailsWithBranchReturnsNoBranches() {
        when(swiftCodeRepository.findBySwiftCode("TESTPLDE001")).thenReturn(Optional.of(mockBranch));

        SwiftCodeWithBranchesDto result = swiftCodeService.getSwiftCodeDetails("TESTPLDE001");

        assertNotNull(result);
        assertEquals("TESTPLDE001", result.getSwiftCode());
        assertEquals("BANK TEST BRANCH", result.getBankName());
        assertEquals(false, result.getIsHeadquarter());
        assertNull(result.getBranches());

        verify(swiftCodeRepository).findBySwiftCode("TESTPLDE001");
        verify(swiftCodeRepository, never()).findByHeadquarter(any());
    }

    @Test
    void getSwiftCodeDetailsWithNonExistingCodeThrowsException() {
        when(swiftCodeRepository.findBySwiftCode("NONEXIST")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                swiftCodeService.getSwiftCodeDetails("NONEXIST")
        );

        verify(swiftCodeRepository).findBySwiftCode("NONEXIST");
    }

    @Test
    void getSwiftCodesByCountryReturnsSwiftCodes() {
        List<SwiftCode> swiftCodes = Arrays.asList(mockHeadquarter, mockBranch);
        when(countryRepository.findByIso2Code("PL")).thenReturn(Optional.of(mockCountry));
        when(swiftCodeRepository.findByCountryIso2Code("PL")).thenReturn(swiftCodes);

        CountrySwiftCodesDto result = swiftCodeService.getSwiftCodesByCountry("PL");

        assertNotNull(result);
        assertEquals("PL", result.getCountryISO2());
        assertEquals("POLAND", result.getCountryName());
        assertEquals(2, result.getSwiftCodes().size());

        verify(countryRepository).findByIso2Code("PL");
        verify(swiftCodeRepository).findByCountryIso2Code("PL");
    }

    @Test
    void getSwiftCodesByCountryWithNonExistingCountryThrowsException() {
        when(countryRepository.findByIso2Code("XX")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                swiftCodeService.getSwiftCodesByCountry("XX")
        );

        verify(countryRepository).findByIso2Code("XX");
        verify(swiftCodeRepository, never()).findByCountryIso2Code(any());
    }

    @Test
    void deleteSwiftCodeWithBranchDeletesSuccessfully() {
        when(swiftCodeRepository.findBySwiftCode("TESTPLDE001")).thenReturn(Optional.of(mockBranch));

        MessageResponseDto result = swiftCodeService.deleteSwiftCode("TESTPLDE001");

        assertNotNull(result);
        assertEquals("Swift code TESTPLDE001 deleted successfully", result.getMessage());

        verify(swiftCodeRepository).findBySwiftCode("TESTPLDE001");
        verify(swiftCodeRepository, never()).findByHeadquarter(any());
        verify(swiftCodeRepository).delete(mockBranch);
    }

    @Test
    void deleteSwiftCodeWithHeadquarterUnlinksBranchesAndDeletes() {
        List<SwiftCode> branches = List.of(mockBranch);
        when(swiftCodeRepository.findBySwiftCode("TESTPLDEXXX")).thenReturn(Optional.of(mockHeadquarter));
        when(swiftCodeRepository.findByHeadquarter(mockHeadquarter)).thenReturn(branches);

        MessageResponseDto result = swiftCodeService.deleteSwiftCode("TESTPLDEXXX");

        assertNotNull(result);
        assertEquals("Swift code TESTPLDEXXX deleted successfully", result.getMessage());

        verify(swiftCodeRepository).findBySwiftCode("TESTPLDEXXX");
        verify(swiftCodeRepository).findByHeadquarter(mockHeadquarter);
        verify(swiftCodeRepository).save(mockBranch);
        verify(swiftCodeRepository).delete(mockHeadquarter);
    }

    @Test
    void addSwiftCodeCreatesBranchLinksToHeadquarter() {
        SwiftCode newBranch = SwiftCode.builder()
                .swiftCode("TESTPLDE002")
                .bankName("BANK TEST BRANCH 2")
                .address("TEST ADDRESS 3")
                .isHeadquarter(false)
                .country(mockCountry)
                .build();

        when(countryRepository.findByIso2Code("PL")).thenReturn(Optional.of(mockCountry));
        when(swiftCodeRepository.findBySwiftCode("TESTPLDE002")).thenReturn(Optional.empty());
        when(swiftCodeRepository.findBySwiftCode("TESTPLDEXXX")).thenReturn(Optional.of(mockHeadquarter));
        when(swiftCodeRepository.save(any(SwiftCode.class))).thenReturn(newBranch);

        MessageResponseDto result = swiftCodeService.addSwiftCode(mockSwiftCodeDto);

        assertNotNull(result);
        assertEquals("Swift code added successfully!", result.getMessage());

        verify(countryRepository).findByIso2Code("PL");
        verify(swiftCodeRepository).findBySwiftCode("TESTPLDE002");
        verify(swiftCodeRepository).findBySwiftCode("TESTPLDEXXX");
        verify(swiftCodeRepository).save(any(SwiftCode.class));
    }

    @Test
    void addSwiftCodeWithExistingCodeThrowsException() {
        when(swiftCodeRepository.findBySwiftCode("TESTPLDE002")).thenReturn(Optional.of(mockBranch));

        assertThrows(IllegalArgumentException.class, () ->
                swiftCodeService.addSwiftCode(mockSwiftCodeDto)
        );

        verify(swiftCodeRepository).findBySwiftCode("TESTPLDE002");
        verify(countryRepository, never()).findByIso2Code(any());
        verify(swiftCodeRepository, never()).save(any());
    }

    @Test
    void addSwiftCodeWithNonExistingCountryCreatesNewCountry() {
        SwiftCodeDto newCountrySwiftCodeDto = SwiftCodeDto.builder()
                .swiftCode("TESTGB00XXX")
                .bankName("TEST BANK UK")
                .address("TEST ADDRESS UK")
                .countryISO2("GB")
                .countryName("UNITED KINGDOM")
                .isHeadquarter(true)
                .build();

        when(countryRepository.findByIso2Code("GB")).thenReturn(Optional.empty());

        Country newCountry = Country.builder()
                .id(2L)
                .iso2Code("GB")
                .name("UNITED KINGDOM")
                .build();
        when(countryRepository.save(any(Country.class))).thenReturn(newCountry);

        when(swiftCodeRepository.findBySwiftCode("TESTGB00XXX")).thenReturn(Optional.empty());

        SwiftCode newSwiftCode = SwiftCode.builder()
                .id(3L)
                .swiftCode("TESTGB00XXX")
                .bankName("TEST BANK UK")
                .address("TEST ADDRESS UK")
                .isHeadquarter(true)
                .country(newCountry)
                .build();
        when(swiftCodeRepository.save(any(SwiftCode.class))).thenReturn(newSwiftCode);

        MessageResponseDto result = swiftCodeService.addSwiftCode(newCountrySwiftCodeDto);

        assertNotNull(result);
        assertEquals("Swift code added successfully!", result.getMessage());

        verify(countryRepository).findByIso2Code("GB");
        verify(countryRepository).save(any(Country.class));

        verify(swiftCodeRepository).findBySwiftCode("TESTGB00XXX");
        verify(swiftCodeRepository).save(any(SwiftCode.class));
    }

    //test cases for SwiftCodeDto validation in Service
    @Test
    void shouldThrowExceptionWhenSwiftCodeEndsWithXXXAndNotHQ() {

        SwiftCodeDto BadBranchDto = SwiftCodeDto.builder()
                .swiftCode("ABCDPLGHXXX")
                .bankName("Test Bank")
                .address("Test Address")
                .countryISO2("PL")
                .isHeadquarter(false)
                .build();


        Exception exception = assertThrows(IllegalArgumentException.class, () -> swiftCodeService.addSwiftCode(BadBranchDto));
        assertEquals("Swift code ending with XXX has to be a headquarter", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenHQSwiftCodeDoesNotEndWithXXX() {
        SwiftCodeDto BadHeadquarterDto = SwiftCodeDto.builder()
                .swiftCode("ABCDPLGHABC")
                .bankName("Test Bank")
                .address("Test Address")
                .countryISO2("PL")
                .isHeadquarter(true)
                .build();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> swiftCodeService.addSwiftCode(BadHeadquarterDto));
        assertEquals("Headquarter swift code must end with XXX", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCountryCodeMismatch() {
        SwiftCodeDto BadCountryDto = SwiftCodeDto.builder()
                .swiftCode("ABCDBGGHABC")
                .bankName("Test Bank")
                .address("Test Address")
                .countryISO2("PL")
                .isHeadquarter(false)
                .build();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> swiftCodeService.addSwiftCode(BadCountryDto));
        assertEquals("Characters 5-6 of swift code must match the country ISO code: PL", exception.getMessage());
    }
}