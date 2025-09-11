package com.forum.integration

import com.forum.dto.request.PostRequest
import com.forum.dto.response.JwtResponse
import com.forum.dto.response.PostResponse
import com.forum.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.RedisContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import spock.lang.Specification
import com.fasterxml.jackson.databind.ObjectMapper

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PostControllerIntegrationSpec extends Specification {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")

    @Container
    static RedisContainer<?> redis = new RedisContainer<>("redis:6.2-alpine")

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl)
        registry.add("spring.datasource.username", mysql::getUsername)
        registry.add("spring.datasource.password", mysql::getPassword)
        registry.add("spring.redis.host", redis::getHost)
        registry.add("spring.redis.port", redis::getFirstMappedPort)
    }

    @Autowired
    MockMvc mockMvc

    @Autowired
    UserRepository userRepository

    @Autowired
    ObjectMapper objectMapper

    String authToken
    Long userId

    def setup() {
        // Clean up before each test
        userRepository.deleteAll()

        // Create test user and get auth token
        def username = "testuser"
        def password = "password123"
        
        // Register user
        def registerRequest = [username: username, email: "test@example.com", password: password]
        def registerResponse = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .response
                .contentAsString
        
        def jwtResponse = objectMapper.readValue(registerResponse, JwtResponse)
        authToken = jwtResponse.token
        userId = jwtResponse.id
    }

    def "create and retrieve post"() {
        given:
        def postRequest = new PostRequest(title: "Integration Test Post", content: "Test content")
        
        when:
        def createResponse = mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer ${authToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .response
                .contentAsString
        
        def post = objectMapper.readValue(createResponse, PostResponse)
        
        then:
        post.title == "Integration Test Post"
        post.authorId == userId
        
        when:
        def getResponse = mockMvc.perform(get("/api/posts/${post.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .response
                .contentAsString
        
        def retrievedPost = objectMapper.readValue(getResponse, PostResponse)
        
        then:
        retrievedPost.id == post.id
        retrievedPost.title == "Integration Test Post"
        retrievedPost.viewCount == 1
    }

    def "update post"() {
        given:
        def postRequest = new PostRequest(title: "Original Title", content: "Original content")
        def postId = createTestPost(postRequest)
        
        def updateRequest = new PostRequest(title: "Updated Title", content: "Updated content")
        
        when:
        def updateResponse = mockMvc.perform(put("/api/posts/${postId}")
                .header("Authorization", "Bearer ${authToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .response
                .contentAsString
        
        def updatedPost = objectMapper.readValue(updateResponse, PostResponse)
        
        then:
        updatedPost.title == "Updated Title"
        updatedPost.content == "Updated content"
    }

    def "delete post"() {
        given:
        def postRequest = new PostRequest(title: "To Be Deleted", content: "Content")
        def postId = createTestPost(postRequest)
        
        when:
        mockMvc.perform(delete("/api/posts/${postId}")
                .header("Authorization", "Bearer ${authToken}"))
                .andExpect(status().isNoContent())
        
        then:
        mockMvc.perform(get("/api/posts/${postId}"))
                .andExpect(status().isNotFound())
    }

    private Long createTestPost(PostRequest request) {
        def response = mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer ${authToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .response
                .contentAsString
        
        return objectMapper.readValue(response, PostResponse).id
    }
}
