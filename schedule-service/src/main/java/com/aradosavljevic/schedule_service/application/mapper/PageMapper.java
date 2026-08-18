package com.aradosavljevic.schedule_service.application.mapper;

import com.aradosavljevic.erp_common.dto.PageResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public final class PageMapper {

    private PageMapper() {
    }

    public static <E, D> PageResponse<D> toPageResponse(Page<E> page, Function<E, D> mapper) {
        return toPageResponse(page, page.getContent().stream().map(mapper).toList());
    }

    /** Varijanta sa vec pripremljenim sadrzajem (za batch mapiranje, izbegavanje N+1). */
    public static <D> PageResponse<D> toPageResponse(Page<?> page, List<D> content) {
        return PageResponse.<D>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }
}
