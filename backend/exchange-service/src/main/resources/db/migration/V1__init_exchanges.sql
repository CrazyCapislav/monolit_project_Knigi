CREATE TABLE exchange_request (
    id             BIGSERIAL PRIMARY KEY,
    requester_id   BIGINT      NOT NULL,
    owner_id       BIGINT      NOT NULL,
    book_requested BIGINT      NOT NULL,
    book_offered   BIGINT,
    status         VARCHAR(20) NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ
);

CREATE INDEX idx_exchange_status ON exchange_request(status);
CREATE INDEX idx_exchange_requester ON exchange_request(requester_id);
CREATE INDEX idx_exchange_owner ON exchange_request(owner_id);
