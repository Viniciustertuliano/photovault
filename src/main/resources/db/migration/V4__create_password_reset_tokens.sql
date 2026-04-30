CREATE TABLE IF NOT EXISTS tb_users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(25) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tb_reset_tokens_password (
    id BIGSERIAL PRIMARY KEY,
    token UUID NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE,
    

    CONSTRAINT fk_password_reset_user 
        FOREIGN KEY (user_id) 
        REFERENCES tb_users (id) 
        ON DELETE CASCADE
);

CREATE INDEX idx_reset_tokens_password ON tb_reset_tokens_password(token);

CREATE INDEX idx_password_reset_user_id ON tb_reset_tokens_password(user_id);