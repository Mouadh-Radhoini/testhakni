package com.testhakni.backend.shared.application;

import java.util.List;
import java.util.function.Function;

/** Page de resultats avec metadonnees de pagination, sans dependance a Spring Data. SEARCH-01 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, page, size, totalElements, totalPages,
                page == 0, page + 1 >= totalPages);
    }

    public <R> PageResponse<R> map(Function<T, R> mapper) {
        return new PageResponse<>(content.stream().map(mapper).toList(), page, size, totalElements,
                totalPages, first, last);
    }
}
