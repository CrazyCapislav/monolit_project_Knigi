@echo off
REM Скрипт для пересборки и запуска Docker контейнеров после изменений кода

setlocal enabledelayedexpansion

REM Включить BuildKit для ускорения сборки
set DOCKER_BUILDKIT=1
set COMPOSE_DOCKER_CLI_BUILD=1

REM Проверка аргументов
set SERVICE_NAME=%1

if "%SERVICE_NAME%"=="" (
    echo ========================================
    echo  Rebuilding ALL services
    echo ========================================
    echo.
    echo 🔨 Building all services in parallel...
    docker-compose build --parallel
    
    if errorlevel 1 (
        echo.
        echo ❌ Build failed!
        exit /b 1
    )
    
    echo.
    echo 🚀 Starting all services...
    docker-compose up -d
    
    if errorlevel 1 (
        echo.
        echo ❌ Failed to start services!
        exit /b 1
    )
    
    echo.
    echo ✅ All services rebuilt and started successfully!
    echo.
    echo 📊 View logs: docker-compose logs -f
    echo 📊 View status: docker-compose ps
) else (
    echo ========================================
    echo  Rebuilding service: %SERVICE_NAME%
    echo ========================================
    echo.
    echo 🔨 Building service: %SERVICE_NAME%
    docker-compose build --parallel %SERVICE_NAME%
    
    if errorlevel 1 (
        echo.
        echo ❌ Build failed!
        exit /b 1
    )
    
    echo.
    echo 🚀 Starting service: %SERVICE_NAME%
    docker-compose up -d %SERVICE_NAME%
    
    if errorlevel 1 (
        echo.
        echo ❌ Failed to start service!
        exit /b 1
    )
    
    echo.
    echo ✅ Service %SERVICE_NAME% rebuilt and started successfully!
    echo.
    echo 📊 View logs: docker-compose logs -f %SERVICE_NAME%
)

endlocal


