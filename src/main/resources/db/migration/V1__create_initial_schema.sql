-- V1__create_initial_schema.sql
-- Creating the initial PhotoVault schema

-- =============================================
-- TABLE: tb_photographers (Photographers Table)
-- =============================================
CREATE TABLE IF NOT EXISTS tb_photographers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20),
    storage_quota_bytes BIGINT DEFAULT 10737418240, -- 10 GB default quota
    storage_used_bytes BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE tb_photographers IS 'Photographers who upload and manage folders and photos in the PhotoVault application.';
COMMENT ON COLUMN tb_photographers.storage_quota_bytes IS 'Storage limit in bytes (default 10GB).';
COMMENT ON COLUMN tb_photographers.storage_used_bytes IS 'storage used in bytes.';

-- =============================================
-- TABLE: tb_clients (Clients Table)
-- =============================================
CREATE TABLE IF NOT EXISTS tb_clients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE tb_clients IS 'Clients who can view and download photos shared by photographers.';

-- =============================================
-- TABLE: tb_folders (Folders Table)
-- =============================================
CREATE TABLE IF NOT EXISTS tb_folders(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    photographer_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_photographer
        FOREIGN KEY (photographer_id)
        REFERENCES tb_photographers(id)
        ON DELETE CASCADE
);

COMMENT ON TABLE tb_folders IS 'Folders created by photographers to organize their photos.';
COMMENT ON COLUMN tb_folders.photographer_id IS 'Reference to the photographer who owns the folder.';

-- =============================================
-- TABLE: tb_files (files Table)
-- =============================================
CREATE TABLE IF NOT EXISTS tb_files (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    stored_name VARCHAR(255) NOT NULL UNIQUE,
    path TEXT NOT NULL,
    size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    folder_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    upload_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deletedAt TIMESTAMP,

    CONSTRAINT fk_file_folder
        FOREIGN KEY (folder_id)
        REFERENCES tb_folders(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_file_size_positive
        CHECK (size > 0)
);

COMMENT ON TABLE tb_files IS 'Files (photos) uploaded by photographers into folders.';
COMMENT ON COLUMN tb_files.stored_name IS 'Unique name used for storing the file on the server.';
COMMENT ON COLUMN tb_files.path IS 'File system path where the file is stored.';
COMMENT ON COLUMN tb_files.size IS 'Size of the file in bytes.';
COMMENT ON COLUMN tb_files.content_type IS 'MIME type of the file (e.g., image/jpeg).';

-- =============================================
-- TABLE: tb_share_links (Share Links Table)
-- =============================================
CREATE TABLE IF NOT EXISTS tb_share_links (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    folder_id BIGINT NOT NULL,
    client_id BIGINT,
    expiration_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    access_count INTEGER DEFAULT 0,

    CONSTRAINT fk_sharelink_folder
    FOREIGN KEY (folder_id)
    REFERENCES tb_folders(id)
    ON DELETE CASCADE,

    CONSTRAINT chk_expiration_future
    CHECK (expiration_date > created_at)
    );
COMMENT ON TABLE tb_share_links IS 'links generated to share folders with clients.';
COMMENT ON COLUMN tb_share_links.token IS 'Unique token for accessing the shared folder.';
COMMENT ON COLUMN tb_share_links.access_count IS 'Number of times the share link has been accessed.';

-- =============================================
-- FUNCTION: Update automatically updated_at
-- =============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS $$
    BEGIN
        NEW.updated_at = CURRENT_TIMESTAMP;
        RETURN NEW;
    END;
    $$ LANGUAGE plpgsql;

-- =============================================
-- TRIGGERS: Update updated_at
-- =============================================
CREATE TRIGGER trigger_photographers_updated_at
    BEFORE UPDATE ON tb_photographers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_clients_updated_at
    BEFORE UPDATE ON tb_clients
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_folders_updated_at
    BEFORE UPDATE ON tb_folders
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();