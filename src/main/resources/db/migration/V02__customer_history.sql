
CREATE TABLE customer_history(
    id                  BIGSERIAL       NOT NULL REFERENCES customer(id),
    -- TODO : ensure version is increment by customer, not for each history
    version             BIGSERIAL       NOT NULL,
    timestamp           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    firstname           VARCHAR(60)     ,
    lastname            VARCHAR(60)     ,
    phone_number        VARCHAR(30)     ,
    email               VARCHAR(100)    ,
    PRIMARY KEY (id, version)
);