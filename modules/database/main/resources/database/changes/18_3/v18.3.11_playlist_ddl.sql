CREATE TABLE playlist (
  id          INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  user_id     INTEGER                            NOT NULL,
  songs_id    INTEGER                            NOT NULL,
  image_id    INTEGER,
  name        VARCHAR2(255 CHAR)                 NOT NULL,
  description VARCHAR2(2000 CHAR),
  FOREIGN KEY (user_id) REFERENCES user (id),
  FOREIGN KEY (songs_id) REFERENCES songs (id),
  FOREIGN KEY (image_id) REFERENCES image (id)
);

CALL CREATE_AUDIT_TABLE('playlist');
