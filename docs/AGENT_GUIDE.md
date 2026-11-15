# LinarTV Open — Engineering Guide for AI Agents

> **PL w skrócie:** To wewnętrzny przewodnik po kodzie LinarTV dla asystentów AI. Obowiązują
> zasady z [`AGENTS.md`](../AGENTS.md): czysta Java (bez Kotlina i Compose), dokumentacja
> dwujęzyczna, sterowanie pilotem, źródła danych tylko zatwierdzone, zero sekretów w repo.
> Przed zmianą kodu przeczytaj sekcje 4, 6, 7 i 13–15.

This file maps the repository for coding agents. It is written in English because it describes
code, not the product UI. It complements, and never overrides, [`AGENTS.md`](../AGENTS.md).
Verified against the working tree on 2026-11-14 (`versionName 1.0.2`, `versionCode 3`).

---

## Contents

1. [Project snapshot](#1-project-snapshot)
2. [Ground rules](#2-ground-rules)
3. [Repository map](#3-repository-map)
4. [Build, test, release](#4-build-test-release)
5. [Runtime architecture](#5-runtime-architecture)
6. [UI, layout and focus model](#6-ui-layout-and-focus-model)
7. [Data layer and cache TTLs](#7-data-layer-and-cache-ttls)
8. [On-device storage](#8-on-device-storage)
9. [Feature flows](#9-feature-flows)
10. [Internationalization](#10-internationalization)
11. [Testing](#11-testing)
12. [Security and privacy invariants](#12-security-and-privacy-invariants)
13. [Known drift and traps](#13-known-drift-and-traps)
14. [Change recipes](#14-change-recipes)
15. [Pre-flight checklist](#15-pre-flight-checklist)
16. [Documentation index](#16-documentation-index)

---

## 1. Project snapshot

LinarTV Open is an Android TV **home screen (launcher)** for football fans. One activity shows a
clock, Open-Meteo weather for a chosen city, the next matches of one or two clubs, a Spotify
"now playing" bar, a dock of apps, wallpapers, a screensaver and in-app updates.

| Item | Value |
| --- | --- |
| Language | Plain Java 17. **No Kotlin, no Compose, no XML layouts.** |
| Module | Single `:app` module |
| Application ID | `io.github.domlin92.linartv.open` (intentionally unchanged in this fork) |
| Java package / namespace | `com.linart.tvpreview` |
| SDK | `compileSdk 34`, `minSdk 26`, `targetSdk 28` (**deliberate**, see below) |
| Current version | `versionName 1.0.2`, `versionCode 3`; CHANGELOG has unreleased `1.0.3` |
| Dependencies | `com.caverock:androidsvg-aar:1.4`, `com.google.zxing:core:3.5.4` |
| Build stack | Gradle wrapper 8.9, Android Gradle Plugin 8.5.2, JDK 17 |
| Update source | Releases of the **fork** `seythegamer/linartv-launcher-seyforked` |

**Fork context.** This repository is a fork of `domlin92/linartv-launcher-open`, maintained as
`seythegamer/linartv-launcher-seyforked`. The package name, upstream docs and credits are kept
for compatibility. `AppUpdates.LATEST` points at the fork's releases so an update can never
replace the fork with an upstream build. README lists what the fork adds (custom dock icons,
RAM widget, weather wallpapers, faster drawer, uninstall from launcher, real "Close apps", etc.).

**Why `targetSdk 28`.** Raising it changes legacy storage and package-visibility behaviour that
this launcher relies on (external Download folder picker, `queries`, `REQUEST_DELETE_PACKAGES`
handling on old TVs). The lint check `ExpiredTargetSdkVersion` is intentionally disabled. Do not
bump it casually; treat it as a migration project.

**One activity.** `DashboardActivity` is about 6.2k lines and owns nearly all UI, settings,
network orchestration, wallpapers and screensavers. Small pure-Java helpers live beside it and
are unit-testable without Android.

---

## 2. Ground rules

These come from [`AGENTS.md`](../AGENTS.md); repeat them in your reasoning before changing code.

- **Java only.** Do not introduce Kotlin, Compose, XML layouts, fragments, ViewModels, Room,
  Retrofit, Hilt or new dependencies without the maintainer's consent.
- **Remote control first.** Every interactive view must be focusable and reachable with the
  D-pad. Back closes the topmost dialog first. Focus must return somewhere sensible after a
  rebuild.
- **Bilingual documentation.** README, CHANGELOG and `docs/*.md` carry Polish **and** English.
  Update both. Code comments and this guide are English.
- **Approved data sources only.** kalendarzeligowe.pl, fixtur.es iCal feeds, Open-Meteo.
  Do not add sources, scrape around reCAPTCHA, or ignore `robots.txt`. Weather icons are bundled
  Meteocons (MIT). Respect the fixtur.es fair-use rules (credit, ≤ 6 h per feed, User-Agent,
  `If-Modified-Since`, no resale).
- **Crests are third-party marks.** Never commit crest image files. The app downloads them on
  the TV and caches them; football-logos.cc is credited in README, `docs/CRESTS.md` and About.
- **No secrets.** No keystores, passwords, private IPs, personal paths or photos. Run
  `python scripts/audit_public.py` before any push.
- **Versioning.** Each published build increments `versionCode` by 1 and sets a semver
  `versionName` in `app/build.gradle`; tag `vX.Y.Z`; add a bilingual CHANGELOG entry.
- **No automatic commits, pushes, or releases.** Do not commit, push, tag, or release on your
  own. Every commit, push, tag, or release must be approved by the user first. Every published
  release bumps `versionCode` by 1 and sets a semantic `versionName` in `app/build.gradle`;
  tag releases `vX.Y.Z`. See [`docs/RELEASING.md`](RELEASING.md).

---

## 3. Repository map

```
AGENTS.md                  Agent instructions (Codex/Claude auto-read); CLAUDE.md imports it
CLAUDE.md                  One line: @AGENTS.md
README.md                  Fork description, fork changes, install links, credits
CHANGELOG.md               Bilingual release notes, newest first
LICENSE                    MIT (upstream)
app/
  build.gradle             SDK levels, version, signing, APK naming, lint config
  src/main/
    AndroidManifest.xml    Permissions, HOME/LAUNCHER/LEANBACK filters, services, provider
    assets/i18n/           de.json, es.json, it.json (280 keys each)
    assets/meteocons/      24 bundled weather SVG/PNG icons
    assets/fixtures-teams.json   Bundled Top-5 club list generated by a script
    res/drawable/          Weather wallpaper templates, phone-add icon
    res/drawable-nodpi/    Launcher icon, banner, toolbar/status icons
    java/com/linart/tvpreview/
      DashboardActivity.java   Single activity: UI, settings, refresh orchestration
      AppUpdates.java          GitHub Releases check + APK download intent
      AppCleanup.java          "Close apps" package allow/deny logic (pure)
      BlurPixels.java          Dialog backdrop blur (pure)
      BootReceiver.java        Boot / package-replaced handling when default home
      WakeService.java         Foreground screen-on service that reopens the dashboard
      CalendarScope.java       Which calendar URLs are accepted (pure)
      ClubDirectory.java       Club search list (PL API + bundled foreign list)
      ClubNames.java           Accent/case-insensitive name normalization (pure)
      CrestNameMatcher.java    Fallback crest matching by name (pure)
      CrestRepository.java     Crest download/cache (kalendarzeligowe + football-logos.cc)
      FixturesClient.java      fixtur.es iCal feeds and foreign crest page scraping
      IcsMatchParser.java      iCal and legacy HTML match parsing
      LanguageChoice.java      System-language resolution (pure)
      LauncherTiming.java      Day/night + screensaver timing math (pure)
      LeagueCatalogClient.java kalendarzeligowe.pl API client and cache format
      MatchCompetition.java    Competition name detection (pure)
      MeteoconIcons.java       Byte-budgeted Meteocons cache
      NowPlayingTicker.java    Marquee text for the Spotify bar
      PhotoUploadServer.java   PIN-protected LAN HTTP photo receiver
      PublicForecastClient.java Open-Meteo forecast + geocoding
      SettingsTargets.java     Picks among installed Settings apps (pure)
      SpotifyNowPlaying.java   MediaSession state for the Spotify bar
      SpotifySessionListener.java NotificationListenerService
      Translations.java        Per-language catalogue, English as fallback key
      UpdateFileProvider.java  FileProvider for the downloaded update APK
      WeatherWallpaper.java    WMO code → wallpaper kind (pure)
docs/                      Bilingual user/maintainer documentation (see §16)
scripts/                   Build, release, test, audit, asset-refresh helpers
tests/                     10 plain-Java `main()` checks (no JUnit, no Android)
tmp/                       Ignored local dumps / compiled checks
```

Stray root files `CrestRepository.java` and `OpenFootballClient.java` are zero-byte leftovers.
Do not treat them as sources; the real classes are under `app/src/main/java/...`.

---

## 4. Build, test, release

Run from the repository root on Windows unless noted otherwise.

```powershell
# Logic checks (no Android SDK required, needs JDK 17 in JAVA_HOME)
.\scripts\test-local.ps1

# Debug APK
.\gradlew.bat :app:assembleDebug
# → app/build/outputs/apk/debug/LinarTV-<versionName>-debug.apk

# Release APK (needs keystore.properties + ANDROID_HOME)
powershell -ExecutionPolicy Bypass -File scripts\build-release.ps1
# → dist\LinarTV.apk
```

- `scripts/test-local.ps1` compiles each pure class **explicitly** with `javac`, runs its
  `tests/*Check.java`, then runs `python scripts/check-translations.py`. **If you add a pure
  class and a check, you must add both to this script** or it will not run in CI-less local
  testing.
- There is no `.github/` directory and no CI pipeline. Local scripts are the only gate.
- Release signing is optional at build time: `app/build.gradle` loads `keystore.properties` if
  present. Never commit that file or `*.jks`.
- APK output naming is `LinarTV-${versionName}-${buildType}.apk`; published releases always
  attach the file as `LinarTV.apk` because the update URL depends on that name.
- `scripts/update-fixtures-teams.py` refreshes the bundled foreign club list (5 page requests,
  once per season). It does not run at app runtime.
- `scripts/measure-popups.ps1` measures drawer/popup timing on a connected device via adb.
- `scripts/audit_public.py` scans tracked text files for credentials, private IPs and personal
  paths. It is a safety net, not a guarantee.
- `scripts/create-keystore.ps1` creates the release key outside the repo
  (`%USERPROFILE%\.linartv`).

**Environment caveat:** if `java`, `JAVA_HOME`, `ANDROID_HOME`, `adb` or `git` are missing, say
so explicitly and do not claim checks passed. You can still run
`python scripts/check-translations.py` and `python scripts/audit_public.py` if Python exists.

---

## 5. Runtime architecture

### Entry points

| Component | Trigger | Behaviour |
| --- | --- | --- |
| `DashboardActivity` | HOME / LAUNCHER / LEANBACK_LAUNCHER | Builds the whole screen in `onCreate` → `buildScreen()` |
| `BootReceiver` | `BOOT_COMPLETED`, `MY_PACKAGE_REPLACED` | **Only if LinarTV is the default home.** Boot starts `WakeService` and schedules the dashboard after ~12 s; package-replaced only restarts `WakeService` |
| `WakeService` | `SCREEN_ON` | Foreground service; after 3.5 s reopens the dashboard if LinarTV is still default home, then stops itself |
| `SpotifySessionListener` | Notification-listener binding | Exposes Spotify MediaSession state to the dashboard |
| `UpdateFileProvider` | Update install flow | Grants a content URI for the downloaded APK |

Because boot/wake flows check `BootReceiver.isDefaultHome()`, they do nothing when the user has
only installed LinarTV as an ordinary app. Keep this guard.

### Screen lifecycle and rebuild model

- `buildScreen()` **destroys and recreates the entire view tree** (`setContentView(root)`), then
  restores drawer/screensaver/focus state where possible. Any new state that must survive a
  rebuild belongs in fields or SharedPreferences, not only in views.
- If a dialog is open (`popupOpen == true`), `buildScreen()` sets `rebuildAfterPopup = true` and
  returns. The rebuild happens when the last dialog closes. This prevents the dashboard from
  changing under an open popup. Preserve this pattern for any new asynchronous refresh.
- Popups are `android.app.Dialog` instances with a shared blurred backdrop. Nested dialogs
  increment `popupDepth`; Back must close the innermost one first.
- `onCreate` loads cached state from `getPreferences(MODE_PRIVATE)`, starts background refresh
  threads, and schedules the clock tick and screensaver timer.
- `onPause`/`onDestroy` remove handler callbacks and close `PhotoUploadServer`.

### Threading

- Main thread: all view work, using `Handler(Looper.getMainLooper())`.
- Background: ad-hoc `new Thread(() -> ...)` for weather, fixtures and directory loads; results
  are posted back with `handler.post(...)`.
- `CrestRepository` owns its own worker executor.
- `ClubDirectory.polish()` uses a fixed pool of 4 for league requests.
- Network calls have explicit connect/read timeouts (usually 5–15 s). Keep timeouts and always
  fall back to cached data on failure.
- Never touch views off the main thread. Never block the main thread on network or disk.

### Memory model

- `BitmapCache` is a small LRU keyed by string with a byte budget: `logoCache` 4 MB,
  `customIconCache` 8 MB; `MeteoconIcons` has a 2 MB byte-budgeted cache.
- Evicted bitmaps are **not recycled** (an `ImageView` may still show them); let GC handle it.
- `onTrimMemory` / `onLowMemory` release caches, and dialog blur bitmaps are freed on close.
- Wallpaper photos are decoded near screen size (`inSampleSize`) rather than full resolution.

---

## 6. UI, layout and focus model

### Programmatic layout

- There are **no XML layouts and no `res/values`**. Everything is built in Java with
  `FrameLayout`/`LinearLayout`, `TextView`, `ImageView`, custom `View` subclasses and
  `GradientDrawable` backgrounds.
- The design canvas is **1672 × 941** (`W`, `H`). `watchFit(root)` computes
  `scale = min(width / 1672, height / 941)`; `place(view, x, y, w, h)` and `px(...)` convert
  design units to pixels. Always lay out in design units, never raw pixels.
- Cards sit at `DASHBOARD_CARD_Y = 328` with `CARD_H = 298`; the single-column layout uses
  `SINGLE_COLUMN_X = 1114`. Match cards use `CREST_SIZE = 118`.
- The dock is 4 tiles (`DOCK_SIZE = 4`), `DOCK_TILE_WIDTH = 340`, `DOCK_GAP = 66`.
- Text helpers (`text(...)`, `detailLine(...)`, etc.) encapsulate colour, weight, position and
  ellipsizing. Prefer them over ad-hoc `TextView` setup.

### Theme and appearance

- `night` is computed by `isNight()` from the appearance mode:
  `auto` (schedule), `light`, `dark`, or `custom` hours (default 08:00/20:00, equal hours
  rejected). Persisted as `appearance_mode` and custom-hour keys.
- Palette fields `ink`, `muted`, `panel`, `outline` are set at the top of `buildScreen()`.
- Tile colours: `TILE_BG`, `TILE_FOCUS_BG`, `FOCUS_RING`. Weather wallpapers and the rest of
  the UI share the same day/night decision.
- When adding a colour, derive it from the existing palette so light/dark both work.

### Focus and D-pad

- Focus is managed manually: views are `setFocusable(true)`, focus changes animate a glow/scale,
  and `onKeyDown` handles arrows, OK, Back and long-press.
- The app drawer maps focus back to the dock: Down from tile N enters the list at tile N; Down
  out of column N returns to dock tile N; Back/Up returns to the tile that opened the drawer
  (`drawerReturnColumn`).
- `suspendedFocusables` temporarily disables dashboard focus while a popup owns the screen.
- Long-press OK opens context menus (dock tile: Move / Swap / Remove / Uninstall; app list:
  app menu). Short OK opens the app.
- `popupReturnFocusKey` / `focusKey(...)` restore focus after a dialog rebuild. Reuse this
  mechanism instead of focusing the first view.
- New interactive elements **must** be reachable in a sensible arrow order and must not steal
  focus from the dashboard after a rebuild.

---

## 7. Data layer and cache TTLs

All network access is best-effort with cache fallback. Never show a blocking spinner; render
cached content and update when fresh data arrives.

| Data | Client / endpoint | Cache location | Refresh policy |
| --- | --- | --- | --- |
| Weather forecast | `PublicForecastClient` → `api.open-meteo.com/v1/forecast` | `weather_json` in activity prefs | 30 min; retry no sooner than 5 min after a failure |
| City geocoding | `PublicForecastClient` → `geocoding-api.open-meteo.com/v1/search` | `city_name`, `city_lat`, `city_lon` in prefs | On city change |
| Polish fixtures | `LeagueCatalogClient` → `https://kalendarzeligowe.pl/api/` | `*_calendar_matches` JSON in prefs (`CACHE_FORMAT = 5`) | 24 h; retry 30 min; parse lookback 26 h |
| Foreign fixtures | `FixturesClient` → `https://ics.fixtur.es/v2/<slug>.ics` | `cacheDir/fixtures-<key>.ics` body, timestamps in `fixtures_feeds` prefs | **Minimum 6 h**; `If-Modified-Since`; keep last body on failure |
| Polish crests | `CrestRepository` → `/api/teams/{id}/logo` | `files/crests-by-team/team-<id>.img` | Redownload when `crestVer` changes, else 7 days; retry 5 min; missing/404 12 h |
| Foreign crests | `CrestRepository`/`FixturesClient` → football-logos.cc pages and images | `files/football-logos-<country>.json`, `files/crests-foreign/<hash>.png` | Pages 7 days; images hash-addressed and effectively permanent |
| Club search list | `ClubDirectory` (PL API + bundled `fixtures-teams.json`) | `files/club-directory-pl.json` | 7 days |
| App update | `AppUpdates` → `api.github.com/repos/seythegamer/linartv-launcher-seyforked/releases/latest` | `app_updates` prefs | Once per day; "Later" hides for 24 h |

Notes:

- Foreign fixtures use iCal, **not** the old HTML/ICS scraping path. `IcsMatchParser` still
  contains a legacy `ROUND` regex, but current foreign data comes from iCal and Polish data
  from JSON; do not build new features on the legacy branch.
- Match visibility: a match stays on the card until `MATCH_VISIBLE_AFTER_KICKOFF_MS`
  (105 min) after kickoff. `scheduleNextMatchBoundary()` refreshes the card at kickoff/end
  boundaries instead of polling.
- Temperature unit is stored as `temperature_unit` (`c`/`f`).
- Weather/forecast caches are invalidated when the icon format or schema changes; bump the cache
  format key instead of silently misreading old JSON.

---

## 8. On-device storage

### SharedPreferences

Most user settings use `getPreferences(MODE_PRIVATE)`, which is **scoped to the activity
class name**. Renaming or moving `DashboardActivity` would orphan every user setting. If a
rename is ever unavoidable, add a migration that reads the old file name.

| Preference file | Owner | Contents |
| --- | --- | --- |
| activity prefs (`DashboardActivity.xml`) | `DashboardActivity` | clubs/leagues/URLs, weather city/coords/unit, language, appearance, wallpapers, dock order/icons, screensaver, setup flags, cached weather/calendar JSON, visibility toggles, `settings_target`, update-test version |
| `app_updates` | `AppUpdates` | `last_check`, `later_version`, `later_until`, `update_test_version` |
| `club_crests` | `CrestRepository` | per-team download time/version |
| `fixtures_feeds` | `FixturesClient` | per-feed fetch time, `Last-Modified`, legacy body copies |

### Files

| Path | Contents |
| --- | --- |
| `files/club-directory-pl.json` | Polish club list (7-day cache) |
| `files/football-logos-<country>.json` | Scraped football-logos.cc page index |
| `files/crests-by-team/team-<id>.img` | Polish crest bitmaps |
| `files/crests-foreign/<hash>.png` | Foreign crest bitmaps |
| `files/custom_icons/<package>.png` | Custom dock icons (≤ 384 px) |
| `files/custom-wallpaper.img` | Legacy single custom wallpaper (migrated/removed) |
| `getExternalFilesDir("wallpapers")` | Wallpaper gallery photos (fallback: `files/wallpapers`) |
| `cacheDir/fixtures-<key>.ics` | Foreign iCal bodies |

Temporary writes use a `.tmp` sibling + atomic move where it matters (crests).

---

## 9. Feature flows

### First run and clubs

- If no club is configured, the dashboard shows a setup card instead of a match card.
- `showTeamSetup()` / the club wizard asks for country → league → club and can configure one or
  two clubs. Search matches names case- and accent-insensitively via `ClubNames.normalize`.
- Voice search uses `RECORD_AUDIO`, requested only when the microphone button is pressed.
- Saved club state includes name, league, league slug and calendar URL. `CalendarScope.accepts`
  validates URLs; do not bypass it.

### Settings

- The toolbar opens a side panel (`renderPublicSettings`), with sub-panels for weather, calendar,
  language, appearance, wallpapers, screensaver, Spotify, visibility, updates, About and phone
  upload.
- Each saved setting rebuilds the panel, preserving focus by key. Changes that affect the
  dashboard are deferred while a popup is open (`rebuildAfterPopup`).
- "Settings" can target either AOSP or Android TV settings; `SettingsTargets` chooses among
  installed candidates, long-press OK lets the user pick, and a missing target falls back to the
  default.

### App dock and drawer

- Dock order is persisted (`dock_apps`), limited to installed apps, and defaults to
  `POPULAR_DOCK_PACKAGES` filtered by installation.
- Dock icons can be replaced with images from the Download folder (PNG/JPG/JPEG/WEBP/SVG,
  SVG rendered with AndroidSVG), stored as PNG at ≤ 384 px in `files/custom_icons`.
- `HIDDEN_APP_PACKAGES` excludes TV system entries that are not real apps.
- The app drawer builds rows lazily; keep the focus-to-dock mapping when changing it.
- The dock and the drawer ask for both `CATEGORY_LEANBACK_LAUNCHER` and `CATEGORY_LAUNCHER`: TV-only
  apps declare just the first. The rows are cached, then rebuilt when the launchable package set
  differs (checked on resume, so installs made while the launcher was paused still appear). Package
  broadcasts must be registered with `addDataScheme("package")`, otherwise they never match.
- Long-press OK offers Move / Swap / Remove; uninstall hands the package to Android's own
  uninstall window (`pendingUninstall`, re-checked on resume). System apps must be explained,
  not silently offered.

### "Close apps"

- `AppCleanup` defines protected packages (launcher, system UI, Settings, Play Store, Google
  services, Chromecast/TV stack, `org.droidtv.*`, always-on VPN) and validates package names.
- With root (`su` found in `SU_PATHS`) it runs `am force-stop` for dock/list apps; without root
  it asks the system to release background processes. Never claim memory was reclaimed.
- `cleanupRunning` prevents concurrent runs; refresh the RAM meter afterwards.

### Wallpapers and screensaver

- Gallery wallpapers are `sunset`, `aurora`, weather-following, or a user photo.
- "Wallpaper follows the weather" maps WMO codes to seven kinds (clear, partly cloudy, cloudy,
  rain, snow, thunderstorm, fog) with day/night variants from `res/drawable/weather_*_*.xml`.
  `WeatherWallpaper` is pure and unit-tested. Picking a photo disables weather-following but
  keeps the photo.
- Phone upload: `PhotoUploadServer` listens on port **8765**, shows a 6-digit PIN and a QR code,
  accepts up to 25 MB, locks after 5 wrong PINs, and runs only while its dialog is open. It
  listens on port 8765 and displays the TV's LAN URL; `usesCleartextTraffic` exists for this page.
- Screensaver: Off, preset times, or custom 1–1440 minutes; slideshow advances every 30 s;
  idle timing is computed by `LauncherTiming` (pure).

### Spotify

- `SpotifySessionListener` is a notification listener; the dashboard reads only MediaSessions
  from `com.spotify.tv.android` and `com.spotify.music`.
- No Spotify account, polling or extra network request. If permission is missing, Settings shows
  the system notification-access screen.
- `NowPlayingTicker` scrolls the title; updates for the same track must not reset the animation.

### Updates

- `AppUpdates` checks once per day, only for package `io.github.domlin92.linartv.open`.
- Release (non-debuggable) builds only, **or** a debuggable build with the
  `update_test_version` preference. Debug builds normally never show the bar because Android
  rejects an APK signed with another key.
- "Later" hides the bar for that version for 24 h. Download goes to cache and is opened through
  `UpdateFileProvider` and Android's install window. Never install silently.

### Boot and wake

- Only when LinarTV is the resolved HOME app. Android TV may start its own TV source first, so
  boot uses an `AlarmManager` delay (~12 s). Wake uses a 3.5 s delay after `SCREEN_ON`.
- Do not add background polling loops; the activity owns refresh timers.

---

## 10. Internationalization

- UI languages: Polish, English, German, Spanish, Italian, plus "system".
- **Polish and English strings live inline in Java**, passed to
  `tr("Polski tekst", "English text")`.
- **English is the lookup key** for the other languages. `Translations` loads
  `assets/i18n/<code>.json` only for `de`, `es`, `it`; `pl` returns the Polish argument and
  unknown codes fall back to English.
- Every `tr(...)` call's English argument must exist in all three JSON catalogues with the same
  set of keys (currently 280). `scripts/check-translations.py` extracts literal and
  string-concatenated English arguments and fails on missing/extra/empty/duplicate keys and on
  the replacement character `\ufffd`.
- Translation keys ending in a space must have values ending in a space (separator convention).
- Language changes apply immediately to the dashboard and an open settings panel. The explicit
  choice is stored as `language` and survives TV language changes; `LanguageChoice` resolves
  `auto` against the system locale, defaulting to English.

Adding a string: call `tr("...", "...")` in code **and** add the English key with DE/ES/IT
values, then run `python scripts/check-translations.py`.

---

## 11. Testing

### What exists

`tests/` contains 10 plain-Java programs with `main()`. No JUnit, no Robolectric, no Android
emulator in the loop. They cover pure logic only:

| Check | Class under test |
| --- | --- |
| `LauncherTimingCheck` | day/night schedule, screensaver timeout (incl. across midnight) |
| `AppCleanupCheck` | protected packages and package-name validation |
| `SettingsTargetsCheck` | Settings app selection/fallback |
| `BlurPixelsCheck` | pixel-exact blur vs. previous algorithm |
| `MatchCompetitionCheck` | competition detection |
| `CalendarScopeCheck` | accepted calendar URLs |
| `ClubNamesCheck` | accent/case normalization |
| `CrestNameMatcherCheck` | crest name matching fallback |
| `LanguageChoiceCheck` | language resolution/fallback |
| `WeatherWallpaperCheck` | WMO → wallpaper kind mapping |

### What is not covered

`DashboardActivity`, all views, D-pad navigation, dialogs, network clients, `AppUpdates`,
`PhotoUploadServer`, Spotify integration and persistence have **no automated tests**. Changes
there need a build plus a manual/emulator checklist (`docs/LOCAL_TESTING.md`).

### Adding a pure check

1. Put logic that does not import `android.*` in a small class (package
   `com.linart.tvpreview`, package-private static methods).
2. Add `tests/<Name>Check.java` with `main()`, assertions and a non-zero exit on failure.
3. Add the `javac` + `java` pair to `scripts/test-local.ps1` **in the same style as the others**
   (UTF-8, output to `tmp/local-tests`).
4. Update `docs/LOCAL_TESTING.md` (PL + EN).

Keep tests deterministic: no network, no clock dependence beyond injected values.

---

## 12. Security and privacy invariants

- **No secrets or personal data in the repo.** `scripts/audit_public.py` blocks private IPs,
  embedded credentials, `C:\Users\...` paths, room names and ignored artifacts. It only scans
  text suffixes; review images manually.
- **No crest or wallpaper binaries committed.** `app/src/main/res/drawable-nodpi/crest_*` is
  git-ignored, as are `wallpapers/`, `dist/`, keystores and `keystore.properties`.
- **Photo upload is LAN-only and short-lived.** Do not expose it on all interfaces longer than
  the dialog, weaken PIN checks, raise the 25 MB limit, or log the PIN. It closes on pause.
- **Shell safety.** `AppCleanup` validates package names before building any `su`/`am` command.
  Never interpolate unvalidated user/network strings into a shell command.
- **Update safety.** Only the fork's GitHub release asset named `LinarTV.apk` is offered. Keep
  the applicationId and release-build gate; Android verifies signatures on install.
- **Network etiquette.** Send a descriptive `User-Agent` (already includes the project URL),
  respect `If-Modified-Since`, do not retry aggressively, do not scrape around anti-bot pages.
- **Permissions.** Ask for `RECORD_AUDIO` and storage only when the feature is used. Do not add
  permissions without a matching feature and explanation.
- **Logging.** `android.util.Log` messages must not contain pins, coordinates of private homes,
  URLs with secrets or user file paths.

---

## 13. Known drift and traps

These are real inconsistencies in the current tree. Fix them when you touch the area, but do not
"fix" them by removing working features.

1. **Stale class Javadoc.** `DashboardActivity` still says it "intentionally does not claim the
   Android HOME intent". The manifest **does** declare a HOME filter, and the app is designed to
   be a launcher. Update the comment, not the manifest.
2. **FUNCTIONAL.md version.** The header says `1.0.1` while `app/build.gradle` is `1.0.2` and
   CHANGELOG has unreleased `1.0.3`. Update docs when releasing.
3. **ROADMAP drift.** `docs/ROADMAP.md` may claim fixtur.es support was removed or that foreign
   clubs show initials. The code supports fixtur.es and football-logos.cc crests. Verify before
   repeating roadmap statements as current behaviour.
4. **Legacy ICS round regex.** `IcsMatchParser.ROUND` expects `Kolejka – Kolejka 12` (the word
   appears twice) and likely never matches. Rounds for the current API come from JSON via
   `MatchCompetition`; do not depend on the legacy regex.
5. **`getPreferences` is activity-name-scoped.** Renaming/moving `DashboardActivity` orphans
   settings. Migrate or keep the class name.
6. **Two `SharedPreferences` worlds.** Only the activity prefs are user settings; `app_updates`,
   `club_crests` and `fixtures_feeds` are caches/state. Do not move user settings into cache
   files without migration.
7. **`targetSdk 28` is intentional.** A bump changes storage/package visibility and can break
   old TVs. The lint suppression is deliberate.
8. **Fork vs upstream.** README/AGENTS mention upstream GitHub Releases, but `AppUpdates`
   checks the fork `seythegamer/linartv-launcher-seyforked`. Keep them consistent when editing
   update docs.
9. **Zero-byte root files.** `CrestRepository.java` and `OpenFootballClient.java` at the repo
   root are leftovers. The real classes are in the Java source tree; do not compile the root
   files.
10. **No CI.** A green local run is the only signal. `scripts/test-local.ps1` must list every
    check; adding a test file alone does nothing.
11. **Popup rebuilds.** Calling `buildScreen()` while a popup is open intentionally defers the
    rebuild. If you bypass it, you can detach views a dialog still references.
12. **`usesCleartextTraffic="true"`** exists for the LAN photo page. Do not remove it without
    replacing that flow; do not use it as a reason to add plain-HTTP internet calls.

---

## 14. Change recipes

### Add a setting

1. Choose a stable preference key and a default. Use `getPreferences(MODE_PRIVATE)` for user
   settings.
2. Add the UI tile/toggle in `renderPublicSettings` (or the relevant sub-panel), using `tr(...)`.
3. Apply the change immediately; if the dashboard must rebuild, call `buildScreen()` (it defers
   while a popup is open) and restore focus.
4. Add DE/ES/IT translations and run `check-translations.py`.
5. Check light/dark colours and D-pad reachability.
6. Update `docs/FUNCTIONAL.md` (PL + EN).

### Add or change a data refresh

1. Reuse the existing pattern: `loading` flag, `attemptedAt`, `updatedAt`, cached body, TTL,
   retry backoff, cache fallback, `handler.post` to the UI.
2. Keep the documented TTLs/fair-use rules. Do not lower the 6 h fixtur.es interval.
3. Render cached data first; never block the dashboard.
4. Invalidate old cache formats by bumping a format field, not by assuming compatibility.
5. If the refresh happens while a dialog is open, let `buildScreen()` defer.

### Add a translation

1. `tr("polski", "English")` in code.
2. Add the English key to `de.json`, `es.json`, `it.json` (UTF-8, no `\ufffd`).
3. Run `python scripts/check-translations.py`; it must report 280+ keys covered for all three.

### Touch the dock or drawer

1. Preserve `dockPackages` order and the focus mapping (`drawerReturnColumn`, Down/Up/Back).
2. Keep dock tiles limited to installed apps and `HIDDEN_APP_PACKAGES` exclusions.
3. Long-press menu actions must remain reachable; do not break short-OK launch.
4. Verify on a device/emulator: focus ring, move/swap/remove, uninstall return, drawer blur.

### Add a pure helper

Follow §11 and add both the class and the check to `scripts/test-local.ps1`.

### Bump a version

1. `versionCode += 1`, semver `versionName`.
2. Add a bilingual CHANGELOG section; use it as the release description.
3. `scripts/build-release.ps1`; tag `vX.Y.Z`; attach `dist/LinarTV.apk` as `LinarTV.apk`.
4. Update `docs/FUNCTIONAL.md` version header and any version references.

---

## 15. Pre-flight checklist

Before proposing or committing a change:

- [ ] `.\scripts\test-local.ps1` passes (or its individual `javac`/`java` commands).
- [ ] `python scripts/check-translations.py` passes if any `tr(...)` string changed.
- [ ] `.\gradlew.bat :app:assembleDebug` compiles.
- [ ] New views are focusable, reachable with arrows, and Back closes popups first.
- [ ] Light and dark appearance both look correct; weather wallpaper day/night still matches.
- [ ] Cached data still renders when the network is unavailable.
- [ ] No new data source, dependency, permission or crest binary was added casually.
- [ ] `python scripts/audit_public.py` passes before any push.
- [ ] Documentation updated in **both** Polish and English.
- [ ] Version/CHANGELOG updated only for an actual release.
- [ ] If a check could not run (missing JDK/SDK/adb), state that explicitly.

---

## 16. Documentation index

| File | Read it for |
| --- | --- |
| [`AGENTS.md`](../AGENTS.md) | Binding agent rules, install-on-TV procedure, data-source policy |
| [`README.md`](../README.md) | Fork story, fork-only features, credits, install links |
| [`CHANGELOG.md`](../CHANGELOG.md) | What changed per release (PL + EN); unreleased 1.0.3 |
| [`docs/FUNCTIONAL.md`](FUNCTIONAL.md) | Feature-by-feature behaviour and data-flow diagrams |
| [`docs/LOCAL_TESTING.md`](LOCAL_TESTING.md) | Local checks, emulator setup, manual release checklist |
| [`docs/RELEASING.md`](RELEASING.md) | Signing key, release build, GitHub release steps |
| [`docs/CRESTS.md`](CRESTS.md) | Crest sources, caching, attribution requirements |
| [`docs/SPOTIFY.md`](SPOTIFY.md) | Notification access and now-playing behaviour |
| [`docs/WEATHER-WALLPAPERS.md`](WEATHER-WALLPAPERS.md) | Weather wallpaper templates and editing |
| [`docs/TRANSLATIONS.md`](TRANSLATIONS.md) | Language catalogue rules and coverage checks |
| [`docs/SECURITY.md`](SECURITY.md) | Secret handling and public-repo audit |
| [`docs/ROADMAP.md`](ROADMAP.md) | Planned work (verify against code before trusting) |

When in doubt: **read the code path, not only the docs**, because the docs occasionally lag the
fork. The fastest orientation is `DashboardActivity.buildScreen()`, then `showPublicSettings()`,
then the relevant client under `app/src/main/java/com/linart/tvpreview/`.