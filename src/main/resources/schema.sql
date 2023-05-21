DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS items CASCADE;
DROP TABLE IF EXISTS requests CASCADE;
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS comments CASCADE;

CREATE TABLE IF NOT EXISTS users
(
    user_id
          BIGINT
        GENERATED
            BY
            DEFAULT AS
            IDENTITY
        PRIMARY
            KEY,
    name
          VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    CONSTRAINT UQ_USER_EMAIL UNIQUE
        (
         email
            )
);

CREATE TABLE IF NOT EXISTS requests
(
    request_id
                 BIGINT
        GENERATED
            BY
            DEFAULT AS
            IDENTITY
        PRIMARY
            KEY,
    description
                 VARCHAR(500)          NOT NULL,
    requester_id BIGINT REFERENCES users
        (
         user_id
            ) ON DELETE RESTRICT,
    created      TIMESTAMP
                     WITHOUT TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS items
(
    item_id
                 BIGINT
        GENERATED
            BY
            DEFAULT AS
            IDENTITY
        PRIMARY
            KEY,
    name
                 VARCHAR(255) NOT NULL,
    description  VARCHAR(500) NOT NULL,
    is_available boolean default true,
    owner_id     BIGINT REFERENCES users
        (
         user_id
            ) ON DELETE RESTRICT,
    request_id   BIGINT REFERENCES requests
        (
         request_id
            )
        ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS bookings
(
    booking_id
              BIGINT
        GENERATED
            BY
            DEFAULT AS
            IDENTITY
        PRIMARY
            KEY,
    start_date
              TIMESTAMP
                  WITHOUT
                      TIME
                      ZONE
        NOT
            NULL,
    end_date
              TIMESTAMP
                  WITHOUT
                      TIME
                      ZONE
        NOT
            NULL,
    item_id
              BIGINT
        REFERENCES
            items
                (
                 item_id
                    ) ON DELETE RESTRICT,
    booker_id BIGINT REFERENCES users
        (
         user_id
            )
        ON DELETE RESTRICT,
    status    enum('WAITING',
                  'APPROVED',
                  'REJECTED',
                  'CANCELED')
);

CREATE TABLE IF NOT EXISTS comments
(
    comment_id
              BIGINT
        GENERATED
            BY
            DEFAULT AS
            IDENTITY
        PRIMARY
            KEY,
    text
              VARCHAR(500)          NOT NULL,
    item_id   BIGINT REFERENCES items
        (
         item_id
            ) ON DELETE RESTRICT,
    author_id BIGINT REFERENCES users
        (
         user_id
            )
        ON DELETE RESTRICT,
    created   TIMESTAMP
                  WITHOUT TIME ZONE NOT NULL
);

