# --- !Ups

INSERT INTO TOPIC (to_title) VALUES ('Recrutement'), ('ABC');

INSERT INTO Client (cl_id, cl_lastname, cl_firstname, cl_login, cl_mail, cl_createAt, cl_lastLogIn)
VALUES (1, 'John', 'Malkovitch', 'tweissbeck', 'malkovitch@lyra-network.com', NOW(), NULL);

# --- !Downs

DELETE FROM TOPIC;
DELETE FROM Client;