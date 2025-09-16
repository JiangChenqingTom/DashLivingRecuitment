# DashLiving Forum

## Front-end code is in `frontend/`

## Back-end implementation as below

| Category | Technology/Component | Version | Purpose |
|----------|---------------------|---------|---------|
| Language | Java | 11      | Main programming language |
| Framework | Spring Boot | 2.7.10  | Microservices foundation |
| Database | MySQL | 8.0.33  | Persistent storage |
| Cache | Redis | 7.0     | Performance optimization and session caching |
| Build Tool | Gradle | 8.10    | Project build and dependency management |
| Containerization | Docker | 28.3.3  | Application containerization |
| Orchestration | Docker Compose | v2.39.2-desktop.1  | Local development environment deployment |
| Auth | Spring Security + JWT | -       | User authentication and authorization |
| Testing | JUnit 5 + Mockito | -       | Unit and integration testing |
| E2E Testing | TestContainers | -       | Containerized integration tests |
| CI/CD | GitHub Actions | -       | Continuous integration and build automation |

## Core Features

- **User Authentication**: Registration, login
- **Post Management**: Create, view, edit, delete posts
- **Comment System**: Support for nested comments with tree structure display
- **Redis Caching**: Cache for hot posts and comments to improve performance
- **Docker Deployment**: Containerized deployment with Docker Compose
- **CI/CD Pipeline**: Automated testing, building, and deployment processes

## Quick Start

### Prerequisites
- JDK 11
- Docker and Docker Compose
- Gradle 8.10
- Git

### Local Development Setup

1. **Clone the project**
```bash
git clone https://github.com/JiangChenqingTom/DashLivingRecuitment.git
cd DashLivingRecuitment
```


### Automated Deployment
Use `startup.bat` script for automated deployment:
```bash
startup.bat
```
This will start the following services:
- MySQL database (port 3306)
- Redis cache (port 6379)
- Forum application (port 8080)
- 
The script will:
1. Check Docker status
2. Verify Docker Hub access token
3. Login to Docker Hub
4. Build and push Docker images
5. Start container services

## Testing

### Unit Tests
```bash
./gradlew test
```

### End-to-End Tests
gradlew test --tests "com.forum.e2e.ForumE2ETest"

Report will be generated at `build/reports/jacoco/test/html/index.html`

## Database Design

### Main Tables
- **users**: User information
- **posts**: Posts content and metadata
- **comments**: Comments with self-referencing for nested structure
- **notifications**: User notifications

### Caching Strategy
- Hot posts cache: Redis Hash structure
- User session cache: Redis for authentication info
- Comment tree cache: Optimized for nested comment query performance
