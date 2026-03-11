-- ╔═══════════════════════════════════════════════════════════════════════════════╗
-- ║                        OmniGame AI — Database Schema                        ║
-- ║  PostgreSQL + pgvector | Supabase Compatible                                ║
-- ║  EAV (Entity-Attribute-Value) Model for Game-Agnostic Metadata              ║
-- ╚═══════════════════════════════════════════════════════════════════════════════╝
--
-- DESIGN RATIONALE:
-- Each game (Skyrim, Minecraft, Elden Ring, etc.) has vastly different metadata
-- requirements. Instead of rigid per-game schemas, we use EAV to allow each game
-- to define its own attribute set without schema migrations.
--
-- The `game_knowledge` table stores RAG embeddings (1536D) for The Collector
-- chatbot's semantic search pipeline.
--
-- DEPLOYMENT: Run this script in your Supabase SQL Editor or via psql.
-- ─────────────────────────────────────────────────────────────────────────────────

-- ╔═══════════════════════════════════════╗
-- ║  1. EXTENSIONS                       ║
-- ╚═══════════════════════════════════════╝

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "vector";          -- pgvector for semantic search

-- ╔═══════════════════════════════════════╗
-- ║  2. CORE TABLES                      ║
-- ╚═══════════════════════════════════════╝

-- ── Games Catalog ────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS games (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(200)    NOT NULL,
    slug        VARCHAR(200)    NOT NULL UNIQUE,
    cover_url   VARCHAR(500),
    description TEXT,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_games_slug ON games(slug);
CREATE INDEX IF NOT EXISTS idx_games_name ON games(name);

COMMENT ON TABLE games IS 'Root catalog of supported games in the OmniGame ecosystem.';

-- ── Game Entities (Mods, Patches, Assets, Tools) ─────────────────────────────────
CREATE TABLE IF NOT EXISTS game_entities (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    game_id          UUID            NOT NULL REFERENCES games(id) ON DELETE CASCADE,
    name             VARCHAR(300)    NOT NULL,
    type             VARCHAR(20)     NOT NULL CHECK (type IN ('MOD', 'PATCH', 'ASSET', 'TOOL')),
    description      TEXT,
    download_url     VARCHAR(500),
    author_name      VARCHAR(200),
    download_count   BIGINT          NOT NULL DEFAULT 0,
    security_audited BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_game_entities_game_id ON game_entities(game_id);
CREATE INDEX IF NOT EXISTS idx_game_entities_type    ON game_entities(type);

COMMENT ON TABLE game_entities IS 'Downloadable entities (mods, patches, assets, tools) within a game catalog.';

-- ╔═══════════════════════════════════════╗
-- ║  3. EAV (Entity-Attribute-Value)     ║
-- ╚═══════════════════════════════════════╝

-- ── Attribute Definitions ────────────────────────────────────────────────────────
-- Describes what metadata fields a game supports.
-- Example: Skyrim defines "Load Order" (INTEGER), "Requires SKSE" (BOOLEAN).
CREATE TABLE IF NOT EXISTS attributes (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(150)    NOT NULL,
    data_type   VARCHAR(20)     NOT NULL CHECK (data_type IN ('STRING', 'INTEGER', 'BOOLEAN', 'FLOAT')),
    description VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_attributes_name ON attributes(name);

COMMENT ON TABLE attributes IS 'EAV attribute definitions — game-agnostic metadata field descriptors.';

-- ── Entity Values (The "Value" in EAV) ───────────────────────────────────────────
-- Stores concrete attribute values for each game entity.
-- Example: Mod "SkyUI" → Attribute "Load Order" → Value "15"
CREATE TABLE IF NOT EXISTS entity_values (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_id    UUID NOT NULL REFERENCES game_entities(id) ON DELETE CASCADE,
    attribute_id UUID NOT NULL REFERENCES attributes(id) ON DELETE CASCADE,
    value        TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_entity_values_entity_id    ON entity_values(entity_id);
CREATE INDEX IF NOT EXISTS idx_entity_values_attribute_id ON entity_values(attribute_id);

-- Composite unique constraint: one value per attribute per entity
CREATE UNIQUE INDEX IF NOT EXISTS idx_entity_values_unique
    ON entity_values(entity_id, attribute_id);

COMMENT ON TABLE entity_values IS 'EAV value storage — links game entities to their attribute values.';

-- ╔═══════════════════════════════════════╗
-- ║  4. RAG KNOWLEDGE BASE (pgvector)    ║
-- ╚═══════════════════════════════════════╝

-- ── Game Knowledge Vectors ───────────────────────────────────────────────────────
-- Powers The Collector's semantic search pipeline.
-- The Harvester agent ingests wiki content, chunks it, generates embeddings,
-- and persists them here. The Collector queries this table during RAG.
CREATE TABLE IF NOT EXISTS game_knowledge (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    game_id        UUID         NOT NULL REFERENCES games(id) ON DELETE CASCADE,
    source_url     VARCHAR(1000),
    context_chunk  TEXT         NOT NULL,
    embedding      vector(1536),   -- OpenAI text-embedding-3-small dimension
    chunk_metadata TEXT,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_game_knowledge_game_id ON game_knowledge(game_id);

-- IVFFlat index for approximate nearest neighbor search on embeddings.
-- Requires at least 100 rows per list for optimal performance.
-- Adjust `lists` parameter based on data volume: sqrt(total_rows).
CREATE INDEX IF NOT EXISTS idx_game_knowledge_embedding
    ON game_knowledge USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

COMMENT ON TABLE game_knowledge IS 'RAG knowledge base — chunked text with pgvector embeddings for semantic search.';

-- ╔═══════════════════════════════════════╗
-- ║  5. USER MANAGEMENT                  ║
-- ╚═══════════════════════════════════════╝

-- ── Users (Linked to Supabase Auth) ──────────────────────────────────────────────
-- Mirrors the Supabase auth.users table with application-specific fields.
-- The `auth_uid` column references the Supabase user UUID from JWT claims.
CREATE TABLE IF NOT EXISTS app_users (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    auth_uid          UUID         NOT NULL UNIQUE,    -- Links to Supabase auth.users.id
    email             VARCHAR(320),
    display_name      VARCHAR(200),
    avatar_url        VARCHAR(500),
    subscription_tier VARCHAR(20)  NOT NULL DEFAULT 'FREE' CHECK (subscription_tier IN ('FREE', 'PREMIUM')),
    stripe_customer_id VARCHAR(100),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_app_users_auth_uid ON app_users(auth_uid);

COMMENT ON TABLE app_users IS 'Application user profiles linked to Supabase Auth with subscription tier (SaaS).';

-- ╔═══════════════════════════════════════╗
-- ║  6. HELPER FUNCTIONS                 ║
-- ╚═══════════════════════════════════════╝

-- Auto-update `updated_at` timestamp on row modification
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_games_updated_at
    BEFORE UPDATE ON games
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE TRIGGER trg_app_users_updated_at
    BEFORE UPDATE ON app_users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ╔═══════════════════════════════════════╗
-- ║  7. SEED DATA (Development)          ║
-- ╚═══════════════════════════════════════╝

-- Insert sample games for development/demo purposes
INSERT INTO games (name, slug, cover_url, description) VALUES
    ('The Elder Scrolls V: Skyrim', 'skyrim',
     'https://images.igdb.com/igdb/image/upload/t_cover_big/co1tnw.webp',
     'The legendary open-world RPG. Massive modding community with 60,000+ mods covering gameplay, graphics, quests, and total conversions.'),

    ('Minecraft', 'minecraft',
     'https://images.igdb.com/igdb/image/upload/t_cover_big/co49x5.webp',
     'The sandbox phenomenon. Supports Forge, Fabric, and Bukkit mod loaders with hundreds of thousands of community mods and resource packs.'),

    ('Elden Ring', 'elden-ring',
     'https://images.igdb.com/igdb/image/upload/t_cover_big/co4jni.webp',
     'FromSoftware''s open-world masterpiece. Growing modding scene with gameplay tweaks, randomizers, and cosmetic mods.'),

    ('Cyberpunk 2077', 'cyberpunk-2077',
     'https://images.igdb.com/igdb/image/upload/t_cover_big/co4hku.webp',
     'Night City''s dystopian RPG. Active modding community with REDmod support, script mods, and visual enhancements.'),

    ('Stardew Valley', 'stardew-valley',
     'https://images.igdb.com/igdb/image/upload/t_cover_big/xrpmydnu9rpxvxfjkiu7.webp',
     'The beloved farming simulator. SMAPI-powered modding ecosystem with content packs, automation mods, and UI improvements.')
ON CONFLICT (slug) DO NOTHING;

-- Insert sample attributes for EAV demonstration
INSERT INTO attributes (name, data_type, description) VALUES
    ('Load Order',     'INTEGER', 'Position in the mod load sequence. Lower numbers load first.'),
    ('Requires SKSE',  'BOOLEAN', 'Whether this mod requires the Skyrim Script Extender.'),
    ('Forge Version',  'STRING',  'Minimum Minecraft Forge version required by this mod.'),
    ('Fabric Version', 'STRING',  'Minimum Fabric Loader version required by this mod.'),
    ('File Size (MB)', 'FLOAT',   'Total file size of the downloadable archive in megabytes.'),
    ('NSFW Content',   'BOOLEAN', 'Whether this mod contains adult or explicit content.'),
    ('Compatible With','STRING',  'Comma-separated list of known compatible mods or mod packs.')
ON CONFLICT DO NOTHING;
