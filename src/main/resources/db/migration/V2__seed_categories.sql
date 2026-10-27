-- ============================================================================
-- Migration: V2__seed_categories.sql
-- Description: Seed Root and Subcategories Taxonomy
-- ============================================================================

-- 1. Root Level Categories
INSERT INTO categories (id, parent_id, name, slug, description, display_order)
VALUES
    ('c0000000-0000-0000-0000-000000000001', NULL, 'Fiction', 'fiction', 'Novels, literature, stories, and imaginary world-building.', 1),
    ('c0000000-0000-0000-0000-000000000002', NULL, 'Non-Fiction', 'non-fiction', 'Biographies, true stories, history, essays, and factual publications.', 2),
    ('c0000000-0000-0000-0000-000000000003', NULL, 'Technology & Programming', 'technology-programming', 'Software architecture, algorithms, coding languages, cloud, and engineering practices.', 3),
    ('c0000000-0000-0000-0000-000000000004', NULL, 'Business & Economics', 'business-economics', 'Leadership, entrepreneurship, investing, finance, and corporate strategy.', 4),
    ('c0000000-0000-0000-0000-000000000005', NULL, 'Science & Mathematics', 'science-mathematics', 'Physics, astronomy, biology, pure mathematics, and scientific discoveries.', 5),
    ('c0000000-0000-0000-0000-000000000006', NULL, 'Self-Help & Personal Development', 'self-help', 'Productivity, psychology, mindfulness, habits, and self-improvement.', 6)
ON CONFLICT (slug) WHERE is_deleted = FALSE DO UPDATE
SET name = EXCLUDED.name, description = EXCLUDED.description, display_order = EXCLUDED.display_order;

-- 2. Child Level Subcategories (Fiction)
INSERT INTO categories (id, parent_id, name, slug, description, display_order)
VALUES
    ('c0000000-0000-0000-0000-000000000011', 'c0000000-0000-0000-0000-000000000001', 'Sci-Fi & Fantasy', 'sci-fi-fantasy', 'Space operas, dystopian futures, high fantasy, and speculative fiction.', 1),
    ('c0000000-0000-0000-0000-000000000012', 'c0000000-0000-0000-0000-000000000001', 'Thrillers & Mystery', 'thrillers-mystery', 'Crime investigation, psychological suspense, and detective novels.', 2),
    ('c0000000-0000-0000-0000-000000000013', 'c0000000-0000-0000-0000-000000000001', 'Romance & Drama', 'romance-drama', 'Contemporary love stories, drama, and heartfelt relationship fiction.', 3),
    ('c0000000-0000-0000-0000-000000000014', 'c0000000-0000-0000-0000-000000000001', 'Historical Fiction', 'historical-fiction', 'Immersive narratives set against historical events and ancient eras.', 4)
ON CONFLICT (slug) WHERE is_deleted = FALSE DO UPDATE
SET parent_id = EXCLUDED.parent_id, name = EXCLUDED.name, description = EXCLUDED.description, display_order = EXCLUDED.display_order;

-- 3. Child Level Subcategories (Non-Fiction)
INSERT INTO categories (id, parent_id, name, slug, description, display_order)
VALUES
    ('c0000000-0000-0000-0000-000000000021', 'c0000000-0000-0000-0000-000000000002', 'Biography & Memoirs', 'biography-memoirs', 'Inspiring memoirs, executive biographies, and life chronicles.', 1),
    ('c0000000-0000-0000-0000-000000000022', 'c0000000-0000-0000-0000-000000000002', 'History & Politics', 'history-politics', 'World wars, civilizations, geopolitics, and socio-economic history.', 2),
    ('c0000000-0000-0000-0000-000000000023', 'c0000000-0000-0000-0000-000000000002', 'Philosophy & Ethics', 'philosophy-ethics', 'Stoicism, existentialism, moral philosophy, and critical thinking.', 3)
ON CONFLICT (slug) WHERE is_deleted = FALSE DO UPDATE
SET parent_id = EXCLUDED.parent_id, name = EXCLUDED.name, description = EXCLUDED.description, display_order = EXCLUDED.display_order;

-- 4. Child Level Subcategories (Technology & Programming)
INSERT INTO categories (id, parent_id, name, slug, description, display_order)
VALUES
    ('c0000000-0000-0000-0000-000000000031', 'c0000000-0000-0000-0000-000000000003', 'Software Engineering & Architecture', 'software-engineering', 'Clean code, DDD, microservices, enterprise patterns, and testing.', 1),
    ('c0000000-0000-0000-0000-000000000032', 'c0000000-0000-0000-0000-000000000003', 'Cloud, DevOps & Distributed Systems', 'cloud-devops', 'Kubernetes, distributed databases, streaming architectures, and SRE.', 2),
    ('c0000000-0000-0000-0000-000000000033', 'c0000000-0000-0000-0000-000000000003', 'AI & Machine Learning', 'ai-machine-learning', 'Deep learning, neural networks, LLMs, NLP, and practical data science.', 3)
ON CONFLICT (slug) WHERE is_deleted = FALSE DO UPDATE
SET parent_id = EXCLUDED.parent_id, name = EXCLUDED.name, description = EXCLUDED.description, display_order = EXCLUDED.display_order;

-- 5. Child Level Subcategories (Business & Economics)
INSERT INTO categories (id, parent_id, name, slug, description, display_order)
VALUES
    ('c0000000-0000-0000-0000-000000000041', 'c0000000-0000-0000-0000-000000000004', 'Leadership & Management', 'leadership-management', 'Team dynamics, organizational growth, executive coaching, and strategy.', 1),
    ('c0000000-0000-0000-0000-000000000042', 'c0000000-0000-0000-0000-000000000004', 'Entrepreneurship & Startups', 'entrepreneurship-startups', 'Venture capital, product-market fit, scaling, and zero-to-one playbooks.', 2),
    ('c0000000-0000-0000-0000-000000000043', 'c0000000-0000-0000-0000-000000000004', 'Investing & Personal Finance', 'investing-personal-finance', 'Value investing, wealth management, behavioral economics, and markets.', 3)
ON CONFLICT (slug) WHERE is_deleted = FALSE DO UPDATE
SET parent_id = EXCLUDED.parent_id, name = EXCLUDED.name, description = EXCLUDED.description, display_order = EXCLUDED.display_order;

-- 6. Child Level Subcategories (Self-Help)
INSERT INTO categories (id, parent_id, name, slug, description, display_order)
VALUES
    ('c0000000-0000-0000-0000-000000000051', 'c0000000-0000-0000-0000-000000000006', 'Productivity & Habits', 'productivity-habits', 'Atomic habits, deep work, time management, and mental frameworks.', 1),
    ('c0000000-0000-0000-0000-000000000052', 'c0000000-0000-0000-0000-000000000006', 'Mindfulness & Mental Wellbeing', 'mindfulness-mental-wellbeing', 'Meditation, stress reduction, emotional resilience, and happiness.', 2)
ON CONFLICT (slug) WHERE is_deleted = FALSE DO UPDATE
SET parent_id = EXCLUDED.parent_id, name = EXCLUDED.name, description = EXCLUDED.description, display_order = EXCLUDED.display_order;
