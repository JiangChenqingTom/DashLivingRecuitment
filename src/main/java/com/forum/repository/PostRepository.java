package com.forum.repository;

import com.forum.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 查找作者的所有帖子
    Page<Post> findByAuthorId(Long authorId, Pageable pageable);

    @Query("SELECT p, u.username FROM Post p JOIN User u ON p.authorId = u.id WHERE p.isPublished = true ORDER BY p.createdAt DESC")
    Page<Object[]> findAllPublishedPostsWithAuthors(Pageable pageable);

    @Query("SELECT u.username FROM User u WHERE u.id = (SELECT p.authorId FROM Post p WHERE p.id = :postId)")
    Optional<String> findUsernameByPostId(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    int incrementViewCount(@Param("postId") Long postId);

}
