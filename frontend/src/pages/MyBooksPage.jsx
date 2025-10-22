import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import BookList from '../components/books/BookList';
import BookForm from '../components/books/BookForm';
import BookDetail from '../components/books/BookDetail';
import Modal from '../components/common/Modal';
import Button from '../components/common/Button';
import { bookService } from '../services/bookService';
import './MyBooksPage.css';

const MyBooksPage = () => {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [selectedBook, setSelectedBook] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const { currentUser, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    loadMyBooks();
  }, [isAuthenticated, navigate]);

  const loadMyBooks = async () => {
    try {
      setLoading(true);
      const myBooks = await bookService.getMyBooks();
      setBooks(myBooks);
    } catch (e) {
      console.error('Failed to load user books', e);
    } finally {
      setLoading(false);
    }
  };

  const handleAddBook = async (bookData) => {
    try {
      await bookService.createBook(bookData);
      setShowModal(false);
      loadMyBooks();
    } catch (error) {
      console.error('Failed to create book:', error);
      alert('Error adding book');
    }
  };

  const handleBookClick = (book) => {
    setSelectedBook(book);
    setShowDetailModal(true);
  };

  const handleCloseDetail = () => {
    setShowDetailModal(false);
    setSelectedBook(null);
  };

  return (
    <div className="my-books-page">
      <div className="page-header">
        <div>
          <h1>Мои книги</h1>
          <p>Управляйте своей коллекцией</p>
        </div>
        <Button variant="primary" onClick={() => setShowModal(true)}>
          + Добавить книгу
        </Button>
      </div>

      <BookList books={books} loading={loading} onBookClick={handleBookClick} />

      <Modal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        title="Добавить новую книгу"
      >
        <BookForm
          onSubmit={handleAddBook}
          onCancel={() => setShowModal(false)}
        />
      </Modal>

      <Modal
        isOpen={showDetailModal}
        onClose={handleCloseDetail}
        title="Детали книги"
      >
        {selectedBook && (
          <BookDetail
            book={selectedBook}
            onClose={handleCloseDetail}
            onExchange={() => alert('Функция обмена в разработке')}
          />
        )}
      </Modal>
    </div>
  );
};

export default MyBooksPage;
