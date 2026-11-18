-- ====================================================================
-- LEARNOVA - ONLAYN TƏHSİL SİSTEMİ VERİLƏNLƏR BAZASI SXEMİ (POSTGRESQL)
-- ER Diaqramına uyğun 12 əsas cədvəl və münasibətlər
-- ====================================================================

-- 1. İSTİFADƏÇİLƏR (USERS) CƏDVƏLİ
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL CHECK (role IN ('STUDENT', 'INSTRUCTOR', 'ADMIN')),
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. KATEQORİYALAR (CATEGORIES) CƏDVƏLİ (Rekursiv / Özünə istinad edən parent_id)
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. KURSLAR (COURSES) CƏDVƏLİ
CREATE TABLE IF NOT EXISTS courses (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    price NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    level VARCHAR(50) NOT NULL CHECK (level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'ALL_LEVELS')),
    status VARCHAR(50) NOT NULL CHECK (status IN ('DRAFT', 'PENDING', 'PUBLISHED', 'REJECTED', 'ARCHIVED')),
    category_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    instructor_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    description TEXT,
    thumbnail_url VARCHAR(500),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. BÖLMƏLƏR (SECTIONS) CƏDVƏLİ
CREATE TABLE IF NOT EXISTS sections (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    order_index INT NOT NULL DEFAULT 0
);

-- 5. DƏRSLƏR (LECTURES) CƏDVƏLİ
CREATE TABLE IF NOT EXISTS lectures (
    id BIGSERIAL PRIMARY KEY,
    section_id BIGINT NOT NULL REFERENCES sections(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    content_type VARCHAR(50) NOT NULL CHECK (content_type IN ('VIDEO', 'ARTICLE')),
    content_url VARCHAR(500),
    text_content TEXT,
    duration_minutes INT DEFAULT 5,
    order_index INT NOT NULL DEFAULT 0
);

-- 6. KURSA QEYDİYYAT (ENROLLMENTS) CƏDVƏLİ
CREATE TABLE IF NOT EXISTS enrollments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    progress_percentage INT NOT NULL DEFAULT 0,
    enrolled_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_enrollment_user_course UNIQUE (user_id, course_id)
);

-- 7. DƏRSİN İZLƏNMƏSİ VƏ TƏRƏQQİ (LECTURE_PROGRESS) CƏDVƏLİ
CREATE TABLE IF NOT EXISTS lecture_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lecture_id BIGINT NOT NULL REFERENCES lectures(id) ON DELETE CASCADE,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uq_progress_user_lecture UNIQUE (user_id, lecture_id)
);

-- 8. TƏLƏBƏ RƏYLƏRİ (REVIEWS) CƏDVƏLİ
CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_review_user_course UNIQUE (user_id, course_id)
);

-- 9. SERTİFİKATLAR (CERTIFICATES) CƏDVƏLİ
CREATE TABLE IF NOT EXISTS certificates (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    certificate_code VARCHAR(100) NOT NULL UNIQUE,
    issued_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_cert_user_course UNIQUE (user_id, course_id)
);

-- 10. KUPONLAR (COUPONS) CƏDVƏLİ
CREATE TABLE IF NOT EXISTS coupons (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    discount_type VARCHAR(50) NOT NULL CHECK (discount_type IN ('PERCENTAGE', 'FIXED')),
    discount_value NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    expires_at TIMESTAMP WITHOUT TIME ZONE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 11. SİFARİŞLƏR (ORDERS) CƏDVƏLİ
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code VARCHAR(100) NOT NULL UNIQUE,
    total_price NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED')),
    coupon_id BIGINT REFERENCES coupons(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 12. SİFARİŞ DETALLARI (ORDER_ITEMS) CƏDVƏLİ
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    price NUMERIC(10, 2) NOT NULL DEFAULT 0.00
);

-- 13. MÜƏLLİM MÜRACİƏTLƏRİ (INSTRUCTOR_APPLICATIONS)
CREATE TABLE IF NOT EXISTS instructor_applications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    bio TEXT,
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP WITHOUT TIME ZONE
);

-- İndekslər
CREATE INDEX IF NOT EXISTS idx_courses_category ON courses(category_id);
CREATE INDEX IF NOT EXISTS idx_courses_instructor ON courses(instructor_id);
CREATE INDEX IF NOT EXISTS idx_sections_course ON sections(course_id);
CREATE INDEX IF NOT EXISTS idx_lectures_section ON lectures(section_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_user ON enrollments(user_id);
CREATE INDEX IF NOT EXISTS idx_certificates_code ON certificates(certificate_code);
CREATE INDEX IF NOT EXISTS idx_instructor_apps_user ON instructor_applications(user_id);
CREATE INDEX IF NOT EXISTS idx_instructor_apps_status ON instructor_applications(status);
