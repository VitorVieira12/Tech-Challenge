-- Script para criar o banco de dados PostgreSQL
-- Execute este script como superusuário do PostgreSQL antes de iniciar a aplicação

-- Nota: O banco de dados 'tech_challenge' já é criado automaticamente pelo docker-compose
-- através da variável POSTGRES_DB. Este script pode conter configurações adicionais se necessário.

-- Comentário sobre o banco de dados
COMMENT ON DATABASE tech_challenge IS 'Banco de dados do sistema de gestão de oficina mecânica - Tech Challenge';

-- As tabelas serão criadas automaticamente pelo Hibernate com base nas entidades JPA
-- quando a aplicação for iniciada pela primeira vez (spring.jpa.hibernate.ddl-auto=update)






