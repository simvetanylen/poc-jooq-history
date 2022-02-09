
CREATE TABLE customer(
    id                  BIGSERIAL       NOT NULL PRIMARY KEY,
    firstname           VARCHAR(60)     ,
    lastname            VARCHAR(60)     ,
    phone_number        VARCHAR(30)     ,
    email               VARCHAR(100)    ,
    create_timestamp    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_timestamp    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);