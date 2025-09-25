

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS agent
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name          VARCHAR(128) NOT NULL,
    description   TEXT,
    system_prompt TEXT,
    model         VARCHAR(64)  NOT NULL,
    allowed_tools JSONB                 DEFAULT '[]'::jsonb,
    allowed_kbs   JSONB                 DEFAULT '[]'::jsonb,
    chat_options  JSONB                 DEFAULT '{}'::jsonb,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS chat_session
(
    id         UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    agent_id   UUID      NOT NULL REFERENCES agent (id) ON DELETE CASCADE,
    title      VARCHAR(255),
    metadata   JSONB              DEFAULT '{}'::jsonb,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_chat_session_agent ON chat_session (agent_id);

CREATE TABLE IF NOT EXISTS chat_message
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    session_id UUID        NOT NULL REFERENCES chat_session (id) ON DELETE CASCADE,
    role       VARCHAR(32) NOT NULL,
    content    TEXT,
    metadata   JSONB                DEFAULT '{}'::jsonb,
    created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP   NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_chat_message_session_time ON chat_message (session_id, created_at);

CREATE TABLE IF NOT EXISTS knowledge_base
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name        VARCHAR(128) NOT NULL,
    description TEXT,
    metadata    JSONB                 DEFAULT '{}'::jsonb,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS document
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    kb_id      UUID         NOT NULL REFERENCES knowledge_base (id) ON DELETE CASCADE,
    filename   VARCHAR(255) NOT NULL,
    filetype   VARCHAR(32),
    size       BIGINT,
    metadata   JSONB                 DEFAULT '{}'::jsonb,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_document_kb ON document (kb_id);

CREATE TABLE IF NOT EXISTS chunk_bge_m3
(
    id         UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    kb_id      UUID      NOT NULL REFERENCES knowledge_base (id) ON DELETE CASCADE,
    doc_id     UUID      NOT NULL REFERENCES document (id) ON DELETE CASCADE,
    content    TEXT      NOT NULL,
    metadata   TEXT,
    embedding  vector(1024),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_chunk_kb ON chunk_bge_m3 (kb_id);

CREATE INDEX IF NOT EXISTS idx_chunk_embedding_ivfflat
    ON chunk_bge_m3 USING ivfflat (embedding vector_l2_ops) WITH (lists = 100);
