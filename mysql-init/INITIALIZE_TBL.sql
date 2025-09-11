-- 用户表：存储用户信息和认证数据
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名，用于登录和显示',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '用户邮箱，用于登录和验证',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希，存储加密后的密码',
    full_name VARCHAR(100) COMMENT '用户全名',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '账户创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '账户信息更新时间',
    last_login_at TIMESTAMP COMMENT '最后登录时间',
    is_active BOOLEAN DEFAULT TRUE COMMENT '账户是否激活',
    INDEX idx_email (email),
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 评论表：存储评论，关联到用户和帖子
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL COMMENT '关联的帖子ID',
    user_id BIGINT NOT NULL COMMENT '关联的用户ID（评论者）',
    content TEXT NOT NULL COMMENT '评论内容',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '评论创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '评论更新时间',
    parent_id BIGINT COMMENT '父评论ID，用于实现评论回复功能（自关联）',
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE COMMENT '级联删除：帖子删除时，其所有评论也会被删除',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE COMMENT '级联删除：用户删除时，其所有评论也会被删除',
    FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE COMMENT '级联删除：父评论删除时，子评论也会被删除',
    INDEX idx_post (post_id),
    INDEX idx_user (user_id),
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';
