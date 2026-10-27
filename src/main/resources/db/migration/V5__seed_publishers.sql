-- ============================================================================
-- Migration: V5__seed_publishers.sql
-- Description: Seed Publishers Directory & Associate Books to Publishers
-- ============================================================================

-- 1. Insert Publishers Directory
INSERT INTO publishers (id, name, slug, description, website)
VALUES
    (
        'p0000000-0000-0000-0000-000000000001',
        'Pearson Education / Addison-Wesley',
        'pearson-addison-wesley',
        'Addison-Wesley Professional produces content, learning tools, and resources for professional software developers, engineers, and tech managers.',
        'https://www.informit.com'
    ),
    (
        'p0000000-0000-0000-0000-000000000002',
        'O''Reilly Media',
        'oreilly-media',
        'O''Reilly Media spreads the knowledge of innovators through books, online learning platforms, and world-class tech conferences.',
        'https://www.oreilly.com'
    ),
    (
        'p0000000-0000-0000-0000-000000000003',
        'Penguin Random House',
        'penguin-random-house',
        'Penguin Random House is the world’s largest general trade book publisher, fostering a passion for reading and diverse storytelling.',
        'https://www.penguinrandomhouse.com'
    ),
    (
        'p0000000-0000-0000-0000-000000000004',
        'HarperCollins Publishers',
        'harpercollins',
        'HarperCollins is one of the world''s leading English-language publishers, with a history spanning over 200 years.',
        'https://www.harpercollins.com'
    ),
    (
        'p0000000-0000-0000-0000-000000000005',
        'Hachette Book Group',
        'hachette-book-group',
        'Hachette Book Group is a leading trade publisher in the United States, publishing great books under renowned imprints like Grand Central and Basic Books.',
        'https://www.hachettebookgroup.com'
    ),
    (
        'p0000000-0000-0000-0000-000000000006',
        'Harriman House',
        'harriman-house',
        'Harriman House is an independent publisher of business, finance, and economics books, known for high-impact commercial titles.',
        'https://www.harriman-house.com'
    ),
    (
        'p0000000-0000-0000-0000-000000000007',
        'Ballantine Books',
        'ballantine-books',
        'Ballantine Books is an American book publisher founded in 1952, known for groundbreaking science fiction, fantasy, and trade fiction.',
        'https://www.penguinrandomhouse.com/imprints/BA/ballantine-books'
    )
ON CONFLICT (slug) WHERE is_deleted = FALSE DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    website = EXCLUDED.website;

-- 2. Associate Books with their Respective Publishers
UPDATE books 
SET publisher_id = 'p0000000-0000-0000-0000-000000000001' 
WHERE id IN (
    'b0000000-0000-0000-0000-000000000001', -- Clean Code
    'b0000000-0000-0000-0000-000000000003', -- Domain-Driven Design
    'b0000000-0000-0000-0000-000000000010'  -- The Pragmatic Programmer
);

UPDATE books 
SET publisher_id = 'p0000000-0000-0000-0000-000000000002' 
WHERE id = 'b0000000-0000-0000-0000-000000000002'; -- Designing Data-Intensive Applications

UPDATE books 
SET publisher_id = 'p0000000-0000-0000-0000-000000000003' 
WHERE id IN (
    'b0000000-0000-0000-0000-000000000004', -- Atomic Habits
    'b0000000-0000-0000-0000-000000000005'  -- Zero to One
);

UPDATE books 
SET publisher_id = 'p0000000-0000-0000-0000-000000000004' 
WHERE id = 'b0000000-0000-0000-0000-000000000006'; -- Sapiens

UPDATE books 
SET publisher_id = 'p0000000-0000-0000-0000-000000000006' 
WHERE id = 'b0000000-0000-0000-0000-000000000007'; -- The Psychology of Money

UPDATE books 
SET publisher_id = 'p0000000-0000-0000-0000-000000000005' 
WHERE id = 'b0000000-0000-0000-0000-000000000008'; -- Deep Work

UPDATE books 
SET publisher_id = 'p0000000-0000-0000-0000-000000000007' 
WHERE id = 'b0000000-0000-0000-0000-000000000009'; -- Project Hail Mary
