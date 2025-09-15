package com.forum.repository;

import com.forum.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
    List<Comment> findByParentId(Long parentId);
    void deleteByPostId(Long postId);
    
    @Query("SELECT c, u.username FROM Comment c JOIN User u ON c.userId = u.id WHERE c.postId = :postId ORDER BY c.createdAt ASC")
    List<Object[]> findCommentsWithUsernamesByPostId(@Param("postId") Long postId);
    
    long countByPostId(Long postId);
}
