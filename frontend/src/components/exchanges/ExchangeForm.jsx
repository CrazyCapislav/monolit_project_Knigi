import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import Button from '../common/Button';
import Toast from '../common/Toast';
import { useToast } from '../../hooks/useToast';
import { bookService } from '../../services/bookService';
import { exchangeService } from '../../services/exchangeService';
import './ExchangeForm.css';


const ExchangeForm = ({ book, onSubmit, onCancel }) => {
    const { toast, showToast, hideToast } = useToast();
    const [myBooks, setMyBooks] = useState([]);
    const [selectedBookId, setSelectedBookId] = useState('');
    const [loading, setLoading] = useState(false);
    const [loadingBooks, setLoadingBooks] = useState(true);
    const { currentUser } = useAuth();

    useEffect(() => {
        loadMyBooks();
    }, []);

    const loadMyBooks = async () => {
        try {
            setLoadingBooks(true);
            const data = await bookService.getBooks(0, 100);
            const allBooks = Array.isArray(data) ? data : data.content || [];
            const myBooksFiltered = allBooks.filter(b => b.owner_id === currentUser?.id);
            setMyBooks(myBooksFiltered);
        } catch (error) {
            console.error('Failed to load books:', error);
        } finally {
            setLoadingBooks(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            const exchangeData = {
                bookRequestedId: Number(book.id),
                bookOfferedId: selectedBookId || null
            };

            await exchangeService.createExchange(exchangeData);
            showToast('Заявка на обмен успешно создана!', 'success');

            // Закрываем форму через 1 секунду после показа toast
            setTimeout(() => {
                if (onSubmit) {
                    onSubmit();
                }
            }, 1000);
        } catch (error) {
            console.error('Failed to create exchange:', error);
            showToast('Ошибка при создании заявки на обмен', 'error');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="exchange-form">
            <div className="book-info-section">
                <h3>Вы хотите получить:</h3>
                <div className="selected-book">
                    <div className="book-icon">📖</div>
                    <div>
                        <h4>{book.title}</h4>
                        <p>Автор: {book.author}</p>
                        {book.publishedYear && <p>Год: {book.publishedYear}</p>}
                    </div>
                </div>
            </div>

            <form onSubmit={handleSubmit}>
                <div className="form-section">
                    <h3>Предложить свою книгу (необязательно):</h3>
                    {loadingBooks ? (
                        <p>Загрузка ваших книг...</p>
                    ) : myBooks.length === 0 ? (
                        <p className="no-books-message">
                            У вас пока нет книг. Вы можете создать заявку без предложения своей книги.
                        </p>
                    ) : (
                        <select
                            value={selectedBookId}
                            onChange={(e) => setSelectedBookId(e.target.value)}
                            className="book-select"
                        >
                            <option value="">Не предлагать книгу</option>
                            {myBooks.map(b => (
                                <option key={b.id} value={b.id}>
                                    {b.title} - {b.author}
                                </option>
                            ))}
                        </select>
                    )}
                </div>

                <div className="form-actions">
                    <Button type="button" variant="outline" onClick={onCancel} disabled={loading}>
                        Отмена
                    </Button>
                    <Button type="submit" disabled={loading}>
                        {loading ? 'Отправка...' : 'Создать заявку'}
                    </Button>
                </div>
            </form>
            <Toast
                message={toast.message}
                type={toast.type}
                isVisible={toast.isVisible}
                onClose={hideToast}
            />
        </div>
    );
};

export default ExchangeForm;
