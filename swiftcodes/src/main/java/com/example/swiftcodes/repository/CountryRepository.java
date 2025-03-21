package com.example.swiftcodes.repository;


import com.example.swiftcodes.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findByIso2Code(String iso2Code);
}
