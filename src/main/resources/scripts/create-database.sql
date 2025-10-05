-- Script para criar o banco de dados PostgreSQL
-- Execute este script como superusuário do PostgreSQL antes de iniciar a aplicação

-- Criar o banco de dados
CREATE DATABASE oficina_db
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'Portuguese_Brazil.1252'
    LC_CTYPE = 'Portuguese_Brazil.1252'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Conectar ao banco de dados
\c oficina_db

-- Comentário sobre o banco de dados
COMMENT ON DATABASE oficina_db IS 'Banco de dados do sistema de gestão de oficina mecânica - Tech Challenge';

-- As tabelas serão criadas automaticamente pelo Hibernate com base nas entidades JPA
-- quando a aplicação for iniciada pela primeira vez (spring.jpa.hibernate.ddl-auto=update)



