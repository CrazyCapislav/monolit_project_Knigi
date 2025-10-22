import React from 'react';
import BookCard from './BookCard';
import Loader from '../common/Loader';
import './BookList.css';

const BookList = ({ books, loading, onBookClick }) => {
  if (loading) {
    return (
      <div className="book-list-loading">
        <Loader size="large" />
      </div>
    );
  }

  if (!books || books.length === 0) {
    return (
      <div className="book-list-empty">
        <div className="empty-icon">📚</div>
        <h3>Книги не найдены</h3>
        <p>Попробуйте изменить фильтры или добавьте первую книгу</p>
      </div>
    );
  }

  return (
    <div className="book-list">
      {books.map((book) => (
        <BookCard 
          key={book.id} 
          book={book} 
          onClick={() => onBookClick && onBookClick(book)}
        />
      ))}
    </div>
  );
};

export default BookList;
