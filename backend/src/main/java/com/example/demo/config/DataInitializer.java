package com.example.demo.config;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Seeds admin (always if missing), optional demo accounts, and sample catalog content.
 * Content seeding is independent of whether users already exist (fixes Docker data.sql race).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final String PLATFORM_INSTRUCTOR_EMAIL = "platform.instructor@learnova.az";

    // Bundled open sample MP4s under static/videos/ — distinct per lesson mapping
    private static final String VIDEO_BBB = "/videos/big_buck_bunny.mp4";
    private static final String VIDEO_ELEPHANTS = "/videos/elephants_dream.mp4";
    private static final String VIDEO_BLAZES = "/videos/for_bigger_blazes.mp4";
    private static final String VIDEO_ESCAPES = "/videos/for_bigger_escapes.mp4";
    private static final String VIDEO_SUBARU = "/videos/subaru_outback.mp4";
    private static final String VIDEO_TEARS = "/videos/tears_of_steel.mp4";

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final LectureRepository lectureRepository;
    private final CouponRepository couponRepository;
    private final ReviewRepository reviewRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LectureProgressRepository lectureProgressRepository;
    private final CertificateRepository certificateRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.admin.email:admin@learnova.az}")
    private String adminEmail;

    @Value("${app.admin.password:password123}")
    private String adminPassword;

    @Value("${app.demo-accounts:false}")
    private boolean demoAccounts;

    @Value("${app.seed.sample-content:true}")
    private boolean seedSampleContent;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           CourseRepository courseRepository,
                           SectionRepository sectionRepository,
                           LectureRepository lectureRepository,
                           CouponRepository couponRepository,
                           ReviewRepository reviewRepository,
                           EnrollmentRepository enrollmentRepository,
                           LectureProgressRepository lectureProgressRepository,
                           CertificateRepository certificateRepository,
                           PasswordEncoder passwordEncoder,
                           @Autowired(required = false) JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.courseRepository = courseRepository;
        this.sectionRepository = sectionRepository;
        this.lectureRepository = lectureRepository;
        this.couponRepository = couponRepository;
        this.reviewRepository = reviewRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lectureProgressRepository = lectureProgressRepository;
        this.certificateRepository = certificateRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(String... args) {
        ensureAdmin();
        if (demoAccounts) {
            seedDemoAccounts();
        }
        if (seedSampleContent && courseRepository.count() == 0) {
            seedSampleCatalog();
        }
        syncSequences();
    }

    private void ensureAdmin() {
        String email = adminEmail.toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User admin = new User(
                "Sistem İnzibatçısı",
                email,
                Role.ADMIN,
                passwordEncoder.encode(adminPassword)
        );
        userRepository.save(admin);
        System.out.println(">>> Admin account created: " + email);
    }

    private void seedDemoAccounts() {
        String hash = passwordEncoder.encode("password123");
        if (!userRepository.existsByEmail("student@learnova.az")) {
            userRepository.save(new User("Aysel Məmmədova", "student@learnova.az", Role.STUDENT, hash));
        }
        if (!userRepository.existsByEmail("instructor@learnova.az")) {
            userRepository.save(new User("Cavid Əliyev", "instructor@learnova.az", Role.INSTRUCTOR, hash));
        }
        System.out.println(">>> Demo accounts seeded (app.demo-accounts=true)");
    }

    private User ensurePlatformInstructor() {
        return userRepository.findByEmail(PLATFORM_INSTRUCTOR_EMAIL).orElseGet(() ->
                userRepository.save(new User(
                        "Platforma Müəllimi",
                        PLATFORM_INSTRUCTOR_EMAIL,
                        Role.INSTRUCTOR,
                        passwordEncoder.encode("password123")
                ))
        );
    }

    private void seedSampleCatalog() {
        System.out.println(">>> Seeding sample courses with local lesson videos...");
        User instructor = ensurePlatformInstructor();

        Category catDev = categoryRepository.save(new Category("Proqramlaşdırma və Texnologiya", null));
        Category catWeb = categoryRepository.save(new Category("Veb Proqramlaşdırma", catDev));
        Category catBackend = categoryRepository.save(new Category("Backend & Cloud Sistemlər", catDev));
        Category catAI = categoryRepository.save(new Category("Süni İntellekt və Data Elmi", null));
        Category catML = categoryRepository.save(new Category("Machine Learning & Deep Learning", catAI));
        Category catDesign = categoryRepository.save(new Category("Dizayn & UI/UX", null));
        Category catUIUX = categoryRepository.save(new Category("Figma & Rəqəmsal Məhsul Dizaynı", catDesign));

        if (couponRepository.count() == 0) {
            couponRepository.save(new Coupon("LEARNOVA50", DiscountType.PERCENTAGE, new BigDecimal("50.00"), LocalDateTime.now().plusMonths(6)));
            couponRepository.save(new Coupon("PULSUZ100", DiscountType.PERCENTAGE, new BigDecimal("100.00"), LocalDateTime.now().plusMonths(6)));
            couponRepository.save(new Coupon("ENDIRIM20", DiscountType.FIXED, new BigDecimal("20.00"), LocalDateTime.now().plusMonths(6)));
        }

        // Course 1: Spring Boot
        Course course1 = courseRepository.save(new Course(
                "Spring Boot 3 və Microservices Masterclass (2026)",
                "spring-boot-3-microservices-masterclass",
                new BigDecimal("49.99"),
                CourseLevel.INTERMEDIATE,
                CourseStatus.PUBLISHED,
                catBackend,
                instructor,
                "0-dan professional səviyyəyə qədər Spring Boot 3, Spring Data JPA, Hibernate, PostgreSQL, Docker və RESTful arxitektura.",
                "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=800&q=80"
        ));
        Section s1_1 = sectionRepository.save(new Section(course1, "Bölmə 1: Giriş və Spring Boot Konseptləri", 1));
        lectureRepository.save(new Lecture(s1_1, "1.1 Kursa Giriş və İnkişaf Mühitinin Qurulması", ContentType.VIDEO, VIDEO_BBB, null, 12, 1));
        lectureRepository.save(new Lecture(s1_1, "1.2 Dependency Injection və IoC", ContentType.ARTICLE, null,
                "### Dependency Injection (DI)\n\nSpring Framework-in təməl fəlsəfəsi Inversion of Control və Dependency Injection prinsipidir.", 15, 2));
        lectureRepository.save(new Lecture(s1_1, "1.3 Auto-configuration və application.yml", ContentType.VIDEO, VIDEO_ELEPHANTS, null, 20, 3));
        Section s1_2 = sectionRepository.save(new Section(course1, "Bölmə 2: PostgreSQL və Spring Data JPA", 2));
        lectureRepository.save(new Lecture(s1_2, "2.1 JPA Entity-lər və Münasibətlər", ContentType.VIDEO, VIDEO_BLAZES, null, 25, 1));
        lectureRepository.save(new Lecture(s1_2, "2.2 N+1 Problemi və Fetch Join", ContentType.ARTICLE, null,
                "### N+1 Problemi\n\nLazy loading zamanı N+1 yaranır. `@Query` ilə JOIN FETCH istifadə edin.", 18, 2));
        lectureRepository.save(new Lecture(s1_2, "2.3 Docker Compose ilə PostgreSQL", ContentType.VIDEO, VIDEO_SUBARU, null, 22, 3));

        // Course 2: Python AI
        Course course2 = courseRepository.save(new Course(
                "Python ilə Süni İntellekt və Machine Learning Bootcamp",
                "python-ai-machine-learning-bootcamp",
                new BigDecimal("39.99"),
                CourseLevel.BEGINNER,
                CourseStatus.PUBLISHED,
                catML,
                instructor,
                "NumPy, Pandas, Scikit-Learn, PyTorch ilə müasir Data Science və Süni İntellekt modelləri.",
                "https://images.unsplash.com/photo-1555949963-aa79dcee981c?auto=format&fit=crop&w=800&q=80"
        ));
        Section s2_1 = sectionRepository.save(new Section(course2, "Bölmə 1: Python Data Kitabxanaları", 1));
        lectureRepository.save(new Lecture(s2_1, "1.1 NumPy Massivləri", ContentType.VIDEO, VIDEO_ESCAPES, null, 18, 1));
        lectureRepository.save(new Lecture(s2_1, "1.2 Pandas DataFrame", ContentType.ARTICLE, null,
                "Pandas cədvəl strukturlu verilənlərlə işləmək üçün sənaye standartıdır.", 20, 2));
        Section s2_2 = sectionRepository.save(new Section(course2, "Bölmə 2: Machine Learning Modelləri", 2));
        lectureRepository.save(new Lecture(s2_2, "2.1 Scikit-Learn ilə İlk ML Modeli", ContentType.VIDEO, VIDEO_TEARS, null, 30, 1));
        lectureRepository.save(new Lecture(s2_2, "2.2 Overfitting və Regularization", ContentType.VIDEO, VIDEO_BBB, null, 25, 2));

        // Course 3: React
        Course course3 = courseRepository.save(new Course(
                "Müasir React 19, TypeScript və Next.js Tam Bələdçi",
                "react-19-typescript-nextjs-guide",
                new BigDecimal("29.99"),
                CourseLevel.ALL_LEVELS,
                CourseStatus.PUBLISHED,
                catWeb,
                instructor,
                "Server Components, TypeScript və Tailwind CSS ilə müasir veb tətbiqləri.",
                "https://images.unsplash.com/photo-1633356122544-f134324a6cee?auto=format&fit=crop&w=800&q=80"
        ));
        Section s3_1 = sectionRepository.save(new Section(course3, "Bölmə 1: React 19 Yenilikləri", 1));
        lectureRepository.save(new Lecture(s3_1, "1.1 React Compiler və Hook-lar", ContentType.VIDEO, VIDEO_ELEPHANTS, null, 22, 1));
        lectureRepository.save(new Lecture(s3_1, "1.2 Server Components ilə SSR", ContentType.VIDEO, VIDEO_BLAZES, null, 18, 2));
        Section s3_2 = sectionRepository.save(new Section(course3, "Bölmə 2: TypeScript", 2));
        lectureRepository.save(new Lecture(s3_2, "2.1 TypeScript Əsasları və Generics", ContentType.VIDEO, VIDEO_SUBARU, null, 20, 1));

        // Course 4: Figma
        Course course4 = courseRepository.save(new Course(
                "Figma ilə Professional UI/UX Dizayn və Prototip",
                "figma-ui-ux-design-prototype",
                new BigDecimal("19.99"),
                CourseLevel.BEGINNER,
                CourseStatus.PUBLISHED,
                catUIUX,
                instructor,
                "Wireframe, responsive UI dizayn və interaktiv prototiplər.",
                "https://images.unsplash.com/photo-1581291518655-9523c932deb8?auto=format&fit=crop&w=800&q=80"
        ));
        Section s4_1 = sectionRepository.save(new Section(course4, "Bölmə 1: Figma Əsasları", 1));
        lectureRepository.save(new Lecture(s4_1, "1.1 Auto-layout və Variantlar", ContentType.VIDEO, VIDEO_TEARS, null, 15, 1));
        lectureRepository.save(new Lecture(s4_1, "1.2 Component Library Qurulması", ContentType.VIDEO, VIDEO_ESCAPES, null, 20, 2));
        Section s4_2 = sectionRepository.save(new Section(course4, "Bölmə 2: Prototyping", 2));
        lectureRepository.save(new Lecture(s4_2, "2.1 Smart Animate ilə Microinteraction", ContentType.VIDEO, VIDEO_BBB, null, 18, 1));

        // Course 5: pending approval sample
        Course course5 = courseRepository.save(new Course(
                "Docker və Kubernetes ilə DevOps Təməlləri",
                "docker-kubernetes-devops-fundamentals",
                new BigDecimal("59.99"),
                CourseLevel.INTERMEDIATE,
                CourseStatus.PENDING,
                catBackend,
                instructor,
                "Konteynerləşdirmə, CI/CD və Kubernetes klaster idarəetməsi.",
                "https://images.unsplash.com/photo-1605745341112-85968b19335b?auto=format&fit=crop&w=800&q=80"
        ));
        Section s5_1 = sectionRepository.save(new Section(course5, "Bölmə 1: Docker Əsasları", 1));
        lectureRepository.save(new Lecture(s5_1, "1.1 Giriş və Konteynerlər", ContentType.VIDEO, VIDEO_BLAZES, null, 15, 1));
        lectureRepository.save(new Lecture(s5_1, "1.2 Docker Compose ilə Çoxlu Servis", ContentType.VIDEO, VIDEO_ELEPHANTS, null, 20, 2));

        System.out.println(">>> Sample catalog seeded with local /videos/*.mp4 sources");
    }

    private void syncSequences() {
        if (jdbcTemplate == null) return;
        try {
            String[] tables = {
                    "users", "categories", "courses", "sections", "lectures", "enrollments",
                    "lecture_progress", "reviews", "certificates", "coupons", "orders",
                    "order_items", "instructor_applications"
            };
            for (String table : tables) {
                try {
                    jdbcTemplate.execute(
                            "SELECT setval('" + table + "_id_seq', (SELECT COALESCE(MAX(id), 1) FROM " + table + "))");
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }
}
