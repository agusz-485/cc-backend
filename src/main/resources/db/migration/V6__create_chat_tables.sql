CREATE TABLE IF NOT EXISTS conversaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario1_id BIGINT NOT NULL,
    usuario2_id BIGINT NOT NULL,
    ultimo_mensaje VARCHAR(500),
    ultimo_mensaje_fecha DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_conversacion_usuario1 FOREIGN KEY (usuario1_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_conversacion_usuario2 FOREIGN KEY (usuario2_id) REFERENCES usuarios (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS mensajes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversacion_id BIGINT NOT NULL,
    remitente_id BIGINT NOT NULL,
    destinatario_id BIGINT NOT NULL,
    contenido TEXT NOT NULL,
    leido BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_mensaje_conversacion FOREIGN KEY (conversacion_id) REFERENCES conversaciones (id) ON DELETE CASCADE,
    CONSTRAINT fk_mensaje_remitente FOREIGN KEY (remitente_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_mensaje_destinatario FOREIGN KEY (destinatario_id) REFERENCES usuarios (id) ON DELETE CASCADE
);

CREATE INDEX idx_conversacion_usuarios ON conversaciones (usuario1_id, usuario2_id);
CREATE INDEX idx_mensajes_conversacion ON mensajes (conversacion_id);