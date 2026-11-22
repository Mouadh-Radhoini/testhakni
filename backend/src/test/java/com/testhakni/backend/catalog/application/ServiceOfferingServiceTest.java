package com.testhakni.backend.catalog.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.testhakni.backend.shared.domain.exception.DomainException;
import com.testhakni.backend.catalog.domain.model.ServiceOffering;
import com.testhakni.backend.catalog.domain.model.ServiceStatus;
import com.testhakni.backend.provider.domain.repository.ProviderProfileRepository;
import com.testhakni.backend.catalog.domain.repository.ServiceOfferingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.testhakni.backend.shared.domain.exception.ErrorType;

/** SEC-02 et regles de statut, sans base de donnees. TEST-01a */
@ExtendWith(MockitoExtension.class)
class ServiceOfferingServiceTest {

    @Mock
    private ServiceOfferingRepository serviceRepository;

    @Mock
    private ProviderProfileRepository profileRepository;

    @InjectMocks
    private ServiceOfferingService service;

    @Test
    void anotherProviderCannotTouchTheService() {
        when(serviceRepository.findById("s1"))
                .thenReturn(Optional.of(ServiceOffering.builder().id("s1").providerId("owner").build()));
        assertThatThrownBy(() -> service.delete("intrus", "s1"))
                .isInstanceOf(DomainException.class)
                .extracting("type").isEqualTo(ErrorType.FORBIDDEN);
    }

    @Test
    void missingServiceIsNotFound() {
        when(serviceRepository.findById("x")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete("owner", "x"))
                .extracting("type").isEqualTo(ErrorType.NOT_FOUND);
    }

    @Test
    void providerCannotLiftAdminSuspension() {
        when(serviceRepository.findById("s1")).thenReturn(Optional.of(
                ServiceOffering.builder().id("s1").providerId("owner").status(ServiceStatus.SUSPENDED).build()));
        assertThatThrownBy(() -> service.changeStatus("owner", "s1", ServiceStatus.ACTIVE))
                .extracting("type").isEqualTo(ErrorType.FORBIDDEN);
    }

    @Test
    void providerCannotSetSuspendedHimself() {
        assertThatThrownBy(() -> service.changeStatus("owner", "s1", ServiceStatus.SUSPENDED))
                .extracting("type").isEqualTo(ErrorType.BAD_REQUEST);
    }
}
