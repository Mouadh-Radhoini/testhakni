package com.testhakni.backend.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

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

/** Demande de service creee par un client. REQ-01 */
@Document("service_requests")
@CompoundIndex(name = "status_category_city", def = "{'status': 1, 'category': 1, 'city': 1}")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequest {

    @Id
    private String id;

    @Indexed
    private String customerId;

    /** Service consulte a l'origine de la demande (optionnel). */
    private String serviceId;

    private String title;
    private String description;
    private ServiceCategory category;
    private String city;
    private String address;
    private LocalDate preferredDate;
    private BigDecimal budget;

    @Builder.Default
    private RequestStatus status = RequestStatus.OPEN;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
