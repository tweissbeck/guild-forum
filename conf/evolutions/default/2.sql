# --- !Ups


INSERT INTO Client (cl_id, cl_lastname, cl_firstname, cl_login, cl_mail, cl_createAt, cl_lastLogIn, cl_admin)
VALUES (1, 'Admin', 'Right', '@dmin', 'admin@forum.com', NOW(), NULL, TRUE);

# --- !Downs
DELETE FROM Client;