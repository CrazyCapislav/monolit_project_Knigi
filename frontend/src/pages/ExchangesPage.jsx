import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import Loader from '../components/common/Loader';
import { exchangeService } from '../services/exchangeService';
import { REQUEST_STATUSES } from '../utils/constants';
import { getStatusColor, formatDate } from '../utils/helpers';
import './ExchangesPage.css';

const ExchangesPage = () => {
  const [exchanges, setExchanges] = useState([]);
  const [loading, setLoading] = useState(true);
  const { currentUser, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    loadExchanges();
  }, [isAuthenticated, navigate]);

  const loadExchanges = async () => {
    try {
      setLoading(true);
      const data = await exchangeService.getExchanges(0, 50);
      setExchanges(Array.isArray(data) ? data : data.content || []);
    } catch (error) {
      console.error('Failed to load exchanges:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAccept = async (id) => {
    try {
      await exchangeService.acceptExchange(id);
      loadExchanges();
    } catch (error) {
      console.error('Failed to accept exchange:', error);
      alert('Ошибка при принятии заявки');
    }
  };

  if (loading) return <Loader fullScreen />;

  return (
    <div className="exchanges-page">
      <div className="page-header">
        <div>
          <h1>Заявки на обмен</h1>
          <p>Управляйте вашими заявками на обмен книгами</p>
        </div>
      </div>

      {exchanges.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">🔄</div>
          <h3>Нет заявок на обмен</h3>
          <p>Начните с просмотра каталога книг</p>
        </div>
      ) : (
        <div className="exchanges-list">
          {exchanges.map(exchange => (
            <Card key={exchange.id} className="exchange-card">
              <div className="exchange-header">
                <h3>Заявка #{exchange.id}</h3>
                <span 
                  className="status-badge"
                  style={{ backgroundColor: getStatusColor(exchange.status) }}
                >
                  {REQUEST_STATUSES[exchange.status] || exchange.status}
                </span>
              </div>
              <div className="exchange-info">
                <p><strong>Запрашивающий:</strong> ID {exchange.requesterId}</p>
                <p><strong>Владелец:</strong> ID {exchange.owner_id}</p>
                <p><strong>Книга:</strong> ID {exchange.bookRequestedId}</p>
                {exchange.bookOfferedId && (
                  <p><strong>Предложенная книга:</strong> ID {exchange.bookOfferedId}</p>
                )}
                <p className="date">Создано: {formatDate(exchange.createdAt)}</p>
              </div>
              {exchange.owner_id === currentUser?.id && exchange.status === 'PENDING' && (
                <Button 
                  variant="primary" 
                  size="small"
                  onClick={() => handleAccept(exchange.id)}
                >
                  Принять
                </Button>
              )}
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};

export default ExchangesPage;
