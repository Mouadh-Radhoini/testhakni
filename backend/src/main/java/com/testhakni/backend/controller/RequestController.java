package com.testhakni.backend.controller;

import java.util.List;

import com.testhakni.backend.dto.RequestPayload;
import com.testhakni.backend.dto.RequestResponse;
import com.testhakni.backend.model.RequestStatus;
import com.testhakni.backend.security.AuthenticatedUser;
import com.testhakni.backend.service.ServiceRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/requests")
@Tag(name = "Demandes", description = "Demandes de service du client (REQ-01 / REQ-02, CUSTOMER)")
public class RequestController {

    private final ServiceRequestService requestService;

    public RequestController(ServiceRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Creer une demande")
    @ApiResponse(responseCode = "400", description = "Champs invalides (detail dans fieldErrors)")
    public RequestResponse create(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                                  @Valid @RequestBody RequestPayload payload) {
        return requestService.create(me.id(), payload);
    }

    @GetMapping("/mine")
    @Operation(summary = "Mes demandes, filtrables par statut")
    public List<RequestResponse> mine(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                                      @RequestParam(required = false) RequestStatus status) {
        return requestService.listMine(me.id(), status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detail d'une de mes demandes")
    @ApiResponse(responseCode = "403", description = "La demande appartient a un autre client")
    public RequestResponse detail(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                                  @PathVariable String id) {
        return requestService.getMine(me.id(), id);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Annuler une de mes demandes (si encore ouverte)")
    @ApiResponse(responseCode = "409", description = "La demande n'est plus ouverte")
    public RequestResponse cancel(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                                  @PathVariable String id) {
        return requestService.cancel(me.id(), id);
    }
}
