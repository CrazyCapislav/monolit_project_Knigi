import React from 'react';
import './Footer.css';

const Footer = () => {
  return (
    <footer className="footer">
      <div className="footer-container">
        <p className="footer-text">© 2025 BookSwap. Все права защищены.</p>
        <div className="footer-links">
          <a href="#" className="footer-link">О нас</a>
          <a href="#" className="footer-link">Контакты</a>
          <a href="#" className="footer-link">Помощь</a>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
