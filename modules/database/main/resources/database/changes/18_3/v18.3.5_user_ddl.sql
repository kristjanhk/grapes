CREATE TABLE user
(
  id          INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  firstname   VARCHAR2(255 CHAR),
  lastname    VARCHAR2(255 CHAR),
  facebook_id INTEGER,
  google_id   INTEGER,
  idcard_id   INTEGER,
  username    VARCHAR2(255 CHAR),
  hash        VARCHAR2(255 CHAR),
  salt        VARCHAR2(255 CHAR),
);

CALL CREATE_AUDIT_TABLE('USER');
