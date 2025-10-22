import React from 'react';
import Card from '../common/Card';
import { BOOK_CONDITIONS } from '../../utils/constants';
import { formatDate } from '../../utils/helpers';
import './BookCard.css';

const BookCard = ({ book, onClick }) => {
  return (
    <Card hoverable onClick={onClick} className="book-card">
      <div className="book-cover">📖</div>
      <div className="book-info">
        <h3 className="book-title">{book.title}</h3>
        <p className="book-author">Автор: {book.author}</p>
        {book.publishedYear && (
          <p className="book-year">Год: {book.publishedYear}</p>
        )}
        <div className="book-condition">
          <span className="condition-badge">
            {BOOK_CONDITIONS[book.condition] || book.condition}
          </span>
        </div>
        {book.genres && (Array.isArray(book.genres) ? book.genres.length > 0 : Object.keys(book.genres).length > 0) && (
          <div className="book-genres">
            {Array.isArray(book.genres) 
              ? book.genres.map((genre, index) => (
                  <span key={index} className="genre-tag">
                    {typeof genre === 'string' ? genre : genre.name || genre}
                  </span>
                ))
              : Object.values(book.genres).map((genreName, index) => (
                  <span key={index} className="genre-tag">
                    {genreName}
                  </span>
                ))
            }
          </div>
        )}
      </div>
    </Card>
  );
};

export default BookCard;
