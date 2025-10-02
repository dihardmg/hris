#!/bin/bash

# Redis Rate Limiting Test Script
# Tests the Redis-based rate limiting functionality

BASE_URL="http://localhost:8081"
TEST_EMAIL="redis-test@example.com"
TEST_PASSWORD="wrongpassword"

echo "=== Redis Rate Limiting Test for Login Endpoint ==="
echo "Testing POST /api/auth/login with Redis-based rate limiting"
echo "Configuration: 5 attempts per 5 minutes"
echo ""

# Function to attempt login and show Redis state
attempt_login_with_redis() {
    local attempt_num=$1
    echo "Attempt #$attempt_num:"

    # Make login attempt
    response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\"}")

    http_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)

    echo "  HTTP Status: $http_code"
    echo "  Response: $response_body"

    # Show Redis state (if redis-cli is available)
    if command -v redis-cli &> /dev/null; then
        echo "  Redis State:"
        redis_key="rate_limit:$TEST_EMAIL"
        redis_data=$(redis-cli -n 1 get "$redis_key" 2>/dev/null)
        if [ "$redis_data" != "(nil)" ]; then
            echo "    Key: $redis_key"
            echo "    Data: $redis_data"
        else
            echo "    No rate limit data found"
        fi
    fi
    echo ""
}

echo "=== Test 1: Making 5 failed login attempts ==="
for i in {1..5}; do
    attempt_login_with_redis $i
    sleep 1
done

echo "=== Test 2: Attempting 6th login (should be rate limited) ==="
attempt_login_with_redis 6

echo "=== Test 3: Waiting 10 seconds and trying again (still blocked) ==="
sleep 10
attempt_login_with_redis 7

echo "=== Test 4: Test successful login (if user exists) ==="
echo "To test successful login reset, create a user with:"
echo "  Email: $TEST_EMAIL"
echo "  Password: correctpassword"
echo "Then run: curl -X POST \"$BASE_URL/api/auth/login\" -H \"Content-Type: application/json\" -d '{\"email\":\"$TEST_EMAIL\",\"password\":\"correctpassword\"}'"

echo ""
echo "=== Redis Rate Limiting Benefits ==="
echo "✅ Distributed rate limiting across multiple application instances"
echo "✅ Persistent storage survives application restarts"
echo "✅ Atomic operations prevent race conditions"
echo "✅ Automatic expiration with Redis TTL"
echo "✅ Easy monitoring and debugging of rate limit state"

echo ""
echo "=== Expected Behavior ==="
echo "- Attempts 1-5: HTTP 401 (Unauthorized) for wrong password"
echo "- Attempt 6+: HTTP 429 (Too Many Requests) due to rate limiting"
echo "- After successful login: Rate limit counter resets in Redis"
echo "- After 5 minutes: Rate limit automatically expires via Redis TTL"