ALTER TABLE resenias DROP FOREIGN KEY FKe1ecsejeyj0muq0298a31jrbt; ALTER TABLE resenias ADD CONSTRAINT fk_resenias_cuidador FOREIGN KEY (cuidador_id) REFERENCES usuarios (id) ON DELETE CASCADE;
