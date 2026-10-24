# Nametag Customizer 2.0 (Fabric, Minecraft 1.21.8)

Klient-only mod do zmiany wyglądu nametagów. Klawisz otwierania ustawień: **N** (można zmienić w Sterowaniu).

## Jak zbudować moda na GitHubie (bez instalowania czegokolwiek)

1. Załóż konto na github.com i kliknij **New repository** (nazwa dowolna, np. `nametag-mod`, Public).
2. Na stronie nowego repozytorium kliknij **uploading an existing file**.
3. Rozpakuj ZIP-a i przeciągnij **całą zawartość** (foldery `.github` i `src` oraz pliki `build.gradle`,
   `settings.gradle`, `gradle.properties` itd.) do okna GitHuba. Kliknij **Commit changes**.
4. Wejdź w zakładkę **Actions** - budowanie ruszy samo (ok. 3-5 min). Zielony znaczek = gotowe.
5. Kliknij ukończone uruchomienie, na dole w sekcji **Artifacts** pobierz `nametag-customizer` (ZIP z jarem).
6. Jara wrzuć do folderu `mods` (Fabric Loader + Fabric API 1.21.8).

Jeśli build jest czerwony: kliknij go, otwórz krok **Build** i skopiuj czerwone linie błędu - wyślij je do Claude'a.
