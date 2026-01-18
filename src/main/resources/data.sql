-- password for admin is admin123123123 and for test users is test123123123
INSERT INTO users (username, password, email, role) VALUES
('admin', '$2a$10$ZspPYGOmntZn4UenheGUSub8HvUIx3em.QBUcH3noGuU8NseC2f9O', 'admin@library.onlajn.com', 'ADMIN'),
('test2', '$2a$10$W0KLl6KVyiXBljW4OP5K0OYbMfr.STP7GVsIax/OcMdA1j8zbf5j2', 'test2@test.com', 'USER'),
('test', '$2a$10$W0KLl6KVyiXBljW4OP5K0OYbMfr.STP7GVsIax/OcMdA1j8zbf5j2', 'test@test.com', 'USER');

INSERT INTO authors (name, last_name, bio) VALUES
('J.K.', 'Rowling', 'Autorka Harrego Piotera'),
('George R.R.', 'Martin', 'Podający się za pisarza człowiek, matka smoków'),
('J.R.R. ', 'Tolkien', 'Pionier fantatyki');

INSERT INTO genres (name) VALUES
('Fantasy'),
('Przygodowe'),
('Drama'),
('Akcja');

INSERT INTO books (title, description, publisher, publish_year, isbn, copies_available) VALUES
('Harry Potter i Kamień filozoficzny', 'Pierwsza książka o Horym Pioterze', 'Scholastic', 1997, '9780590353403', 5),
('Harry Potter i Komnata Tajemnic', 'Druga (tzn. gorsza) książka o Horym Pioterze', 'Scholastic', 1999, '9780590353403', 1),
('Gra o tron', 'Pierwsza nowela, która zainspirowała netfliksa', 'Bantam Spectra', 1996, '9780553103540', 3),
('Hobbit', 'Trochę krótka książka o pewnym krótkim człowieczki', 'George Allen & Unwin', 1937, '9780261102217', 10);

INSERT INTO book_authors (book_id, author_id) VALUES
(1, 1),
(2, 1),
(3, 2),
(4, 3);

INSERT INTO book_genres (book_id, genre_id) VALUES
(1, 1),
(1, 2),
(2, 1),
(2, 2),
(3, 1),
(3, 3),
(4, 1),
(4, 2),
(4, 4);

INSERT INTO reservations (user_id, book_id, status) VALUES
(2, 1, 'POTWIERDZONA'),
(3, 2, 'OCZEKUJĄCA');

INSERT INTO ratings (user_id, book_id, rating) VALUES
(2, 1, 5),
(3, 1, 4),
(2, 3, 5);

UPDATE books SET average_rating = (SELECT AVG(rating) FROM ratings WHERE ratings.book_id = books.id)
WHERE EXISTS (SELECT 1 FROM ratings WHERE ratings.book_id = books.id);

INSERT INTO comments (user_id, book_id, content) VALUES
(2, 1, 'Świetna ksionszka'),
(3, 1, 'Lubiem'),
(2, 3, 'Fajnie się czytało');
