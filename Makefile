# Makefile для управления Docker контейнерами

.PHONY: help rebuild-service rebuild-all build build-parallel up down logs ps clean

# Включить BuildKit
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1

help:
	@echo "Available commands:"
	@echo "  make rebuild-service SERVICE=<name>  - Rebuild and restart specific service"
	@echo "  make rebuild-all                     - Rebuild all services in parallel"
	@echo "  make build                          - Build all services"
	@echo "  make build-parallel                 - Build all services in parallel"
	@echo "  make up                             - Start all services"
	@echo "  make down                           - Stop all services"
	@echo "  make logs SERVICE=<name>            - View logs (all or specific service)"
	@echo "  make ps                             - Show running containers"
	@echo "  make clean                          - Remove containers, networks, and volumes"
	@echo ""
	@echo "Available services:"
	@echo "  - eureka-server"
	@echo "  - config-server"
	@echo "  - gateway"
	@echo "  - auth-service"
	@echo "  - book-service"
	@echo "  - exchange-service"
	@echo "  - publication-service"

rebuild-service:
	@if [ -z "$(SERVICE)" ]; then \
		echo "Error: SERVICE is required"; \
		echo "Usage: make rebuild-service SERVICE=<service-name>"; \
		exit 1; \
	fi
	@echo "🔨 Building service: $(SERVICE)"
	docker-compose build --parallel $(SERVICE)
	@echo "🚀 Starting service: $(SERVICE)"
	docker-compose up -d $(SERVICE)
	@echo "✅ Service $(SERVICE) rebuilt and started!"

rebuild-all:
	@echo "🔨 Building all services in parallel..."
	docker-compose build --parallel
	@echo "🚀 Starting all services..."
	docker-compose up -d
	@echo "✅ All services rebuilt and started!"

build:
	docker-compose build

build-parallel:
	docker-compose build --parallel

up:
	docker-compose up -d

down:
	docker-compose down

logs:
	@if [ -z "$(SERVICE)" ]; then \
		docker-compose logs -f; \
	else \
		docker-compose logs -f $(SERVICE); \
	fi

ps:
	docker-compose ps

clean:
	docker-compose down -v
	docker system prune -f

