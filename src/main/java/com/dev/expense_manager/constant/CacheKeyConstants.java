package com.dev.expense_manager.constant;

public final class CacheKeyConstants {

    private CacheKeyConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String DASHBOARD_PREFIX = "dashboard:";
    public static final String STATISTICS_PREFIX = "statistics:";

    public static final String DASHBOARD_OVERVIEW = DASHBOARD_PREFIX + "overview:";
    public static final String DASHBOARD_RECENT = DASHBOARD_PREFIX + "recent:";
    public static final String DASHBOARD_TREND = DASHBOARD_PREFIX + "trend:";

    public static String dashboardKey(String userId) {
        return DASHBOARD_PREFIX + userId;
    }

    public static String statisticsKey(String userId) {
        return STATISTICS_PREFIX + userId;
    }

    public static String dashboardOverviewKey(String userId) {
        return DASHBOARD_OVERVIEW + userId;
    }

    public static String dashboardRecentKey(String userId) {
        return DASHBOARD_RECENT + userId;
    }

    public static String dashboardTrendKey(String userId, int days) {
        return DASHBOARD_TREND + userId + ":" + days;
    }
}
