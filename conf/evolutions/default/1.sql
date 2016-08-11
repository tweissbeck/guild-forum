# --- !Ups

CREATE TABLE Client (
  cl_id         BIGINT        NOT NULL PRIMARY KEY AUTO_INCREMENT,
  cl_lastName   VARCHAR(60)   NOT NULL,
  cl_firstName  VARCHAR(60)   NOT NULL,
  cl_login      VARCHAR(128),
  cl_mail       VARCHAR(128)  NOT NULL,
  cl_admin      BOOLEAN       NOT NULL,
  cl_createdAt  DATETIME      NOT NULL,
  cl_lastLogIn  DATETIME,
  cl_password   VARCHAR(2000) NOT NULL,
  cl_salt       VARCHAR(1000) NOT NULL,
  cl_postNumber INT           NOT NULL             DEFAULT 0,
  UNIQUE (cl_mail),
  UNIQUE (cl_login),
  UNIQUE (cl_firstName, cl_lastName)
);


CREATE TABLE Role (
  ri_id    BIGINT      NOT NULL  PRIMARY KEY AUTO_INCREMENT,
  ri_label VARCHAR(60) NOT NULL,
  UNIQUE (ri_label)
);

CREATE TABLE Category (
  ca_id     BIGINT       NOT NULL  PRIMARY KEY AUTO_INCREMENT,
  ca_label  VARCHAR(100) NOT NULL,
  ca_parent BIGINT,
  FOREIGN KEY (ca_parent) REFERENCES Category (ca_id)
);

CREATE TABLE Message (
  me_id      BIGINT         NOT NULL  PRIMARY KEY AUTO_INCREMENT,
  me_message VARCHAR(64000) NOT NULL              DEFAULT '',
  me_order  INT NOT NULL ,
  me_createdAt DATETIME NOT NULL ,
  me_author BIGINT NOT NULL,
  me_topic BIGINT NOT NULL,
  FOREIGN KEY (me_author)

);

CREATE TABLE Topic (
  to_id        BIGINT       NOT NULL  PRIMARY KEY AUTO_INCREMENT,
  to_label     VARCHAR(100) NOT NULL,
  to_author    BIGINT       NOT NULL,
  to_createdAt DATETIME     NOT NULL,
  to_category  BIGINT       NOT NULL,
  FOREIGN KEY (to_category) REFERENCES Category (ca_id),
  FOREIGN KEY (to_message) REFERENCES Message (me_id),
  FOREIGN KEY (to_author) REFERENCES Client (cl_id)

);

CREATE TABLE JoinCategoryRight (
  jcr_category BIGINT NOT NULL,
  jcr_right    BIGINT NOT NULL,
  FOREIGN KEY (jcr_category) REFERENCES Category (ca_id),
  FOREIGN KEY (jcr_right) REFERENCES Role (ri_id),
  PRIMARY KEY (jcr_category, jcr_right)
);

# --- !Downs
DROP TABLE Client;
DROP TABLE Role;
