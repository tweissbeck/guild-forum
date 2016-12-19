# --- !Ups


CREATE TABLE Authentication (
  au_id           BIGINT       NOT NULL PRIMARY KEY AUTO_INCREMENT,
  au_token        VARCHAR(128) NOT NULL,
  au_refreshToken VARCHAR(128),
  au_createdAt    DATETIME     NOT NULL,
  au_expireIn     INT
);

CREATE TABLE Client (
  cl_id             BIGINT        NOT NULL PRIMARY KEY AUTO_INCREMENT,
  cl_lastName       VARCHAR(60)   NOT NULL,
  cl_firstName      VARCHAR(60)   NOT NULL,
  cl_login          VARCHAR(128),
  cl_mail           VARCHAR(128)  NOT NULL,
  cl_admin          BOOLEAN       NOT NULL,
  cl_createdAt      DATETIME      NOT NULL,
  cl_lastLogIn      DATETIME,
  cl_password       VARCHAR(2000) NOT NULL,
  cl_salt           VARCHAR(1000) NOT NULL,
  cl_postNumber     INT           NOT NULL             DEFAULT 0,
  cl_authentication BIGINT,
  UNIQUE (cl_mail),
  UNIQUE (cl_login),
  FOREIGN KEY (cl_authentication) REFERENCES Authentication (au_id)
);


CREATE TABLE Role (
  ri_id    BIGINT      NOT NULL  PRIMARY KEY AUTO_INCREMENT,
  ri_label VARCHAR(60) NOT NULL,

  UNIQUE (ri_label)
);

CREATE TABLE Category (
  ca_id        BIGINT       NOT NULL  PRIMARY KEY AUTO_INCREMENT,
  ca_label     VARCHAR(100) NOT NULL,
  -- ca_childType ENUM ('BOTH', 'MESSAGE', 'CATEGORY'),
  ca_parent    BIGINT,
  FOREIGN KEY (ca_parent) REFERENCES Category (ca_id)
);

CREATE TABLE Message (
  me_id        BIGINT         NOT NULL  PRIMARY KEY AUTO_INCREMENT,
  me_message   VARCHAR(64000) NOT NULL              DEFAULT '',
  me_order     INT            NOT NULL,
  me_createdAt DATETIME       NOT NULL,
  me_author    BIGINT         NOT NULL,
  me_topic     BIGINT         NOT NULL,
  FOREIGN KEY (me_author) REFERENCES Client (cl_id)

);

CREATE TABLE Topic (
  to_id        BIGINT       NOT NULL  PRIMARY KEY AUTO_INCREMENT,
  to_label     VARCHAR(100) NOT NULL,
  to_author    BIGINT       NOT NULL,
  to_createdAt DATETIME     NOT NULL,
  to_category  BIGINT       NOT NULL,
  FOREIGN KEY (to_category) REFERENCES Category (ca_id),
  FOREIGN KEY (to_author) REFERENCES Client (cl_id)

);

CREATE TABLE JoinCategoryRight (
  jcr_category BIGINT NOT NULL,
  jcr_right    BIGINT NOT NULL,
  jcr_view     BOOLEAN DEFAULT FALSE,
  jcr_new      BOOLEAN DEFAULT FALSE,


  FOREIGN KEY (jcr_category) REFERENCES Category (ca_id),
  FOREIGN KEY (jcr_right) REFERENCES Role (ri_id),
  PRIMARY KEY (jcr_category, jcr_right)
);

INSERT INTO Client (
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
) VALUES (1, 'admin', 'admin', 'admin', 'admin@fake.com', TRUE, now(), NULL, '123', '123', 0, NULL);

INSERT INTO Role (
  ri_label

) VALUES ('Guild Master'), ('Officer'), ('Raid Member'), ('Member'), ('Apply'), ('Public');



# --- !Downs
DROP TABLE IF EXISTS Client;
DROP TABLE IF EXISTS Authentication;
DROP TABLE IF EXISTS Role;
DROP TABLE IF EXISTS Category;
DROP TABLE IF EXISTS Message;
DROP TABLE IF EXISTS Topic;
DROP TABLE IF EXISTS JoinCategoryRight;
