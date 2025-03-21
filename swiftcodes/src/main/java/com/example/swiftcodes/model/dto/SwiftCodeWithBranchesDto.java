package com.example.swiftcodes.model.dto;

import com.example.swiftcodes.model.SwiftCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SwiftCodeWithBranchesDto {
    private String address;
    private String bankName;
    private String countryISO2;
    private String countryName;
    private Boolean isHeadquarter;
    private String swiftCode;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SwiftCodeDto> branches;
}
