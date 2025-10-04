# Environment Security Setup Guide

This guide explains how to securely configure different environments (development, staging, production) for the HRIS application without exposing sensitive data in version control.

## 🔐 Security Overview

### What's Secured
- Database credentials
- JWT secret keys
- Email API keys
- Redis passwords
- SSL certificates
- Environment-specific configurations

### What's in Git
- Non-sensitive default configurations
- Configuration templates
- Docker compose files
- Documentation

## 📁 File Structure

```
hris/
├── .gitignore                              # Secures sensitive files
├── .env.template                          # Environment variables template
├── .env                                    # Your local environment (NOT in git)
├── docker-compose.yml                     # Base docker configuration
├── src/main/resources/
│   ├── application.properties              # Base Spring configuration
│   ├── application-dev.properties.template # Development template
│   ├── application-prod.properties.template # Production template
│   ├── application-dev.properties          # Your dev config (NOT in git)
│   └── application-prod.properties         # Your prod config (NOT in git)
├── scripts/
│   └── setup-dev.sh                       # Development setup script
└── docs/
    └── ENVIRONMENT_SETUP.md               # This documentation
```

## 🚀 Quick Start - Development

### 1. Automatic Setup
```bash
# Run the setup script
./scripts/setup-dev.sh
```

### 2. Manual Setup

#### Step 1: Create Environment Files
```bash
# Copy templates
cp .env.template .env
cp src/main/resources/application-dev.properties.template src/main/resources/application-dev.properties
```

#### Step 2: Update Environment Variables
Edit `.env` file:
```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=hris
DB_USERNAME=hris_user
DB_PASSWORD=your_secure_db_password

JWT_SECRET=your_very_long_and_secure_jwt_secret_key_at_least_256_bits
MAIL_PASSWORD=your_sendgrid_api_key
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8081
FRONTEND_URL=http://localhost:3000
```

#### Step 3: Update Application Properties
Edit `src/main/resources/application-dev.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/hris
spring.datasource.username=hris_user
spring.datasource.password=your_secure_db_password

# JWT Configuration
jwt.secret=your_very_long_and_secure_jwt_secret_key_at_least_256_bits

# Mail Configuration
spring.mail.password=your_sendgrid_api_key
```

#### Step 4: Start Services
```bash
# Start database and Redis
docker-compose up -d

# Run application
mvn spring-boot:run
```

## 🏭 Production Setup

### 1. Create Production Configuration
```bash
# Copy production template
cp src/main/resources/application-prod.properties.template src/main/resources/application-prod.properties
```

### 2. Configure Production Secrets
Update `application-prod.properties` with production values:

#### Database Security
```properties
spring.datasource.url=jdbc:postgresql://your-production-host:5432/hris_prod
spring.datasource.username=production_db_user
spring.datasource.password=VERY_STRONG_PRODUCTION_PASSWORD
```

#### JWT Security
```properties
jwt.secret=CHANGE_THIS_TO_VERY_STRONG_PRODUCTION_SECRET_AT_LEAST_256_BITS_LONG_RANDOM_STRING_FOR_JWT_SIGNATURE
```

#### SSL Configuration
```properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=CHANGE_THIS_KEYSTORE_PASSWORD
server.ssl.key-store-type=PKCS12
```

### 3. Environment Variables for Production
Set environment variables in your production environment:
```bash
export DB_HOST=your-production-host
export DB_PASSWORD=your_production_db_password
export JWT_SECRET=your_production_jwt_secret
export MAIL_PASSWORD=your_production_sendgrid_key
export SPRING_PROFILES_ACTIVE=prod
```

### 4. Run with Production Profile
```bash
java -jar -Dspring.profiles.active=prod hris.jar
```

## 🐳 Docker Deployment

### Development Docker
```bash
# Use docker-compose with environment file
docker-compose -f docker-compose.yml --env-file .env up
```

### Production Docker
```bash
# Use production override (create docker-compose.prod.yml)
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

## 🔒 Security Best Practices

### 1. Password Security
- Use strong, unique passwords for each environment
- Minimum 12 characters with mixed case, numbers, and symbols
- Rotate passwords regularly

### 2. JWT Secret Security
- Use at least 256-bit random strings
- Different secrets for dev, staging, and production
- Never use default or example secrets in production

### 3. Database Security
- Different databases for each environment
- Least privilege database users
- Enable SSL for production database connections

### 4. Email Security
- Use different API keys for each environment
- Rotate API keys regularly
- Monitor email usage

### 5. Environment Isolation
- Never share configuration between environments
- Use different subdomains/domains for each environment
- Implement proper firewalls and network segmentation

## 🔧 Configuration Management

### Environment Variables Priority
1. Command line arguments: `-Dproperty=value`
2. Java system properties: `System.getProperty()`
3. OS environment variables
4. Application properties files
5. Default properties

### Spring Profiles
- `dev` - Development environment with debug logging
- `prod` - Production environment with security optimizations
- Custom profiles can be created as needed

### Loading Configuration
```java
// Access environment variables
@Value("${DB_PASSWORD}")
private String dbPassword;

// Use in configuration classes
@Configuration
public class AppConfig {
    @Bean
    public DataSource dataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password) {
        return DataSourceBuilder.create()
            .url(url)
            .username(username)
            .password(password)
            .build();
    }
}
```

## 📋 Environment Setup Checklist

### Development Setup
- [ ] Copy `.env.template` to `.env` and update values
- [ ] Copy `application-dev.properties.template` and update
- [ ] Generate strong JWT secret
- [ ] Configure local database
- [ ] Test email configuration
- [ ] Verify Redis connection
- [ ] Run application with dev profile

### Production Setup
- [ ] Copy `application-prod.properties.template` and update
- [ ] Configure production database with SSL
- [ ] Generate production JWT secret
- [ ] Configure production email API
- [ ] Set up SSL certificates
- [ ] Configure monitoring and logging
- [ ] Test with production profile
- [ ] Set up backup procedures
- [ ] Configure security headers

### Security Review
- [ ] No sensitive data in git
- [ ] All secrets are environment-specific
- [ ] Strong passwords for all services
- [ ] SSL/TLS enabled
- [ ] Security headers configured
- [ ] Rate limiting enabled
- [ ] Monitoring configured
- [ ] Backup procedures documented

## 🚨 Common Mistakes to Avoid

### ❌ Don't:
- Commit actual secrets to git
- Use the same secrets across environments
- Use default/example passwords in production
- Store secrets in code
- Log sensitive information
- Ignore SSL certificate warnings

### ✅ Do:
- Use environment-specific configuration files
- Generate strong, random secrets
- Use environment variables for deployment
- Implement proper access controls
- Regular security audits
- Keep dependencies updated

## 📞 Support

For environment setup issues:
1. Check the logs for specific error messages
2. Verify all required files exist and are properly configured
3. Ensure all services (database, Redis) are running
4. Validate network connectivity
5. Review this documentation for common solutions

Remember: Security is a process, not a one-time setup. Regular reviews and updates are essential!