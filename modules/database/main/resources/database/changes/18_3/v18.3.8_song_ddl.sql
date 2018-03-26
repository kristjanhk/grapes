CREATE TABLE song (
  id         INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  name       VARCHAR2(255 CHAR)                 NOT NULL,
  artists_id INTEGER,
  album_id   INTEGER,
  length     INTEGER                            NOT NULL,
  rating     INTEGER,
  spotify    VARCHAR2(255 CHAR),
  soundcloud VARCHAR2(255 CHAR),
  youtube    VARCHAR2(255 CHAR),
  FOREIGN KEY (artists_id) REFERENCES artists (id),
  FOREIGN KEY (album_id) REFERENCES album (id),
  CONSTRAINT song_src_not_null CHECK (COALESCE(spotify, soundcloud, youtube) IS NOT NULL)
);

CALL CREATE_AUDIT_TABLE('song');

CREATE TABLE songs (
  id      INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  song_id INTEGER                            NOT NULL,
  FOREIGN KEY (song_id) REFERENCES song (id)
);

CALL CREATE_AUDIT_TABLE('songs');
