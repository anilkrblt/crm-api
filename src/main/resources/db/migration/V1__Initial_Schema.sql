-- Drop tables in the correct order (child tables first)
-- DROP TABLE IF EXISTS ticket_comments;
-- DROP TABLE IF EXISTS tickets;
-- DROP TABLE IF EXISTS customers;
-- DROP TABLE IF EXISTS agents;
-- DROP TABLE IF EXISTS departments; -- Yeni eklendi
-- DROP TABLE IF EXISTS _user;
-- DROP TABLE IF EXISTS flyway_schema_history; -- Also drop Flyway's own table if resetting

-- Create Tables using PostgreSQL syntax, with VARCHAR for ENUMs (workaround)
CREATE TABLE _user (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       first_name VARCHAR(255),
                       last_name VARCHAR(255),
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL -- (Enum: ADMIN, AGENT, CUSTOMER)
);

-- YENİ TABLO: Departmanları tanımlar
CREATE TABLE departments (
                             id BIGSERIAL PRIMARY KEY, -- Otomatik ID ekledik (PK olarak name yerine)
                             name VARCHAR(100) NOT NULL UNIQUE, -- Departman adı hâlâ benzersiz olmalı
                             description VARCHAR(255)
);

CREATE TABLE agents (
                        id BIGSERIAL PRIMARY KEY,
                        user_id BIGINT NOT NULL UNIQUE,
                        department_id BIGINT NOT NULL, -- department VARCHAR(100) yerine ID eklendi
                        version BIGINT,
                        created_at TIMESTAMP(6),
                        updated_at TIMESTAMP(6),
                        CONSTRAINT fk_agent_user FOREIGN KEY (user_id) REFERENCES _user (id) ON DELETE CASCADE,
                        CONSTRAINT fk_agent_department FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE RESTRICT -- Departman silinirse ajanlar etkilenmesin
);

CREATE TABLE customers (
                           id BIGSERIAL PRIMARY KEY,
                           user_id BIGINT NOT NULL UNIQUE,
                           phone VARCHAR(20),
                           version BIGINT,
                           created_at TIMESTAMP(6),
                           updated_at TIMESTAMP(6),
                           CONSTRAINT fk_customer_user FOREIGN KEY (user_id) REFERENCES _user (id) ON DELETE CASCADE
);

CREATE TABLE tickets (
                         id BIGSERIAL PRIMARY KEY,
                         customer_id BIGINT NOT NULL,
                         department_id BIGINT NOT NULL, -- agent_id yerine department_id eklendi (zorunlu)
                         assigned_agent_id BIGINT, -- Bileti üstlenen ajan (opsiyonel)
                         subject VARCHAR(255) NOT NULL,
                         description TEXT,
                         status VARCHAR(50), -- (Enum: OPEN, IN_PROGRESS, ON_HOLD, CLOSED)
                         priority VARCHAR(50), -- (Enum: LOW, MEDIUM, HIGH, URGENT)
                         version BIGINT,
                         created_at TIMESTAMP(6),
                         updated_at TIMESTAMP(6),
                         CONSTRAINT fk_ticket_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
                         CONSTRAINT fk_ticket_department FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE RESTRICT, -- Departman silinirse biletler etkilenmesin
                         CONSTRAINT fk_ticket_assigned_agent FOREIGN KEY (assigned_agent_id) REFERENCES agents (id) ON DELETE SET NULL -- Ajan silinirse bilet sahipsiz kalsın
);

CREATE TABLE ticket_comments (
                                 id BIGSERIAL PRIMARY KEY,
                                 ticket_id BIGINT NOT NULL,
                                 author_user_id BIGINT NOT NULL,
                                 comment TEXT NOT NULL,
                                 version BIGINT,
                                 created_at TIMESTAMP(6),
                                 CONSTRAINT fk_comment_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id) ON DELETE CASCADE,
                                 CONSTRAINT fk_comment_author FOREIGN KEY (author_user_id) REFERENCES _user (id) -- Kullanıcı silinirse yorumları ne olacak? (SET NULL veya RESTRICT düşünülebilir)
);