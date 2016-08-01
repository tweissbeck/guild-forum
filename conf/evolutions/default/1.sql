# --- !Ups

CREATE TABLE Client (
  cl_id        BIGINT       NOT NULL PRIMARY KEY AUTO_INCREMENT,
  cl_lastName  VARCHAR(60)  NOT NULL,
  cl_firstName VARCHAR(60)  NOT NULL,
  cl_login     VARCHAR(128),
  cl_mail      VARCHAR(128) NOT NULL,
  cl_createAt  DATETIME     NOT NULL,
  cl_lastLogIn DATETIME
);

CREATE INDEX Client_index_login ON Client(cl_login);

-- Table topic
CREATE TABLE TOPIC (
  to_id    BIGINT  NOT NULL PRIMARY KEY AUTO_INCREMENT,
  to_title VARCHAR NOT NULL
);

CREATE TABLE MESSAGE (
  me_id      BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  me_message VARCHAR(65000),
  me_topic   BIGINT NOT NULL,
  FOREIGN KEY (me_id) REFERENCES TOPIC (to_id)
);

# --- !Downs

DROP TABLE MESSAGE;
DROP TABLE TOPIC;
