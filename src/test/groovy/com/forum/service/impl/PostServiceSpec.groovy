package com.forum.service.impl

import com.forum.dto.request.PostRequest
import com.forum.exception.BadRequestException
import com.forum.exception.ResourceNotFoundException
import com.forum.model.Post
import com.forum.model.User
import com.forum.repository.CommentRepository
import com.forum.repository.PostRepository
import com.forum.repository.UserRepository
import com.forum.service.PostService
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

class PostServiceSpec extends Specification {

    PostRepository postRepository = Mock()
    UserRepository userRepository = Mock()
    CommentRepository commentRepository = Mock()

    @Subject
    PostService postService = new PostService(
        postRepository: postRepository,
        userRepository: userRepository,
        commentRepository: commentRepository
    )

    def "createPost should save and return post response"() {
        given:
        def userId = 1L
        def user = new User(id: userId, username: "testuser")
        def postRequest = new PostRequest(title: "Test Title", content: "Test Content", isPublished: true)
        
        when:
        def result = postService.createPost(postRequest, userId)
        
        then:
        1 * userRepository.findById(userId) >> Optional.of(user)
        1 * postRepository.save(_ as Post) >> { args ->
            def post = args[0]
            assert post.title == "Test Title"
            assert post.content == "Test Content"
            assert post.authorId == userId
            assert post.isPublished()
            return new Post(id: 1L, title: post.title, content: post.content, authorId: post.authorId, 
                            isPublished: post.isPublished(), createdAt: LocalDateTime.now())
        }
        result.id == 1L
        result.title == "Test Title"
        result.authorUsername == "testuser"
    }

    def "getPostById should return post response with author info"() {
        given:
        def postId = 1L
        def authorId = 1L
        def post = new Post(id: postId, title: "Test Title", content: "Test Content", 
                           authorId: authorId, viewCount: 5)
        def username = "testuser"
        
        when:
        def result = postService.getPostById(postId)
        
        then:
        1 * postRepository.findPostWithAuthorUsername(postId) >> Optional.of([post, username] as Object[])
        1 * postRepository.incrementViewCount(postId)
        1 * commentRepository.countByPostId(postId) >> 3L
        
        result.id == postId
        result.title == "Test Title"
        result.authorUsername == username
        result.viewCount == 6 // 5 + 1
        result.commentCount == 3L
    }

    def "getPostById should throw exception when post not found"() {
        given:
        def postId = 999L
        
        when:
        postService.getPostById(postId)
        
        then:
        1 * postRepository.findPostWithAuthorUsername(postId) >> Optional.empty()
        thrown(ResourceNotFoundException)
    }

    def "updatePost should update and return post response when user is author"() {
        given:
        def postId = 1L
        def userId = 1L
        def post = new Post(id: postId, title: "Old Title", content: "Old Content", authorId: userId)
        def postRequest = new PostRequest(title: "New Title", content: "New Content", isPublished: false)
        
        when:
        def result = postService.updatePost(postId, postRequest, userId)
        
        then:
        1 * postRepository.findById(postId) >> Optional.of(post)
        1 * userRepository.findById(userId) >> Optional.of(new User(id: userId, username: "testuser"))
        1 * postRepository.save(_ as Post) >> { args ->
            def updatedPost = args[0]
            assert updatedPost.title == "New Title"
            assert updatedPost.content == "New Content"
            assert !updatedPost.isPublished()
            return updatedPost
        }
        1 * commentRepository.countByPostId(postId) >> 2L
        
        result.title == "New Title"
        result.content == "New Content"
        !result.isPublished()
    }

    def "updatePost should throw exception when user is not author"() {
        given:
        def postId = 1L
        def authorId = 1L
        def userId = 2L // Different user
        def post = new Post(id: postId, authorId: authorId)
        def postRequest = new PostRequest()
        
        when:
        postService.updatePost(postId, postRequest, userId)
        
        then:
        1 * postRepository.findById(postId) >> Optional.of(post)
        thrown(BadRequestException)
    }
}
