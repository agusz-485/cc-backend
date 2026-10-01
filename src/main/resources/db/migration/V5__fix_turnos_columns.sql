-- V5: Permitir NULL en columnas legacy de turnos
ALTER TABLE turnos MODIFY fecha_inicio datetime NULL;
ALTER TABLE turnos MODIFY fecha_fin datetime NULL;
ALTER TABLE turnos MODIFY monto_total decimal(10,2) NULL;
