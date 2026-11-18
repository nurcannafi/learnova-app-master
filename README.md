# Learnova — Onlayn Təhsil Platforması

Spring Boot 4.1.2 + PostgreSQL 17 + JWT RBAC ilə hazırlanmış onlayn təhsil platforması (LMS).

## 🚀 Texnologiyalar (Tech Stack)

- **Backend:** Java 17, Spring Boot 4.1.2, Spring Security (JWT), Spring Data JPA
- **Database:** PostgreSQL 17
- **Frontend:** Vanilla JS SPA, Bootstrap (`backend/src/main/resources/static/`)
- **Konteynerləşdirmə:** Docker & Docker Compose

## 📦 Layihə Strukturu

```text
learnova-app/
├── backend/                  # Spring Boot tətbiqi
│   ├── src/                  # Java mənbə kodları və statik web resursları
│   ├── Dockerfile            # Backend Dockerfile
│   ├── pom.xml               # Maven asılılıqları
│   └── mvnw / mvnw.cmd       # Maven Wrapper
├── database/
│   └── schema.sql            # Verilənlər bazası cədvəl strukturu
├── docker-compose.yml        # PostgreSQL və Backend servis konfiqurasiyası
├── start-app.bat             # Windows üçün birbaşa işəsalma skripti
└── README.md                 # Layihə sənədləşməsi
```

## ⚙️ İşə salma (Quick Start)

### 1. Docker ilə işə salma (Tövsiyə olunan)

```bash
docker compose up -d --build
```
və ya Windows mühitində birbaşa **`start-app.bat`** faylını iki dəfə klikləyərək işə sala bilərsiniz.

* **Veb tətbiq:** [http://localhost:8080](http://localhost:8080)
* **PostgreSQL:** `localhost:5433` (Docker daxilində `5432`)
  * Baza: `learnova_db`
  * İstifadəçi: `postgres`
  * Şifrə: `password`

### 2. Standart Giriş və Seed Məlumatları

İlk işə düşmədə avtomatik seed olunur:
- **Admin:** `admin@learnova.az` / `password123`
- Nümunə kurslar və lokal video dərslər avtomatik yüklənir.
- Dev mühitində test hesabları aktivləşdirmək üçün: `APP_DEMO_ACCOUNTS=true`

## 👥 Rollar və İcazələr

| Rol | Necə əldə edilir? | İcazələr |
|---|---|---|
| **STUDENT** | Açıq qeydiyyat vasitəsilə | Kursları izləmək, qeydiyyatdan keçmək, rəy yazmaq |
| **INSTRUCTOR** | Tələbə müraciət edir → Admin təsdiqləyir | Kurs yaratmaq, bölmə/dərs əlavə etmək |
| **ADMIN** | Mühit dəyişənləri və ilkin seed | Təsdiqlər, istifadəçi və məzmun idarəetməsi |

## 🔄 Təmiz Yenidən Başlama (Reset Database)

Köhnə verilənlər bazası həcmini (volume) sıfırlayaraq təmiz başlatmaq üçün:

```bash
docker compose down -v && docker compose up -d --build
```
