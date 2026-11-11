package com.linart.tvpreview;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Runs with a plain JDK; no TV, emulator, network, or Gradle dependency. */
public final class SettingsTargetsCheck {
    public static void main(String[] args) {
        List<String> both = Arrays.asList("com.android.settings/.homepage.SettingsHomepageActivity",
                "com.android.tv.settings/.MainSettings");

        check(SettingsTargets.choose(null, both, both.get(0)).equals(both.get(0)), "No choice: resolved");
        check(SettingsTargets.choose(both.get(1), both, both.get(0)).equals(both.get(1)),
                "Saved choice wins over the system default");
        check(SettingsTargets.choose("gone.app/.Main", both, both.get(1)).equals(both.get(1)),
                "A stale choice falls back to the resolved one");
        check(SettingsTargets.choose("gone.app/.Main", both, "also.gone/.Main").equals(both.get(0)),
                "A stale choice and no resolution: the first app");
        check(SettingsTargets.choose(null, both, "also.gone/.Main").equals(both.get(0)),
                "Resolution that is not in the list is ignored");
        check(SettingsTargets.choose("gone.app/.Main", Collections.singletonList(both.get(0)), null)
                .equals(both.get(0)), "Single app");
        check(SettingsTargets.choose(null, Collections.emptyList(), both.get(0)) == null,
                "No settings app at all");
        check(SettingsTargets.choose(both.get(0), Collections.emptyList(), null) == null,
                "Saved choice on a device without settings apps");

        System.out.println("OK: 8 settings target checks");
    }

    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
    }
}
