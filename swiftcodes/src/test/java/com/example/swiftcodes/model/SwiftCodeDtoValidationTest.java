package com.example.swiftcodes.model;
import com.example.swiftcodes.model.dto.SwiftCodeDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SwiftCodeDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationForValidDto() {
        SwiftCodeDto validDto = SwiftCodeDto.builder()
                .address("Test Address")
                .bankName("Test Bank")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(true)
                .swiftCode("TESTPLDEXXX")
                .build();

        Set<ConstraintViolation<SwiftCodeDto>> violations = validator.validate(validDto);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidationWhenAddressIsBlank() {
        SwiftCodeDto invalidDto = SwiftCodeDto.builder()
                .address("")
                .bankName("Test Bank")
                .countryISO2("PL")
                .isHeadquarter(true)
                .swiftCode("TESTPLDEXXX")
                .build();

        Set<ConstraintViolation<SwiftCodeDto>> violations = validator.validate(invalidDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Address cannot be empty"));
    }

    @Test
    void shouldFailValidationForInvalidCountryISO2() {
        SwiftCodeDto invalidDto = SwiftCodeDto.builder()
                .address("Test Address")
                .bankName("Test Bank")
                .countryISO2("USA")
                .isHeadquarter(true)
                .swiftCode("TESTPLDEXXX")
                .build();

        Set<ConstraintViolation<SwiftCodeDto>> violations = validator.validate(invalidDto);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Country ISO code must be exactly 2 characters"));
    }

    @Test
    void shouldFailValidationForInvalidSwiftCodeFormat() {
        SwiftCodeDto invalidDto = SwiftCodeDto.builder()
                .address("Test Address")
                .bankName("Test Bank")
                .countryISO2("US")
                .isHeadquarter(true)
                .swiftCode("INVALID")
                .build();

        Set<ConstraintViolation<SwiftCodeDto>> violations = validator.validate(invalidDto);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Swift code must follow the format"));
    }

    @Test
    void shouldFailValidationWhenIsHeadquarterIsNull() {
        SwiftCodeDto invalidDto = SwiftCodeDto.builder()
                .address("123 Street")
                .bankName("Bank")
                .countryISO2("US")
                .swiftCode("BANKUS33XXX")
                .isHeadquarter(null)
                .build();

        Set<ConstraintViolation<SwiftCodeDto>> violations = validator.validate(invalidDto);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Headquarter flag must be specified"));
    }
}

