# Define the build directory
BUILD_DIR = public
DEPS_DIR = node_modules

clean:
	@if [ -d "$(DEPS_DIR)" ]; then \
		echo "Removing $(DEPS_DIR)..."; \
		rm -r "$(DEPS_DIR)"; \
		rm -r "package-lock.json"; \
	fi

# Setup 
setup: clean
	@npm i web

# Run server in dev mode
serve:
	$(MAKE) -j 2 frontend-serve backend-serve

# Run prettier source
format:
	@npx prettier . --write
	./mvnw spotless:apply

# Build for production
build: clean_build
	@npm run build

# Clean build directory if it exists
clean_build:
	@if [ -d "$(BUILD_DIR)" ]; then \
		echo "Removing $(BUILD_DIR)..."; \
		rm -r "$(BUILD_DIR)"; \
	fi

frontend-serve: clean_build
	@node browsersync.mjs	

backend-serve:
	./mvnw exec:java


test:
	./mvnw test	

include ./config/dev.env
DB_DSN:="host=$(POSTGRES_HOST) user=$(POSTGRES_USER) password=$(POSTGRES_PASSWORD) dbname=$(POSTGRES_DB) port=$(POSTGRES_PORT) sslmode=disable"
MIGRATE_OPTIONS=-allow-missing -dir="./sql"

db-up: ## up down on database
	goose -v $(MIGRATE_OPTIONS) postgres $(DB_DSN) up

db-down: ## Migrate down on database
	goose -v $(MIGRATE_OPTIONS) postgres $(DB_DSN) reset

db-rebuild: ## Reset the database
	make db-down
	make db-up