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
