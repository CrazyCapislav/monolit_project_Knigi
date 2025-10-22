import React, { useEffect, useState } from 'react';
import './Toast.css';

const Toast = ({ message, type = 'success', isVisible, onClose, duration = 3000 }) => {
    const [isAnimating, setIsAnimating] = useState(false);
    const [shouldRender, setShouldRender] = useState(false);

    useEffect(() => {
        if (isVisible) {
            // Показываем компонент
            setShouldRender(true);
            // Небольшая задержка для запуска анимации въезда
            setTimeout(() => setIsAnimating(true), 10);

            // Автоматическое закрытие через duration
            if (duration > 0) {
                const timer = setTimeout(() => {
                    handleClose();
                }, duration);
                return () => clearTimeout(timer);
            }
        } else {
            handleClose();
        }
    }, [isVisible, duration]);

    const handleClose = () => {
        // Запускаем анимацию выезда
        setIsAnimating(false);

        // Ждем завершения анимации перед удалением из DOM
        setTimeout(() => {
            setShouldRender(false);
            onClose();
        }, 300); // 300ms - время анимации
    };

    // Не рендерим если не нужно
    if (!shouldRender) return null;

    const icons = {
        success: '✓',
        error: '✕',
        warning: '⚠',
        info: 'ℹ'
    };

    return (
        <div className={`toast toast-${type} ${isAnimating ? 'toast-visible' : ''}`}>
            <div className="toast-icon">{icons[type]}</div>
            <div className="toast-message">{message}</div>
            <button className="toast-close" onClick={handleClose}>✕</button>
        </div>
    );
};

export default Toast;
