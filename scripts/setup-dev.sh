#!/bin/bash

# ===================================================================
# HRIS Development Environment Setup Script
# ===================================================================
# This script securely sets up the development environment using
# environment variables. All sensitive data is stored in .env file.
#
# Security Features:
# - Environment variables for all secrets
# - Secure default values
# - Validation of required configurations
# - Automatic secret generation (optional)
# ===================================================================

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# Function to generate secure random string
generate_secret() {
    openssl rand -base64 64 | tr -d '\n'
}

echo "🚀 Setting up HRIS Development Environment..."
echo "=================================================================="

# Check if .env file exists
if [ ! -f .env ]; then
    print_status "Creating .env file from template..."
    cp .env.template .env

    # Generate secure JWT secret
    JWT_SECRET=$(generate_secret)
    sed -i.bak "s|your_very_long_and_secure_jwt_secret_key_at_least_256_bits_long_random_string|$JWT_SECRET|g" .env

    # Generate secure database password
    DB_PASSWORD=$(openssl rand -base64 32 | tr -d '\n' | tr -d '/')
    sed -i.bak "s|your_secure_db_password_here|$DB_PASSWORD|g" .env

    # Generate secure DevTools secret
    DEVTOOLS_SECRET=$(openssl rand -base64 32 | tr -d '\n' | tr -d '/')
    sed -i.bak "s|dev_secret_change_me|$DEVTOOLS_SECRET|g" .env

    # Remove backup files
    rm -f .env.bak

    print_success ".env file created with secure random secrets!"
    echo ""
    print_warning "⚠️  SECURITY NOTICES:"
    echo "   1. A secure JWT secret has been generated automatically"
    echo "   2. A secure database password has been generated"
    echo "   3. Update your SendGrid API key in .env file: MAIL_PASSWORD"
    echo "   4. Update FRONTEND_URL if your frontend runs on different port"
    echo ""
    read -p "Press Enter after reviewing .env file, or type 'exit' to quit: " response
    if [[ "$response" == "exit" ]]; then
        exit 0
    fi
else
    print_status ".env file already exists"
fi

# Check if application-dev.properties exists
if [ ! -f src/main/resources/application-dev.properties ]; then
    print_status "Creating application-dev.properties from template..."
    cp src/main/resources/application-dev.properties.template src/main/resources/application-dev.properties
    print_success "application-dev.properties created!"
fi

# Source the .env file to load environment variables
if [ -f .env ]; then
    print_status "Loading environment variables from .env file..."
    set -a  # Automatically export all variables
    source .env
    set +a  # Stop automatic export
fi

# Validate required environment variables
print_status "Validating required environment variables..."

missing_vars=()

if [ -z "$DB_HOST" ]; then missing_vars+=("DB_HOST"); fi
if [ -z "$DB_PORT" ]; then missing_vars+=("DB_PORT"); fi
if [ -z "$DB_NAME" ]; then missing_vars+=("DB_NAME"); fi
if [ -z "$DB_USERNAME" ]; then missing_vars+=("DB_USERNAME"); fi
if [ -z "$DB_PASSWORD" ]; then missing_vars+=("DB_PASSWORD"); fi
if [ -z "$JWT_SECRET" ]; then missing_vars+=("JWT_SECRET"); fi
if [ -z "$SERVER_PORT" ]; then missing_vars+=("SERVER_PORT"); fi

if [ ${#missing_vars[@]} -gt 0 ]; then
    print_error "Missing required environment variables: ${missing_vars[*]}"
    print_error "Please update your .env file with the missing values."
    exit 1
fi

print_success "All required environment variables are set!"

# Stop existing containers if any
print_status "Checking for existing containers..."
if docker-compose ps | grep -q "Up"; then
    print_status "Stopping existing containers..."
    docker-compose down
fi

# Start Docker services
print_status "Starting PostgreSQL and Redis containers..."
docker-compose up -d

# Wait for services to be ready
print_status "Waiting for services to be ready..."
echo "⏳ This may take up to 30 seconds..."

# Wait for database to be ready
for i in {1..30}; do
    if docker-compose exec postgres pg_isready -U "$DB_USERNAME" -d "$DB_NAME" >/dev/null 2>&1; then
        print_success "Database is ready!"
        break
    fi
    if [ $i -eq 30 ]; then
        print_error "Database connection failed. Please check your configuration."
        print_error "Container logs:"
        docker-compose logs postgres
        exit 1
    fi
    sleep 1
done

# Wait for Redis to be ready
for i in {1..15}; do
    if docker-compose exec redis redis-cli ping >/dev/null 2>&1; then
        print_success "Redis is ready!"
        break
    fi
    if [ $i -eq 15 ]; then
        print_error "Redis connection failed. Please check your configuration."
        print_error "Container logs:"
        docker-compose logs redis
        exit 1
    fi
    sleep 1
done

# Test Docker Compose configuration
print_status "Testing Docker Compose configuration..."
if docker-compose config >/dev/null 2>&1; then
    print_success "Docker Compose configuration is valid!"
else
    print_warning "Docker Compose configuration has issues (non-critical)"
fi

# Create necessary directories
print_status "Creating necessary directories..."
mkdir -p logs
mkdir -p docker/postgres/init
mkdir -p docker/redis

# Create basic Redis configuration if not exists
if [ ! -f docker/redis/redis.conf ]; then
    print_status "Creating Redis configuration..."
    cat > docker/redis/redis.conf << EOF
# Redis configuration for HRIS Development
appendonly yes
maxmemory 256mb
maxmemory-policy allkeys-lru
save 900 1
save 300 10
save 60 10000
EOF
fi

echo ""
print_success "🎉 Development environment setup complete!"
echo "=================================================================="
echo ""
echo "📋 Environment Summary:"
echo "   🗄️  Database:     PostgreSQL on ${DB_HOST}:${DB_PORT}"
echo "   📊  Database Name: $DB_NAME"
echo "   🐳  Redis:        Redis on ${REDIS_HOST}:${REDIS_PORT:-6379}"
echo "   🌐  Application:  http://localhost:${SERVER_PORT}"
echo "   🎯  Frontend:     $FRONTEND_URL"
echo ""
echo "🔑 Security Configuration:"
echo "   ✅ JWT secret is configured (secure random generated)"
echo "   ✅ Database password is set"
echo "   ✅ All secrets are in .env file"
echo "   ✅ Environment variables are loaded"
echo ""
echo "🚀 Next Steps:"
echo "   1. Review and update .env file if needed:"
echo "      nano .env"
echo ""
echo "   2. Start the application:"
echo "      mvn spring-boot:run"
echo ""
echo "   3. Or run with Docker (optional):"
echo "      docker-compose --profile application up -d"
echo ""
echo "   4. Initialize default data:"
echo "      curl -X POST http://localhost:${SERVER_PORT}/api/migration/initialize \\"
echo "        -H \"Authorization: Bearer <admin-token>\" \\"
echo "        -H \"Content-Type: application/json\""
echo ""
echo "   5. Access the application at: http://localhost:${SERVER_PORT}"
echo ""
echo "🛠️  Useful Commands:"
echo "   View logs:           docker-compose logs -f"
echo "   Stop services:       docker-compose down"
echo "   Restart services:    docker-compose restart"
echo "   Check status:        docker-compose ps"
echo ""
echo "📚 Documentation:"
echo "   Environment Setup:  docs/ENVIRONMENT_SETUP.md"
echo "   API Documentation:   README.md"
echo ""
echo "⚠️  Important Security Reminders:"
echo "   - Never commit .env with actual secrets to git"
echo "   - Use different secrets for production"
echo "   - Regularly rotate your secrets"
echo "   - Keep your .env file secure and backed up"
echo ""
echo "=================================================================="
print_success "Setup completed successfully! Your HRIS development environment is ready!"