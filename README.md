# CRM API

Bu proje, Spring Boot 3 kullanılarak geliştirilmiş bir Müşteri İlişkileri Yönetimi (CRM) API'sidir. Proje, tam "dockerize" edilmiş olup Docker Compose ile kolayca ayağa kaldırılabilir.

## Projenin Amacı

Bu API, küçük ve orta ölçekli işletmeler için temel CRM işlevlerini sağlar. API'yi kullanarak yeni müşteriler (Customers) ve ilgili kişileri (Agents) oluşturabilir, mevcutları listeleyebilir, güncelleyebilir ve silebilirsiniz. Sistem aynı zamanda bu müşterilere ait sorunları ve istekleri (Tickets) yönetmeye de olanak tanır.

-----

## API Endpoint'leri

Proje, JWT (JSON Web Token) ile korunan RESTful endpoint'ler sunar. Giriş (Authentication) endpoint'leri herkese açıkken, diğer tüm endpoint'ler geçerli bir JWT token gerektirir.

Detaylı API dokümantasyonu aşağıdaki adreste canlı olarak mevcuttur:

**[https://crm-api-production-5e37.up.railway.app/redoc.html](https://crm-api-production-5e37.up.railway.app/redoc.html)**

-----

## Teknoloji Yığını

  * **Java 17**
  * **Spring Boot** (Web, Data JPA, Security)
  * **Spring Security** (JWT token bazlı kimlik doğrulama)
  * **PostgreSQL** (Veritabanı)
  * **Flyway** (Veritabanı migrasyon yönetimi)
  * **Maven** (Bağımlılık yönetimi)
  * **Docker & Docker Compose** (Container yönetimi)

-----

## Projeyi Yerel Olarak Çalıştırma

### Gereksinimler

  * [Docker](https://www.docker.com/get-started)
  * [Docker Compose](https://docs.docker.com/compose/install/)

### Adımlar

1.  Projeyi klonlayın ve proje dizinine gidin:

    ```bash
    git clone https://github.com/anilkrblt/crm-api.git
    cd crm-api
    ```

2.  Projenin ana dizininde aşağıdaki `docker-compose.yml` dosyasının bulunduğundan emin olun. Bu dosya, hem `app` (sizin API'niz) hem de `db` (PostgreSQL) servislerini tanımlar.

    ```yaml
    services:
      app:
        build: .
        container_name: crm-api
        depends_on:
          - db
        ports:
          - "8080:8080"
        environment:
          - DB_HOST=db
          - DB_PORT=5432
          - DB_NAME=crm_db
          - DB_USERNAME=postgres
          - DB_PASSWORD=postgres
          - JWT_SECRET_KEY=lO2aZgqJ7B3yV/G4K+N8E/uD9F/rS6wJ5H+tXqY3Z0w=
          - APP_USER=user
          - APP_PASSWORD=password

      db:
        image: postgres:15
        container_name: crm-db
        environment:
          - POSTGRES_DB=crm_db
          - POSTGRES_USER=postgres
          - POSTGRES_PASSWORD=postgres
        ports:
          - "5433:5432"
        volumes:
          - postgres-data:/var/lib/postgresql/data

    volumes:
      postgres-data:
    ```

3.  Aşağıdaki komut ile projeyi başlatın:

    ```bash
    docker-compose up --build
    ```

Uygulama artık `http://localhost:8080` adresinde çalışıyor olacaktır. Flyway migrasyonları otomatik olarak çalışacak ve veritabanı şeması hazırlanacaktır.

-----

## Yapılandırma (Environment Değişkenleri)

Uygulamanın tüm kritik ayarları, dağıtım (deployment) ortamındaki veya yerel `docker-compose.yml` dosyasındaki ortam değişkenleri (environment variables) ile sağlanır:

  * `DB_HOST`: Veritabanı sunucusu
  * `DB_PORT`: Veritabanı portu
  * `DB_NAME`: Veritabanı adı
  * `DB_USERNAME`: Veritabanı kullanıcı adı
  * `DB_PASSWORD`: Veritabanı parolası
  * `JWT_SECRET_KEY`: JWT imzalamak için kullanılacak gizli anahtar
  * `APP_USER` / `APP_PASSWORD`: Spring Security için temel kullanıcı bilgileri
