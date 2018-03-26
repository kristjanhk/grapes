CREATE TABLE album (
  id         INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  artists_id INTEGER,
  name       VARCHAR2(255 CHAR)                 NOT NULL,
  year       INTEGER,
  FOREIGN KEY (artists_id) REFERENCES artists (id)
);

CALL CREATE_AUDIT_TABLE('album');
