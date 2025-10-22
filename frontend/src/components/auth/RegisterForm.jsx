import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import Input from '../common/Input';
import Button from '../common/Button';
import { validateEmail, validateDisplayName } from '../../utils/helpers';
import './RegisterForm.css';

const RegisterForm = () => {
  const [formData, setFormData] = useState({
    email: '',
    displayName: '',
    password: ''
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: null }));
    }
  };

  const validate = () => {
    const newErrors = {};

    if (!formData.email) {
      newErrors.email = 'Email обязателен';
    } else if (!validateEmail(formData.email)) {
      newErrors.email = 'Некорректный email';
    }

    if (!formData.displayName) {
      newErrors.displayName = 'Имя обязательно';
    } else if (!validateDisplayName(formData.displayName)) {
      newErrors.displayName = 'Имя должно быть от 3 до 120 символов';
    }

    if (!formData.password) {
      newErrors.password = 'Пароль обязателен';
    } else if (formData.password.length < 6) {
      newErrors.password = 'Пароль должен быть не менее 6 символов';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setLoading(true);
    try {
      await register(formData);
      navigate('/');
    } catch (error) {
      setErrors({ submit: error.message || 'Ошибка регистрации' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2 className="auth-title">Регистрация</h2>
        <p className="auth-subtitle">Создайте аккаунт для обмена книгами</p>

        <form onSubmit={handleSubmit} className="auth-form">
          <Input
            label="Email"
            type="email"
            value={formData.email}
            onChange={(e) => handleChange('email', e.target.value)}
            error={errors.email}
            required
            placeholder="your@email.com"
          />

          <Input
            label="Отображаемое имя"
            value={formData.displayName}
            onChange={(e) => handleChange('displayName', e.target.value)}
            error={errors.displayName}
            required
            placeholder="Как вас называть?"
          />

          <Input
            label="Пароль"
            type="password"
            value={formData.password}
            onChange={(e) => handleChange('password', e.target.value)}
            error={errors.password}
            required
            placeholder="Минимум 6 символов"
          />

          {errors.submit && (
            <div className="error-alert">{errors.submit}</div>
          )}

          <Button type="submit" fullWidth disabled={loading}>
            {loading ? 'Регистрация...' : 'Зарегистрироваться'}
          </Button>
        </form>

        <p className="auth-footer">
          Уже есть аккаунт? <a href="/login">Войти</a>
        </p>
      </div>
    </div>
  );
};

export default RegisterForm;
