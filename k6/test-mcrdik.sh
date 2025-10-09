#!/bin/bash

# K6 Login Test Runner for mcrdik@gmail.com
# Simple script to run login tests for the specified user

set -e

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Configuration
BASE_URL="${BASE_URL:-http://localhost:8081}"
DEBUG="${DEBUG:-false}"

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Show usage
show_usage() {
    echo "K6 Login Test Runner for mcrdik@gmail.com"
    echo ""
    echo "Usage: $0 [TEST_TYPE] [OPTIONS]"
    echo ""
    echo "TEST_TYPE:"
    echo "  basic        Comprehensive login test (recommended)"
    echo "  quick        Quick 30-second test"
    echo "  rate-limit   Test rate limiting behavior (6 minutes)"
    echo ""
    echo "OPTIONS:"
    echo "  -u, --url URL       Base URL (default: http://localhost:8081)"
    echo "  -d, --debug         Enable debug logging"
    echo "  -h, --help          Show this help"
    echo ""
    echo "Examples:"
    echo "  $0 basic                    # Run comprehensive test"
    echo "  $0 quick                    # Run quick test"
    echo "  $0 rate-limit               # Test rate limiting"
    echo "  $0 -u https://api.hris.com # Test against production"
    echo "  $0 -d basic                 # Run with debug mode"
}

# Check if k6 is installed
check_k6() {
    if ! command -v k6 &> /dev/null; then
        print_error "k6 is not installed. Please install k6 first:"
        echo "  macOS: brew install k6"
        echo "  Linux: sudo apt-get install k6"
        exit 1
    fi
}

# Check if HRIS is running
check_hris() {
    local max_attempts=10
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
            print_success "HRIS is running at $BASE_URL"
            return 0
        fi

        print_warning "Attempt $attempt/$max_attempts: HRIS not accessible at $BASE_URL"
        sleep 2
        ((attempt++))
    done

    print_error "HRIS is not accessible at $BASE_URL"
    exit 1
}

# Run test function
run_test() {
    local test_type=$1
    local test_file=""

    case $test_type in
        "basic")
            test_file="tests/mcrdik-login-test.js"
            ;;
        "quick")
            test_file="tests/quick-login-test.js"
            ;;
        "rate-limit")
            test_file="tests/mcrdik-rate-limit-test.js"
            ;;
        *)
            print_error "Unknown test type: $test_type"
            show_usage
            exit 1
            ;;
    esac

    if [ ! -f "$test_file" ]; then
        print_error "Test file not found: $test_file"
        exit 1
    fi

    print_status "Running $test_type test for mcrdik@gmail.com..."
    print_status "Base URL: $BASE_URL"
    print_status "Test file: $test_file"
    echo ""

    # Prepare environment
    local env_vars="BASE_URL=$BASE_URL"
    if [ "$DEBUG" = "true" ]; then
        env_vars="$env_vars DEBUG=true"
    fi

    # Run the test
    if env $env_vars k6 run "$test_file"; then
        print_success "$test_type test completed successfully!"
    else
        print_error "$test_type test failed!"
        exit 1
    fi
}

# Parse command line arguments
TEST_TYPE=""
while [[ $# -gt 0 ]]; do
    case $1 in
        -u|--url)
            BASE_URL="$2"
            shift 2
            ;;
        -d|--debug)
            DEBUG="true"
            shift
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        basic|quick|rate-limit)
            TEST_TYPE="$1"
            shift
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Default to basic test if not specified
TEST_TYPE="${TEST_TYPE:-basic}"

# Show configuration
print_status "Configuration:"
echo "  User: mcrdik@gmail.com"
echo "  Base URL: $BASE_URL"
echo "  Test Type: $TEST_TYPE"
echo "  Debug Mode: $DEBUG"
echo ""

# Pre-flight checks
print_status "Running pre-flight checks..."
check_k6
check_hris

# Run the test
run_test "$TEST_TYPE"

echo ""
print_success "Test completed! Check the output above for results."