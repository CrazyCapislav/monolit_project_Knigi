import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import Input from '../common/Input';
import Button from '../common/Button';
import './RegisterForm.css';

const LoginForm = () => {
  const [userId, setUserId] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!userId) {
      setError('ID пользователя обязателен');
      return;
    }

    setLoading(true);
    try {
      await login(userId);
      navigate('/');
    } catch (error) {
      setError(error.message || 'Ошибка входа');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2 className="auth-title">Вход</h2>
        <p className="auth-subtitle">Введите ваш ID пользователя</p>

        <form onSubmit={handleSubmit} className="auth-form">
          <Input
            label="ID пользователя"
            type="number"
            value={userId}
            onChange={(e) => {
              setUserId(e.target.value);
              setError('');
            }}
            error={error}
            required
            placeholder="Например, 1"
          />

          <Button type="submit" fullWidth disabled={loading}>
            {loading ? 'Вход...' : 'Войти'}
          </Button>
        </form>

        <p className="auth-footer">
          Нет аккаунта? <a href="/register">Зарегистрироваться</a>
        </p>
      </div>
    </div>
  );
};

export default LoginForm;
