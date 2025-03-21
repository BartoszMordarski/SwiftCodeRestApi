package com.example.swiftcodes.repository;


import com.example.swiftcodes.model.SwiftCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SwiftCodeRepository extends JpaRepository<SwiftCode, Long> {
    Optional<SwiftCode> findBySwiftCode(String swiftCode);
    List<SwiftCode> findByCountryIso2Code(String countryIso2Code);
    List<SwiftCode> findByHeadquarter(SwiftCode headquarter);
}
