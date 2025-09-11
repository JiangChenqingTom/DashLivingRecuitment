-- 帖子表：存储帖子内容
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL COMMENT '帖子标题',
    content TEXT NOT NULL COMMENT '帖子内容',
    author_id BIGINT NOT NULL COMMENT '关联的用户ID（作者）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '帖子创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '帖子更新时间',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    is_published BOOLEAN DEFAULT TRUE COMMENT '是否发布',
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE COMMENT '级联删除：用户删除时，其所有帖子也会被删除',
    INDEX idx_author (author_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子内容表';