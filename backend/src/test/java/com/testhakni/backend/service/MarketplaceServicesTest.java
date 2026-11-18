package com.testhakni.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import com.testhakni.backend.dto.MarketplacePayloads.Feedback;
import com.testhakni.backend.dto.PublicReviews;
import com.testhakni.backend.exception.ApiException;
import com.testhakni.backend.model.*;
import com.testhakni.backend.repository.*;
import com.testhakni.backend.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

class MarketplaceServicesTest {
    @Test
    void unavailableProviderCannotDiscoverRequests() {
        var profiles = mock(ProviderProfileRepository.class);
        when(profiles.findByUserId("p")).thenReturn(Optional.of(ProviderProfile.builder().available(false).build()));
        var db = mock(MongoTemplate.class);
        var service = new OfferService(null, null, profiles, null, null, null, db, null);
        assertThat(service.discover("p")).isEmpty();
        verifyNoInteractions(db);
    }

    @Test
    void providerCannotConfirmOwnBooking() {
        var repo = mock(BookingRepository.class);
        var booking = new Booking("b","r","o","c","p",null,"Plomberie",null,null,null,
                BookingStatus.AWAITING_CONFIRMATION,false,List.of());
        when(repo.findById("b")).thenReturn(Optional.of(booking));
        var service = new BookingService(repo,null,null,null);
        var provider = new AuthenticatedUser("p","p@example.test",Role.PROVIDER);
        assertThatThrownBy(() -> service.transition(provider,"b",BookingStatus.COMPLETED))
                .isInstanceOf(ApiException.class).hasMessageContaining("participant");
        assertThatThrownBy(() -> service.review(provider,"b",new Feedback(5,"Excellent")))
                .isInstanceOf(ApiException.class).hasMessageContaining("client");
    }

    @Test
    void unreadCountUsesAuthenticatedRecipient() {
        var repo = mock(NotificationRepository.class);
        when(repo.countByRecipientIdAndReadAtNull("c")).thenReturn(2L);
        assertThat(new NotificationService(repo,null).unread("c")).isEqualTo(2);
    }

    @Test
    void publicAggregateHandlesEmptyAndMultipleRatings() {
        assertThat(PublicReviews.from(List.of()).average()).isNull();
        var ratings = List.of(3,4).stream().map(rating ->
                new Review("r","b","p",null,rating,"Travail vérifié",false,null)).toList();
        assertThat(PublicReviews.from(ratings).average()).isEqualTo(3.5);
    }
}
