package com.forum.e2e;

import com.forum.dto.request.CommentRequest;
import com.forum.dto.request.LoginRequest;
import com.forum.dto.request.PostRequest;
import com.forum.dto.request.RegisterRequest;
import com.forum.dto.response.CommentResponse;
import com.forum.dto.response.JwtResponse;
import com.forum.dto.response.PostResponse;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ForumE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MockMvc mockMvc;

    private static String baseUrl;
    private static String authToken;
    private static Long testUserId;
    private static Long testPostId;
    private static Long testCommentId;

    // Test container configurations
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.33"))
            .withEnv("MYSQL_ROOT_PASSWORD", "root_pass")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_pass")
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("MySQLContainer")))
            .waitingFor(Wait.forLogMessage(".*ready for connections.*", 1));

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.0"))
            .withExposedPorts(6379)
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("RedisContainer")))
            .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api";
    }

    @Test
    @Order(1)
    void testUserRegistration() throws Exception {
        // Create registration request
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Test@1234");
        registerRequest.setFullName("Test User");

        // Send registration request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterRequest> request = new HttpEntity<>(registerRequest, headers);

        ResponseEntity<JwtResponse> response = restTemplate.postForEntity(
                baseUrl + "/auth/register",
                request,
                JwtResponse.class
        );

        // Validate response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotEmpty();
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");

        // Store auth token and user ID for subsequent tests
        authToken = response.getBody().getToken();
        testUserId = response.getBody().getId();
    }

    @Test
    @Order(2)
    void testUserLogin() throws Exception {
        // Create login request
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("Test@1234");

        // Send login request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<JwtResponse> response = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                request,
                JwtResponse.class
        );

        // Validate response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotEmpty();

    }

    @Test
    @Order(3)
    void testCreatePost() {
        // Create post request
        PostRequest postRequest = new PostRequest();
        postRequest.setTitle("Test Post Title");
        postRequest.setContent("This is the content of the test post.");
        postRequest.setPublished(true);

        // Send create post request with auth token
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        HttpEntity<PostRequest> request = new HttpEntity<>(postRequest, headers);

        ResponseEntity<PostResponse> response = restTemplate.postForEntity(
                baseUrl + "/posts",
                request,
                PostResponse.class
        );

        // Validate response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Test Post Title");
        assertThat(response.getBody().getContent()).isEqualTo("This is the content of the test post.");
        assertThat(response.getBody().getAuthorId()).isEqualTo(testUserId);

        // Store post ID for subsequent tests
        testPostId = response.getBody().getId();
    }

    @Test
    @Order(4)
    void testGetPostById() {
        // Send get post request
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<PostResponse> response = restTemplate.exchange(
                baseUrl + "/posts/" + testPostId,
                HttpMethod.GET,
                request,
                PostResponse.class
        );

        // Validate response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(testPostId);
        assertThat(response.getBody().getTitle()).isEqualTo("Test Post Title");
    }

    @Test
    @Order(5)
    void testUpdatePost() {
        // Create updated post request
        PostRequest updateRequest = new PostRequest();
        updateRequest.setTitle("Updated Test Post Title");
        updateRequest.setContent("This is the updated content of the test post.");
        updateRequest.setPublished(true);

        // Send update post request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        HttpEntity<PostRequest> request = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<PostResponse> response = restTemplate.exchange(
                baseUrl + "/posts/" + testPostId,
                HttpMethod.PUT,
                request,
                PostResponse.class
        );

        // Validate response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(testPostId);
        assertThat(response.getBody().getTitle()).isEqualTo("Updated Test Post Title");
        assertThat(response.getBody().getContent()).isEqualTo("This is the updated content of the test post.");
    }

    @Test
    @Order(6)
    void testCreateComment() {
        // Create comment request
        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setContent("This is a test comment.");
        commentRequest.setParentId(null); // Top-level comment

        // Send create comment request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        HttpEntity<CommentRequest> request = new HttpEntity<>(commentRequest, headers);

        ResponseEntity<CommentResponse> response = restTemplate.postForEntity(
                baseUrl + "/posts/" + testPostId + "/comments",
                request,
                CommentResponse.class
        );

        // Validate response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEqualTo("This is a test comment.");
        assertThat(response.getBody().getPostId()).isEqualTo(testPostId);
        assertThat(response.getBody().getUserId()).isEqualTo(testUserId);

        // Store comment ID for subsequent tests
        testCommentId = response.getBody().getId();
    }

    @Test
    @Order(7)
    void testCreateReplyToComment() {
        // Create reply request
        CommentRequest replyRequest = new CommentRequest();
        replyRequest.setContent("This is a reply to the test comment.");
        replyRequest.setParentId(testCommentId); // Reply to the comment we created

        // Send create reply request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        HttpEntity<CommentRequest> request = new HttpEntity<>(replyRequest, headers);

        ResponseEntity<CommentResponse> response = restTemplate.postForEntity(
                baseUrl + "/posts/" + testPostId + "/comments",
                request,
                CommentResponse.class
        );

        // Validate response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEqualTo("This is a reply to the test comment.");
        assertThat(response.getBody().getParentId()).isEqualTo(testCommentId);
    }

    @Test
    @Order(8)
    void testGetCommentsForPost() {
        // Send get comments request
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<CommentResponse[]> response = restTemplate.exchange(
                baseUrl + "/posts/" + testPostId + "/comments",
                HttpMethod.GET,
                request,
                CommentResponse[].class
        );

        // Validate response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);

        // Verify hierarchy - first comment should have one reply
        CommentResponse firstComment = response.getBody()[0];
        if (firstComment.getId().equals(testCommentId)) {
            assertThat(firstComment.getReplies()).hasSize(1);
            assertThat(firstComment.getReplies().get(0).getContent()).isEqualTo("This is a reply to the test comment.");
        }
    }

    @Test
    @Order(9)
    void testUpdateComment() {
        // Create updated comment request
        CommentRequest updateRequest = new CommentRequest();
        updateRequest.setContent("This is the updated test comment.");
        updateRequest.setParentId(null);

        // Send update comment request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        HttpEntity<CommentRequest> request = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<CommentResponse> response = restTemplate.exchange(
                baseUrl + "/posts/" + testPostId + "/comments/" + testCommentId,
                HttpMethod.PUT,
                request,
                CommentResponse.class
        );

        // Validate response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEqualTo("This is the updated test comment.");
    }

    @Test
    @Order(10)
    void testDeleteComment() {
        // Send delete comment request
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/posts/" + testPostId + "/comments/" + testCommentId,
                HttpMethod.DELETE,
                request,
                Void.class
        );

        // Validate response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(11)
    void testDeletePost() {
        // Send delete post request
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/posts/" + testPostId,
                HttpMethod.DELETE,
                request,
                Void.class
        );

        // Validate response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

}