package com.forum.repository;

import com.forum.common.model.Post;
import com.forum.common.model.PostWithUserName;
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

    @Query("SELECT new com.forum.common.model.PostWithUserName(p, u.username) FROM Post p LEFT JOIN User u ON p.authorId = u.id WHERE p.id = :postId")
    Optional<PostWithUserName> findPostWithUsernameById(@Param("postId") Long postId);

    @Query("SELECT new com.forum.common.model.PostWithUserName(p, u.username) FROM Post p JOIN User u ON p.authorId = u.id WHERE p.isPublished = true ORDER BY p.createdAt DESC")
    Page<PostWithUserName> findAllPublishedPostsWithAuthors(Pageable pageable);

    @Query("SELECT u.username FROM User u WHERE u.id = (SELECT p.authorId FROM Post p WHERE p.id = :postId)")
    Optional<String> findUsernameByPostId(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    int incrementViewCount(@Param("postId") Long postId);

}
