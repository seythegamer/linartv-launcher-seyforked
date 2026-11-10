package com.linart.tvpreview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Runs with a plain JDK; no TV, emulator, network, or Gradle dependency. */
public final class AppCleanupCheck {
    public static void main(String[] args) {
        List<String> launchable = new ArrayList<>(Arrays.asList(
                "com.google.android.youtube.tv",
                "com.netflix.ninja",
                "com.android.systemui",
                "com.google.android.gms",
                "com.android.tv.settings",
                "com.android.vending",
                "io.github.domlin92.linartv.open",
                "org.droidtv.playtv",
                "com.android.settings",
                "com.google.android.apps.mediashell",
                "com.google.android.googlequicksearchbox",
                "com.spotify.tv.android",
                "com.netflix.ninja",
                "tv.twitch.android.app"));

        List<String> chosen = AppCleanup.candidates(launchable, "io.github.domlin92.linartv.open");
        check(chosen.equals(Arrays.asList("com.google.android.youtube.tv", "com.netflix.ninja",
                "com.spotify.tv.android", "tv.twitch.android.app")), "Candidates: " + chosen);
        check(!chosen.contains("io.github.domlin92.linartv.open"), "The launcher itself stays");
        for (String pkg : chosen) check(!AppCleanup.protect(pkg), "Nothing protected survives: " + pkg);
        check(chosen.size() == 4, "Duplicates are dropped");

        for (String protectedName : Arrays.asList("android", "com.android.systemui", "com.android.settings",
                "com.android.tv.settings", "com.google.android.gms", "com.google.android.gsf",
                "com.google.android.apps.mediashell", "com.android.vending",
                "com.google.android.packageinstaller", "com.google.android.katniss",
                "com.google.android.googlequicksearchbox", "org.droidtv.playtv",
                "org.droidtv.channels", "org.droidtv.eum"))
            check(AppCleanup.protect(protectedName), "Protected: " + protectedName);

        check(!AppCleanup.protect("com.google.android.youtube.tv"), "YouTube may be stopped");
        check(!AppCleanup.protect("com.netflix.ninja"), "Netflix may be stopped");
        check(!AppCleanup.protect("org.droidtvfake.app"), "Prefix needs the dot");

        List<String> kept = AppCleanup.candidates(launchable, "io.github.domlin92.linartv.open",
                Arrays.asList("tv.twitch.android.app", "com.example.missing"));
        check(!kept.contains("tv.twitch.android.app"), "The always-on VPN stays");
        check(kept.size() == 3, "Only the extra keep is dropped: " + kept);

        String script = AppCleanup.forceStopScript(Arrays.asList(
                "com.google.android.youtube.tv", "com.netflix.ninja", "com.spotify.tv.android"));
        check(script.equals("am force-stop com.google.android.youtube.tv; "
                + "am force-stop com.netflix.ninja; am force-stop com.spotify.tv.android"),
                "Script: " + script);
        check(AppCleanup.forceStopScript(Collections.emptyList()).isEmpty(), "Empty script when nothing runs");
        check(AppCleanup.forceStopScript(Arrays.asList("good.name", "bad name", "$(reboot)",
                "x; reboot", "a&&b", "-flag", "quote\"name", "back\\slash", "", ".")).equals(
                "am force-stop good.name"), "Only safe names reach the shell");

        check(AppCleanup.valid("com.example.app"), "Normal name");
        check(AppCleanup.valid("com.example.app_2"), "Underscore and digit");
        check(!AppCleanup.valid(null), "Null name");
        check(!AppCleanup.valid(""), "Empty name");
        check(!AppCleanup.valid(".hidden"), "Leading dot");
        check(!AppCleanup.valid("com.example app"), "Space");
        check(!AppCleanup.valid("com.example;rm -rf /"), "Command injection");
        check(!AppCleanup.valid("com.example\nreboot"), "Newline injection");
        check(!AppCleanup.valid("com.example`id`"), "Backticks");
        check(!AppCleanup.valid("com.example$(id)"), "Subshell");
        check(!AppCleanup.valid("com.example|cat"), "Pipe");
        check(!AppCleanup.valid("com.example>file"), "Redirect");
        check(!AppCleanup.valid("ćma.example"), "Non-ASCII");
        check(!AppCleanup.valid(repeat('a', 201)), "Absurd length");

        System.out.println("OK: 32 app cleanup checks");
    }

    private static String repeat(char value, int count) {
        StringBuilder text = new StringBuilder(count);
        for (int i = 0; i < count; i++) text.append(value);
        return text.toString();
    }

    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
    }
}
