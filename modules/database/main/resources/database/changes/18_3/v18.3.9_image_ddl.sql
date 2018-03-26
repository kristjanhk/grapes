CREATE TABLE image (
  id       INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  data     BLOB,
  link     VARCHAR2(255 CHAR),
  filename VARCHAR2(255 CHAR),
  width    INTEGER,
  height   INTEGER,
  type     VARCHAR2(255 CHAR),
  CONSTRAINT image_src_not_null CHECK (COALESCE(data, link, filename) IS NOT NULL)
);

CALL CREATE_AUDIT_TABLE('image');
