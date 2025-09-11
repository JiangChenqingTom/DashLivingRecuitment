package com.forum.repository;

import com.forum.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 查找作者的所有帖子
    Page<Post> findByAuthorId(Long authorId, Pageable pageable);

    // 查找所有已发布的帖子，并关联查询作者用户名
    @Query("SELECT p, u.username FROM Post p JOIN User u ON p.authorId = u.id WHERE p.isPublished = true ORDER BY p.createdAt DESC")
    Page<Object[]> findAllPublishedPostsWithAuthors(Pageable pageable);

    // 根据帖子ID查找作者用户名
    @Query("SELECT u.username FROM User u WHERE u.id = (SELECT p.authorId FROM Post p WHERE p.id = :postId)")
    Optional<String> findUsernameByPostId(@Param("postId") Long postId);

    // 查找热点帖子（浏览量>10）
    @Query("SELECT p, u.username FROM Post p JOIN User u ON p.authorId = u.id WHERE p.viewCount > 10 AND p.isPublished = true ORDER BY p.viewCount DESC")
    Page<Object[]> findHotPostsWithAuthors(Pageable pageable);
}
