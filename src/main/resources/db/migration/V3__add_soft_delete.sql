-- V3__add_soft_delete.sql
-- Adds support for soft delete on all tables.

-- =============================================
-- SOFT DELETE: tb_photographers
-- =============================================
ALTER TABLE tb_photographers
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

CREATE INDEX IF NOT EXISTS idx_photographers_deleted_at
    ON tb_photographers(deleted_at)
    WHERE deleted_at IS NULL;

COMMENT ON COLUMN tb_photographers.deleted_at IS 'Indicates when the photographer was soft deleted. NULL means active.';

-- =============================================
-- SOFT DELETE: tb_clients
-- =============================================
ALTER TABLE tb_clients
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

CREATE INDEX IF NOT EXISTS idx_clients_deleted_at
    ON tb_clients(deleted_at)
    WHERE deleted_at IS NULL;

COMMENT ON COLUMN tb_clients.deleted_at IS 'Indicates when the client was soft deleted. NULL means active.';

-- =============================================
-- SOFT DELETE: tb_folders
-- =============================================
ALTER TABLE tb_folders
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

CREATE INDEX IF NOT EXISTS idx_folders_deleted_at
    ON tb_folders(deleted_at)
    WHERE deleted_at IS NULL;

COMMENT ON COLUMN tb_folders.deleted_at IS 'Indicates when the folder was soft deleted. NULL means active.';

-- =============================================
-- SOFT DELETE: tb_files
-- =============================================
ALTER TABLE tb_files
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

CREATE INDEX IF NOT EXISTS idx_files_deleted_at
    ON tb_files(deleted_at)
    WHERE deleted_at IS NULL;

COMMENT ON COLUMN tb_files.deleted_at IS 'Indicates when the file was soft deleted. NULL means active.';

-- =============================================
-- SOFT DELETE: tb_share_links
-- =============================================
ALTER TABLE tb_share_links
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

CREATE INDEX IF NOT EXISTS idx_share_links_deleted_at
    ON tb_share_links(deleted_at)
    WHERE deleted_at IS NULL;

COMMENT ON COLUMN tb_share_links.deleted_at IS 'Indicates when the share link was soft deleted. NULL means active.';

-- =============================================
-- VIEW: Active Folders (Not Deleted)
-- =============================================
CREATE OR REPLACE VIEW v_folders_active AS
    SELECT * FROM tb_folders
    WHERE deleted_at IS NULL;

COMMENT ON VIEW v_folders_active IS 'View of folders that are not soft deleted.';

-- =============================================
-- VIEW: Active Files (Not Deleted)
-- =============================================
CREATE OR REPLACE VIEW v_files_active AS
    SELECT * FROM tb_files
    WHERE deleted_at IS NULL;

COMMENT ON VIEW v_files_active IS 'View of files that are not soft deleted.';

-- =============================================
-- FUNCTION: Cascade Soft Delete
-- =============================================
CREATE OR REPLACE FUNCTION soft_delete_folder_cascade()
RETURNS TRIGGER AS $$
BEGIN
    -- Soft delete files in the folder
    UPDATE tb_files
    SET deleted_at = NEW.deleted_at
    WHERE folder_id = NEW.id
    AND deleted_at IS NULL;

    --Disable folder sharing links
    UPDATE tb_share_links
    SET active = FALSE,
        deleted_at = NEW.deleted_at
    WHERE folder_id = NEW.id
    AND deleted_at IS NULL;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =============================================
-- TRIGGER: Soft Delete Cascade
-- =============================================
CREATE TRIGGER trigger_folder_soft_delete_cascade
    AFTER UPDATE OF deleted_at ON tb_folders
    FOR EACH ROW
    WHEN ( NEW.deleted_at IS NOT NULL AND OLD.deleted_at IS NULL)
    EXECUTE FUNCTION soft_delete_folder_cascade();

COMMENT ON TRIGGER trigger_folder_soft_delete_cascade ON tb_folders IS 'Cascades soft delete from folders to their files and disables share links.';
