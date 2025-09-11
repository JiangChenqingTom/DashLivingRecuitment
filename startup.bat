@echo off
chcp 65001 >nul 2>&1  # Set code page to UTF-8 to prevent garbled characters

:: Configuration (Modify these values according to your environment)
set "DOCKER_HUB_USERNAME=jiangthomas"
set "TOKEN_FILE_PATH=%USERPROFILE%\.ssh\DockerToken.txt"

:: Force run in new console window and keep it open
if not "%1"=="stayopen" (
    start cmd /k "%0 stayopen"
    exit /b
)

echo.
echo ==============================================
echo           Docker Application Deployment
echo ==============================================
echo.

:: Check if Docker is running
echo [1/5] Checking Docker status...
docker version >nul 2>&1
if %errorlevel% equ 0 (
    echo [SUCCESS] Docker is running
) else (
    echo [ERROR] Docker is NOT running. Please start Docker Desktop first.
    goto exit
)

:: Check token file existence
echo.
echo [2/5] Checking access token...
if not exist "%TOKEN_FILE_PATH%" (
    echo [ERROR] Token file not found: %TOKEN_FILE_PATH%
    echo        Please store your Docker Hub access token in this file.
    goto exit
)

:: Read access token
set /p DOCKER_ACCESS_TOKEN=<"%TOKEN_FILE_PATH%"
set "DOCKER_ACCESS_TOKEN=%DOCKER_ACCESS_TOKEN: =%"  # Remove spaces
if "%DOCKER_ACCESS_TOKEN%"=="" (
    echo [ERROR] Token file is empty
    goto exit
)

:: Login to Docker Hub
echo.
echo [3/5] Logging in to Docker Hub...
echo %DOCKER_ACCESS_TOKEN% | docker login -u "%DOCKER_HUB_USERNAME%" --password-stdin >nul 2>&1
if %errorlevel% equ 0 (
    echo [SUCCESS] Login successful
) else (
    echo [ERROR] Login failed. Check your username and token.
    goto exit
)

:: Pull images
echo.
echo [4/5] Pulling images...

echo Pulling MySQL image...
docker pull mysql:8.0.33
if %errorlevel% neq 0 (
    echo [ERROR] Failed to pull MySQL image
    goto exit
)

echo Pulling Redis image...
docker pull redis:7.0
if %errorlevel% neq 0 (
    echo [ERROR] Failed to pull Redis image
    goto exit
)

echo Pulling Java application image...
docker pull %DOCKER_HUB_USERNAME%/forum-app:latest
if %errorlevel% neq 0 (
    echo [ERROR] Failed to pull Java application image
    goto exit
)

:: Stop and remove existing containers (new step)
echo.
echo [5/5] Stopping and removing existing containers...
docker-compose down >nul 2>&1
if %errorlevel% equ 0 (
    echo [SUCCESS] Existing containers stopped and removed
) else (
    echo [WARNING] No existing containers found or failed to stop (proceeding anyway)
)

:: Start services
echo.
echo Starting services...
docker-compose up -d
if %errorlevel% equ 0 (
    echo.
    echo ==============================================
    echo           Operation completed successfully!
    echo ==============================================
    echo Running containers:
    docker-compose ps
) else (
    echo [ERROR] Failed to start services
)

:exit
echo.
echo Press any key to close this window...
pause >nul
