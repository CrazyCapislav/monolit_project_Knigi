ALTER TABLE book ADD COLUMN cover_image_id BIGINT;
CREATE INDEX idx_book_cover_image_id ON book(cover_image_id);