import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import BookList from '../components/books/BookList';
import Button from '../components/common/Button';
import BookDetail from '../components/books/BookDetail';
import ExchangeForm from '../components/exchanges/ExchangeForm';
import Modal from '../components/common/Modal';
import { useToast } from '../hooks/useToast';
import Toast from '../components/common/Toast';

import { bookService } from '../services/bookService';
import './HomePage.css';
import {useAuth} from "../context/AuthContext";

const HomePage = () => {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [hasMore, setHasMore] = useState(true);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const { toast, showToast, hideToast } = useToast();
  const navigate = useNavigate();
  const [selectedBook, setSelectedBook] = useState(null);
  const [showExchangeModal, setShowExchangeModal] = useState(false);
  const { isAuthenticated } = useAuth();


  useEffect(() => {
    loadBooks();
  }, []);

  const loadBooks = async () => {
    try {
      setLoading(true);
      const data = await bookService.getBooksFeed(null, 20);
      setBooks(data);
      setHasMore(data.length === 20);
    } catch (error) {
      console.error('Failed to load books:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleBookClick = (book) => {
      setSelectedBook(book);
      setShowDetailModal(true);
  };

  return (
    <div className="home-page">
      <div className="hero-section">
        <h1 className="hero-title">Добро пожаловать в BookSwap</h1>
        <p className="hero-subtitle">
          Обменивайтесь книгами с другими читателями
        </p>
        <div className="hero-actions">
          <Button variant="primary" size="large" onClick={() => navigate('/books')}>
            Каталог книг
          </Button>
          <Button variant="secondary" size="large" onClick={() => navigate('/my-books')}>
            Мои книги
          </Button>
        </div>
      </div>

      <div className="section">
        <h2 className="section-title">Последние книги</h2>
        <BookList 
          books={books} 
          loading={loading}
          onBookClick={handleBookClick}
        />
      </div>

      {/* Модалка с деталями книги */}
      <Modal
          isOpen={showDetailModal}
          onClose={() => {
            setShowDetailModal(false);
            setSelectedBook(null);
          }}
          title="Информация о книге"
      >
        {selectedBook && (
            <BookDetail
                book={selectedBook}
                onClose={() => {
                  setShowDetailModal(false);
                  setSelectedBook(null);
                }}
                onExchange={() => {
                  if (!isAuthenticated) {
                    showToast('Пожалуйста, войдите в систему для создания заявки на обмен', 'warning');
                    return;
                  }
                  setShowDetailModal(false);
                  setTimeout(() => setShowExchangeModal(true), 300);
                }}
            />
        )}
      </Modal>

      {/* Модалка с формой обмена */}
      <Modal
          isOpen={showExchangeModal}
          onClose={() => setShowExchangeModal(false)}
          title="Создать заявку на обмен"
      >
        {selectedBook && (
            <ExchangeForm
                book={selectedBook}
                onSubmit={() => {
                  setShowExchangeModal(false);
                  setSelectedBook(null);
                }}
                onCancel={() => setShowExchangeModal(false)}
            />
        )}
      </Modal>

      {/* Toast уведомления */}
      <Toast
          message={toast.message}
          type={toast.type}
          isVisible={toast.isVisible}
          onClose={hideToast}
      />
    </div>
  );
};

export default HomePage;
