-- ============================================================================
-- Migration: V3__seed_books.sql
-- Description: Seed Bestselling Books, Multi-Format Variants & Category Junctions
-- ============================================================================

-- 1. Insert Core Books Catalog
INSERT INTO books (id, title, subtitle, slug, synopsis, language, cover_image_url, published_date, copies_sold, average_rating, ratings_count, is_featured)
VALUES
    (
        'b0000000-0000-0000-0000-000000000001',
        'Clean Code',
        'A Handbook of Agile Software Craftsmanship',
        'clean-code',
        'Even bad code can function. But if code isn’t clean, it can bring a development organization to its knees. Every year, countless hours and significant resources are lost because of poorly written code. Clean Code helps developers write software that is readable, maintainable, and robust.',
        'English',
        'https://images.bookworm.example.com/covers/clean-code.jpg',
        '2008-08-01',
        18540,
        4.85,
        3420,
        TRUE
    ),
    (
        'b0000000-0000-0000-0000-000000000002',
        'Designing Data-Intensive Applications',
        'The Big Ideas Behind Reliable, Scalable, and Maintainable Systems',
        'designing-data-intensive-applications',
        'Data is at the center of many challenges in system design today. Difficult issues need to be figured out, such as scalability, consistency, reliability, efficiency, and maintainability. Martin Kleppmann provides a comprehensive tour of the landscape of data storage and processing technologies.',
        'English',
        'https://images.bookworm.example.com/covers/ddia.jpg',
        '2017-03-16',
        24890,
        4.95,
        5120,
        TRUE
    ),
    (
        'b0000000-0000-0000-0000-000000000003',
        'Domain-Driven Design',
        'Tackling Complexity in the Heart of Software',
        'domain-driven-design',
        'This book provides a systematic approach to domain-driven design, presenting an extensive set of modeling best practices, design patterns, and fundamental principles that facilitate the development of software projects facing complex domains.',
        'English',
        'https://images.bookworm.example.com/covers/ddd.jpg',
        '2003-08-30',
        12400,
        4.78,
        2150,
        TRUE
    ),
    (
        'b0000000-0000-0000-0000-000000000004',
        'Atomic Habits',
        'An Easy & Proven Way to Build Good Habits & Break Bad Ones',
        'atomic-habits',
        'No matter your goals, Atomic Habits offers a proven framework for improving every day. James Clear, one of the world’s leading experts on habit formation, reveals practical strategies that will teach you exactly how to form good habits, break bad ones, and master the tiny behaviors that lead to remarkable results.',
        'English',
        'https://images.bookworm.example.com/covers/atomic-habits.jpg',
        '2018-10-16',
        54200,
        4.92,
        9870,
        TRUE
    ),
    (
        'b0000000-0000-0000-0000-000000000005',
        'Zero to One',
        'Notes on Startups, or How to Build the Future',
        'zero-to-one',
        'The great secret of our time is that there are still uncharted frontiers to explore and new inventions to create. In Zero to One, legendary entrepreneur and investor Peter Thiel shows how we can find singular ways to create those new things.',
        'English',
        'https://images.bookworm.example.com/covers/zero-to-one.jpg',
        '2014-09-16',
        31200,
        4.65,
        4300,
        FALSE
    ),
    (
        'b0000000-0000-0000-0000-000000000006',
        'Sapiens',
        'A Brief History of Humankind',
        'sapiens-a-brief-history-of-humankind',
        'One hundred thousand years ago, at least six different species of humans inhabited Earth. Yet today there is only one—homo sapiens. What happened to the others? And what may happen to us? Yuval Noah Harari spans the whole of human history from the very first humans to walk the earth to the radical revolutions of the cognitive, agricultural, and scientific eras.',
        'English',
        'https://images.bookworm.example.com/covers/sapiens.jpg',
        '2015-02-10',
        42100,
        4.88,
        8640,
        TRUE
    ),
    (
        'b0000000-0000-0000-0000-000000000007',
        'The Psychology of Money',
        'Timeless Lessons on Wealth, Greed, and Happiness',
        'the-psychology-of-money',
        'Doing well with money isn’t necessarily about what you know. It’s about how you behave. And behavior is hard to teach, even to really smart people. Morgan Housel shares 19 short stories exploring the strange ways people think about money.',
        'English',
        'https://images.bookworm.example.com/covers/psychology-of-money.jpg',
        '2020-09-08',
        38900,
        4.82,
        6540,
        TRUE
    ),
    (
        'b0000000-0000-0000-0000-000000000008',
        'Deep Work',
        'Rules for Focused Success in a Distracted World',
        'deep-work',
        'Deep work is the ability to focus without distraction on a cognitively demanding task. It is a super power in our increasingly competitive twenty-first century economy. Cal Newport lays out four actionable rules for transforming your mind and habits.',
        'English',
        'https://images.bookworm.example.com/covers/deep-work.jpg',
        '2016-01-05',
        19800,
        4.74,
        3820,
        FALSE
    ),
    (
        'b0000000-0000-0000-0000-000000000009',
        'Project Hail Mary',
        'A Novel',
        'project-hail-mary',
        'Ryland Grace is the sole survivor on a desperate, last-chance mission—and if he fails, humanity and the earth itself are doomed. Except right now, he doesn’t know that. He can’t even remember his own name, let alone the nature of his assignment or how to complete it.',
        'English',
        'https://images.bookworm.example.com/covers/project-hail-mary.jpg',
        '2021-05-04',
        27500,
        4.94,
        7100,
        TRUE
    ),
    (
        'b0000000-0000-0000-0000-000000000010',
        'The Pragmatic Programmer',
        'Your Journey to Mastery (20th Anniversary Edition)',
        'the-pragmatic-programmer',
        'The Pragmatic Programmer is one of those rare tech audiobooks/books you will read, re-read, and read again over the years. Whether you’re new to the field or an experienced practitioner, you’ll come away each time with fresh insights.',
        'English',
        'https://images.bookworm.example.com/covers/pragmatic-programmer.jpg',
        '2019-09-13',
        22400,
        4.89,
        4180,
        TRUE
    )
ON CONFLICT (slug) WHERE is_deleted = FALSE DO UPDATE
SET title = EXCLUDED.title,
    subtitle = EXCLUDED.subtitle,
    synopsis = EXCLUDED.synopsis,
    cover_image_url = EXCLUDED.cover_image_url,
    copies_sold = EXCLUDED.copies_sold,
    average_rating = EXCLUDED.average_rating,
    ratings_count = EXCLUDED.ratings_count,
    is_featured = EXCLUDED.is_featured;

-- 2. Insert Multi-Format Book Variants (Paperback, Hardcover, Ebook, Audio)
INSERT INTO book_variants (id, book_id, format_type, isbn, sku, list_price, selling_price, currency, stock_quantity, weight_grams, page_count, is_active)
VALUES
    -- Clean Code
    ('v0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 'PAPERBACK', '9780132350884', 'CC-PB-01', 799.00, 599.00, 'INR', 150, 420, 464, TRUE),
    ('v0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000001', 'HARDCOVER', '9780132350885', 'CC-HC-01', 1299.00, 999.00, 'INR', 45, 680, 464, TRUE),
    ('v0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000001', 'EBOOK', '9780132350886', 'CC-EB-01', 499.00, 399.00, 'INR', 9999, NULL, NULL, TRUE),

    -- Designing Data-Intensive Applications
    ('v0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000002', 'PAPERBACK', '9781449373320', 'DDIA-PB-01', 1499.00, 1199.00, 'INR', 200, 850, 616, TRUE),
    ('v0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000002', 'HARDCOVER', '9781449373321', 'DDIA-HC-01', 2199.00, 1799.00, 'INR', 60, 1100, 616, TRUE),
    ('v0000000-0000-0000-0000-000000000006', 'b0000000-0000-0000-0000-000000000002', 'EBOOK', '9781449373322', 'DDIA-EB-01', 899.00, 699.00, 'INR', 9999, NULL, NULL, TRUE),

    -- Domain-Driven Design
    ('v0000000-0000-0000-0000-000000000007', 'b0000000-0000-0000-0000-000000000003', 'HARDCOVER', '9780321125217', 'DDD-HC-01', 1899.00, 1499.00, 'INR', 80, 920, 560, TRUE),
    ('v0000000-0000-0000-0000-000000000008', 'b0000000-0000-0000-0000-000000000003', 'EBOOK', '9780321125218', 'DDD-EB-01', 799.00, 599.00, 'INR', 9999, NULL, NULL, TRUE),

    -- Atomic Habits
    ('v0000000-0000-0000-0000-000000000009', 'b0000000-0000-0000-0000-000000000004', 'PAPERBACK', '9781847941831', 'AH-PB-01', 599.00, 449.00, 'INR', 350, 320, 320, TRUE),
    ('v0000000-0000-0000-0000-000000000010', 'b0000000-0000-0000-0000-000000000004', 'HARDCOVER', '9781847941832', 'AH-HC-01', 899.00, 699.00, 'INR', 120, 510, 320, TRUE),
    ('v0000000-0000-0000-0000-000000000011', 'b0000000-0000-0000-0000-000000000004', 'AUDIO', '9781847941833', 'AH-AU-01', 499.00, 299.00, 'INR', 9999, NULL, NULL, TRUE),

    -- Zero to One
    ('v0000000-0000-0000-0000-000000000012', 'b0000000-0000-0000-0000-000000000005', 'PAPERBACK', '9780804139298', 'ZTO-PB-01', 499.00, 375.00, 'INR', 180, 280, 224, TRUE),
    ('v0000000-0000-0000-0000-000000000013', 'b0000000-0000-0000-0000-000000000005', 'HARDCOVER', '9780804139299', 'ZTO-HC-01', 799.00, 599.00, 'INR', 70, 430, 224, TRUE),

    -- Sapiens
    ('v0000000-0000-0000-0000-000000000014', 'b0000000-0000-0000-0000-000000000006', 'PAPERBACK', '9780099590088', 'SAP-PB-01', 699.00, 499.00, 'INR', 220, 490, 498, TRUE),
    ('v0000000-0000-0000-0000-000000000015', 'b0000000-0000-0000-0000-000000000006', 'HARDCOVER', '9780099590089', 'SAP-HC-01', 1199.00, 899.00, 'INR', 90, 720, 498, TRUE),

    -- The Psychology of Money
    ('v0000000-0000-0000-0000-000000000016', 'b0000000-0000-0000-0000-000000000007', 'PAPERBACK', '9789390166268', 'POM-PB-01', 499.00, 349.00, 'INR', 300, 270, 256, TRUE),
    ('v0000000-0000-0000-0000-000000000017', 'b0000000-0000-0000-0000-000000000007', 'HARDCOVER', '9789390166269', 'POM-HC-01', 799.00, 599.00, 'INR', 110, 460, 256, TRUE),

    -- Deep Work
    ('v0000000-0000-0000-0000-000000000018', 'b0000000-0000-0000-0000-000000000008', 'PAPERBACK', '9781455586691', 'DW-PB-01', 550.00, 399.00, 'INR', 140, 310, 304, TRUE),
    ('v0000000-0000-0000-0000-000000000019', 'b0000000-0000-0000-0000-000000000008', 'EBOOK', '9781455586692', 'DW-EB-01', 399.00, 299.00, 'INR', 9999, NULL, NULL, TRUE),

    -- Project Hail Mary
    ('v0000000-0000-0000-0000-000000000020', 'b0000000-0000-0000-0000-000000000009', 'PAPERBACK', '9780593135204', 'PHM-PB-01', 650.00, 499.00, 'INR', 210, 460, 496, TRUE),
    ('v0000000-0000-0000-0000-000000000021', 'b0000000-0000-0000-0000-000000000009', 'HARDCOVER', '9780593135205', 'PHM-HC-01', 1100.00, 849.00, 'INR', 85, 680, 496, TRUE),

    -- The Pragmatic Programmer
    ('v0000000-0000-0000-0000-000000000022', 'b0000000-0000-0000-0000-000000000010', 'HARDCOVER', '9780135957059', 'PP-HC-01', 1699.00, 1349.00, 'INR', 95, 780, 352, TRUE),
    ('v0000000-0000-0000-0000-000000000023', 'b0000000-0000-0000-0000-000000000010', 'EBOOK', '9780135957050', 'PP-EB-01', 799.00, 599.00, 'INR', 9999, NULL, NULL, TRUE)
ON CONFLICT (sku) WHERE is_deleted = FALSE DO UPDATE
SET list_price = EXCLUDED.list_price,
    selling_price = EXCLUDED.selling_price,
    stock_quantity = EXCLUDED.stock_quantity,
    is_active = EXCLUDED.is_active;

-- 3. Link Books to Categories in Junction Table (book_categories)
INSERT INTO book_categories (id, book_id, category_id, is_primary)
VALUES
    -- Clean Code -> Technology / Software Engineering
    ('bc000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000031', TRUE),
    -- DDIA -> Technology / Cloud, DevOps & Distributed Systems
    ('bc000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000032', TRUE),
    -- DDD -> Technology / Software Engineering
    ('bc000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000031', TRUE),
    -- Atomic Habits -> Self-Help / Productivity & Habits
    ('bc000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000051', TRUE),
    -- Zero to One -> Business / Entrepreneurship & Startups
    ('bc000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000042', TRUE),
    -- Sapiens -> Non-Fiction / History & Politics
    ('bc000000-0000-0000-0000-000000000006', 'b0000000-0000-0000-0000-000000000006', 'c0000000-0000-0000-0000-000000000022', TRUE),
    -- Psychology of Money -> Business / Investing & Personal Finance
    ('bc000000-0000-0000-0000-000000000007', 'b0000000-0000-0000-0000-000000000007', 'c0000000-0000-0000-0000-000000000043', TRUE),
    -- Deep Work -> Self-Help / Productivity & Habits
    ('bc000000-0000-0000-0000-000000000008', 'b0000000-0000-0000-0000-000000000008', 'c0000000-0000-0000-0000-000000000051', TRUE),
    -- Project Hail Mary -> Fiction / Sci-Fi & Fantasy
    ('bc000000-0000-0000-0000-000000000009', 'b0000000-0000-0000-0000-000000000009', 'c0000000-0000-0000-0000-000000000011', TRUE),
    -- Pragmatic Programmer -> Technology / Software Engineering
    ('bc000000-0000-0000-0000-000000000010', 'b0000000-0000-0000-0000-000000000010', 'c0000000-0000-0000-0000-000000000031', TRUE)
ON CONFLICT (book_id, category_id) WHERE is_deleted = FALSE DO NOTHING;
