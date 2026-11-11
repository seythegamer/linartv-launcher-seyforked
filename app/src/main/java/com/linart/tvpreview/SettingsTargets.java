package com.linart.tvpreview;

import java.util.Collection;

/** Which settings app the "Android TV settings" button opens. Plain Java so tests can check it. */
final class SettingsTargets {
    private SettingsTargets() { }

    /**
     * The saved choice while that app is still installed, otherwise the app Android resolves for
     * {@code android.settings.SETTINGS}, otherwise the first one found.
     */
    static String choose(String saved, Collection<String> available, String resolved) {
        if (saved != null && available.contains(saved)) return saved;
        if (resolved != null && available.contains(resolved)) return resolved;
        for (String component : available) return component;
        return null;
    }
}
