-- Fix face_template column type to match the expected type
ALTER TABLE employees ALTER COLUMN face_template TYPE bytea USING face_template::bytea;