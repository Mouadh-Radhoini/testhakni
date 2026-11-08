package com.testhakni.backend.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** Profil professionnel d'un prestataire, lie 1-1 a son compte. PROV-01 */
@Document("provider_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderProfile {

    @Id
    private String id;

    /** Id du User prestataire (unique : un seul profil par compte). */
    @Indexed(unique = true)
    private String userId;

    private String businessName;
    private String bio;

    @Builder.Default
    private List<ServiceCategory> categories = new ArrayList<>();

    @Builder.Default
    private List<String> skills = new ArrayList<>();

    @Indexed
    private String city;

    /** Zone d'intervention (ex. "Tunis et banlieue", "20 km"). */
    private String serviceArea;

    private Integer yearsOfExperience;

    @Builder.Default
    private boolean available = true;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
