package com.testhakni.backend.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.testhakni.backend.exception.ApiException;
import com.testhakni.backend.model.ServiceOffering;
import com.testhakni.backend.model.ServiceStatus;
import com.testhakni.backend.repository.ProviderProfileRepository;
import com.testhakni.backend.repository.ServiceOfferingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;

/** SEC-02 et regles de statut, sans base de donnees. TEST-01a */
@ExtendWith(MockitoExtension.class)
class ServiceOfferingServiceTest {

    @Mock
    private ServiceOfferingRepository serviceRepository;

    @Mock
    private ProviderProfileRepository profileRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ServiceOfferingService service;

    @Test
    void anotherProviderCannotTouchTheService() {
        when(serviceRepository.findById("s1"))
                .thenReturn(Optional.of(ServiceOffering.builder().id("s1").providerId("owner").build()));
        assertThatThrownBy(() -> service.delete("intrus", "s1"))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void missingServiceIsNotFound() {
        when(serviceRepository.findById("x")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete("owner", "x"))
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void providerCannotLiftAdminSuspension() {
        when(serviceRepository.findById("s1")).thenReturn(Optional.of(
                ServiceOffering.builder().id("s1").providerId("owner").status(ServiceStatus.SUSPENDED).build()));
        assertThatThrownBy(() -> service.changeStatus("owner", "s1", ServiceStatus.ACTIVE))
                .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void providerCannotSetSuspendedHimself() {
        assertThatThrownBy(() -> service.changeStatus("owner", "s1", ServiceStatus.SUSPENDED))
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
