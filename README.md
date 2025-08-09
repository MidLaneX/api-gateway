# API Gateway - Environment Configuration Setup

## Security & Configuration

This API Gateway has been configured to use environment variables for all sensitive configuration to prevent security exploits and credential exposure.

## Quick Setup

1. **Copy the environment template:**
   ```bash
   cp .env.template .env
   ```

2. **Update the `.env` file with your actual values:**
   - Change `JWT_SECRET` to a strong, randomly generated secret (minimum 256 bits)
   - Update service URLs to match your deployment environment
   - Adjust other configuration as needed

3. **NEVER commit the `.env` file to version control!**
   The `.gitignore` file has been configured to prevent this.

## Environment Variables

### Required Variables
- `JWT_SECRET`: Secret key for JWT signing/verification (CRITICAL - must be secure)
- `JWT_EXPIRATION`: JWT token expiration time in milliseconds

### Service URLs
- `AUTH_SERVICE_URL`: URL of the authentication service
- `USER_SERVICE_URL`: URL of the user service
- `PROJECT_SERVICE_URL`: URL of the project service
- `TASK_SERVICE_URL`: URL of the task service
- `NOTIFICATION_SERVICE_URL`: URL of the notification service
- `HEALTH_SERVICE_URL`: URL for health check endpoints

### Optional Configuration
- `SERVER_PORT`: Port for the API Gateway (default: 8080)
- `CONFIG_SERVER_URL`: Spring Cloud Config Server URL
- `CORS_ALLOWED_ORIGINS`: Allowed CORS origins
- `LOG_LEVEL_*`: Logging levels for different components

## Security Features

### JWT Authentication
- Auth endpoints (`/api/auth/**`) are forwarded directly to the auth service without verification
- All other protected endpoints require valid JWT tokens
- Tokens are validated using the configured JWT secret

### CORS Configuration
- Configurable CORS settings via environment variables
- Default settings allow all origins (configure appropriately for production)

### Environment Variable Loading
- Automatic loading of `.env` file on startup
- System environment variables take precedence over `.env` file
- Fallback to default values if neither is provided

## Production Deployment

### JWT Secret Generation
Generate a secure JWT secret using:
```bash
# Option 1: Using OpenSSL
openssl rand -base64 64

# Option 2: Using Java
java -cp ".:target/classes" -c "
import java.security.SecureRandom;
import java.util.Base64;
SecureRandom random = new SecureRandom();
byte[] secret = new byte[64];
random.nextBytes(secret);
System.out.println(Base64.getEncoder().encodeToString(secret));
"
```

### Environment-Specific Configuration
- Create separate `.env` files for different environments
- Use container orchestration secrets management in production
- Consider using Spring Cloud Config for centralized configuration

### Security Checklist
- [ ] JWT secret is randomly generated and at least 256 bits
- [ ] `.env` file is not committed to version control
- [ ] CORS origins are properly configured for production
- [ ] Service URLs use appropriate protocols (HTTPS in production)
- [ ] Logging levels are appropriate for the environment

## Troubleshooting

### Common Issues
1. **JWT validation fails**: Check that JWT_SECRET matches between auth service and gateway
2. **Service not reachable**: Verify service URLs in environment configuration
3. **CORS errors**: Check CORS_ALLOWED_ORIGINS configuration

### Logs
The application logs JWT validation attempts and gateway routing for debugging.
