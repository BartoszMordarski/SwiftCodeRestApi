package com.example.swiftcodes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "countries",
        indexes = {
                @Index(name = "idx_iso2_code", columnList = "iso2_code")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iso2_code", length = 2, unique = true, nullable = false)
    private String iso2Code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "time_zone")
    private String timeZone;

    @PrePersist
    @PreUpdate
    public void ensureUppercase() {
        if (iso2Code != null) {
            iso2Code = iso2Code.toUpperCase();
        }
        if (name != null) {
            name = name.toUpperCase();
        }
    }
}