package com.testhakni.backend.controller;

import java.math.BigDecimal;
import java.util.List;

import com.testhakni.backend.dto.PageResponse;
import com.testhakni.backend.dto.ServicePayload;
import com.testhakni.backend.dto.ServiceResponse;
import com.testhakni.backend.dto.ServiceStatusRequest;
import com.testhakni.backend.model.ServiceCategory;
import com.testhakni.backend.security.AuthenticatedUser;
import com.testhakni.backend.service.ServiceOfferingService;
import com.testhakni.backend.service.ServiceOfferingService.SearchCriteria;
import com.testhakni.backend.service.ServiceOfferingService.SortOption;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/services")
@Tag(name = "Services", description = "Catalogue public (SEARCH) et gestion par le prestataire (SERV)")
public class ServiceController {

    private final ServiceOfferingService serviceService;

    public ServiceController(ServiceOfferingService serviceService) {
        this.serviceService = serviceService;
    }

    // ------------------------------------------------------------ public

    @GetMapping
    @SecurityRequirements
    @Operation(summary = "Rechercher des services actifs (SEARCH-01 / SEARCH-02)",
            description = "Tous les filtres sont optionnels et combinables. Resultat pagine.")
    @ApiResponse(responseCode = "200", description = "Page de resultats (eventuellement vide)")
    @ApiResponse(responseCode = "400", description = "Filtre invalide (categorie inconnue, prix negatif, min > max...)")
    public PageResponse<ServiceResponse> search(
            @Parameter(description = "Mot-cle cherche dans le titre et la description")
            @RequestParam(required = false) @Size(max = 100) String q,
            @RequestParam(required = false) ServiceCategory category,
            @Parameter(description = "Ville (recherche partielle, insensible a la casse)")
            @RequestParam(required = false) @Size(max = 80) String city,
            @RequestParam(required = false) @DecimalMin(value = "0", message = "Doit etre positif") BigDecimal minPrice,
            @RequestParam(required = false) @DecimalMin(value = "0", message = "Doit etre positif") BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Doit etre >= 0") int page,
            @RequestParam(defaultValue = "12") @Min(value = 1, message = "Doit etre >= 1") @Max(value = 50, message = "50 maximum") int size,
            @RequestParam(defaultValue = "NEWEST") SortOption sort) {
        return serviceService.search(new SearchCriteria(q, category, city, minPrice, maxPrice), page, size, sort);
    }

    @GetMapping("/mine")
    @Operation(summary = "Mes services, tous statuts confondus (PROVIDER)")
    public List<ServiceResponse> mine(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
        return serviceService.listMine(me.id());
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    @Operation(summary = "Detail d'un service (SEARCH-03)")
    @ApiResponse(responseCode = "404", description = "Service inexistant ou non visible")
    public ServiceResponse detail(@PathVariable String id,
                                  @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser viewer) {
        return serviceService.getById(id, viewer);
    }

    // ------------------------------------------------------------ prestataire

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Publier un service (SERV-02, PROVIDER)")
    public ServiceResponse create(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                                  @Valid @RequestBody ServicePayload payload) {
        return serviceService.create(me.id(), payload);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un de mes services (SERV-03, PROVIDER)")
    @ApiResponse(responseCode = "403", description = "Le service appartient a un autre prestataire")
    public ServiceResponse update(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                                  @PathVariable String id, @Valid @RequestBody ServicePayload payload) {
        return serviceService.update(me.id(), id, payload);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activer / desactiver un de mes services (PROVIDER)")
    public ServiceResponse changeStatus(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                                        @PathVariable String id, @Valid @RequestBody ServiceStatusRequest request) {
        return serviceService.changeStatus(me.id(), id, request.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un de mes services (SERV-04, PROVIDER)")
    @ApiResponse(responseCode = "403", description = "Le service appartient a un autre prestataire")
    public void delete(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                       @PathVariable String id) {
        serviceService.delete(me.id(), id);
    }
}
