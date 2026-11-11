# Zmiany / Changelog

Każde wydanie ma tu wpis po polsku i po angielsku. Najnowsze na górze.
Every release gets an entry here in Polish and English. Newest first.

## 1.0.3 — niepublikowane / unreleased

**Polski**

Nowe
- **Odinstalowanie aplikacji z launchera.** Przytrzymaj OK na kafelku w docku albo na liście aplikacji i wybierz „Odinstaluj aplikację” — telewizor pokazuje wtedy swoje okno odinstalowywania, więc nie trzeba wchodzić w Ustawienia telewizora. Aplikacje systemowe launcher opisuje komunikatem zamiast próbować je usuwać, a po odinstalowaniu kafelek i pozycja na liście znikają same. Krótkie naciśnięcie OK nadal otwiera aplikację — także na liście, gdzie do tej pory wystarczyło samo naciśnięcie.
- **„Zamknij aplikacje” naprawdę zamyka aplikacje.** Kafelek bierze teraz wszystkie aplikacje z docku i listy (wcześniej kilka nazw wpisanych na sztywno) i pomija tylko to, czego telewizor potrzebuje: launcher, interfejs systemowy, Ustawienia, Sklep Play, usługi Google, obsługę Chromecasta i stos telewizora. Na urządzeniu z rootem wykonuje `am force-stop`, czyli prawdziwe zamknięcie razem z usługami; bez roota prosi system o zwolnienie procesów w tle. Komunikat podaje liczbę aplikacji i ile pamięci RAM wróciło, a wskaźnik RAM odświeża się od razu. Nazwy pakietów są sprawdzane przed trafieniem do powłoki (test w `tests/AppCleanupCheck.java`).
- **Wybór ustawień pod przyciskiem „Ustawienia Android TV”.** Boksy z Androidem często mają dwa programy do ustawień: zwykłe ustawienia AOSP i panel w stylu Android TV (na tym boksie `com.android.settings` i `com.android.tv.settings`). Oba zgłaszają to samo polecenie systemowe, więc przycisk otwierał ten, który wybrał Android. Teraz **przytrzymanie OK** na przycisku pokazuje listę wszystkich programów ustawień, zaznacza aktualny i zapisuje wybór; krótkie naciśnięcie otwiera wybrany. Gdy zapisany program zniknie, launcher wraca do domyślnego. Wybór jest sprawdzany testem `tests/SettingsTargetsCheck.java`.

Ulepszone
- **Lżejsza pamięć.** Zdjęcie tapety jest skalowane dokładnie do rozmiaru ekranu (wcześniej do 2560×1440), okładka Spotify do wielkości paska, a miniatury w galerii tapet są mniejsze. Własne ikony docku mają najwyżej 384 px — starsze, 512-pikselowe, są zmniejszane przy wczytaniu. Ikony, herby i pogoda mają cache z limitem pamięci, a przy braku pamięci system zwalnia je od razu (`onTrimMemory`).
- **Płynniejsze otwieranie listy aplikacji.** Poświata fokusu jest rysowana raz na rozmiar i kolor, bez warstwy software na każdym kafelku; lista otwiera się w ok. 70 ms zamiast ok. 350 ms, a poświata nie jest już przycinana na brzegach.
- Po zamknięciu ustawień pilot działa od razu: fokus wraca na kafelek, z którego otwarto panel, także gdy panel zdążył przerysować pulpit.

Do wiadomości
- Repozytorium forka zmieniło nazwę na `linartv-launcher-seyforked` (wcześniej `linartv-launcher-open`). Aplikacja sprawdza aktualizacje w wydaniach tego forka, a nie upstreamu, więc aktualizacja nie podmieni forka na wersję oryginalną.

**English**

New
- **Uninstall an app from the launcher.** Hold OK on a dock tile or on a tile in the app list and choose "Uninstall app" — the TV then shows its own uninstall window, so the TV settings never have to be opened. System apps are explained instead of touched, and once an app is gone its tile and its place in the list disappear on their own. A short OK still opens the app, including in the list, where a single press used to be enough.
- **"Close apps" really closes apps.** The tile now takes every app from the dock and the app list (previously a handful of names typed into the code) and skips only what the TV needs: the launcher, System UI, Settings, Play Store, Google services, the Chromecast receiver and the vendor television stack. On a rooted box it runs `am force-stop`, a real force stop that also takes down services; without root it asks Android to drop each app's background processes. The toast reports the app count and how much RAM came back, and the RAM widget refreshes at once. Package names are validated before they reach a shell (`tests/AppCleanupCheck.java` covers it).
- **Choose what "Android TV settings" opens.** Boxes often ship two settings apps: plain AOSP settings and the Android-TV-style side panel (on this box `com.android.settings` and `com.android.tv.settings`). Both answer the same system intent, so the button always opened whichever Android preferred. **Holding OK** on the button now lists every settings app, ticks the current one and saves the pick; a short press opens the choice from then on. If the saved app disappears, the launcher falls back to the system default. `tests/SettingsTargetsCheck.java` covers the fallback rules.

Improved
- **Lighter memory use.** A wallpaper photo is scaled exactly to the screen (previously up to 2560×1440), the Spotify cover to the bar size, and gallery thumbnails are smaller. Custom dock icons are kept at most 384 px — older 512-pixel ones shrink when loaded. Icons, crests and weather art use byte-limited caches, and the system can drop them immediately under memory pressure (`onTrimMemory`).
- **Faster app list.** The focus aura is rendered once per size and colour instead of a software layer on every tile; the list opens in about 70 ms instead of about 350 ms, and the aura is no longer clipped at the edges.
- The remote works right after closing the settings panel: focus returns to the tile that opened it, even when the panel rebuilt the dashboard behind it.

Note
- The fork repository was renamed to `linartv-launcher-seyforked` (previously `linartv-launcher-open`). The app checks for updates in this fork's releases rather than upstream's, so an update cannot replace the fork with the original build.

## 1.0.2 — 2026-10-30

**Polski**

Nowe
- **Ligi zagraniczne Top 5:** Premier League, La Liga, Bundesliga, Serie A i Ligue 1. Terminarz łączy ligę, krajowe puchary i europejskie puchary. Dane meczów z [fixtur.es](https://fixtur.es), herby z [football-logos.cc](https://football-logos.cc).
- **Wyszukiwanie klubu po nazwie** w kreatorze — kluby polskie i zagraniczne naraz. Także **głosowo**: przycisk mikrofonu przy wyszukiwaniu klubu i miasta.
- **Języki:** niemiecki, hiszpański i włoski. Domyślnie język systemu telewizora (opcja „Systemowy”), można też wybrać ręcznie.
- **Aktualizacje w aplikacji:** gdy na GitHubie jest nowsza wersja, pojawia się pasek „Dostępna aktualizacja” — „Aktualizuj” pobiera plik i otwiera okno instalacji. Ustawienia zostają.

Ulepszone
- Karty meczu i pogody mają pasek „Dane dostarcza: …” i czas odświeżenia.
- Tytuł meczu „Gospodarz – Gość”; długie nazwy przechodzą do drugiej linii w całości. Data w dwóch liniach („Sobota, 7 listopada” / „za 12 dni”), w dniu meczu zielony znaczek DZISIAJ. Nagłówek karty: rozgrywki, a pod nimi kolejka lub etap.
- Godziny meczów w strefie czasowej telewizora.
- Wygodniejsze sterowanie pilotem: okna wyboru zaznaczają bieżące ustawienie (✓), strzałka w dół prowadzi do głównego przycisku, widać tylko najnowsze okienko, a po zapisaniu fokus wraca na użyty kafelek.
- Herby zagraniczne ładują się szybciej; drobne poprawki (podwójne wyniki wyszukiwania, inicjały bez znaków typu „&”).

Do wiadomości
- Przy pierwszym użyciu mikrofonu Android zapyta o dostęp. Przy pierwszej aktualizacji z poziomu aplikacji Android poprosi o zgodę na instalowanie z LinarTV.

Podziękowania: Otto (fixtur.es) za dane meczów i Leo (football-logos.cc) za herby.

**English**

New
- **Top 5 foreign leagues:** Premier League, La Liga, Bundesliga, Serie A and Ligue 1. One schedule with the league, domestic cups and European cups. Match data from [fixtur.es](https://fixtur.es), crests from [football-logos.cc](https://football-logos.cc).
- **Search clubs by name** in the wizard — Polish and foreign clubs at once. Also **by voice**: a microphone button next to club and city search.
- **Languages:** German, Spanish and Italian. The TV's system language by default ("System" option), or choose one yourself.
- **In-app updates:** when a newer version is on GitHub, an "Update available" bar appears — "Update" downloads the file and opens the install window. Settings are kept.

Improved
- Match and weather cards show "Data provided by: …" and the update time.
- Match title "Host – Guest"; long names move to the second line as a whole. Two-line date ("Saturday, 7 November" / "in 12 days"), a green TODAY badge on match day. Card header: competition, then round or stage.
- Kick-off times in the TV's time zone.
- Easier remote control: choice windows mark the current setting (✓), arrow down goes to the main button, only the newest window is visible, and after saving the focus returns to the tile you used.
- Foreign crests load faster; small fixes (duplicated search results, initials without characters such as "&").

Good to know
- Android asks for microphone access on first use. On the first in-app update Android asks to allow installing from LinarTV.

Thanks: Otto (fixtur.es) for match data and Leo (football-logos.cc) for the crests.

## 1.0.1 — 2026-10-26

**Polski**

Nowe
- Pogoda dla **dowolnego miasta na świecie** (Open-Meteo) zamiast tylko polskich miast z IMGW. Godziny prognozy są w czasie lokalnym wybranego miasta.
- Nowe, kolorowe ikony pogody Meteocons, wbudowane w aplikację (działają bez internetu).
- **IV liga** — wszystkie 16 grup wojewódzkich. W kreatorze: liga → „IV liga” → województwo → klub.
- **Herby wszystkich klubów z kalendarzeligowe.pl**, zawsze aktualne. Nowy herb pobiera się sam, gdy serwis go zmieni. Drużyny bez herbu mają inicjały.
- LinarTV może być **domyślnym ekranem głównym**. Wtedy otwiera się po włączeniu telewizora i po wybudzeniu z czuwania, a nie np. telewizja na żywo.
- Ikona na liście aplikacji także na zwykłym Androidzie (np. projektory jak Wanbo T2 Max).

Ulepszone
- Większe ikony i temperatury na karcie pogody. Długie nazwy miast mieszczą się w jednej lub dwóch liniach, bez ucinania.
- Plik aplikacji jest dużo mniejszy (0,7 MB zamiast 6,2 MB), bo herby nie są już w nim zapisane.

Do wiadomości
- Po aktualizacji telewizor może zapytać, która aplikacja ma być ekranem głównym. Wybierz LinarTV i „Zawsze”.

**English**

New
- Weather for **any city in the world** (Open-Meteo) instead of Polish cities from IMGW only. Forecast hours use the chosen city's local time.
- New colourful Meteocons weather icons, bundled in the app (work offline).
- **IV liga** — all 16 regional groups. In the wizard: league → "IV liga" → region → club.
- **Crests of all clubs from kalendarzeligowe.pl**, always current. A new crest downloads by itself when the site changes it. Teams without a crest show initials.
- LinarTV can be the **default home screen**. It then opens after the TV is switched on and after waking from standby, instead of e.g. live TV.
- App icon in the app list on plain Android as well (e.g. projectors such as the Wanbo T2 Max).

Improved
- Larger icons and temperatures on the weather card. Long city names fit on one or two lines, never cut off.
- The app file is much smaller (0.7 MB instead of 6.2 MB) because crests are no longer stored in it.

Good to know
- After the update the TV may ask which app should be the home screen. Choose LinarTV and "Always".

## 1.0.0 — 2026-10-25

Pierwsze wydanie: terminarz jednego lub dwóch klubów (Ekstraklasa – III liga, Puchar Polski) z herbami, pogoda IMGW dla polskiego miasta, dock aplikacji, tryb dzienny i nocny, tapety z telefonu, wygaszacz, pasek Spotify, język polski lub angielski.

First release: fixtures of one or two clubs (Ekstraklasa – III liga, Polish Cup) with crests, IMGW weather for a Polish city, app dock, day and night modes, wallpapers from your phone, screensaver, Spotify bar, Polish or English.
