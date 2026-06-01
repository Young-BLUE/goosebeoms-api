package com.goosebeoms.tickets.domain.user.service;

import com.goosebeoms.tickets.domain.user.dto.AdminUserResponse;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.domain.user.repository.UserRepository;
import com.goosebeoms.tickets.global.exception.BusinessException;
import com.goosebeoms.tickets.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;

    public Page<AdminUserResponse> search(String q, Pageable pageable) {
        String query = StringUtils.hasText(q) ? q.trim() : null;
        return userRepository.searchForAdmin(query, pageable).map(AdminUserResponse::from);
    }

    public AdminUserResponse get(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return AdminUserResponse.from(user);
    }
}
