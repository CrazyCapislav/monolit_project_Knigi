import React from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import Card from '../components/common/Card';
import './ProfilePage.css';

const ProfilePage = () => {
  const { currentUser, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  if (!isAuthenticated) {
    navigate('/login');
    return null;
  }

  return (
    <div className="profile-page">
      <div className="page-header">
        <h1>Профиль</h1>
      </div>

      <Card className="profile-card">
        <div className="profile-avatar">👤</div>
        <div className="profile-info">
          <h2>{currentUser?.displayName}</h2>
          <p className="email">{currentUser?.email}</p>
          <p className="role">Роль: {currentUser?.role || 'USER'}</p>
          <p className="user-id">ID: {currentUser?.id}</p>
        </div>
      </Card>
    </div>
  );
};

export default ProfilePage;
