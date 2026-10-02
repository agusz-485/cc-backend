DROP TABLE IF EXISTS `reportes`;
CREATE TABLE `reportes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `reportante_id` bigint NOT NULL,
  `reportado_id` bigint DEFAULT NULL,
  `turno_id` bigint DEFAULT NULL,
  `tipo` varchar(50) NOT NULL,
  `motivo` varchar(150) NOT NULL,
  `descripcion` text NOT NULL,
  `estado` varchar(50) NOT NULL DEFAULT 'PENDIENTE',
  `respuesta_admin` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_reporte_reportante` (`reportante_id`),
  KEY `idx_reporte_reportado` (`reportado_id`),
  KEY `idx_reporte_turno` (`turno_id`),
  KEY `idx_reporte_estado` (`estado`),
  CONSTRAINT `fk_reporte_reportante` FOREIGN KEY (`reportante_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_reporte_reportado` FOREIGN KEY (`reportado_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_reporte_turno` FOREIGN KEY (`turno_id`) REFERENCES `turnos` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
