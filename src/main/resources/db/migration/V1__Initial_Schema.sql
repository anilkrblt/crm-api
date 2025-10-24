DROP TABLE IF EXISTS ticket_comments;
DROP TABLE IF EXISTS tickets;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS agents;
DROP TABLE IF EXISTS _user;


CREATE TABLE _user (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       email VARCHAR(255) NOT NULL,
                       first_name VARCHAR(255),
                       last_name VARCHAR(255),
                       password VARCHAR(255) NOT NULL,
                       role ENUM('ADMIN','AGENT','CUSTOMER') NOT NULL,
                       PRIMARY KEY (id)
);

CREATE TABLE agents (
                        created_at DATETIME(6),
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        updated_at DATETIME(6),
                        user_id BIGINT NOT NULL,
                        version BIGINT,
                        department VARCHAR(100),
                        PRIMARY KEY (id)
);

CREATE TABLE customers (
                           created_at DATETIME(6),
                           id BIGINT NOT NULL AUTO_INCREMENT,
                           updated_at DATETIME(6),
                           user_id BIGINT NOT NULL,
                           version BIGINT,
                           phone VARCHAR(20),
                           PRIMARY KEY (id)
);

CREATE TABLE tickets (
                         agent_id BIGINT,
                         created_at DATETIME(6),
                         customer_id BIGINT NOT NULL,
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         updated_at DATETIME(6),
                         version BIGINT,
                         description TEXT,
                         subject VARCHAR(255) NOT NULL,
                         priority ENUM('HIGH','LOW','MEDIUM','URGENT'),
                         status ENUM('CLOSED','IN_PROGRESS','ON_HOLD','OPEN'),
                         PRIMARY KEY (id)
);

CREATE TABLE ticket_comments (
                                 author_user_id BIGINT NOT NULL,
                                 created_at DATETIME(6),
                                 id BIGINT NOT NULL AUTO_INCREMENT,
                                 ticket_id BIGINT NOT NULL,
                                 version BIGINT,
                                 comment TEXT NOT NULL,
                                 PRIMARY KEY (id)
);


ALTER TABLE _user ADD CONSTRAINT UKk11y3pdtsrjgy8w9b6q4bjwrx UNIQUE (email);
ALTER TABLE agents ADD CONSTRAINT UKica2nnf6jymrt09bvuv7e4mai UNIQUE (user_id);
ALTER TABLE customers ADD CONSTRAINT UKeuat1oase6eqv195jvb71a93s UNIQUE (user_id);


ALTER TABLE agents ADD CONSTRAINT FK372w0t74hbt0a26amim8wh2an FOREIGN KEY (user_id) REFERENCES _user (id);
ALTER TABLE customers ADD CONSTRAINT FKjbpg4t766b41ha3aq1wjppqx1 FOREIGN KEY (user_id) REFERENCES _user (id);
ALTER TABLE ticket_comments ADD CONSTRAINT FKd3o727vvw09y4us8unsuav2x6 FOREIGN KEY (author_user_id) REFERENCES _user (id);
ALTER TABLE ticket_comments ADD CONSTRAINT FKdoce3fj1osdn71h25dhfs160v FOREIGN KEY (ticket_id) REFERENCES tickets (id);
ALTER TABLE tickets ADD CONSTRAINT FKod7hki6d5kpasjet8w6glky7m FOREIGN KEY (agent_id) REFERENCES agents (id);
ALTER TABLE tickets ADD CONSTRAINT FKi81xre2n3j3as1sp24j440kq1 FOREIGN KEY (customer_id) REFERENCES customers (id);