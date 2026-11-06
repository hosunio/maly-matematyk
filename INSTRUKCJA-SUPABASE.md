# Podłączenie panelu Mały Matematyk do Supabase

## A. Utwórz konto administratora
1. W Supabase otwórz Authentication → Users.
2. Kliknij Add user / Create new user.
3. Podaj swój adres e-mail i ustaw silne hasło.
4. Utwórz użytkownika i skopiuj jego User ID (UUID).

## B. Dodaj administratora i polityki bazy
1. Otwórz SQL Editor → New query.
2. Wklej całą zawartość pliku `SUPABASE-KONFIGURACJA.sql`.
3. Kliknij Run.
4. W tym samym SQL Editor uruchom osobne polecenie, zastępując UUID swoim User ID:
   `insert into public.admin_users (user_id) values ('TU_WKLEJ_USER_ID');`

## C. Dodaj zmienne w GitHub
W repozytorium GitHub otwórz Settings → Secrets and variables → Actions → New repository secret.
Dodaj dwa sekrety:
- Name: `VITE_SUPABASE_URL`; Value: adres projektu, np. `https://....supabase.co`
- Name: `VITE_SUPABASE_PUBLISHABLE_KEY`; Value: klucz `sb_publishable_...`

Klucz publishable jest przeznaczony do użycia w aplikacji przeglądarkowej. Nigdy nie dodawaj `service_role` ani `secret key` do repozytorium lub aplikacji.

## D. Wdrożenie
1. Skopiuj z paczki cały folder `admin/src` do repozytorium, zastępując pliki.
2. Zastąp `admin/package.json`.
3. Zastąp `.github/workflows/deploy-admin.yml`.
4. Dodaj pliki `SUPABASE-KONFIGURACJA.sql` i `INSTRUKCJA-SUPABASE.md` do głównego folderu projektu.
5. GitHub Desktop → Commit to main → Push origin.
6. Poczekaj na zielony wynik w Actions i otwórz stronę panelu.

## Ważne
- W panelu będą widoczne zadania znajdujące się w tabeli `public.tasks`.
- Dotychczasowe trzy zadania demonstracyjne nie są automatycznie wysyłane do Supabase.
- Stare zadania z localStorage nie zostaną automatycznie przeniesione. Przed aktualizacją wyeksportuj je z obecnego panelu do JSON, jeśli chcesz je zachować.
- Po zalogowaniu panel czyta i zapisuje dane w centralnej bazie.
- Aplikacja Android nie jest jeszcze podłączona do Supabase; to będzie kolejny etap.
