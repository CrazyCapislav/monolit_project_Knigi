# BookSwap React App

React-приложение для обмена книгами.

## Структура проекта

```
src/
├── components/       # Все React компоненты
│   ├── layout/      # Header, Footer
│   ├── common/      # Button, Input, Card, Modal, Loader
│   ├── books/       # BookCard, BookList, BookForm
│   ├── exchanges/   # Компоненты обменов
│   ├── publications/# Компоненты публикаций
│   └── auth/        # LoginForm, RegisterForm
├── pages/           # Страницы приложения
├── services/        # API сервисы
├── hooks/           # Custom React hooks
├── context/         # React Context (AuthContext)
├── utils/           # Утилиты и константы
├── App.jsx          # Главный компонент
└── index.js         # Точка входа
```

## Установка и запуск

1. Установите зависимости:
```bash
npm install
```

2. Убедитесь, что бэкенд запущен на `http://localhost:8080`

3. Запустите приложение:
```bash
npm start
```

Приложение откроется по адресу [http://localhost:3000](http://localhost:3000)

## Функционал

- 📚 Каталог книг с фильтрацией и пагинацией
- ➕ Добавление своих книг
- 🔄 Заявки на обмен книгами
- 📝 Заявки на публикацию
- 🏷️ Управление жанрами
- 👤 Профиль пользователя
- 🔐 Регистрация и авторизация

## Технологии

- React 18
- React Router v6
- CSS Modules
- Fetch API
- Context API

## API

Приложение работает с бэкендом на `http://localhost:8080/api/v1`

Авторизация через заголовок `X-User-Id`
