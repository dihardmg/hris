#!/bin/bash

# K6 Load Testing Runner for HRIS Login Endpoint
# This script provides easy execution of all test scenarios

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default configuration
BASE_URL="${BASE_URL:-http://localhost:8081}"
ENVIRONMENT="${ENVIRONMENT:-development}"
DEBUG="${DEBUG:-false}"
OUTPUT_DIR="results"

# Create results directory
mkdir -p "$OUTPUT_DIR"

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if k6 is installed
check_k6() {
    if ! command -v k6 &> /dev/null; then
        print_error "k6 is not installed. Please install k6 first:"
        echo "  macOS: brew install k6"
        echo "  Linux: sudo apt-get install k6"
        echo "  Windows: choco install k6"
        exit 1
    fi
    print_success "k6 is installed"
}

# Function to check if HRIS is running
check_hris() {
    local max_attempts=30
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

    print_error "HRIS is not accessible at $BASE_URL after $max_attempts attempts"
    print_status "Please ensure HRIS application is running before executing tests"
    exit 1
}

# Function to run a test
run_test() {
    local test_name=$1
    local test_file=$2
    local additional_options=$3

    print_status "Running $test_name..."

    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local json_output="$OUTPUT_DIR/${test_name}_${timestamp}.json"
    local html_output="$OUTPUT_DIR/${test_name}_${timestamp}.html"

    # Prepare environment variables
    local env_vars="BASE_URL=$BASE_URL"
    if [ "$DEBUG" = "true" ]; then
        env_vars="$env_vars DEBUG=true"
    fi

    # Run the test
    if env $env_vars k6 run "$test_file" \
        --out json="$json_output" \
        $additional_options 2>&1 | tee "$OUTPUT_DIR/${test_name}_${timestamp}.log"; then

        print_success "$test_name completed successfully"

        # Generate HTML report if k6 reporter is available
        if command -v k6-reporter &> /dev/null; then
            cat "$json_output" | k6-reporter > "$html_output"
            print_status "HTML report generated: $html_output"
        fi

        return 0
    else
        print_error "$test_name failed"
        return 1
    fi
}

# Function to show usage
show_usage() {
    echo "HRIS K6 Load Testing Runner"
    echo ""
    echo "Usage: $0 [OPTIONS] [TEST_TYPE]"
    echo ""
    echo "TEST_TYPE:"
    echo "  basic         Run basic load test"
    echo "  rate-limit    Run rate limiting test"
    echo "  stress        Run stress test"
    echo "  soak          Run soak test (endurance)"
    echo "  spike         Run spike test"
    echo "  all           Run all tests (default)"
    echo ""
    echo "OPTIONS:"
    echo "  -u, --url URL        Base URL for HRIS API (default: http://localhost:8081)"
    echo "  -e, --env ENV        Environment (development|staging|production)"
    echo "  -d, --debug          Enable debug logging"
    echo "  -o, --output DIR     Output directory (default: results)"
    echo "  -h, --help           Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                           # Run all tests"
    echo "  $0 basic                     # Run only basic load test"
    echo "  $0 -u https://api.hris.com  # Run against production"
    echo "  $0 -d -e staging            # Run with debug mode against staging"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -u|--url)
            BASE_URL="$2"
            shift 2
            ;;
        -e|--env)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -d|--debug)
            DEBUG="true"
            shift
            ;;
        -o|--output)
            OUTPUT_DIR="$2"
            mkdir -p "$OUTPUT_DIR"
            shift 2
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        basic|rate-limit|stress|soak|spike|all)
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

# Default to all tests if not specified
TEST_TYPE="${TEST_TYPE:-all}"

# Print configuration
print_status "HRIS Load Testing Configuration:"
echo "  Base URL: $BASE_URL"
echo "  Environment: $ENVIRONMENT"
echo "  Debug Mode: $DEBUG"
echo "  Output Directory: $OUTPUT_DIR"
echo "  Test Type: $TEST_TYPE"
echo ""

# Pre-flight checks
print_status "Running pre-flight checks..."
check_k6
check_hris

# Test configuration mapping
declare -A TESTS=(
    ["basic"]="Basic Load Test|tests/basic-load-test.js"
    ["rate-limit"]="Rate Limiting Test|tests/rate-limiting-test.js"
    ["stress"]="Stress Test|tests/stress-test.js"
    ["soak"]="Soak Test (Endurance)|tests/soak-test.js"
    ["spike"]="Spike Test|tests/spike-test.js"
)

# Run tests based on selection
if [ "$TEST_TYPE" = "all" ]; then
    print_status "Running all test scenarios..."

    total_tests=0
    passed_tests=0

    for test_key in "${!TESTS[@]}"; do
        ((total_tests++))

        IFS='|' read -r test_name test_file <<< "${TESTS[$test_key]}"

        if run_test "$test_key" "$test_file"; then
            ((passed_tests++))
        fi

        # Add delay between tests
        if [ "$test_key" != "soak" ]; then
            print_status "Waiting 10 seconds before next test..."
            sleep 10
        fi
    done

    # Summary
    echo ""
    print_status "Test Execution Summary:"
    echo "  Total Tests: $total_tests"
    echo "  Passed: $passed_tests"
    echo "  Failed: $((total_tests - passed_tests))"

    if [ $passed_tests -eq $total_tests ]; then
        print_success "All tests completed successfully!"
    else
        print_warning "Some tests failed. Check the logs in $OUTPUT_DIR"
    fi

else
    # Run specific test
    if [[ -n "${TESTS[$TEST_TYPE]}" ]]; then
        IFS='|' read -r test_name test_file <<< "${TESTS[$TEST_TYPE]}"
        run_test "$TEST_TYPE" "$test_file"
    else
        print_error "Unknown test type: $TEST_TYPE"
        echo ""
        echo "Available test types: basic, rate-limit, stress, soak, spike, all"
        exit 1
    fi
fi

# Show results location
echo ""
print_status "Test results saved to: $OUTPUT_DIR"
echo "Files generated:"
ls -la "$OUTPUT_DIR" | tail -n +2 | awk '{print "  " $9}'

echo ""
print_success "Load testing completed! 🎉"