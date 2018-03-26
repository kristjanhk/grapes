CREATE TABLE artist (
  id   INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  name VARCHAR2(255 CHAR)                 NOT NULL
);

CALL CREATE_AUDIT_TABLE('artist');

CREATE TABLE artists (
  id        INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  artist_id INTEGER                            NOT NULL,
  FOREIGN KEY (artist_id) REFERENCES artist (id)
);

CALL CREATE_AUDIT_TABLE('artists');