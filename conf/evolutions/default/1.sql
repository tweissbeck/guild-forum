# --- !Ups

CREATE TABLE Client (
  cl_id        BIGINT       NOT NULL PRIMARY KEY AUTO_INCREMENT,
  cl_lastName  VARCHAR(60)  NOT NULL,
  cl_firstName VARCHAR(60)  NOT NULL,
  cl_login     VARCHAR(128),
  cl_mail      VARCHAR(128) NOT NULL,
  cl_admin     BOOLEAN,
  cl_createAt  DATETIME     NOT NULL,
  cl_lastLogIn DATETIME,
  UNIQUE (cl_mail),
  UNIQUE (cl_login),
  UNIQUE (cl_firstName, cl_lastName)
);

# --- !Downs
DROP TABLE Client;
