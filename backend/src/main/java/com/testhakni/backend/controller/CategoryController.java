package com.testhakni.backend.controller;

import java.util.Arrays;
import java.util.List;

import com.testhakni.backend.dto.CategoryResponse;
import com.testhakni.backend.model.ServiceCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Taxonomie des services (SERV-05)")
@SecurityRequirements
public class CategoryController {

    @GetMapping
    @Operation(summary = "Liste des categories de services")
    public List<CategoryResponse> list() {
        return Arrays.stream(ServiceCategory.values()).map(CategoryResponse::from).toList();
    }
}
