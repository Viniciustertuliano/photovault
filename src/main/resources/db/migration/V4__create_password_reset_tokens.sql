CREATE TABLE tb_password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token UUID NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE,
    

    CONSTRAINT fk_password_reset_user 
        FOREIGN KEY (user_id) 
        REFERENCES tb_photographers (id) 
        ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_token ON tb_password_reset_tokens(token);

CREATE INDEX idx_password_reset_user_id ON tb_password_reset_tokens(user_id);