package com.goosebeoms.tickets.domain.admin.dto;

public record AdminStatsResponse(
        long totalBookings,
        long todayBookings,
        long totalRevenue,
        long todayRevenue,
        long activeShows,
        long soldOutShows,
        long totalUsers,
        long activeCoupons
) {}
