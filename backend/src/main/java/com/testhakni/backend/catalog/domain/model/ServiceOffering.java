package com.testhakni.backend.catalog.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** Service publie par un prestataire. SERV-01 */
@Document("services")
@CompoundIndex(name = "status_category_city", def = "{'status': 1, 'category': 1, 'city': 1}")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceOffering {

    @Id
    private String id;

    /** Id du User prestataire proprietaire. */
    @Indexed
    private String providerId;

    private String title;
    private String description;
    private ServiceCategory category;
    private String city;

    /** Prix indicatif (null si sur devis). */
    private BigDecimal price;

    @Builder.Default
    private PriceType priceType = PriceType.FIXED;

    @Builder.Default
    private ServiceStatus status = ServiceStatus.ACTIVE;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
