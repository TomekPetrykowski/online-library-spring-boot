-- Seed Users (Password is 'password' encoded with BCrypt)
INSERT INTO users (username, password, email, role) VALUES
('admin', '$2a$10$vNfO24u2Y0b9HkYjY6H9.O.X.j/W6Zp0G0v.Q5.S8Y.M2L.I6P.m.', 'admin@library.com', 'ADMIN'),
('user1', '$2a$10$vNfO24u2Y0b9HkYjY6H9.O.X.j/W6Zp0G0v.Q5.S8Y.M2L.I6P.m.', 'user1@example.com', 'USER'),
('user2', '$2a$10$vNfO24u2Y0b9HkYjY6H9.O.X.j/W6Zp0G0v.Q5.S8Y.M2L.I6P.m.', 'user2@example.com', 'USER');

-- Seed Authors
INSERT INTO authors (name, bio) VALUES
('J.K. Rowling', 'British author, best known for the Harry Potter series.'),
('George R.R. Martin', 'American novelist and short-story writer in the fantasy, horror, and science fiction genres.'),
('J.R.R. Tolkien', 'English writer, poet, philologist, and academic, best known as the author of The Hobbit and The Lord of the Rings.');

-- Seed Genres
INSERT INTO genres (name) VALUES
('Fantasy'),
('Adventure'),
('Drama'),
('Action');

-- Seed Books
INSERT INTO books (title, description, publisher, publish_year, isbn, copies_available) VALUES
('Harry Potter and the Sorcerer''s Stone', 'The first book in the Harry Potter series.', 'Scholastic', 1997, '9780590353403', 5),
('A Game of Thrones', 'The first novel in A Song of Ice and Fire.', 'Bantam Spectra', 1996, '9780553103540', 3),
('The Hobbit', 'A fantasy novel and children''s book.', 'George Allen & Unwin', 1937, '9780261102217', 10);

-- Link Books and Authors
INSERT INTO book_authors (book_id, author_id) VALUES
(1, 1),
(2, 2),
(3, 3);

-- Link Books and Genres
INSERT INTO book_genres (book_id, genre_id) VALUES
(1, 1),
(1, 2),
(2, 1),
(2, 3),
(3, 1),
(3, 2);

-- Seed Reservations
INSERT INTO reservations (user_id, book_id, status) VALUES
(2, 1, 'POTWIERDZONA'),
(3, 2, 'OCZEKUJĄCA');

-- Seed Ratings
INSERT INTO ratings (user_id, book_id, rating) VALUES
(2, 1, 5),
(3, 1, 4),
(2, 3, 5);

-- Update Average Ratings
UPDATE books SET average_rating = (SELECT AVG(rating) FROM ratings WHERE ratings.book_id = books.id)
WHERE EXISTS (SELECT 1 FROM ratings WHERE ratings.book_id = books.id);

-- Seed Comments
INSERT INTO comments (user_id, book_id, content) VALUES
(2, 1, 'Amazing book! A must read.'),
(3, 1, 'Great start to the series.'),
(2, 3, 'Classic fantasy adventure.');
