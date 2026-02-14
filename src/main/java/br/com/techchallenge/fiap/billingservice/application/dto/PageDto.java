package br.com.techchallenge.fiap.billingservice.application.dto;

import java.util.List;

/**
 * DTO for paginated responses.
 */
public record PageDto<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {
    public static <T> PageDto<T> of(List<T> content, int pageNumber, int pageSize, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean first = pageNumber == 0;
        boolean last = pageNumber >= totalPages - 1;
        
        return new PageDto<>(content, pageNumber, pageSize, totalElements, totalPages, first, last);
    }
}
