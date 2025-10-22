import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Header from './components/layout/Header';
import Footer from './components/layout/Footer';
import HomePage from './pages/HomePage';
import BooksPage from './pages/BooksPage';
import MyBooksPage from './pages/MyBooksPage';
import ExchangesPage from './pages/ExchangesPage';
import PublicationsPage from './pages/PublicationsPage';
import ProfilePage from './pages/ProfilePage';
import GenresPage from './pages/GenresPage';
import LoginForm from './components/auth/LoginForm';
import RegisterForm from './components/auth/RegisterForm';
import './App.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="app">
          <Header />
          <main className="main-content">
            <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/books" element={<BooksPage />} />
              <Route path="/my-books" element={<MyBooksPage />} />
              <Route path="/exchanges" element={<ExchangesPage />} />
              <Route path="/publications" element={<PublicationsPage />} />
              <Route path="/profile" element={<ProfilePage />} />
              <Route path="/genres" element={<GenresPage />} />
              <Route path="/login" element={<LoginForm />} />
              <Route path="/register" element={<RegisterForm />} />
            </Routes>
          </main>
          <Footer />
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
