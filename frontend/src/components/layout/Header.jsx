import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import Button from '../common/Button';
import './Header.css';

const Header = () => {
  const { currentUser, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="header">
      <div className="header-container">
        <Link to="/" className="logo">
          <span className="logo-icon">📚</span>
          <span className="logo-text">BookSwap</span>
        </Link>

        <nav className="nav">
          <Link to="/books" className="nav-link">Каталог</Link>
          {isAuthenticated && (
            <>
              <Link to="/my-books" className="nav-link">Мои книги</Link>
              <Link to="/exchanges" className="nav-link">Обмены</Link>
              <Link to="/publications" className="nav-link">Публикации</Link>
              <Link to="/genres" className="nav-link">Жанры</Link>
            </>
          )}
        </nav>

        <div className="header-actions">
          {isAuthenticated ? (
            <>
              <Link to="/profile" className="user-info">
                <span className="user-icon">👤</span>
                <span className="user-name">{currentUser?.displayName}</span>
              </Link>
              <Button variant="outline" size="small" onClick={handleLogout}>
                Выйти
              </Button>
            </>
          ) : (
            <>
              <Link to="/login">
                <Button variant="outline" size="small">Войти</Button>
              </Link>
              <Link to="/register">
                <Button variant="primary" size="small">Регистрация</Button>
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;
