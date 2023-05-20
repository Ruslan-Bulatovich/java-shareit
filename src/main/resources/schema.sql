CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    name  VARCHAR(100)                                        NOT NULL,
    email VARCHAR(100)                                        NOT NULL,
    CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);
CREATE TABLE IF NOT EXISTS items
(
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    name        VARCHAR(100)                                        NOT NULL,
    description VARCHAR(1000)                                       NOT NULL,
    available   Boolean                                             NOT NULL,
    user_id     BIGINT REFERENCES users (id) ON DELETE CASCADE      NOT NULL
);

CREATE TABLE IF NOT EXISTS bookings
(
    id            BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    item_id       BIGINT REFERENCES items (id) ON DELETE CASCADE,
    booker_id     BIGINT REFERENCES users (id) ON DELETE CASCADE,
    status        VARCHAR(100),
    start_booking TIMESTAMP WITHOUT TIME ZONE,
    end_booking   TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE IF NOT EXISTS comments
(
    id        BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    text      VARCHAR(1000),
    item_id   BIGINT REFERENCES items (id) ON DELETE CASCADE,
    author_id BIGINT REFERENCES users (id) ON DELETE CASCADE,
    created   TIMESTAMP WITH TIME ZONE
);
