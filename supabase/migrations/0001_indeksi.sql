-- Indeksi za poizvedbe, ki jih izvaja aplikacija.
-- Zazeni v Supabase SQL Editorju. Skripta je idempotentna (IF NOT EXISTS),
-- zato jo je varno pognati vec kot enkrat.

-- ---------------------------------------------------------------------------
-- listings
-- ---------------------------------------------------------------------------

-- Vse poizvedbe nad oglasi filtrirajo is_filled = false, zato so delni indeksi
-- manjsi od polnih in jih nacrtovalnik pogosteje izbere.
create index if not exists listings_available_created_idx
    on public.listings (created_at desc)
    where is_filled = false;

create index if not exists listings_available_price_idx
    on public.listings (price_per_month)
    where is_filled = false;

-- Iskanje po lokaciji uporablja ILIKE '%niz%'. Vodilni '%' onemogoci B-tree,
-- zato potrebujemo trigramski indeks.
create extension if not exists pg_trgm;

create index if not exists listings_location_trgm_idx
    on public.listings using gin (location gin_trgm_ops);

-- ---------------------------------------------------------------------------
-- messages
-- ---------------------------------------------------------------------------

-- Filter (match_id) in razvrscanje (sent_at) v enem indeksu: brez tega mora
-- baza sporocila klepeta se posebej sortirati.
create index if not exists messages_match_sent_idx
    on public.messages (match_id, sent_at);

-- ---------------------------------------------------------------------------
-- matches
-- ---------------------------------------------------------------------------

-- getMyMatches() isce po user_id_a OR user_id_b. Enega sestavljenega indeksa
-- za tak pogoj ni; z dvema locenima ju nacrtovalnik zdruzi z BitmapOr.
create index if not exists matches_user_a_idx
    on public.matches (user_id_a, created_at desc);

create index if not exists matches_user_b_idx
    on public.matches (user_id_b, created_at desc);

-- ---------------------------------------------------------------------------
-- favorites
-- ---------------------------------------------------------------------------

-- Poleg pohitritve poizvedb preprecuje podvojene vnose: addFavorite() vstavlja
-- brez zascite, zato sta dva hitra klika doslej ustvarila dve vrstici.
create unique index if not exists favorites_profile_listing_idx
    on public.favorites (profile_id, listing_id);

-- ---------------------------------------------------------------------------
-- questionnaire_answers
-- ---------------------------------------------------------------------------

-- Potreben za upsert z onConflict = "profile_id,question_id" v saveMyAnswers();
-- hkrati pokriva branje odgovorov po profilu.
create unique index if not exists questionnaire_answers_profile_question_idx
    on public.questionnaire_answers (profile_id, question_id);

-- ---------------------------------------------------------------------------
-- profiles
-- ---------------------------------------------------------------------------

-- Priporocila berejo samo razpolozljive profile; delni indeks zajame le te.
create index if not exists profiles_available_idx
    on public.profiles (id)
    where is_available = true;

-- ---------------------------------------------------------------------------
-- Tuji kljuci
-- ---------------------------------------------------------------------------
-- PostgREST zna vgnezditi povezano tabelo v en sam odgovor (npr. favorites +
-- listings) samo, ce med njima obstaja tuji kljuc. Brez tega mora odjemalec
-- poslati dve zahtevi. Preverjamo po stolpcu, ne po imenu omejitve, da skripta
-- ne podvoji kljuca, ki je bil ustvarjen pod drugim imenom.

do $$
begin
    if not exists (
        select 1
        from pg_constraint c
        join pg_attribute a on a.attrelid = c.conrelid and a.attnum = any (c.conkey)
        where c.conrelid = 'public.favorites'::regclass
          and c.contype = 'f'
          and a.attname = 'listing_id'
    ) then
        alter table public.favorites
            add constraint favorites_listing_id_fkey
            foreign key (listing_id) references public.listings (id) on delete cascade;
    end if;
end $$;

do $$
begin
    if not exists (
        select 1
        from pg_constraint c
        join pg_attribute a on a.attrelid = c.conrelid and a.attnum = any (c.conkey)
        where c.conrelid = 'public.questionnaire_answers'::regclass
          and c.contype = 'f'
          and a.attname = 'profile_id'
    ) then
        alter table public.questionnaire_answers
            add constraint questionnaire_answers_profile_id_fkey
            foreign key (profile_id) references public.profiles (id) on delete cascade;
    end if;
end $$;
