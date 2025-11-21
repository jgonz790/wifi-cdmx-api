.PHONY: help build test run stop clean logs restart rebuild db-shell

# Default target
help:
	@echo "WiFi CDMX API - Available commands:"
	@echo ""
	@echo "  make build       - Build the application with Maven"
	@echo "  make test        - Run unit tests"
	@echo "  make run         - Start all services with Docker Compose"
	@echo "  make stop        - Stop all services"
	@echo "  make clean       - Stop services and remove volumes"
	@echo "  make logs        - Show application logs"
	@echo "  make restart     - Restart all services"
	@echo "  make rebuild     - Rebuild and restart all services"
	@echo "  make db-shell    - Connect to PostgreSQL shell"
	@echo "  make db-start    - Start only PostgreSQL"
	@echo "  make app-start   - Start only the application"
	@echo "  make package     - Create JAR package"
	@echo "  make dev         - Run application in development mode"
	@echo ""

# Build the application with Maven
build:
	@echo "Building application with Maven..."
	./mvnw clean package -DskipTests

# Run all tests
test:
	@echo "Running tests..."
	./mvnw test

# Start all services
run:
	@echo "Starting all services..."
	docker-compose up -d
	@echo "Services started. API available at http://localhost:8080"
	@echo "Swagger UI: http://localhost:8080/swagger-ui.html"

# Stop all services
stop:
	@echo "Stopping all services..."
	docker-compose down

# Stop services and remove volumes (clean database)
clean:
	@echo "Cleaning up services and volumes..."
	docker-compose down -v
	@echo "Cleaning Maven build..."
	./mvnw clean

# Show logs
logs:
	docker-compose logs -f app

# Restart all services
restart:
	@echo "Restarting services..."
	docker-compose restart

# Rebuild and restart services
rebuild:
	@echo "Rebuilding and restarting services..."
	docker-compose down
	docker-compose up -d --build
	@echo "Services rebuilt and started."

# Connect to PostgreSQL shell
db-shell:
	@echo "Connecting to PostgreSQL..."
	docker exec -it wifi-cdmx-db psql -U admin -d wifi_cdmx

# Start only PostgreSQL
db-start:
	@echo "Starting PostgreSQL..."
	docker-compose up -d postgres

# Start only the application (assumes PostgreSQL is running)
app-start:
	@echo "Starting application..."
	docker-compose up -d app

# Create JAR package
package:
	@echo "Creating JAR package..."
	./mvnw clean package

# Run application in development mode (local, with PostgreSQL in Docker)
dev:
	@echo "Starting PostgreSQL..."
	docker-compose up -d postgres
	@echo "Waiting for PostgreSQL to be ready..."
	@sleep 5
	@echo "Starting application in development mode..."
	./mvnw spring-boot:run

# Check API health
health:
	@echo "Checking API health..."
	@curl -s http://localhost:8080/api/v1/wifi-points/health || echo "API is not running"

# Show service status
status:
	@echo "Service status:"
	@docker-compose ps
