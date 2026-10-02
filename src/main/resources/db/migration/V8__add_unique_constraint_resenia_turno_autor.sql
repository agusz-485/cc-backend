ALTER TABLE resenias ADD CONSTRAINT uq_resenia_turno_autor UNIQUE (turno_id, autor_id);
