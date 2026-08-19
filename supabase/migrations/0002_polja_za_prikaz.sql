-- Dodatna polja oglasa in profila, namenjena prikazu v seznamu.
-- Zazeni v Supabase SQL Editorju. Skripta je idempotentna (IF NOT EXISTS),
-- zato jo je varno pognati vec kot enkrat.
--
-- Zakaj: kartica v seznamu je doslej prikazala le lokacijo, opis in ceno. Uporabnik je
-- moral odpreti podrobnosti, da je izvedel kar koli drugega. Vsak stolpec spodaj ustreza
-- natanko enemu podatku, ki ga kartica ali podrobnosti oglasa odslej prikazujejo.

-- ---------------------------------------------------------------------------
-- listings
-- ---------------------------------------------------------------------------

-- Naslov oglasa. Doslej je vlogo naslova opravljala lokacija, zato je bila kartica
-- brez besedila, ki bi oglas locilo od drugih v isti cetrti.
alter table public.listings add column if not exists title text not null default '';

-- Vrsta nastanitve: 'soba', 'garsonjera', 'deljeno stanovanje', 'celo stanovanje'.
-- Namenoma brez check omejitve -- nabor se lahko se spremeni, aplikacija pa ponuja
-- zaprt seznam pri vnosu.
alter table public.listings add column if not exists room_type text not null default '';

-- Cetrt (Bezigrad, Center, Siska ...). Loceno od `location`, ker se po lokaciji isce
-- z ILIKE, cetrt pa je prikazna oznaka pod naslovom.
alter table public.listings add column if not exists district text not null default '';

-- Datum vselitve. NULL pomeni "takoj oziroma po dogovoru" -- to je resnicno stanje
-- pri obstojecih vrsticah, zato privzete vrednosti namenoma ni.
alter table public.listings add column if not exists available_from date;

-- Kvadratura in varscina sta lahko neznani, zato dopuscata NULL. Vrednost 0 bi
-- pomenila "nic kvadratov" in bi jo bilo treba posebej razlikovati od "ni podatka".
alter table public.listings add column if not exists size_sqm integer;
alter table public.listings add column if not exists deposit numeric(10, 2);

alter table public.listings add column if not exists bills_included boolean not null default false;
alter table public.listings add column if not exists furnished boolean not null default false;

-- Stevilo sostanovalcev, s katerimi si najemnik deli stanovanje (0 = sam).
alter table public.listings add column if not exists flatmates_count integer not null default 0;

-- Pripravljeno za prikaz fotografij. Aplikacija stolpca zaenkrat se ne bere --
-- dodan je zdaj, da fotografije ne bodo zahtevale se ene migracije.
alter table public.listings add column if not exists photo_url text not null default '';

-- ---------------------------------------------------------------------------
-- profiles
-- ---------------------------------------------------------------------------

-- Starost in fakulteta sta konteksta, ki ju uporabnik isce pri izbiri sostanovalca,
-- hkrati pa fakulteta podpira preverjeni akademski status: oznaka "preverjen student"
-- pove, da je status potrjen, fakulteta pa, kje.
alter table public.profiles add column if not exists age integer;
alter table public.profiles add column if not exists faculty text not null default '';
alter table public.profiles add column if not exists bio text not null default '';
alter table public.profiles add column if not exists avatar_url text not null default '';

-- ---------------------------------------------------------------------------
-- Indeksi
-- ---------------------------------------------------------------------------

-- Seznam oglasov je pogosto omejen na eno vrsto nastanitve. Delni indeks zajame
-- samo nezasedene oglase, enako kot indeksi iz 0001.
create index if not exists listings_available_type_idx
    on public.listings (room_type)
    where is_filled = false;
