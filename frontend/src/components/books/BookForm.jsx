import React, { useState, useEffect } from 'react';
import Input from '../common/Input';
import Button from '../common/Button';
import Modal from '../common/Modal';
import { genreService } from '../../services/genreService';
import { BOOK_CONDITIONS } from '../../utils/constants';
import './BookForm.css';

const BookForm = ({ onSubmit, onCancel, initialData = {} }) => {
  const [formData, setFormData] = useState({
    title: initialData.title || '',
    author: initialData.author || '',
    isbn: initialData.isbn || '',
    publishedYear: initialData.publishedYear || '',
    condition: initialData.condition || 'GOOD',
    genreIds: initialData.genreIds || []
  });

  const [genres, setGenres] = useState([]);
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [loadingGenres, setLoadingGenres] = useState(true);


  const [showAddGenreModal, setShowAddGenreModal] = useState(false);
  const [newGenreName, setNewGenreName] = useState('');
  const [addingGenre, setAddingGenre] = useState(false);
  const [genreError, setGenreError] = useState('');

  useEffect(() => {
    loadGenres();
  }, []);

  const loadGenres = async () => {
    try {
      setLoadingGenres(true);
      const data = await genreService.getGenres();
      setGenres(data);
    } catch (error) {
      console.error('Failed to load genres:', error);
    } finally {
      setLoadingGenres(false);
    }
  };

  const handleChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: null }));
    }
  };

  const handleGenreToggle = (genreId) => {
    setFormData(prev => ({
      ...prev,
      genreIds: prev.genreIds.includes(genreId)
          ? prev.genreIds.filter(id => id !== genreId)
          : [...prev.genreIds, genreId]
    }));
  };

  const handleAddGenre = async () => {
    if (!newGenreName.trim()) {
      setGenreError('Enter genre name');
      return;
    }

    if (newGenreName.length < 2 || newGenreName.length > 60) {
      setGenreError('Name must be between 2 and 60 characters');
      return;
    }

 
    const existingGenre = genres.find(
        g => g.name.toLowerCase() === newGenreName.toLowerCase()
    );

    if (existingGenre) {
      setGenreError('Genre already exists');
      return;
    }

    setAddingGenre(true);
    try {
      const newGenre = await genreService.createGenre({ name: newGenreName });

      setGenres(prev => [...prev, newGenre]);

      setFormData(prev => ({
        ...prev,
        genreIds: [...prev.genreIds, newGenre.id]
      }));

      setShowAddGenreModal(false);
      setNewGenreName('');
      setGenreError('');

    } catch (error) {
      console.error('Failed to create genre:', error);
      setGenreError('Error creating genre: ' + (error.message || ''));
    } finally {
      setAddingGenre(false);
    }
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.title.trim()) newErrors.title = 'Title is required';
    if (!formData.author.trim()) newErrors.author = 'Author is required';
    if (formData.publishedYear && (formData.publishedYear < 0 || formData.publishedYear > 2100)) {
      newErrors.publishedYear = 'Year must be between 0 and 2100';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setLoading(true);
    try {
      await onSubmit(formData);
    } catch (error) {
      console.error('Form submission error:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
      <>
        <form onSubmit={handleSubmit} className="book-form">
          <Input
              label="Название книги"
              value={formData.title}
              onChange={(e) => handleChange('title', e.target.value)}
              error={errors.title}
              required
              placeholder="Введите название книги"
          />

          <Input
              label="Автор"
              value={formData.author}
              onChange={(e) => handleChange('author', e.target.value)}
              error={errors.author}
              required
              placeholder="Введите имя автора"
          />

          <Input
              label="ISBN"
              value={formData.isbn}
              onChange={(e) => handleChange('isbn', e.target.value)}
              placeholder="Введите ISBN (необязательно)"
              maxLength={32}
          />

          <Input
              label="Год издания"
              type="number"
              value={formData.publishedYear}
              onChange={(e) => handleChange('publishedYear', e.target.value)}
              error={errors.publishedYear}
              placeholder="Например, 2020"
              min="0"
              max="2100"
          />

          <div className="form-group">
            <label className="form-label">Состояние книги *</label>
            <select
                value={formData.condition}
                onChange={(e) => handleChange('condition', e.target.value)}
                className="form-select"
            >
              {Object.entries(BOOK_CONDITIONS).map(([key, value]) => (
                  <option key={key} value={key}>{value}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <div className="genre-header">
              <label className="form-label">Жанры</label>
              <button
                  type="button"
                  className="add-genre-btn"
                  onClick={() => setShowAddGenreModal(true)}
              >
                + Добавить жанр
              </button>
            </div>

            {loadingGenres ? (
                <p className="loading-text">Загрузка жанров...</p>
            ) : genres.length === 0 ? (
                <p className="no-genres-text">
                  Жанров пока нет. Нажмите "Добавить жанр" чтобы создать первый.
                </p>
            ) : (
                <div className="genre-checkboxes">
                  {genres.map(genre => (
                      <label key={genre.id} className="checkbox-label">
                        <input
                            type="checkbox"
                            checked={formData.genreIds.includes(genre.id)}
                            onChange={() => handleGenreToggle(genre.id)}
                        />
                        <span>{genre.name}</span>
                      </label>
                  ))}
                </div>
            )}

            {formData.genreIds.length > 0 && (
                <div className="selected-genres-info">
                  Выбрано жанров: {formData.genreIds.length}
                </div>
            )}
          </div>

          <div className="form-actions">
            <Button type="button" variant="outline" onClick={onCancel} disabled={loading}>
              Отмена
            </Button>
            <Button type="submit" disabled={loading}>
              {loading ? 'Сохранение...' : 'Сохранить'}
            </Button>
          </div>
        </form>

        {/* Модальное окно для добавления нового жанра */}
        <Modal
            isOpen={showAddGenreModal}
            onClose={() => {
              setShowAddGenreModal(false);
              setNewGenreName('');
              setGenreError('');
            }}
            title="Добавить новый жанр"
        >
          <div className="add-genre-form">
            <Input
                label="Название жанра"
                value={newGenreName}
                onChange={(e) => {
                  setNewGenreName(e.target.value);
                  setGenreError('');
                }}
                error={genreError}
                placeholder="Например, Фантастика"
                required
                maxLength={60}
            />

            <p className="genre-hint">
              💡 После создания жанр будет автоматически выбран для вашей книги
            </p>

            <div className="modal-actions">
              <Button
                  type="button"
                  variant="outline"
                  onClick={() => {
                    setShowAddGenreModal(false);
                    setNewGenreName('');
                    setGenreError('');
                  }}
                  disabled={addingGenre}
              >
                Отмена
              </Button>
              <Button
                  type="button"
                  onClick={handleAddGenre}
                  disabled={addingGenre || !newGenreName.trim()}
              >
                {addingGenre ? 'Добавление...' : 'Добавить'}
              </Button>
            </div>
          </div>
        </Modal>
      </>
  );
};

export default BookForm;
