CREATE TABLE playback_history (
  id      INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  user_id INTEGER                            NOT NULL,
  song_id INTEGER                            NOT NULL,
  time    DATETIME AS NOW(),
  FOREIGN KEY (user_id) REFERENCES user (id),
  FOREIGN KEY (song_id) REFERENCES song (id)
);

CALL CREATE_AUDIT_TABLE('playback_history');
