CREATE TABLE tb_access_permissions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    folder_id BIGINT NOT NULL,
    permission_type VARCHAR(50) NOT NULL,
    invited_by_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_folder_permission UNIQUE (user_id, folder_id, permission_type),
    CONSTRAINT fk_permission_user FOREIGN KEY (user_id) REFERENCES tb_users (id) ON DELETE CASCADE,
    CONSTRAINT fk_permission_folder FOREIGN KEY (folder_id) REFERENCES tb_folders (id) ON DELETE CASCADE,
    CONSTRAINT fk_permission_inviter FOREIGN KEY (invited_by_id) REFERENCES tb_users (ID) ON DELETE SET NULL
);