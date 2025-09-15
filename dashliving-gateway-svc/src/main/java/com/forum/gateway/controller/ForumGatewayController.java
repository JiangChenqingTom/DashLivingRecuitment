package com.forum.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;

@RestController
public class ForumGatewayController {

    private final RestTemplate restTemplate;
    private final String authServiceUrl;
    private final String forumServiceUrl;

    public ForumGatewayController(RestTemplate restTemplate,
                                  @Value("${services.auth.url}") String authServiceUrl,
                                  @Value("${services.forum.url}") String forumServiceUrl) {
        this.restTemplate = restTemplate;
        this.authServiceUrl = authServiceUrl;
        this.forumServiceUrl = forumServiceUrl;
    }

    @RequestMapping("/**")
    public ResponseEntity<?> routeRequest(HttpServletRequest request) throws IOException {
        String requestPath = request.getRequestURI();
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        String targetUrl;
        if (requestPath.startsWith("/api/auth")) {
            targetUrl = authServiceUrl + requestPath;
        } else if (requestPath.startsWith("/api/posts")) {
            targetUrl = forumServiceUrl + requestPath;
        } else {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!"Host".equalsIgnoreCase(headerName)) {
                headers.set(headerName, request.getHeader(headerName));
            }
        }

        byte[] requestBody = request.getInputStream().readAllBytes();
        HttpEntity<byte[]> requestEntity = new HttpEntity<>(requestBody, headers);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(targetUrl);
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            uriBuilder.queryParam(paramName, request.getParameter(paramName));
        }

        ResponseEntity<?> response = restTemplate.exchange(
                uriBuilder.toUriString(),
                method,
                requestEntity,
                Object.class
        );

        return new ResponseEntity<>(response.getBody(), response.getStatusCode());
    }
}
