import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../hooks/useToast';
import Toast from '../components/common/Toast';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import Input from '../components/common/Input';
import Modal from '../components/common/Modal';
import { genreService } from '../services/genreService';
import './GenresPage.css';

const GenresPage = () => {
  const [genres, setGenres] = useState([]);
  const { toast, showToast, hideToast } = useToast();
  const [genreToDelete, setGenreToDelete] = useState(null);
  const [confirmModalOpen, setConfirmModalOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [newGenreName, setNewGenreName] = useState('');
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    loadGenres();
  }, []);

  const loadGenres = async () => {
    try {
      setLoading(true);
      const data = await genreService.getGenres();
      setGenres(data);
    } catch (error) {
      console.error('Failed to load genres:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAddGenre = async () => {
    if (!newGenreName.trim() || newGenreName.length < 2) {
      alert('Название жанра должно быть не менее 2 символов');
      return;
    }

    try {
      await genreService.createGenre({ name: newGenreName });
      setNewGenreName('');
      setShowModal(false);
      loadGenres();
    } catch (error) {
      console.error('Failed to create genre:', error);
      alert('Ошибка при создании жанра');
    }
  };

  const confirmDelete = (genre) => {
    setGenreToDelete(genre);
    setConfirmModalOpen(true);
  };

  const performDelete = async () => {
    try {
      await genreService.deleteGenre(genreToDelete.id);
      showToast('Жанр успешно удалён', 'success');
      setGenres(prev => prev.filter(g => g.id !== genreToDelete.id));
    } catch (e) {
      console.error(e);
      showToast('Ошибка при удалении жанра', 'error');
    } finally {
      setConfirmModalOpen(false);
      setGenreToDelete(null);
    }
  };


  return (
    <div className="genres-page">
      <div className="page-header">
        <div>
          <h1>Жанры</h1>
          <p>Управление жанрами книг</p>
        </div>
        {isAuthenticated && (
          <Button variant="primary" onClick={() => setShowModal(true)}>
            + Добавить жанр
          </Button>
        )}
      </div>

      {loading ? (
        <p>Загрузка...</p>
      ) : (
        <div className="genres-grid">
          {genres.map(genre => (
            <Card key={genre.id} className="genre-card">
              <div className="genre-info">
                <h3>{genre.name}</h3>
                {isAuthenticated && (
                  <button
                      className="delete-btn"
                      onClick={() => confirmDelete(genre)}
                  >
                    🗑️
                  </button>
                )}
              </div>
            </Card>
          ))}
        </div>
      )}

      <Modal
          isOpen={confirmModalOpen}
          onClose={() => setConfirmModalOpen(false)}
          title="Подтвердите удаление"
      >
        <p>Вы уверены, что хотите удалить жанр «{genreToDelete?.name}»?</p>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '20px' }}>
          <Button variant="outline" onClick={() => setConfirmModalOpen(false)}>
            Отмена
          </Button>
          <Button variant="danger" onClick={performDelete}>
            Удалить
          </Button>
        </div>
      </Modal>

      <Modal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        title="Добавить жанр"
      >
        <Input
          label="Название жанра"
          value={newGenreName}
          onChange={(e) => setNewGenreName(e.target.value)}
          placeholder="Например, Фантастика"
        />
        <div style={{ marginTop: '20px', display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
          <Button variant="outline" onClick={() => setShowModal(false)}>
            Отмена
          </Button>
          <Button onClick={handleAddGenre}>
            Добавить
          </Button>
        </div>
      </Modal>

      <Toast
          message={toast.message}
          type={toast.type}
          isVisible={toast.isVisible}
          onClose={hideToast}
      />
    </div>
  );
};

export default GenresPage;
