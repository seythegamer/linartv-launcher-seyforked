package com.linart.tvpreview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Rules behind the "Close apps" tile: which packages may be stopped, and the root
 * force-stop script built from them. Plain Java, so tests/ can check it on a desktop.
 */
final class AppCleanup {
    /** Packages the TV needs to stay usable; stopping any of them would break the launcher. */
    private static final Set<String> PROTECTED = new HashSet<>(Arrays.asList(
            "android",
            "com.android.systemui",
            "com.android.settings",
            "com.android.tv.settings",
            "com.android.shell",
            "com.android.vending",
            "com.android.packageinstaller",
            "com.google.android.packageinstaller",
            "com.google.android.gms",
            "com.google.android.gsf",
            "com.google.android.apps.mediashell",
            "com.google.android.googlequicksearchbox",
            "com.google.android.katniss"));

    /** Whole vendor television stacks: picture, inputs and tuner live there. */
    private static final String[] PROTECTED_PREFIXES = {"org.droidtv."};

    private static final String FORCE_STOP = "am force-stop ";
    private static final int MAX_PACKAGE_LENGTH = 200;

    private AppCleanup() { }

    /** Launchable packages minus the launcher itself and everything the TV cannot lose. */
    static List<String> candidates(Collection<String> launchable, String self) {
        return candidates(launchable, self, java.util.Collections.<String>emptyList());
    }

    /** Same as above; {@code keep} holds extras such as the always-on VPN that must keep running. */
    static List<String> candidates(Collection<String> launchable, String self, Collection<String> keep) {
        LinkedHashSet<String> chosen = new LinkedHashSet<>();
        for (String pkg : launchable) {
            if (!valid(pkg) || pkg.equals(self) || protect(pkg) || keep.contains(pkg)) continue;
            chosen.add(pkg);
        }
        return new ArrayList<>(chosen);
    }

    static boolean protect(String pkg) {
        if (PROTECTED.contains(pkg)) return true;
        for (String prefix : PROTECTED_PREFIXES) if (pkg.startsWith(prefix)) return true;
        return false;
    }

    /**
     * One root shell call for every app: {@code am force-stop a; am force-stop b}.
     * An empty result means there is nothing to stop.
     */
    static String forceStopScript(Collection<String> packages) {
        StringBuilder script = new StringBuilder();
        for (String pkg : packages) {
            if (!valid(pkg)) continue;
            if (script.length() > 0) script.append("; ");
            script.append(FORCE_STOP).append(pkg);
        }
        return script.toString();
    }

    /** A package name reaches a shell, so anything but letters, digits, dots and underscores is refused. */
    static boolean valid(String pkg) {
        if (pkg == null || pkg.isEmpty() || pkg.length() > MAX_PACKAGE_LENGTH) return false;
        for (int i = 0; i < pkg.length(); i++) {
            char c = pkg.charAt(i);
            boolean allowed = c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z'
                    || c >= '0' && c <= '9' || c == '_' || c == '.' && i > 0;
            if (!allowed) return false;
        }
        return true;
    }
}
