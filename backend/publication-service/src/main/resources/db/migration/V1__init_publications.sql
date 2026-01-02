CREATE TABLE publication_request (
    id               BIGSERIAL PRIMARY KEY,
    requester_id     BIGINT       NOT NULL,
    title            VARCHAR(500) NOT NULL,
    author           VARCHAR(200) NOT NULL,
    isbn             VARCHAR(20),
    published_year   INTEGER,
    description      TEXT,
    status           VARCHAR(20)  NOT NULL,
    publisher_id     BIGINT,
    created_book_id  BIGINT,
    rejection_reason VARCHAR(500),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ
);

CREATE INDEX idx_publication_status ON publication_request(status);
CREATE INDEX idx_publication_requester ON publication_request(requester_id);
CREATE INDEX idx_publication_publisher ON publication_request(publisher_id);