package com.goosebeoms.tickets.domain.show.service;

import com.goosebeoms.tickets.domain.show.dto.AdminShowCreateRequest;
import com.goosebeoms.tickets.domain.show.dto.AdminShowUpdateRequest;
import com.goosebeoms.tickets.domain.show.dto.ShowResponse;
import com.goosebeoms.tickets.domain.show.entity.Show;
import com.goosebeoms.tickets.domain.show.repository.ShowRepository;
import com.goosebeoms.tickets.global.exception.BusinessException;
import com.goosebeoms.tickets.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminShowService {

    private final ShowRepository showRepository;

    public ShowResponse create(AdminShowCreateRequest request) {
        Show show = showRepository.save(Show.builder()
                .title(request.title())
                .description(request.description())
                .venue(request.venue())
                .category(request.category())
                .status(request.status())
                .posterUrl(request.posterUrl())
                .minPrice(request.minPrice())
                .maxPrice(request.maxPrice())
                .bookingStartAt(request.bookingStartAt())
                .bookingEndAt(request.bookingEndAt())
                .build());
        return ShowResponse.from(show);
    }

    public ShowResponse update(Long showId, AdminShowUpdateRequest request) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOW_NOT_FOUND));
        show.update(
                request.title(), request.description(), request.venue(),
                request.category(), request.posterUrl(),
                request.minPrice(), request.maxPrice(),
                request.bookingStartAt(), request.bookingEndAt()
        );
        return ShowResponse.from(show);
    }

    public ShowResponse updateStatus(Long showId, Show.Status status) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOW_NOT_FOUND));
        show.updateStatus(status);
        return ShowResponse.from(show);
    }
}
