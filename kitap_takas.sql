create table book_conditions
(
    id          int         not null
        primary key,
    `condition` varchar(50) null
);

create table book_genres
(
    id   int auto_increment
        primary key,
    name varchar(100) null
);

create table users
(
    id       int auto_increment
        primary key,
    name     varchar(100) not null,
    email    varchar(100) not null,
    password varchar(100) not null,
    location varchar(100) not null,
    constraint email_uk
        unique (email)
);

create table books
(
    id           int auto_increment
        primary key,
    title        varchar(150) not null,
    author       varchar(100) not null,
    description  text         not null,
    owner_id     int          not null,
    genre_id     int          not null,
    condition_id int          not null,
    constraint books_users__fk
        foreign key (owner_id) references users (id)
            on delete cascade,
    constraint condition_fk
        foreign key (condition_id) references book_conditions (id),
    constraint fk_genre
        foreign key (genre_id) references book_genres (id)
);

create table book_images
(
    id          int auto_increment
        primary key,
    book_id     int          null,
    image_url   varchar(250) null,
    uploaded_at datetime     null,
    constraint book_images_books_id_fk
        foreign key (book_id) references books (id)
);

create table logs
(
    id          int auto_increment
        primary key,
    user_id     int          null,
    action      varchar(100) null comment '''book_added'', ''swap_sent'' vb.',
    target_type varchar(50)  null comment '	book, user, request vb.',
    target_id   int          null,
    timestamp   datetime     null,
    details     text         null comment 'Ek bilgi / açıklama',
    constraint logs_users_id_fk
        foreign key (user_id) references users (id)
);

create table messages
(
    id          int auto_increment
        primary key,
    sender_id   int                                  null,
    receiver_id int                                  null,
    content     text                                 null,
    timestamp   datetime default current_timestamp() null,
    constraint messages_receiver_fk
        foreign key (receiver_id) references users (id),
    constraint messages_sender_fk
        foreign key (sender_id) references users (id)
);

create table notifications
(
    id         int auto_increment
        primary key,
    user_id    int         null,
    type       varchar(50) null comment '	''swap_request'', ''message'', ''info'' vb.',
    is_read    tinyint(1)  null,
    created_at datetime    null,
    constraint notifications_users_id_fk
        foreign key (user_id) references users (id)
);

create table reviews
(
    id          int auto_increment
        primary key,
    reviewer_id int null,
    reviewed_id int null,
    rating      int null,
    constraint reviewed_users_id_fk
        foreign key (reviewed_id) references users (id),
    constraint reviewer_users_id_fk
        foreign key (reviewer_id) references users (id),
    constraint check_rating_range
        check (`rating` between 1 and 5)
);

create table swap_request
(
    id                int auto_increment
        primary key,
    sender_id         int                                                                          null,
    receiver_id       int                                                                          null,
    book_offered_id   int                                                                          null,
    book_requested_id int                                                                          null,
    status            enum ('Beklemede', 'Kabul Edildi', 'Reddedildi') default 'Beklemede'         null,
    note              text                                                                         null,
    created_at        datetime                                                                     not null,
    updated_at        datetime                                         default current_timestamp() null on update current_timestamp(),
    constraint book_of_fk
        foreign key (book_offered_id) references books (id),
    constraint book_req_fk
        foreign key (book_requested_id) references books (id),
    constraint received_id
        foreign key (receiver_id) references users (id),
    constraint sender_fk
        foreign key (sender_id) references users (id)
);

create table wishlist
(
    id         int auto_increment
        primary key,
    user_id    int          null,
    book_title varchar(200) null,
    author     varchar(150) null,
    genre_id   int          null,
    created_at datetime     null,
    constraint wishlist_genre_fk
        foreign key (genre_id) references book_genres (id),
    constraint wishlist_users_id_fk
        foreign key (user_id) references users (id)
);


