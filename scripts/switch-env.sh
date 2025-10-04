#!/bin/bash

# ===================================================================
# HRIS Environment Switching Script
# ===================================================================
# This script helps switch between different environments
# Usage: ./switch-env.sh [dev|staging|prod]
#
# SECURITY: This script handles sensitive environment files with care
# ===================================================================

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_usage() {
    echo -e "${BLUE}HRIS Environment Switcher${NC}"
    echo "Usage: $0 [environment]"
    echo ""
    echo "Available environments:"
    echo "  dev     - Development environment (local)"
    echo "  staging - Staging environment (pre-production)"
    echo "  prod    - Production environment"
    echo ""
    echo "Examples:"
    echo "  $0 dev"
    echo "  $0 staging"
    echo "  $0 prod"
}

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

# Check if environment parameter is provided
if [ $# -eq 0 ]; then
    print_usage
    exit 1
fi

ENVIRONMENT=$1
case $ENVIRONMENT in
    dev|development)
        ENV_FILE=".env"
        ENV_NAME="Development"
        ;;
    staging)
        ENV_FILE=".env.staging"
        ENV_NAME="Staging"
        ;;
    prod|production)
        ENV_FILE=".env.production"
        ENV_NAME="Production"
        ;;
    *)
        print_error "Invalid environment: $ENVIRONMENT"
        print_usage
        exit 1
        ;;
esac

print_status "Switching to $ENV_NAME environment..."

# Check if .env template exists for the target environment
TEMPLATE_FILE=".env.${ENVIRONMENT}.template"
if [ ! -f "$TEMPLATE_FILE" ]; then
    print_error "Template file not found: $TEMPLATE_FILE"
    print_error "Please create the template file first."
    exit 1
fi

# Backup existing .env if it exists
if [ -f ".env" ]; then
    BACKUP_FILE=".env.backup.$(date +%Y%m%d_%H%M%S)"
    print_status "Backing up current .env to $BACKUP_FILE"
    cp .env "$BACKUP_FILE"
fi

# Copy template to .env
print_status "Creating .env from $TEMPLATE_FILE"
cp "$TEMPLATE_FILE" ".env"

# Load environment variables
print_status "Loading environment variables from .env"
set -a
source .env
set +a

# Validate critical environment variables
MISSING_VARS=()

if [ -z "$DB_HOST" ]; then MISSING_VARS+=("DB_HOST"); fi
if [ -z "$DB_NAME" ]; then MISSING_VARS+=("DB_NAME"); fi
if [ -z "$JWT_SECRET" ]; then MISSING_VARS+=("JWT_SECRET"); fi
if [ -z "$SERVER_PORT" ]; then MISSING_VARS+=("SERVER_PORT"); fi

# For production and staging, validate more strictly
if [ "$ENVIRONMENT" = "prod" ] || [ "$ENVIRONMENT" = "staging" ]; then
    if [ -z "$DB_PASSWORD" ] || [[ "$DB_PASSWORD" == *"CHANGE_ME"* ]]; then
        MISSING_VARS+=("DB_PASSWORD (needs to be set for $ENV_NAME)")
    fi
    if [[ "$JWT_SECRET" == *"CHANGE_ME"* ]]; then
        MISSING_VARS+=("JWT_SECRET (needs to be set for $ENV_NAME)")
    fi
    if [ -z "$MAIL_PASSWORD" ] || [[ "$MAIL_PASSWORD" == *"CHANGE_ME"* ]]; then
        MISSING_VARS+=("MAIL_PASSWORD (needs to be set for $ENV_NAME)")
    fi
fi

if [ ${#MISSING_VARS[@]} -gt 0 ]; then
    print_error "Missing or incomplete environment variables:"
    for var in "${MISSING_VARS[@]}"; do
        echo "  - $var"
    done
    print_error "Please update .env file with the missing values."
    exit 1
fi

print_success "Environment switched to $ENV_NAME successfully!"

# Show current configuration summary
echo ""
echo -e "${BLUE}Current Configuration:${NC}"
echo "  Environment: $ENV_NAME"
echo "  Database: $DB_HOST:$DB_PORT/$DB_NAME"
echo "  Server: http://localhost:$SERVER_PORT"
echo "  Profile: $SPRING_PROFILES_ACTIVE"

if [ -n "$FRONTEND_URL" ]; then
    echo "  Frontend: $FRONTEND_URL"
fi

# Provide next steps
echo ""
echo -e "${BLUE}Next Steps:${NC}"
echo "1. Review the .env file to ensure all values are correct:"
echo "   nano .env"
echo ""
echo "2. Start the application:"
echo "   mvn spring-boot:run -Dspring-boot.run.profiles=$SPRING_PROFILES_ACTIVE"
echo ""

# Special warnings for production
if [ "$ENVIRONMENT" = "prod" ]; then
    print_warning "🚨 PRODUCTION ENVIRONMENT WARNINGS:"
    echo "   - Double-check all configuration values"
    echo "   - Ensure database backups are configured"
    echo "   - Verify SSL certificates are properly set up"
    echo "   - Monitor logs for any issues"
    echo "   - Test all functionality before going live"
    echo ""
    echo "   ⚠️  SECURITY REMINDERS:"
    echo "   - Never commit actual .env.production to git"
    "   - Rotate secrets regularly"
    echo "   - Use strong, unique passwords"
    echo "   - Enable monitoring and alerting"
elif [ "$ENVIRONMENT" = "staging" ]; then
    print_warning "⚠️  STAGING ENVIRONMENT REMINDERS:"
    echo "   - Test all production-like scenarios"
    echo "   - Verify email templates and integrations"
    echo "   - Load test if needed"
    echo "   - Validate all features work correctly"
fi

echo "=================================================================="
print_success "Environment switch completed successfully!"