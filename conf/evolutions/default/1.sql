# --- !Ups

-- Schema

CREATE TABLE Authentication (
  au_id           BIGSERIAL NOT NULL PRIMARY KEY,
  au_token        VARCHAR(128) NOT NULL,
  au_refreshToken VARCHAR(128),
  au_createdAt    TIMESTAMP    NOT NULL,
  au_expireIn     INT
);

CREATE TABLE Client (
  cl_id             BIGSERIAL NOT NULL PRIMARY KEY,
  cl_lastName       VARCHAR(60)   NOT NULL,
  cl_firstName      VARCHAR(60)   NOT NULL,
  cl_login          VARCHAR(128),
  cl_mail           VARCHAR(128)  NOT NULL,
  cl_admin          BOOLEAN       NOT NULL,
  cl_createdAt      TIMESTAMP     NOT NULL,
  cl_lastLogIn      TIMESTAMP,
  cl_password       VARCHAR(2000) NOT NULL,
  cl_salt           VARCHAR(1000) NOT NULL,
  cl_postNumber     INT           NOT NULL             DEFAULT 0,
  cl_authentication BIGINT,
  UNIQUE (cl_mail),
  UNIQUE (cl_login),
  FOREIGN KEY (cl_authentication) REFERENCES Authentication (au_id)
);


CREATE TABLE Role (
  ri_id    BIGSERIAL NOT NULL PRIMARY KEY,
  ri_label VARCHAR(60) NOT NULL,

  UNIQUE (ri_label)
);

CREATE TABLE Category (
  ca_id     BIGSERIAL NOT NULL PRIMARY KEY,
  ca_label  VARCHAR(100) NOT NULL,
  -- ca_childType ENUM ('BOTH', 'MESSAGE', 'CATEGORY'),
  ca_parent BIGINT,
  FOREIGN KEY (ca_parent) REFERENCES Category (ca_id)
);

CREATE TABLE Message (
  me_id        BIGSERIAL NOT NULL PRIMARY KEY,
  me_message   VARCHAR(64000) NOT NULL              DEFAULT '',
  me_order     INT            NOT NULL,
  me_createdAt TIMESTAMP      NOT NULL,
  me_author    BIGINT         NOT NULL,
  me_topic     BIGINT         NOT NULL,
  FOREIGN KEY (me_author) REFERENCES Client (cl_id)

);

CREATE TABLE Topic (
  to_id        BIGSERIAL NOT NULL PRIMARY KEY,
  to_label     VARCHAR(100) NOT NULL,
  to_author    BIGINT       NOT NULL,
  to_createdAt TIMESTAMP    NOT NULL,
  to_category  BIGINT       NOT NULL,
  FOREIGN KEY (to_category) REFERENCES Category (ca_id),
  FOREIGN KEY (to_author) REFERENCES Client (cl_id)

);

CREATE TABLE JoinCategoryRole (
  jcr_category BIGINT NOT NULL,
  jcr_role     BIGINT NOT NULL,
  -- can view topic with this role in this category ?
  jcr_view     BOOLEAN DEFAULT FALSE,
  -- can create with this role in this category new topic ?
  jcr_new      BOOLEAN DEFAULT FALSE,


  FOREIGN KEY (jcr_category) REFERENCES Category (ca_id),
  FOREIGN KEY (jcr_role) REFERENCES Role (ri_id),
  PRIMARY KEY (jcr_category, jcr_role)
);

CREATE TABLE JoinUserRole (
  jur_user BIGINT NOT NULL,
  jur_role BIGINT NOT NULL,
  FOREIGN KEY (jur_user) REFERENCES Client (cl_id),
  FOREIGN KEY (jur_user) REFERENCES Role (ri_id),
  UNIQUE (jur_user) -- user can only have one role in application. If existing role doesn't fi, just create new roles was latch better
);

CREATE TYPE AppStatus AS ENUM ('NEW', 'IN_PROGRESS', 'VALIDATE', 'REFUSED');

CREATE TABLE Application (
  ap_id           BIGSERIAL NOT NULL PRIMARY KEY,
  ap_user         BIGINT         NOT NULL,
  ap_status       AppStatus,
  ap_creationDate TIMESTAMP      NOT NULL,
  ap_data         VARCHAR(64000) NOT NULL
);

-- INSERT some test data here

INSERT INTO CLIENT (
  cl_id,
  cl_lastName,
  cl_firstName,
  cl_login,
  cl_mail,
  cl_admin,
  cl_createdAt,
  cl_lastLogIn,
  cl_password,
  cl_salt,
  cl_postNumber,
  cl_authentication
) VALUES (1, 'admin', 'admin', 'admin', 'admin@fake.com', TRUE, now(
), NULL, '123', '123', 0, NULL
);

INSERT INTO Role (ri_label)
VALUES
  ('Guild Master'), ('Officer'), ('Raid Member'), ('Member'), ('Apply'), ('Public');


INSERT INTO JoinUserRole
VALUES
  (1, (SELECT ri_id
       FROM Role
       WHERE ri_label = 'Guild Master'));

INSERT INTO Category (ca_label, ca_parent)
VALUES
  ('Recrutement', NULL),
  ('Taverne', NULL),
  ('Abscences', NULL);

-- sub categories
INSERT INTO Category (ca_label, ca_parent)
VALUES
  ('Etat du recrutement', (SELECT ca_id
                           FROM Category
                           WHERE ca_label = 'Recrutement')),
  ('Recrutement en cours', (SELECT ca_id
                            FROM Category
                            WHERE ca_label = 'Recrutement'));

INSERT INTO JoinCategoryRole (jcr_category, jcr_role)
VALUES
  ((SELECT ca_id
    FROM Category
    WHERE ca_label = 'Recrutement'), (SELECT ri_id
                                      FROM Role
                                      WHERE ri_label = 'Public')),
  ((SELECT ca_id
    FROM Category
    WHERE ca_label = 'Etat du recrutement'), (SELECT ri_id
                                              FROM Role
                                              WHERE ri_label = 'Public')),
  ((SELECT ca_id
    FROM Category
    WHERE ca_label = 'Recrutement'), (SELECT ri_id
                                      FROM Role
                                      WHERE ri_label = 'Apply')),
  ((SELECT ca_id
    FROM Category
    WHERE ca_label = 'Recrutement'), (SELECT ri_id
                                      FROM Role
                                      WHERE ri_label = 'Member')),
  ((SELECT ca_id
    FROM Category
    WHERE ca_label = 'Recrutement'), (SELECT ri_id
                                      FROM Role
                                      WHERE ri_label = 'Raid Member')),
  ((SELECT ca_id
    FROM Category
    WHERE ca_label = 'Recrutement'), (SELECT ri_id
                                      FROM Role
                                      WHERE ri_label = 'Officer')),
  ((SELECT ca_id
    FROM Category
    WHERE ca_label = 'Recrutement'), (SELECT ri_id
                                      FROM Role
                                      WHERE ri_label = 'Guild Master')),
  ((SELECT ca_id
    FROM Category
    WHERE ca_label = 'Taverne'), (SELECT ri_id
                                  FROM Role
                                  WHERE ri_label = 'Guild Master'));

# --- !Downs
DROP TABLE IF EXISTS Message CASCADE;
DROP TABLE IF EXISTS Topic CASCADE;
DROP TABLE IF EXISTS Category CASCADE;
DROP TABLE IF EXISTS Client CASCADE;
DROP TABLE IF EXISTS Authentication CASCADE;
DROP TABLE IF EXISTS Role CASCADE;
DROP TABLE IF EXISTS JoinCategoryRole CASCADE;
DROP TABLE IF EXISTS Application CASCADE;
DROP TYPE IF EXISTS AppStatus;
