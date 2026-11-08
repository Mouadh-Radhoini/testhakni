package com.testhakni.backend.dto;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

/** Page de resultats avec metadonnees de pagination. SEARCH-01 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {

    public static <S, T> PageResponse<T> from(Page<S> page, Function<S, T> mapper) {
        return new PageResponse<>(page.getContent().stream().map(mapper).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(),
                page.isFirst(), page.isLast());
    }

    public static <T> PageResponse<T> of(List<T> content, Page<?> page) {
        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast());
    }
}
