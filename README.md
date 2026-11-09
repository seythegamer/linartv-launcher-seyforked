# LinarTV Open — seythegamer fork

**This is a fork of [LinarTV Open](https://github.com/domlin92/linartv-launcher-open) by domlin92, kept in
[seythegamer/linartv-launcher-seyforked](https://github.com/seythegamer/linartv-launcher-seyforked).**
It is not the original project and it is not maintained by the original author. Installation, credits and
license come from upstream and still apply; the changes listed below exist only in this repository.

Upstream is the place to go for the official releases, documentation and issue reports:
<https://github.com/domlin92/linartv-launcher-open>.

The fork repository was renamed to `linartv-launcher-seyforked` (from `linartv-launcher-open`); GitHub
redirects the old links, and the app's user agent and update check already use the new name.

## What the app is

LinarTV is a home screen (launcher) for Android TV built around football — from the Polish IV liga to the
European top 5. One screen shows the clock, the weather for a chosen city and the next matches of one or two
clubs you follow, and keeps your favourite apps one click away. Plain Java, one activity, no Kotlin and no
Compose.

LinarTV replaces the home screen. It does not uninstall the TV manufacturer's apps or remove their
advertising and tracking services. If you also want to tidy up the TV's system apps,
[CleanYourTV](https://github.com/Gathin23/cleanyourtv) is a separate project you can use alongside it.

## What this fork changes

Everything sits on top of upstream `main` (`2a17480`). The app still reports `versionName 1.0.2`
(`versionCode 3`) in `app/build.gradle`; the next section of the [CHANGELOG](CHANGELOG.md) is 1.0.3
(unreleased) and covers the work below.

### 1. Custom dock icons and a clearer app drawer

- Dock tiles can show an image you pick: an in-launcher browser of the **Download** folder, including
  subfolders, for **PNG, JPG, JPEG, WEBP and SVG** (vectors are rendered).
- A tile's background colour comes from a **12-colour RYB palette** in a single row, from red through yellow
  to blue and violet.
- Live preview, working **Enlarge / Shrink**, images stored on the device as PNG (max 512 px) in
  `files/custom_icons`. Storage permission is asked once on the first pick, with the system file picker as a
  fallback.
- The app drawer now maps focus to the dock: Down from tile N highlights tile N, Down out of column N returns
  to dock tile N, and Back/Up returns to the tile that opened the list.
- A selected app uses the same aura as the match and weather cards (no drop shadow), grows 10% and gets a
  2 px frame — black on a light app, white on a dark one.

### 2. RAM widget, buttons visibility and wallpapers in Launcher appearance

- A **RAM readout** (used / total) sits left of Watch TV, Inputs and Settings, drawn like the round buttons.
  The top-right group packs against the right edge, so hiding any of the buttons leaves no gap.
- A **Buttons visibility** panel in Settings with switches for the RAM widget, Watch TV and Inputs. The choices
  are stored on the device and applied when the panel closes.
- **Wallpapers** moved from their own settings tile into **Launcher appearance**; the freed tile is the new
  visibility panel.

### 3. Weather wallpaper — 7 kinds × day / night

- A **"Wallpaper follows the weather"** switch in the wallpaper gallery
  (Settings → Launcher appearance → Wallpapers): the home screen background shows the current Open-Meteo
  conditions of the chosen city.
- **Seven kinds** folded from the WMO codes the widget already uses — clear, partly cloudy, cloudy, rain,
  snow, thunderstorm, fog. Drizzle and showers count as rain, sleet as snow, hail as a thunderstorm and an
  unknown code as clouds, so nothing falls outside the seven.
- **Day and night** follow the launcher's own appearance mode (automatic schedule, always light, always dark,
  custom hours), so the background flips together with the rest of the interface — "always light" always shows
  the day variant.
- **Fourteen templates** in `app/src/main/res/drawable/weather_<kind>_<day|night>.xml`. They are meant to be
  replaced in the source code; the launcher cannot edit them. Change the gradient colours, or replace a file
  with your own 1920×1080 PNG / WEBP / JPG under the same base name — no code changes needed.
  See [docs/WEATHER-WALLPAPERS.md](docs/WEATHER-WALLPAPERS.md) (Polish and English).
- Picking a photo in the gallery turns the option off and keeps the photo. Without a forecast yet, the
  wallpaper chosen in the gallery stays visible.
- The logic lives in `WeatherWallpaper.java` (plain Java, no Android imports), covered by
  `tests/WeatherWallpaperCheck.java`.

### 4. Faster app drawer, a clean selection aura and lower memory use

- **The drawer opens about five times faster.** Only the visible column is built, the blurred wallpaper is
  reused instead of being rendered again, and the animation runs on the already warm view tree. Measured on
  the Android TV box the fork is developed on (Android TV 11, sdk 30, 1080p): median of the 90th percentile
  **350 ms → ~70 ms**.
- **The selection aura stays inside the tile:** a thin 2 px frame plus a softer glow instead of a shadow
  bleeding over the neighbours — black on a light app, white on a dark one. Select on the same column it
  was entered from, and **Back returns focus to the tile that opened the drawer**.
- **Lower memory use, no feature removed:**
  - a wallpaper photo is decoded at screen size × 1.12 instead of 2560×1440 (`inSampleSize` plus density
    scaling, so the result is never larger than the old one);
  - club crests and custom dock icons live in bitmap LRU caches (4 MB and 8 MB) instead of two maps that
    only grew; evicted bitmaps are dropped, not recycled;
  - weather icons (Meteocons) use a byte-budgeted LRU cache (2 MB) with `clear()` on demand;
  - gallery thumbnails are 400×280 and custom icons are stored at 384 px — older 512 px files are shrunk
    when they are loaded;
  - the Spotify cover is downscaled to 192 px;
  - `onTrimMemory` / `onLowMemory` release the caches, and the dialog background blur bitmap is freed when
    the dialog closes;
  - a closed settings dialog hands focus back to the dashboard, so the remote works on the first press
    instead of after pressing something twice.
- Cold-start memory in the state measured on that TV: **PSS 58 588 → 55 950 KB**, and a photo wallpaper
  uses **15 744 KB instead of 24 544 KB** of graphics memory.
- **Update check points at this fork.** `AppUpdates.LATEST` asks
  `seythegamer/linartv-launcher-seyforked` for releases, so an update can never replace the fork with an
  upstream build. Debug builds still never show the update bar (Android refuses an APK signed with another
  key); a debuggable build can test the bar with the `update_test_version` preference. The pinned upstream
  certificate was replaced by a "release build only" check, so the fork can publish its own signed releases
  — see [docs/RELEASING.md](docs/RELEASING.md).

### 5. Uninstall an app from the launcher

- **Hold OK** on a dock tile — or on any tile in the app list — and choose **Uninstall app**. Android's own
  uninstall window opens and asks once more, so the TV settings are never needed.
- Nothing is deleted behind your back: a launcher cannot remove a package silently, and the app list only
  offers the action. **System apps** (a factory-installed YouTube, for example) cannot be removed at all
  without a system privilege — the launcher explains that instead of offering an action that would fail.
- Once the app is gone, its dock tile and its place in the list disappear on their own and the dock closes
  the gap. A short OK still opens an app; the list now opens it on key release and the menu on a held key,
  because the list handles OK itself so the arrow keys keep working.
- `REQUEST_DELETE_PACKAGES` was added to the manifest — a normal, auto-granted permission that
  `ACTION_UNINSTALL_PACKAGE` needs since Android 10.

## Build from source

JDK 17 and the Android SDK are required (`JAVA_HOME`, `ANDROID_HOME`).

```text
./gradlew :app:assembleDebug
```

Output: `app/build/outputs/apk/debug/LinarTV-<version>-debug.apk`.
Local checks: `scripts/test-local.ps1` (Windows) or the Java checks in `tests/`.

## Install on a TV

The official signed APK for Android TV is published by upstream in
[its Releases](https://github.com/domlin92/linartv-launcher-open/releases/latest). Releases published in
[this fork](https://github.com/seythegamer/linartv-launcher-seyforked/releases/latest) carry the fork's
changes; local `./gradlew :app:assembleDebug` builds are debug builds — install those over `adb`:

```text
adb connect <TV_IP>:5555
adb install -r LinarTV-<version>-debug.apk
adb shell am start -n io.github.domlin92.linartv.open/com.linart.tvpreview.DashboardActivity
```

To make it the home screen, use your TV's *Default home* / *Home screen* setting, or:

```text
adb shell cmd package set-home-activity --user 0 io.github.domlin92.linartv.open/com.linart.tvpreview.DashboardActivity
```

Enable developer options and network debugging on the TV first (Settings → Device Preferences → About →
click *Build* 7 times). On Google TV the stock launcher may keep taking over even when the command prints
`Success`; [AGENTS.md](AGENTS.md) describes that case, along with the commands to restore it.

How the app works and where the data comes from: [docs/FUNCTIONAL.md](docs/FUNCTIONAL.md).

## Credits

- Inspired by and started inside [Clean Your TV](https://cleanyour.tv/) by Gathin (MIT).
- Polish club fixtures: [kalendarzeligowe.pl](https://kalendarzeligowe.pl) — used with the site owner's permission.
- Foreign club fixtures (Top 5 leagues, domestic and European cups): match data by [fixtur.es](https://fixtur.es)
  — used under their fair-use rules; credited on every match card.
- Weather data: [Open-Meteo.com](https://open-meteo.com/) (CC BY 4.0); city search: Open-Meteo geocoding /
  GeoNames (CC BY 4.0).
- Weather icons: [Meteocons](https://github.com/basmilius/weather-icons) by Bas Milius (MIT), bundled in the app.
- Weather Icons font by Erik Flowers (SIL OFL 1.1) — used for the °C / °F sign.
- Club crests: [kalendarzeligowe.pl](https://kalendarzeligowe.pl); foreign clubs without a crest there:
  [football-logos.cc](https://football-logos.cc). Crests belong to their clubs — see [docs/CRESTS.md](docs/CRESTS.md).

## License

Code: [MIT](LICENSE). Club crests and third-party logos are not covered by the code license.
