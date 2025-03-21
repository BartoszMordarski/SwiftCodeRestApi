package com.example.swiftcodes.controller;

import com.example.swiftcodes.model.dto.CountrySwiftCodesDto;
import com.example.swiftcodes.model.dto.MessageResponseDto;
import com.example.swiftcodes.model.dto.SwiftCodeDto;
import com.example.swiftcodes.model.dto.SwiftCodeWithBranchesDto;
import com.example.swiftcodes.service.SwiftCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/swift-codes")
public class SwiftCodeController {

    private final SwiftCodeService swiftCodeService;

    public SwiftCodeController(SwiftCodeService swiftCodeService) {
        this.swiftCodeService = swiftCodeService;
    }

    @GetMapping("/{swiftCode}")
    public ResponseEntity<SwiftCodeWithBranchesDto> getSwiftCodeDetails(@PathVariable String swiftCode) {
        SwiftCodeWithBranchesDto response = swiftCodeService.getSwiftCodeDetails(swiftCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/country/{countryIso2Code}")
    public ResponseEntity<CountrySwiftCodesDto> getSwiftCodesByCountry(@PathVariable String countryIso2Code) {
        CountrySwiftCodesDto response = swiftCodeService.getSwiftCodesByCountry(countryIso2Code);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{swiftCode}")
    public ResponseEntity<MessageResponseDto> deleteSwiftCode(@PathVariable String swiftCode) {
        MessageResponseDto response = swiftCodeService.deleteSwiftCode(swiftCode);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<MessageResponseDto> addSwiftCode(@RequestBody SwiftCodeDto swiftCodeDto) {
        MessageResponseDto response = swiftCodeService.addSwiftCode(swiftCodeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
