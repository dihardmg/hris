#!/bin/bash

# Rate Limiting Test Script
# Tests the login rate limiting functionality (5 attempts per 5 minutes)

BASE_URL="http://localhost:8080"
TEST_EMAIL="ratelimit@test.com"
TEST_PASSWORD="wrongpassword"

echo "=== Rate Limiting Test for Login Endpoint ==="
echo "Testing POST /api/auth/login with rate limiting (5 attempts per 5 minutes)"
echo ""

# Function to attempt login
attempt_login() {
    local attempt_num=$1
    echo "Attempt #$attempt_num:"

    response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\"}")

    http_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)

    echo "  HTTP Status: $http_code"
    echo "  Response: $response_body"
    echo ""

    return $http_code
}

# Test 1: Make 5 failed login attempts
echo "=== Test 1: Making 5 failed login attempts ==="
for i in {1..5}; do
    attempt_login $i
    sleep 1  # Small delay between attempts
done

echo "=== Test 2: Attempting 6th login (should be rate limited) ==="
attempt_login 6

echo "=== Test 3: Waiting 10 seconds and trying again (still blocked) ==="
sleep 10
attempt_login 7

echo "=== Test 4: Testing with correct credentials (if user exists) ==="
# This would only work if the test user actually exists
# For now, we'll just show the structure
echo "To test successful login reset, create a user with:"
echo "  Email: $TEST_EMAIL"
echo "  Password: correctpassword"
echo "Then run: curl -X POST \"$BASE_URL/api/auth/login\" -H \"Content-Type: application/json\" -d '{\"email\":\"$TEST_EMAIL\",\"password\":\"correctpassword\"}'"

echo ""
echo "=== Test Summary ==="
echo "Expected behavior:"
echo "- Attempts 1-5: HTTP 401 (Unauthorized) for wrong password"
echo "- Attempt 6+: HTTP 429 (Too Many Requests) due to rate limiting"
echo "- After successful login: Rate limit counter resets"
echo "- After 5 minutes: Rate limit automatically expires"