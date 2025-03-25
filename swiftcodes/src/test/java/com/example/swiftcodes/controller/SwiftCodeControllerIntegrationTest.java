package com.example.swiftcodes.controller;

import com.example.swiftcodes.model.Country;
import com.example.swiftcodes.model.SwiftCode;
import com.example.swiftcodes.model.dto.SwiftCodeDto;
import com.example.swiftcodes.repository.CountryRepository;
import com.example.swiftcodes.repository.SwiftCodeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public class SwiftCodeControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private SwiftCodeRepository swiftCodeRepository;

    private Country polandCountry;
    private SwiftCode headquarterSwiftCode;
    private SwiftCode branchSwiftCode;

    @BeforeEach
    public void setup() {
        swiftCodeRepository.deleteAll();
        countryRepository.deleteAll();

        polandCountry = countryRepository.save(
                Country.builder()
                        .iso2Code("PL")
                        .name("POLAND")
                        .build()
        );

        headquarterSwiftCode = swiftCodeRepository.save(
                SwiftCode.builder()
                        .swiftCode("PKOPLPWAXXX")
                        .bankName("PKO BANK POLSKI")
                        .address("UL. PUŁAWSKA 15, 02-515 WARSZAWA")
                        .isHeadquarter(true)
                        .country(polandCountry)
                        .build()
        );

        branchSwiftCode = SwiftCode.builder()
                .swiftCode("PKOPLPWA001")
                .bankName("PKO BANK POLSKI - ODDZIAŁ 1")
                .address("UL. MARSZ. FOCHA 5, 85-070 BYDGOSZCZ")
                .isHeadquarter(false)
                .country(polandCountry)
                .headquarter(headquarterSwiftCode)
                .build();

        swiftCodeRepository.save(branchSwiftCode);

    }

    @Test
    public void testGetSwiftCodeDetailsWhenSwiftCodeExistsReturnsDetails() throws Exception {
        mockMvc.perform(get("/v1/swift-codes/{swiftCode}", headquarterSwiftCode.getSwiftCode()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.swiftCode", is(headquarterSwiftCode.getSwiftCode())))
                .andExpect(jsonPath("$.bankName", is(headquarterSwiftCode.getBankName())))
                .andExpect(jsonPath("$.address", is(headquarterSwiftCode.getAddress())))
                .andExpect(jsonPath("$.countryISO2", is(polandCountry.getIso2Code())))
                .andExpect(jsonPath("$.countryName", is(polandCountry.getName())))
                .andExpect(jsonPath("$.isHeadquarter", is(true)))
                .andExpect(jsonPath("$.branches", hasSize(1)))
                .andExpect(jsonPath("$.branches[0].swiftCode", is(branchSwiftCode.getSwiftCode())));
    }

    @Test
    public void testGetSwiftCodeDetailsWhenSwiftCodeDoesNotExistReturnsNotFound() throws Exception {
        mockMvc.perform(get("/v1/swift-codes/{swiftCode}", "NONEXISTENTXXX"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetSwiftCodesByCountryWhenCountryExistsReturnsSwiftCodes() throws Exception {
        mockMvc.perform(get("/v1/swift-codes/country/{countryIso2Code}", polandCountry.getIso2Code()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.countryISO2", is(polandCountry.getIso2Code())))
                .andExpect(jsonPath("$.countryName", is(polandCountry.getName())))
                .andExpect(jsonPath("$.swiftCodes", hasSize(2)))
                .andExpect(jsonPath("$.swiftCodes[*].swiftCode", containsInAnyOrder(
                        headquarterSwiftCode.getSwiftCode(), branchSwiftCode.getSwiftCode())));
    }

    @Test
    public void testGetSwiftCodesByCountryWhenCountryDoesNotExistReturnsNotFound() throws Exception {
        mockMvc.perform(get("/v1/swift-codes/country/{countryIso2Code}", "XX"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteSwiftCodeWhenSwiftCodeExistsDeletesSuccessfully() throws Exception {
        mockMvc.perform(delete("/v1/swift-codes/{swiftCode}", branchSwiftCode.getSwiftCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("deleted successfully")));

        assertFalse(swiftCodeRepository.findBySwiftCode(branchSwiftCode.getSwiftCode()).isPresent());
    }

    @Test
    public void testDeleteSwiftCodeWhenSwiftCodeDoesNotExistReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/v1/swift-codes/{swiftCode}", "NONEXISTENTXXX"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteHeadquarterSwiftCodeClearsReferencesFromBranches() throws Exception {
        mockMvc.perform(delete("/v1/swift-codes/{swiftCode}", headquarterSwiftCode.getSwiftCode()))
                .andExpect(status().isOk());

        assertFalse(swiftCodeRepository.findBySwiftCode(headquarterSwiftCode.getSwiftCode()).isPresent());

        SwiftCode updatedBranch = swiftCodeRepository.findBySwiftCode(branchSwiftCode.getSwiftCode())
                .orElseThrow();
        assertNull(updatedBranch.getHeadquarter());
    }

    @Test
    public void testAddSwiftCodeWithValidDataAddsSuccessfully() throws Exception {
        SwiftCodeDto newSwiftCodeDto = SwiftCodeDto.builder()
                .swiftCode("INGBPLPWXXX")
                .bankName("ING BANK ŚLĄSKI")
                .address("UL. SOKOLSKA 34, 40-086 KATOWICE")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(true)
                .build();

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSwiftCodeDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", containsString("Swift code added successfully")));

        assertTrue(swiftCodeRepository.findBySwiftCode("INGBPLPWXXX").isPresent());
    }

    @Test
    public void testAddSwiftCodeWhenSwiftCodeAlreadyExistsReturnsBadRequest() throws Exception {
        SwiftCodeDto existingSwiftCodeDto = SwiftCodeDto.builder()
                .swiftCode(headquarterSwiftCode.getSwiftCode())
                .bankName("DUPLICATE BANK")
                .address("DUPLICATE ADDRESS")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(true)
                .build();

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingSwiftCodeDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddBranchSwiftCodeAutomaticallyLinksToHeadquarter() throws Exception {
        SwiftCodeDto branchDto = SwiftCodeDto.builder()
                .swiftCode("INGBPLPW002")
                .bankName("ING BANK ŚLĄSKI - ODDZIAŁ 2")
                .address("UL. MICKIEWICZA 3, 40-092 KATOWICE")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(false)
                .build();

        SwiftCodeDto headquarterDto = SwiftCodeDto.builder()
                .swiftCode("INGBPLPWXXX")
                .bankName("ING BANK ŚLĄSKI")
                .address("UL. SOKOLSKA 34, 40-086 KATOWICE")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(true)
                .build();

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(headquarterDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(branchDto)))
                .andExpect(status().isCreated());

        SwiftCode addedBranch = swiftCodeRepository.findBySwiftCode("INGBPLPW002").orElseThrow();
        SwiftCode headquarter = swiftCodeRepository.findBySwiftCode("INGBPLPWXXX").orElseThrow();
        assertEquals(headquarter.getId(), addedBranch.getHeadquarter().getId());
    }
}