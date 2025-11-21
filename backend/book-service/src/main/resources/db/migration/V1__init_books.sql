CREATE TABLE genre (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE book (
    id             BIGSERIAL PRIMARY KEY,
    title          VARCHAR(255) NOT NULL,
    author         VARCHAR(255) NOT NULL,
    isbn           VARCHAR(32),
    published_year INT,
    owner_id       BIGINT       NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    condition      VARCHAR(20)  NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ
);

CREATE TABLE book_genre (
    book_id  BIGINT NOT NULL REFERENCES book (id) ON DELETE CASCADE,
    genre_id BIGINT NOT NULL REFERENCES genre (id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, genre_id)
);

CREATE INDEX idx_book_created ON book (created_at DESC, id DESC);
CREATE INDEX idx_book_owner ON book (owner_id);
CREATE INDEX idx_book_status ON book (status);

INSERT INTO genre (name) VALUES
    ('Fiction'),
    ('Non-Fiction'),
    ('Science Fiction'),
    ('Fantasy'),
    ('Mystery'),
    ('Romance'),
    ('Biography'),
    ('History'),
    ('Science'),
    ('Technology');
