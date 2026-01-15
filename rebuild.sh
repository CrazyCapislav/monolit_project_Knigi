#!/bin/bash
# Скрипт для пересборки и запуска Docker контейнеров после изменений кода

# Включить BuildKit для ускорения сборки
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1

# Проверка аргументов
SERVICE_NAME="$1"

if [ -z "$SERVICE_NAME" ]; then
    echo "========================================"
    echo "  Rebuilding ALL services"
    echo "========================================"
    echo ""
    echo "🔨 Building all services in parallel..."
    docker-compose build --parallel
    
    if [ $? -ne 0 ]; then
        echo ""
        echo "❌ Build failed!"
        exit 1
    fi
    
    echo ""
    echo "🚀 Starting all services..."
    docker-compose up -d
    
    if [ $? -ne 0 ]; then
        echo ""
        echo "❌ Failed to start services!"
        exit 1
    fi
    
    echo ""
    echo "✅ All services rebuilt and started successfully!"
    echo ""
    echo "📊 View logs: docker-compose logs -f"
    echo "📊 View status: docker-compose ps"
else
    echo "========================================"
    echo "  Rebuilding service: $SERVICE_NAME"
    echo "========================================"
    echo ""
    echo "🔨 Building service: $SERVICE_NAME"
    docker-compose build --parallel "$SERVICE_NAME"
    
    if [ $? -ne 0 ]; then
        echo ""
        echo "❌ Build failed!"
        exit 1
    fi
    
    echo ""
    echo "🚀 Starting service: $SERVICE_NAME"
    docker-compose up -d "$SERVICE_NAME"
    
    if [ $? -ne 0 ]; then
        echo ""
        echo "❌ Failed to start service!"
        exit 1
    fi
    
    echo ""
    echo "✅ Service $SERVICE_NAME rebuilt and started successfully!"
    echo ""
    echo "📊 View logs: docker-compose logs -f $SERVICE_NAME"
fi

