package com.example.swiftcodes.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SwiftCodeDto {
    @NotBlank(message = "Address cannot be empty")
    private String address;

    @NotBlank(message = "Bank name cannot be empty")
    private String bankName;

    @NotBlank(message = "Country ISO code cannot be empty")
    @Size(min = 2, max = 2, message = "Country ISO code must be exactly 2 characters")
    @Pattern(regexp = "[A-Z]{2}", message = "Country ISO code must consist of 2 uppercase letters")
    private String countryISO2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String countryName;

    @NotNull(message = "Headquarter flag must be specified")
    private Boolean isHeadquarter;

    @NotBlank(message = "Swift code cannot be empty")
    @Pattern(regexp = "[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?",
            message = "Swift code must follow the format: 6 letters + 2 letters/digits + optional 3 letters/digits")
    private String swiftCode;
}
