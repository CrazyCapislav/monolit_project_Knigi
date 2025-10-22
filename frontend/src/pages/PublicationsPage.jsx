import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import Loader from '../components/common/Loader';
import { publicationService } from '../services/publicationService';
import { REQUEST_STATUSES } from '../utils/constants';
import { getStatusColor, formatDate } from '../utils/helpers';
import './PublicationsPage.css';

const PublicationsPage = () => {
  const [publications, setPublications] = useState([]);
  const [loading, setLoading] = useState(true);
  const { currentUser, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    loadPublications();
  }, [isAuthenticated, navigate]);

  const loadPublications = async () => {
    try {
      setLoading(true);
      const data = await publicationService.getPublications(0, 50);
      setPublications(Array.isArray(data) ? data : data.content || []);
    } catch (error) {
      console.error('Failed to load publications:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (id) => {
    try {
      await publicationService.approvePublication(id);
      loadPublications();
    } catch (error) {
      console.error('Failed to approve publication:', error);
      alert('Ошибка при одобрении заявки');
    }
  };

  const handleReject = async (id) => {
    try {
      await publicationService.rejectPublication(id);
      loadPublications();
    } catch (error) {
      console.error('Failed to reject publication:', error);
      alert('Ошибка при отклонении заявки');
    }
  };

  const isPublisher = currentUser?.role === 'PUBLISHER' || currentUser?.role === 'ADMIN';

  if (loading) return <Loader fullScreen />;

  return (
    <div className="publications-page">
      <div className="page-header">
        <div>
          <h1>Заявки на публикацию</h1>
          <p>Заявки на публикацию новых книг</p>
        </div>
      </div>

      {publications.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">📝</div>
          <h3>Нет заявок на публикацию</h3>
          <p>Здесь будут отображаться заявки на добавление новых книг</p>
        </div>
      ) : (
        <div className="publications-list">
          {publications.map(pub => (
            <Card key={pub.id} className="publication-card">
              <div className="publication-header">
                <h3>{pub.title}</h3>
                <span 
                  className="status-badge"
                  style={{ backgroundColor: getStatusColor(pub.status) }}
                >
                  {REQUEST_STATUSES[pub.status] || pub.status}
                </span>
              </div>
              <div className="publication-info">
                <p><strong>Автор книги:</strong> {pub.author}</p>
                <p><strong>Запрашивающий:</strong> ID {pub.requesterId}</p>
                {pub.publisherId && (
                  <p><strong>Издатель:</strong> ID {pub.publisherId}</p>
                )}
                {pub.message && (
                  <p><strong>Сообщение:</strong> {pub.message}</p>
                )}
                <p className="date">Создано: {formatDate(pub.createdAt)}</p>
                {pub.decidedAt && (
                  <p className="date">Решено: {formatDate(pub.decidedAt)}</p>
                )}
              </div>
              {isPublisher && pub.status === 'PENDING' && (
                <div className="publication-actions">
                  <Button 
                    variant="primary" 
                    size="small"
                    onClick={() => handleApprove(pub.id)}
                  >
                    Одобрить
                  </Button>
                  <Button 
                    variant="danger" 
                    size="small"
                    onClick={() => handleReject(pub.id)}
                  >
                    Отклонить
                  </Button>
                </div>
              )}
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};

export default PublicationsPage;
