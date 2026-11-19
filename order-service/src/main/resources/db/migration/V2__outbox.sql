CREATE TABLE  outbox (
    id UUID PRIMARY KEY,       -- id события
    aggregate_id UUID NOT NULL,
    topic VARCHAR(128) NOT NULL,
    payload JSONB NOT NULL,    -- содержит весь EventEnvelope
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at TIMESTAMPTZ
);

CREATE INDEX idx_outbox_unpublished
ON outbox (created_at, id)
WHERE published_at IS NULL;