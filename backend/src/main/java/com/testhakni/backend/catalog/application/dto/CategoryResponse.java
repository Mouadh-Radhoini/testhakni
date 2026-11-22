package com.testhakni.backend.catalog.application.dto;

import com.testhakni.backend.catalog.domain.model.ServiceCategory;

/** SERV-05 */
public record CategoryResponse(ServiceCategory code, String label) {

    public static CategoryResponse from(ServiceCategory c) {
        return new CategoryResponse(c, c.getLabel());
    }
}
