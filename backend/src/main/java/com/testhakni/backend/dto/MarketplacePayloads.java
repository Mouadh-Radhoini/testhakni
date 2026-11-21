package com.testhakni.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.*;
import com.testhakni.backend.model.*;

public final class MarketplacePayloads {
    private MarketplacePayloads() {}

    public record Proposal(@NotNull @Positive @Digits(integer=9, fraction=2) BigDecimal price,
                           @NotNull @FutureOrPresent LocalDate proposedDate,
                           @NotBlank @Size(max=2000) String description) {}
    public record Progress(@NotNull BookingStatus status) {}
    public record Feedback(@NotNull @Min(1) @Max(5) Integer rating,
                           @NotBlank @Size(max=2000) String comment) {}
    public record Signalement(@NotNull ContentType targetType, @NotBlank String targetId,
                              @NotBlank @Size(max=1000) String reason) {}
    public record Decision(@NotNull Boolean enabled, @NotBlank @Size(max=1000) String reason) {}
    public record Resolution(@NotNull ReportStatus status, @NotBlank @Size(max=1000) String reason) {}
    public record OpenRequest(String id, String title, String description, ServiceCategory category,
                              String city, BigDecimal budget, LocalDate preferredDate) {
        public static OpenRequest from(ServiceRequest r) {
            return new OpenRequest(r.getId(), r.getTitle(), r.getDescription(), r.getCategory(),
                    r.getCity(), r.getBudget(), r.getPreferredDate());
        }
    }
}
