-- 1. Remove the inherited columns from tb_photographers
ALTER TABLE tb_photographers 
    DROP COLUMN name,
    DROP COLUMN email,
    DROP COLUMN password,
    DROP COLUMN role;

-- 2. Transforms the photographer's ID into a Foreign Key pointing to tb_users.
ALTER TABLE tb_photographers 
    ADD CONSTRAINT fk_photo_user FOREIGN KEY (id) REFERENCES tb_users (id) ON DELETE CASCADE;

-- 3. Remove the inherited columns from tb_clients
ALTER TABLE tb_clients 
    DROP COLUMN name,
    DROP COLUMN email,
    DROP COLUMN password,
    DROP COLUMN role;

-- 4. Transforms the cliente's ID into a Foreign Key pointing to tb_users.

ALTER TABLE tb_clients 
    ADD CONSTRAINT fk_client_user FOREIGN KEY (id) REFERENCES tb_users (id) ON DELETE CASCADE;