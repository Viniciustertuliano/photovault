-- V2__add_indexes.sql
-- Add indexes for performance optimization.

-- =============================================
-- INDEXES: tb_photographers
-- =============================================

-- Find by email (used at login)
CREATE INDEX IF NOT EXISTS idx_photographers_email ON tb_photographers(email);

-- Find by name (used in auto-complete search, filtering)
CREATE INDEX IF NOT EXISTS idx_photographers_name ON tb_photographers(name);

-- order by created_at (used in listing photographers)
CREATE INDEX IF NOT EXISTS idx_photographers_created_at ON tb_photographers(created_at DESC);

COMMENT ON INDEX idx_photographers_email IS 'Index to optimizes login and single email verification.';
COMMENT ON INDEX idx_photographers_name IS 'Index to optimizes photographer search and autocomplete.';

-- =============================================
-- INDEXES: tb_clients
-- =============================================

-- Find by email (used at login)
CREATE INDEX IF NOT EXISTS idx_clients__email ON tb_clients(email);

-- Find by name
CREATE INDEX IF NOT EXISTS idx_clients_name ON tb_clients(name);

-- order by created_at (used in listing clients)
CREATE INDEX IF NOT EXISTS idx_clients_created_at ON tb_clients(created_at DESC);

-- =============================================
-- INDEXES: tb_folders
-- =============================================

-- Find by photographer_id (used in listing folders for a photographer)
CREATE INDEX IF NOT EXISTS idx_folders_photographer_id ON tb_folders(photographer_id);

-- Find by name within a photographer
CREATE INDEX IF NOT EXISTS idx_folders_photographer_name ON tb_folders(photographer_id, name);

-- order by created_at (used in listing folders)
CREATE INDEX IF NOT EXISTS idx_folders_created_at ON tb_folders(created_at DESC);

-- Composite index for optimized pagination.
CREATE INDEX IF NOT EXISTS idx_folders_photographer_created_at ON tb_folders(photographer_id, created_at DESC);

COMMENT ON INDEX idx_folders_photographer_id IS 'FK index - optimizes JOIN and search within a photographer.';
COMMENT ON INDEX idx_folders_photographer_name IS 'Index to optimize folder name search within a photographer.';

-- =============================================
-- INDEXES: tb_files
-- =============================================

-- Find for folder_id (used in listing files within a folder)
CREATE INDEX IF NOT EXISTS idx_files_folder_id ON tb_files(folder_id);

-- Find by fileName
CREATE INDEX IF NOT EXISTS idx_files_name ON tb_files(name);

-- Find by content_type (used in filtering by type)
CREATE INDEX IF NOT EXISTS idx_files_content_type ON tb_files(content_type);

-- Order by upload_date (used in listing files)
CREATE INDEX IF NOT EXISTS idx_files_upload_date ON tb_files(upload_date DESC);

-- Composite index for optimized pagination.
CREATE INDEX IF NOT EXISTS idx_files_folder_upload ON tb_files(folder_id, upload_date DESC);

-- Find large files (used in storage management)
CREATE INDEX IF NOT EXISTS idx_files_size ON tb_files(size);

-- Index for calculating space used by photographer
CREATE INDEX IF NOT EXISTS idx_files_folder_size ON tb_files(folder_id, size);

COMMENT ON INDEX idx_files_folder_id IS 'FK index - optimizes JOIN and search within a folder.';
COMMENT ON INDEX idx_files_folder_upload IS 'Index to optimize file listing and pagination within a folder.';

-- =============================================
-- INDEXES: tb_share_links
-- =============================================

-- Find by link_token (used when accessing a shared link)
CREATE INDEX IF NOT EXISTS idx_share_links_token ON tb_share_links(token);

-- Find links by folder_id (used in managing share links for a folder)
CREATE INDEX IF NOT EXISTS idx_share_links_folder_id ON tb_share_links(folder_id);

-- Find active links (used in validating link access)
CREATE INDEX IF NOT EXISTS idx_share_links_active ON tb_share_links(active) WHERE active = TRUE;

-- Composite index for token validation.
CREATE INDEX IF NOT EXISTS idx_share_links_token_active ON tb_share_links(token, active, expiration_date);

COMMENT ON INDEX idx_share_links_token IS 'Index to optimize access to shared folders with token.';
COMMENT ON INDEX idx_share_links_active IS 'Index to optimize validation of active share links.';