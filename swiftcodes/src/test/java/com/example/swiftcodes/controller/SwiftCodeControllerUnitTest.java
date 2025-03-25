package com.example.swiftcodes.controller;

import com.example.swiftcodes.model.dto.CountrySwiftCodesDto;
import com.example.swiftcodes.model.dto.MessageResponseDto;
import com.example.swiftcodes.model.dto.SwiftCodeDto;
import com.example.swiftcodes.model.dto.SwiftCodeWithBranchesDto;
import com.example.swiftcodes.service.SwiftCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SwiftCodeControllerUnitTest {

    @Mock
    private SwiftCodeService swiftCodeService;

    @InjectMocks
    private SwiftCodeController swiftCodeController;

    private SwiftCodeDto swiftCodeDto;
    private SwiftCodeWithBranchesDto swiftCodeWithBranchesDto;
    private CountrySwiftCodesDto countrySwiftCodesDto;
    private MessageResponseDto messageResponseDto;

    @BeforeEach
    void setUp() {
        swiftCodeDto = SwiftCodeDto.builder()
                .swiftCode("TESTCODE001")
                .bankName("TEST BANK")
                .address("TEST ADDRESS")
                .countryISO2("PL")
                .isHeadquarter(false)
                .build();

        SwiftCodeDto branchDto = SwiftCodeDto.builder()
                .swiftCode("TESTCODE001")
                .bankName("TEST BANK BRANCH")
                .address("BRANCH ADDRESS")
                .countryISO2("PL")
                .isHeadquarter(false)
                .build();

        swiftCodeWithBranchesDto = SwiftCodeWithBranchesDto.builder()
                .swiftCode("TESTCODEXXX")
                .bankName("TEST BANK HQ")
                .address("HQ ADDRESS")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(true)
                .branches(List.of(branchDto))
                .build();

        countrySwiftCodesDto = CountrySwiftCodesDto.builder()
                .countryISO2("PL")
                .countryName("POLAND")
                .swiftCodes(List.of(swiftCodeDto, branchDto))
                .build();

        messageResponseDto = new MessageResponseDto("Operation successful");
    }

    @Test
    void getSwiftCodeDetailsReturnsSwiftCodeWithBranchesDto() {
        when(swiftCodeService.getSwiftCodeDetails("TESTCODEXXX")).thenReturn(swiftCodeWithBranchesDto);

        ResponseEntity<SwiftCodeWithBranchesDto> response = swiftCodeController.getSwiftCodeDetails("TESTCODEXXX");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TESTCODEXXX", response.getBody().getSwiftCode());
        assertEquals("TEST BANK HQ", response.getBody().getBankName());
        assertTrue(response.getBody().getIsHeadquarter());
        assertEquals(1, response.getBody().getBranches().size());

        verify(swiftCodeService).getSwiftCodeDetails("TESTCODEXXX");
    }

    @Test
    void getSwiftCodesByCountryReturnsCountrySwiftCodesDto() {
        when(swiftCodeService.getSwiftCodesByCountry("PL")).thenReturn(countrySwiftCodesDto);

        ResponseEntity<CountrySwiftCodesDto> response = swiftCodeController.getSwiftCodesByCountry("PL");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PL", response.getBody().getCountryISO2());
        assertEquals("POLAND", response.getBody().getCountryName());
        assertEquals(2, response.getBody().getSwiftCodes().size());

        verify(swiftCodeService).getSwiftCodesByCountry("PL");
    }

    @Test
    void deleteSwiftCodeReturnsSuccessMessage() {
        when(swiftCodeService.deleteSwiftCode("TESTCODEXXX")).thenReturn(messageResponseDto);

        ResponseEntity<MessageResponseDto> response = swiftCodeController.deleteSwiftCode("TESTCODEXXX");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Operation successful", response.getBody().getMessage());

        verify(swiftCodeService).deleteSwiftCode("TESTCODEXXX");
    }

    @Test
    void addSwiftCodeReturnsCreatedStatusAndSuccessMessage() {
        when(swiftCodeService.addSwiftCode(any(SwiftCodeDto.class))).thenReturn(messageResponseDto);

        ResponseEntity<MessageResponseDto> response = swiftCodeController.addSwiftCode(swiftCodeDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Operation successful", response.getBody().getMessage());

        verify(swiftCodeService).addSwiftCode(swiftCodeDto);
    }
}