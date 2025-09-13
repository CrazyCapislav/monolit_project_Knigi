-- Роли/пользователи
create table users (
    id           bigserial primary key,
    email        varchar(320) not null unique,
    password_hash varchar(255) not null,
    display_name varchar(120) not null,
    role         varchar(20)  not null, -- USER, ADMIN, PUBLISHER
    created_at   timestamptz  not null default now()
);

-- Жанры
create table genre (
    id   bigserial primary key,
    name varchar(80) not null unique
);

-- Книги (указываем владельца - обычный пользователь или издательство)
create table book (
    id           bigserial primary key,
    title        varchar(255) not null,
    author       varchar(255) not null,
    isbn         varchar(32),
    published_year int,
    owner_id     bigint not null references users(id) on delete cascade,
    status       varchar(20) not null, -- AVAILABLE, RESERVED, EXCHANGED
    condition    varchar(20) not null, -- NEW, GOOD, FAIR, BAD
    created_at   timestamptz not null default now()
);

-- M:N книга-жанр
create table book_genre (
    book_id  bigint not null references book(id) on delete cascade,
    genre_id bigint not null references genre(id) on delete cascade,
    primary key (book_id, genre_id)
);

-- M:N с доп полями: рейтинг книги пользователем
create table book_rating (
    user_id bigint not null references users(id) on delete cascade,
    book_id bigint not null references book(id) on delete cascade,
    rating  int    not null check (rating between 1 and 5),
    comment text,
    rated_at timestamptz not null default now(),
    primary key (user_id, book_id)
);

-- Заявка на обмен (между двумя пользователями)
create table exchange_request (
    id             bigserial primary key,
    requester_id   bigint not null references users(id) on delete cascade,
    owner_id       bigint not null references users(id) on delete cascade,
    book_requested bigint not null references book(id) on delete cascade,
    book_offered   bigint references book(id) on delete set null,
    status         varchar(20) not null, -- WAITING, ACCEPTED, DECLINED, COMPLETED
    created_at     timestamptz not null default now(),
    updated_at     timestamptz
);

-- Заявка в издательство на выпуск книги
create table publication_request (
    id           bigserial primary key,
    requester_id bigint not null references users(id) on delete cascade,
    publisher_id bigint not null references users(id) on delete cascade, -- user с ролью PUBLISHER
    title        varchar(255) not null,
    author       varchar(255) not null,
    message      text,
    status       varchar(20) not null, -- SUBMITTED, REVIEW, APPROVED, REJECTED
    created_at   timestamptz not null default now(),
    decided_at   timestamptz
);

-- Индексы для поиска/ленты
create index idx_book_created on book(created_at desc, id desc);
create index idx_exchange_status on exchange_request(status);
create index idx_publication_status on publication_request(status);
