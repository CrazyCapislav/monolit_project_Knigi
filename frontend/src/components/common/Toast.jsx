import React, { useCallback, useEffect, useRef, useState } from 'react';
import './Toast.css';

const Toast = ({ message, type = 'success', isVisible, onClose, duration = 3000 }) => {
    const [isAnimating, setIsAnimating] = useState(false);
    const [shouldRender, setShouldRender] = useState(false);

    // onClose приходит из родителя и обычно пересоздаётся на каждый рендер.
    // Держим его в ref, чтобы handleClose оставался стабильным: иначе эффект
    // ниже перезапускался бы на каждый рендер и менял состояние по кругу.
    const onCloseRef = useRef(onClose);
    useEffect(() => {
        onCloseRef.current = onClose;
    });

    const handleClose = useCallback(() => {
        setIsAnimating(false);

        setTimeout(() => {
            setShouldRender(false);
            onCloseRef.current?.();
        }, 300);
    }, []);

    useEffect(() => {
        if (isVisible) {
            setShouldRender(true);
            const appear = setTimeout(() => setIsAnimating(true), 10);

            if (duration > 0) {
                const timer = setTimeout(handleClose, duration);
                return () => {
                    clearTimeout(appear);
                    clearTimeout(timer);
                };
            }

            return () => clearTimeout(appear);
        }

        handleClose();
        return undefined;
    }, [isVisible, duration, handleClose]);

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
