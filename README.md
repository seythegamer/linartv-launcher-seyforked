# LinarTV Open — seythegamer fork

<p align="center">
  <a href="https://github.com/seythegamer/linartv-launcher-seyforked/releases/latest"><img alt="Latest release" src="https://img.shields.io/github/v/release/seythegamer/linartv-launcher-seyforked"></a>
  <a href="LICENSE"><img alt="MIT license" src="https://img.shields.io/badge/license-MIT-blue"></a>
  <img alt="Android TV 8.0+" src="https://img.shields.io/badge/Android%20TV-8.0%2B-3DDC84">
  <img alt="Java 17" src="https://img.shields.io/badge/Java-17-007396">
</p>

<p align="center"><b>English</b> · <i>This is a fork, not the original project.</i></p>

---

## About this fork

**LinarTV Open — seythegamer fork** is a fork of
[domlin92/linartv-launcher-open](https://github.com/domlin92/linartv-launcher-open),
kept at [seythegamer/linartv-launcher-seyforked](https://github.com/seythegamer/linartv-launcher-seyforked).

It is **not** the original project and is **not** maintained by the original author.
Upstream remains the place for the official releases, documentation and issue reports:
<https://github.com/domlin92/linartv-launcher-open>.

The repository was renamed to `linartv-launcher-seyforked` (from `linartv-launcher-open`); GitHub redirects the old links, and the app's user agent and update check already use the new name. The app package ID is unchanged (`io.github.domlin92.linartv.open`), so you can install this fork over an upstream build.

**Updates:** the in-app update check asks this fork's releases (`seythegamer/linartv-launcher-seyforked`) for the latest version, so an update can never replace the fork with an upstream build. Debug builds never show the update bar; see [docs/RELEASING.md](docs/RELEASING.md) for publishing your own signed releases.

---

## What LinarTV is

LinarTV is a clean, fast home screen (launcher) for Android TV built around football — from the Polish IV liga to the European top 5. One screen shows the clock, the weather for a chosen city and the next matches of one or two clubs you follow, and keeps your favourite apps one click away.

LinarTV replaces the home screen. It does not uninstall the TV manufacturer's apps or remove their advertising and tracking services. If you also want to tidy up the TV's system apps, [CleanYourTV](https://github.com/Gathin23/cleanyourtv) is a separate project you can use alongside it.

Core features (from upstream, all kept in this fork):
- **Your clubs' fixtures** — one or two teams: Polish clubs (Ekstraklasa down to all 16 IV liga groups, via [kalendarzeligowe.pl](https://kalendarzeligowe.pl)) and clubs from the Premier League, La Liga, Bundesliga, Serie A and Ligue 1 (via [fixtur.es](https://fixtur.es)) — league, domestic cups and European cups in one schedule, with crests.
- **Find your club by name — or by voice** — club and city search in the setup wizard, with a microphone button.
- **Match card at a glance** — competition and round, kick-off time in your time zone, "in 3 days", a green TODAY badge and a LIVE badge.
- **Weather for any city in the world** — current conditions, hourly and 7-day forecast from Open-Meteo, with Meteocons icons.
- **Polish, English, German, Spanish or Italian interface** — follows the TV's system language or one you choose; °C or °F.
- **Updates inside the app** — "Update available" bar; one click downloads and opens the install window. Your settings stay.
- **Day and night modes**, app dock and full app drawer, quick access to TV inputs.
- **Wallpapers from your phone** — scan a QR code and upload photos over your home Wi-Fi (6-digit code, attempt limit).
- **Screensaver** with your photos after 5 minutes of inactivity.
- **Spotify bar** on the home screen.

How the app works and where the data comes from: [docs/FUNCTIONAL.md](docs/FUNCTIONAL.md).

---

## What this fork changes

Built on top of upstream `main`. The changelog is in [CHANGELOG.md](CHANGELOG.md).

### 1. Custom dock icons and a clearer app drawer
- Dock tiles can show an image you pick: an in-launcher browser of the **Download** folder (including subfolders) for **PNG, JPG, JPEG, WEBP and SVG** (vectors rendered).
- A tile's background colour comes from a **12-colour RYB palette** in a single row, red → yellow → blue → violet.
- Live preview, working **Enlarge / Shrink**; images stored on the device as PNG (max 512 px) in `files/custom_icons`. Storage permission is asked once on the first pick, with the system file picker as fallback.
- The app drawer maps focus to the dock: Down from tile N highlights tile N; Down out of column N returns to dock tile N; Back/Up returns to the tile that opened the list.
- A selected app uses the same aura as the match and weather cards (no drop shadow), grows 10% and gets a 2 px frame — black on a light app, white on a dark one.

### 2. RAM widget, button visibility and wallpapers in Launcher appearance
- A **RAM readout** (used / total) sits left of Watch TV, Inputs and Settings, drawn like the round buttons. The top-right group packs against the right edge, so hiding any button leaves no gap.
- A **Visibility** panel in Settings with switches for the RAM widget, Watch TV and Inputs. Choices are stored on the device and applied when the panel closes.
- **Wallpapers** moved from their own settings tile into **Launcher appearance**; the freed tile is the new visibility panel.

### 3. Weather wallpaper — 7 kinds × day / night
- A **"Wallpaper follows the weather"** switch in the wallpaper gallery (Settings → Launcher appearance → Wallpapers): the home screen background shows the current Open-Meteo conditions of the chosen city.
- **Seven kinds** folded from the WMO codes — clear, partly cloudy, cloudy, rain, snow, thunderstorm, fog. Drizzle and showers count as rain, sleet as snow, hail as thunderstorm, unknown as clouds.
- **Day and night** follow the launcher's appearance mode (automatic schedule, always light, always dark, custom hours), so the background flips with the interface — "always light" always shows the day variant.
- **Fourteen templates** in `app/src/main/res/drawable/weather_<kind>_<day|night>.xml`. Replace colours or swap a file with your own 1920×1080 PNG/WEBP/JPG under the same base name — no code changes needed. See [docs/WEATHER-WALLPAPERS.md](docs/WEATHER-WALLPAPERS.md).
- Picking a photo in the gallery turns the option off and keeps the photo. Without a forecast yet, the gallery-chosen wallpaper stays visible.
- Logic in `WeatherWallpaper.java` (plain Java, no Android imports), covered by `tests/WeatherWallpaperCheck.java`.

### 4. Faster app drawer, clean selection aura, lower memory use
- **The drawer opens about five times faster.** Only the visible column is built, the blurred wallpaper is reused, and the animation runs on the warm view tree: median of the 90th percentile **350 ms → ~70 ms** (Android TV 11, sdk 30, 1080p).
- **The selection aura stays inside the tile:** a thin 2 px frame plus a softer glow instead of a shadow bleeding over neighbours — black on a light app, white on a dark one. Back returns focus to the tile that opened the drawer.
- **Lower memory use, no feature removed:** wallpaper decoded at screen size × 1.12 instead of 2560×1440; crests and custom dock icons in bitmap LRU caches (4 MB and 8 MB); weather icons in a 2 MB byte-budgeted LRU cache; gallery thumbnails 400×280, custom icons at 384 px (older 512 px files shrink on load); Spotify cover downscaled to 192 px; `onTrimMemory` / `onLowMemory` release caches; dialog background blur freed on close; closed settings dialog returns focus to the dashboard.
- Cold-start PSS **58 588 → 55 950 KB**; a photo wallpaper uses **15 744 KB instead of 24 544 KB** graphics memory.
- **Update check points at this fork** (see section 1 above).

### 5. Uninstall an app from the launcher
- **Hold OK** on a dock tile — or on any tile in the app list — and choose **Uninstall app**. Android's own uninstall window opens and confirms once more, so the TV settings are never needed.
- Nothing is deleted behind your back: a launcher cannot remove a package silently, and the app list only offers the action. **System apps** cannot be removed without a system privilege — the launcher explains that instead of offering an action that would fail.
- Once the app is gone, its dock tile and list place disappear on their own and the dock closes the gap. A short OK still opens an app; the list opens it on key release and the menu on a held key.
- `REQUEST_DELETE_PACKAGES` was added to the manifest (normal, auto-granted; required by `ACTION_UNINSTALL_PACKAGE` since Android 10).

### 6. Close apps really closes apps
- The **Close apps** tile used to `killBackgroundProcesses` seven hard-coded packages. It now scans every app in the dock or app list and stops them all, keeping only what the TV needs: the launcher, System UI, Settings, Play Store, Google services, the cast receiver (`mediashell`), the vendor television stack (`org.droidtv.*`) and an always-on VPN.
- **With root** (Magisk/Superuser or AOSP `su`): builds one script of `am force-stop <package>` lines and runs it in a single root shell — a real force stop, taking services down too. The first press may ask for root once; the answer is kept for the run. Refusal or timeout silently falls back.
- **Without root:** Android offers no public force stop, so the launcher calls `killBackgroundProcesses` for every app — the same API as before, now applied to the whole list instead of seven names.
- The toast reports how many background apps were targeted and how much RAM came back (measured before/after with `ActivityManager.MemoryInfo`); the RAM widget and settings overview refresh immediately. A second press while it works is ignored.
- The safety list and script builder live in plain Java (`AppCleanup.java`), so `tests/AppCleanupCheck.java` checks them on a desktop JVM — including command-injection attempts such as `a; reboot` or `$(id)`.

### 7. Android TV settings: choose which settings app opens
- Some boxes ship **two settings apps**: AOSP (`com.android.settings`) and the TV panel (`com.android.tv.settings`). Both answer `android.settings.SETTINGS`, so the button always opened whichever Android preferred.
- **Hold OK** on **Android TV settings** now opens a chooser listing every installed app answering that intent — the app label, with the package name when two share a label. The default is ticked and focused. Picking an entry saves it (`settings_target` in launcher preferences); a short press opens that app from then on. If the saved app is uninstalled, the stored choice is dropped on the next press.
- Covered by `tests/SettingsTargetsCheck.java`.

### 8. Local IP address in Settings
- The **Settings** panel (gear icon at top right) now shows the TV's **local IP address** at the bottom, below Language, About and Spotify — easier to find the TV on the network, e.g. for `adb connect <TV_IP>:5555` or installing an APK over ADB.

### 9. Sunrise/sunset automatic day/night mode
- In **Settings → Launcher appearance → Automatic**, choose whether day/night follows **sunrise/sunset for your weather city** (Open-Meteo data) or **fixed hours**.
- Sunrise/sunset is used by default once weather data is available; the launcher switches automatically at sunset and sunrise. With no weather data yet, it falls back to fixed 08:00–20:00.

---

## Build from source

JDK 17 and the Android SDK are required (`JAVA_HOME`, `ANDROID_HOME`).

```text
./gradlew :app:assembleDebug
```

Output: `app/build/outputs/apk/debug/LinarTV-<version>-debug.apk`.

Local checks: `scripts/test-local.ps1` (Windows) or the Java checks in `tests/`.

Signed releases: see [docs/RELEASING.md](docs/RELEASING.md). What changed in each version: [CHANGELOG.md](CHANGELOG.md).

---

## Install on a TV

The official signed APK for Android TV is published by upstream in
[its Releases](https://github.com/domlin92/linartv-launcher-open/releases/latest).
Releases published in [this fork](https://github.com/seythegamer/linartv-launcher-seyforked/releases/latest) carry the fork's changes.

Local `./gradlew :app:assembleDebug` builds are debug builds — install them over `adb`:

```text
adb connect <TV_IP>:5555
adb install -r LinarTV-<version>-debug.apk
adb shell am start -n io.github.domlin92.linartv.open/com.linart.tvpreview.DashboardActivity
```

To make it the home screen, use your TV's *Default home* / *Home screen* setting, or:

```text
adb shell cmd package set-home-activity --user 0 io.github.domlin92.linartv.open/com.linart.tvpreview.DashboardActivity
```

Enable developer options and network debugging on the TV first (Settings → Device Preferences → About → click *Build* 7 times). On Google TV the stock launcher may keep taking over even when the command prints `Success`; [AGENTS.md](AGENTS.md) describes that case, along with the commands to restore it.

---

## Credits

- Inspired by and started inside [Clean Your TV](https://cleanyour.tv/) by Gathin (MIT).
- Polish club fixtures: [kalendarzeligowe.pl](https://kalendarzeligowe.pl) — used with the site owner's permission.
- Foreign club fixtures (Top 5 leagues, domestic and European cups): match data by [fixtur.es](https://fixtur.es) — used under their fair-use rules; credited on every match card.
- Weather data: [Open-Meteo.com](https://open-meteo.com/) (CC BY 4.0); city search: Open-Meteo geocoding / GeoNames (CC BY 4.0).
- Weather icons: [Meteocons](https://github.com/basmilius/weather-icons) by Bas Milius (MIT), bundled in the app.
- Weather Icons font by Erik Flowers (SIL OFL 1.1) — used for the °C / °F sign.
- Club crests: [kalendarzeligowe.pl](https://kalendarzeligowe.pl); foreign clubs without a crest there: [football-logos.cc](https://football-logos.cc). Crests belong to their clubs — see [docs/CRESTS.md](docs/CRESTS.md).

---

## License

Code: [MIT](LICENSE). Club crests and third-party logos are not covered by the code license.
