# K6 Login Load Testing Makefile

.PHONY: help test-quick test-normal test-stress test-all

# Default variables
BASE_URL ?= http://localhost:8081
EMAIL ?= mcrdik@gmail.com
PASSWORD ?= week123

help: ## Show this help message
	@echo "K6 Login Load Testing Commands:"
	@echo ""
	@echo "  test-quick      Run quick 30-second test"
	@echo "  test-normal     Run normal 2-minute test"
	@echo "  test-stress     Run stress test (up to 150 VUs)"
	@echo "  test-all        Run all tests sequentially"
	@echo ""
	@echo "Variables:"
	@echo "  BASE_URL=$(BASE_URL)"
	@echo "  EMAIL=$(EMAIL)"
	@echo "  PASSWORD=$(PASSWORD)"
	@echo ""
	@echo "Examples:"
	@echo "  make test-quick"
	@echo "  make test-normal BASE_URL=https://api.hris.com"
	@echo "  make test-stress EMAIL=user@example.com PASSWORD=pass123"

test-quick: ## Run quick 30-second test
	@echo "🚀 Running quick login test (30s)..."
	BASE_URL=$(BASE_URL) EMAIL=$(EMAIL) PASSWORD=$(PASSWORD) k6 run login-test-quick.js

test-normal: ## Run normal 2-minute test
	@echo "🚀 Running normal login test (2m)..."
	BASE_URL=$(BASE_URL) EMAIL=$(EMAIL) PASSWORD=$(PASSWORD) k6 run login-test.js

test-stress: ## Run stress test
	@echo "🚀 Running stress test (up to 150 VUs)..."
	BASE_URL=$(BASE_URL) EMAIL=$(EMAIL) PASSWORD=$(PASSWORD) k6 run login-test-stress.js

test-all: ## Run all tests sequentially
	@echo "🚀 Running all login tests..."
	@echo ""
	@echo "1️⃣ Quick Test (30s):"
	$(MAKE) test-quick
	@echo ""
	@echo "2️⃣ Normal Test (2m):"
	$(MAKE) test-normal
	@echo ""
	@echo "3️⃣ Stress Test:"
	$(MAKE) test-stress
	@echo ""
	@echo "✅ All tests completed!"

# Install k6 if not installed
install-k6:
	@echo "📦 Installing k6..."
	@if command -v brew > /dev/null 2>&1; then \
		brew install k6; \
	elif command -v apt > /dev/null 2>&1; then \
		sudo apt-get update && sudo apt-get install -y k6; \
	else \
		echo "Please install k6 manually: https://k6.io/docs/getting-started/installation/"; \
	fi

# Check if HRIS is running
check-api:
	@echo "🔍 Checking HRIS API at $(BASE_URL)..."
	@if curl -s $(BASE_URL)/actuator/health > /dev/null 2>&1; then \
		echo "✅ HRIS API is accessible"; \
	else \
		echo "❌ HRIS API is not accessible at $(BASE_URL)"; \
		echo "Please ensure HRIS is running before executing tests."; \
		exit 1; \
	fi

# Run tests with API check
test: check-api test-all ## Full test suite with API check