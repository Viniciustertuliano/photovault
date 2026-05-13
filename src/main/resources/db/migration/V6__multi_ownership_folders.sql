-- 1. Create the associative table.
CREATE TABLE tb_folders_owners(
    folder_id BIGINT NOT NULL,
    photographer_id BIGINT NOT NULL,
    PRIMARY KEY (folder_id, photographer_id),
    CONSTRAINT fk_folder_owner_folder FOREIGN KEY (folder_id) REFERENCES tb_folders (id) ON DELETE CASCADE,
    CONSTRAINT fk_folder_owner_photographer FOREIGN KEY (photographer_id) REFERENCES tb_photographers (id) ON DELETE CASCADE
);

-- 2. Migrate the existing data from the tb_folders table to the new associative table.
INSERT INTO tb_folders_owners (folder_id, photographer_id)
SELECT id, photographer_id FROM tb_folders WHERE photographer_id IS NOT NULL;

-- 3. Remove the old constraint (foreign key).
ALTER TABLE tb_folders DROP CONSTRAINT fk_photographer;

-- 4. Remove the photographer_id column from the tb_folders table
ALTER TABLE tb_folders DROP COLUMN photographer_id CASCADE;

-- 5. Recreates the v_folders_active view (now without the photographer_id column).
CREATE OR REPLACE VIEW v_folders_active AS
    SELECT * FROM tb_folders
    WHERE deleted_at IS NULL;

COMMENT ON VIEW v_folders_active IS 'View of folders that are not soft deleted.';