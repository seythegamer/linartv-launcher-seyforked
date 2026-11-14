package com.linart.tvpreview;

/** Scheduling rules shared by the launcher and local JVM checks. All times are local. */
final class LauncherTiming {
    static boolean isNight(String mode, int minute, int dayStart, int nightStart) {
        if ("dark".equals(mode)) return true;
        if ("light".equals(mode)) return false;
        boolean day = dayStart < nightStart ? minute >= dayStart && minute < nightStart
                : minute >= dayStart || minute < nightStart;
        return !day;
    }

    /**
     * Auto mode using sunrise/sunset times (minutes from midnight). Returns true when it is
     * night: after sunset or before sunrise. If either value is 0 (unknown), falls back to
     * fixed hours via {@link #isNight(String, int, int, int)} with dayStart=480, nightStart=1200.
     */
    static boolean isNightAuto(String mode, int minute, int sunriseMinute, int sunsetMinute) {
        if ("dark".equals(mode)) return true;
        if ("light".equals(mode)) return false;
        if (sunriseMinute > 0 && sunsetMinute > 0)
            return minute >= sunsetMinute || minute < sunriseMinute;
        // No sun data yet: fall back to fixed 08:00/20:00.
        return isNight(mode, minute, 480, 1200);
    }

    static long idleMillis(int minutes) {
        return Math.max(0, Math.min(1440, minutes)) * 60_000L;
    }
}
