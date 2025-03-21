package com.example.swiftcodes.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(
        name = "swift_codes",
        indexes = {
                @Index(name = "idx_swift_code", columnList = "swift_code"),
                @Index(name = "idx_country_id", columnList = "country_id"),
                @Index(name = "idx_headquarter_id", columnList = "headquarter_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "branches")
@Builder
public class SwiftCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "swift_code", length = 11, unique = true, nullable = false)
    private String swiftCode;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "town_name")
    private String townName;

    @Column(name = "is_headquarter", nullable = false)
    private Boolean isHeadquarter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "headquarter_id")
    private SwiftCode headquarter;

    @OneToMany(mappedBy = "headquarter", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<SwiftCode> branches;
}
