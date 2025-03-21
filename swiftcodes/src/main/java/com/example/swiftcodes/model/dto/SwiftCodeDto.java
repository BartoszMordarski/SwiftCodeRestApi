package com.example.swiftcodes.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SwiftCodeDto {
    private String address;
    private String bankName;
    private String countryISO2;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String countryName;
    private Boolean isHeadquarter;
    private String swiftCode;
}
