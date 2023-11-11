-- currencies table
CREATE TABLE IF NOT EXISTS currencies
(
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    code VARCHAR(3) NOT NULL,
    full_name VARCHAR(70) NOT NULL,
    sign VARCHAR(5) NOT NULL
);

CREATE UNIQUE INDEX currency_code_index
    ON currencies (code);

--DROP TABLE IF EXISTS currencies;

INSERT INTO currencies (code, full_name, sign)
VALUES ('USD', 'US Dollar', '$'), --1
       ('EUR', 'Euro', '€'), --2
       ('BYN', 'Belarusian Ruble', 'Br'), --3
       ('RUB', 'Russian Ruble', '₽'), --4
       ('UAH', 'Hryvnia', '₴'), --5
       ('PLN', 'Zloty', 'zł'), --6
       ('KZT', 'Tenge', '₸'), --7,
       ('CZK', 'Czech Koruna', 'Kč'), --8
       ('TRY', 'Turkish Lira', '₺'), --9
       ('JPY', 'Yen', '¥'); -- 10

--exchange rates table
CREATE TABLE IF NOT EXISTS exchange_rates (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    base_currency_id INTEGER,
    target_currency_id INTEGER,
    rate DECIMAL(6),
    FOREIGN KEY (base_currency_id) REFERENCES currencies(id) ON DELETE CASCADE,
    FOREIGN KEY (target_currency_id) REFERENCES currencies(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX exchange_pair_index
    ON exchange_rates (base_currency_id, target_currency_id);

--DROP TABLE IF EXISTS exchange_rates;

INSERT INTO exchange_rates (base_currency_id, target_currency_id, rate)
VALUES (1, 2, 0.9367),
       (1, 3, 3.1764),
       (1, 4, 92.0109),
       (1, 5, 36.1057),
       (1, 6, 4.1387),
       (1, 7, 467.8745),
       (2, 1, 1.0676),
       (2, 3, 3.3911),
       (2, 4, 98.2301),
       (2, 5, 38.5462),
       (2, 6, 4.4185),
       (2, 7, 499.4992),
       (3, 1, 0.3148),
       (3, 2, 0.2949),
       (3, 4, 28.967),
       (3, 5, 11.3669),
       (3, 6, 1.303),
       (3, 7, 147.2971),
       (2, 8, 24.4961),
       (1, 9, 28.5519),
       (1, 10, 151.459);