package com.testhakni.backend.marketplace;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.testhakni.backend.booking.application.BookingService;
import com.testhakni.backend.booking.domain.model.Booking;
import com.testhakni.backend.booking.domain.model.BookingStatus;
import com.testhakni.backend.booking.domain.repository.BookingRepository;
import com.testhakni.backend.identity.domain.model.AuthenticatedUser;
import com.testhakni.backend.identity.domain.model.Role;
import com.testhakni.backend.notification.application.NotificationService;
import com.testhakni.backend.notification.domain.repository.NotificationRepository;
import com.testhakni.backend.offer.application.OfferService;
import com.testhakni.backend.provider.domain.model.ProviderProfile;
import com.testhakni.backend.provider.domain.repository.ProviderProfileRepository;
import com.testhakni.backend.request.domain.repository.ServiceRequestRepository;
import com.testhakni.backend.review.application.dto.Feedback;
import com.testhakni.backend.review.application.dto.PublicReviews;
import com.testhakni.backend.review.domain.model.Review;
import com.testhakni.backend.shared.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

/** Regles des cas d'usage du parcours de reservation, sans base de donnees (ports mockes). */
class MarketplaceServicesTest {
    @Test
    void unavailableProviderCannotDiscoverRequests() {
        var profiles = mock(ProviderProfileRepository.class);
        when(profiles.findByUserId("p")).thenReturn(Optional.of(ProviderProfile.builder().available(false).build()));
        var requests = mock(ServiceRequestRepository.class);
        var service = new OfferService(null, null, profiles, null, requests, null, null);
        assertThat(service.discover("p")).isEmpty();
        verifyNoInteractions(requests);
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
                .isInstanceOf(DomainException.class).hasMessageContaining("participant");
        assertThatThrownBy(() -> service.review(provider,"b",new Feedback(5,"Excellent")))
                .isInstanceOf(DomainException.class).hasMessageContaining("client");
    }

    @Test
    void unreadCountUsesAuthenticatedRecipient() {
        var repo = mock(NotificationRepository.class);
        when(repo.countUnread("c")).thenReturn(2L);
        assertThat(new NotificationService(repo).unread("c")).isEqualTo(2);
    }

    @Test
    void publicAggregateHandlesEmptyAndMultipleRatings() {
        assertThat(PublicReviews.from(List.of()).average()).isNull();
        var ratings = List.of(3,4).stream().map(rating ->
                new Review("r","b","p",null,rating,"Travail vérifié",false,null)).toList();
        assertThat(PublicReviews.from(ratings).average()).isEqualTo(3.5);
    }

    @Test
    void bookingLifecycleOnlyMovesForward() {
        assertThat(BookingStatus.IN_PROGRESS.expectedPrevious()).contains(BookingStatus.BOOKED);
        assertThat(BookingStatus.AWAITING_CONFIRMATION.expectedPrevious()).contains(BookingStatus.IN_PROGRESS);
        assertThat(BookingStatus.COMPLETED.expectedPrevious()).contains(BookingStatus.AWAITING_CONFIRMATION);
        assertThat(BookingStatus.BOOKED.expectedPrevious()).isEmpty();
    }
}
