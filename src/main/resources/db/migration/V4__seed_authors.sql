-- ============================================================================
-- Migration: V4__seed_authors.sql
-- Description: Seed Authors Directory & Book Authors Junction Records
-- ============================================================================

-- 1. Insert Authors Directory
INSERT INTO authors (id, name, slug, biography, profile_image_url)
VALUES
    (
        'a0000000-0000-0000-0000-000000000001',
        'Robert C. Martin',
        'robert-c-martin',
        'Robert Cecil Martin, colloquially known as "Uncle Bob", is an American software engineer, author, and international speaker. He is best known for being one of the co-authors of the Agile Manifesto and for popularizing the SOLID software design principles.',
        'https://images.bookworm.example.com/authors/uncle-bob.jpg'
    ),
    (
        'a0000000-0000-0000-0000-000000000002',
        'Martin Kleppmann',
        'martin-kleppmann',
        'Martin Kleppmann is an Associate Professor in Computer Science at the University of Cambridge, specialising in distributed systems, local-first software, CRDTs, and security.',
        'https://images.bookworm.example.com/authors/martin-kleppmann.jpg'
    ),
    (
        'a0000000-0000-0000-0000-000000000003',
        'Eric Evans',
        'eric-evans',
        'Eric Evans is a thought leader in software design and domain modeling. He founded Domain Language, a consulting group dedicated to helping teams build software deeply aligned with business core domains.',
        'https://images.bookworm.example.com/authors/eric-evans.jpg'
    ),
    (
        'a0000000-0000-0000-0000-000000000004',
        'James Clear',
        'james-clear',
        'James Clear is an author and speaker focused on habits, decision making, and continuous improvement. His work frequently appears in the New York Times, Entrepreneur, and Time Magazine.',
        'https://images.bookworm.example.com/authors/james-clear.jpg'
    ),
    (
        'a0000000-0000-0000-0000-000000000005',
        'Peter Thiel',
        'peter-thiel',
        'Peter Thiel is an entrepreneur, venture capitalist, and hedge fund manager. He co-founded PayPal, Palantir Technologies, and Founders Fund, and was the first outside investor in Facebook.',
        'https://images.bookworm.example.com/authors/peter-thiel.jpg'
    ),
    (
        'a0000000-0000-0000-0000-000000000006',
        'Blake Masters',
        'blake-masters',
        'Blake Masters is an American venture capitalist, attorney, and author. He co-authored Zero to One with Peter Thiel based on notes taken during Stanford University lectures.',
        'https://images.bookworm.example.com/authors/blake-masters.jpg'
    ),
    (
        'a0000000-0000-0000-0000-000000000007',
        'Yuval Noah Harari',
        'yuval-noah-harari',
        'Yuval Noah Harari is an Israeli historian, philosopher, and professor in the Department of History at the Hebrew University of Jerusalem. He is the author of global bestsellers Sapiens, Homo Deus, and 21 Lessons for the 21st Century.',
        'https://images.bookworm.example.com/authors/yuval-harari.jpg'
    ),
    (
        'a0000000-0000-0000-0000-000000000008',
        'Morgan Housel',
        'morgan-housel',
        'Morgan Housel is a partner at Collaborative Fund and a former columnist at The Wall Street Journal and The Motley Fool. He is a two-time winner of the Best in Business Award from the Society of American Business Editors and Writers.',
        'https://images.bookworm.example.com/authors/morgan-housel.jpg'
    ),
    (
        'a0000000-0000-0000-0000-000000000009',
        'Cal Newport',
        'cal-newport',
        'Cal Newport is a Professor of Computer Science at Georgetown University and the author of eight books, including Deep Work, Digital Minimalism, and A World Without Email.',
        'https://images.bookworm.example.com/authors/cal-newport.jpg'
    ),
    (
        'a0000000-0000-0000-0000-000000000010',
        'Andy Weir',
        'andy-weir',
        'Andy Weir is an American novelist and former software engineer. His breakthrough debut novel, The Martian, was adapted into a blockbuster film, followed by Artemis and Project Hail Mary.',
        'https://images.bookworm.example.com/authors/andy-weir.jpg'
    ),
    (
        'a0000000-0000-0000-0000-000000000011',
        'David Thomas',
        'david-thomas',
        'David Thomas is a programmer, author, and editor. He co-authored The Pragmatic Programmer and was one of the original signatories of the Agile Manifesto.',
        'https://images.bookworm.example.com/authors/david-thomas.jpg'
    ),
    (
        'a0000000-0000-0000-0000-000000000012',
        'Andrew Hunt',
        'andrew-hunt',
        'Andrew Hunt is an agile software pioneer, musician, and author of books on software development, pragmatic thinking, and learning systems.',
        'https://images.bookworm.example.com/authors/andrew-hunt.jpg'
    )
ON CONFLICT (slug) WHERE is_deleted = FALSE DO UPDATE
SET name = EXCLUDED.name,
    biography = EXCLUDED.biography,
    profile_image_url = EXCLUDED.profile_image_url;

-- 2. Link Books and Authors in Junction Table (book_authors)
INSERT INTO book_authors (id, book_id, author_id, author_order)
VALUES
    -- Clean Code -> Robert C. Martin
    ('ba000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', 1),
    -- Designing Data-Intensive Applications -> Martin Kleppmann
    ('ba000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000002', 1),
    -- Domain-Driven Design -> Eric Evans
    ('ba000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000003', 1),
    -- Atomic Habits -> James Clear
    ('ba000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000004', 'a0000000-0000-0000-0000-000000000004', 1),
    -- Zero to One -> Peter Thiel (Lead) & Blake Masters (Co-author)
    ('ba000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000005', 'a0000000-0000-0000-0000-000000000005', 1),
    ('ba000000-0000-0000-0000-000000000006', 'b0000000-0000-0000-0000-000000000005', 'a0000000-0000-0000-0000-000000000006', 2),
    -- Sapiens -> Yuval Noah Harari
    ('ba000000-0000-0000-0000-000000000007', 'b0000000-0000-0000-0000-000000000006', 'a0000000-0000-0000-0000-000000000007', 1),
    -- The Psychology of Money -> Morgan Housel
    ('ba000000-0000-0000-0000-000000000008', 'b0000000-0000-0000-0000-000000000007', 'a0000000-0000-0000-0000-000000000008', 1),
    -- Deep Work -> Cal Newport
    ('ba000000-0000-0000-0000-000000000009', 'b0000000-0000-0000-0000-000000000008', 'a0000000-0000-0000-0000-000000000009', 1),
    -- Project Hail Mary -> Andy Weir
    ('ba000000-0000-0000-0000-000000000010', 'b0000000-0000-0000-0000-000000000009', 'a0000000-0000-0000-0000-000000000010', 1),
    -- The Pragmatic Programmer -> David Thomas (Lead) & Andrew Hunt (Co-author)
    ('ba000000-0000-0000-0000-000000000011', 'b0000000-0000-0000-0000-000000000010', 'a0000000-0000-0000-0000-000000000011', 1),
    ('ba000000-0000-0000-0000-000000000012', 'b0000000-0000-0000-0000-000000000010', 'a0000000-0000-0000-0000-000000000012', 2)
ON CONFLICT (book_id, author_id) WHERE is_deleted = FALSE DO UPDATE
SET author_order = EXCLUDED.author_order;
