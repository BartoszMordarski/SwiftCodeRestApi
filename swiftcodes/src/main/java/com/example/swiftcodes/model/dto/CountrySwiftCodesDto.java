package com.example.swiftcodes.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountrySwiftCodesDto {
    private String countryISO2;
    private String countryName;
    private List<SwiftCodeDto> swiftCodes;
}