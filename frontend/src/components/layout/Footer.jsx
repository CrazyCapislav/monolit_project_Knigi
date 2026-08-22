import React from 'react';
import './Footer.css';

const Footer = () => {
  return (
    <footer className="footer">
      <div className="footer-container">
        <p className="footer-text">© 2025 BookSwap. Все права защищены.</p>
        <div className="footer-links">
          {/* Разделы ещё не реализованы: кнопки вместо ссылок с href="#",
              которые ломают навигацию с клавиатуры и скринридеры. */}
          <button type="button" className="footer-link">О нас</button>
          <button type="button" className="footer-link">Контакты</button>
          <button type="button" className="footer-link">Помощь</button>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
