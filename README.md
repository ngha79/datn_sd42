# Spring Boot Base Project

Base project với JWT Authentication, Refresh Token và Global Exception Handling.

## Cấu trúc project

```
src/main/java/com/base/
├── Application.java
├── config/
│   ├── SecurityConfig.java        # Spring Security + filter chain
│   └── DataInitializer.java       # Seed data khi khởi động
├── controller/
│   ├── AuthController.java        # /api/v1/auth/**
│   └── UserController.java        # /api/v1/me, /api/v1/admin/**
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   └── RefreshTokenRequest.java
│   └── response/
│       ├── ApiResponse.java       # Generic wrapper
│       └── AuthResponse.java
├── entity/
│   ├── User.java                  # implements UserDetails
│   └── RefreshToken.java
├── exception/
│   ├── BadRequestException.java
│   ├── ForbiddenException.java
│   ├── ResourceAlreadyExistsException.java
│   ├── ResourceNotFoundException.java
│   ├── TokenRefreshException.java
│   ├── UnauthorizedException.java
│   └── handler/
│       ├── ErrorResponse.java
│       └── GlobalExceptionHandler.java
├── repository/
│   ├── UserRepository.java
│   └── RefreshTokenRepository.java
├── security/jwt/
│   ├── JwtService.java            # Tạo và validate JWT
│   └── JwtAuthenticationFilter.java
└── service/
    ├── AuthService.java           # Interface
    ├── RefreshTokenService.java
    └── impl/
        └── AuthServiceImpl.java
```

## Chạy project

```bash
mvn spring-boot:run
```

Project dùng H2 in-memory database. Truy cập H2 console tại: http://localhost:8080/h2-console

## Default users (tự động seed)

| Username | Password | Role  |
|----------|----------|-------|
| admin    | admin123 | ADMIN |
| user     | user123  | USER  |

## API Endpoints

### Auth
| Method | URL                          | Auth | Mô tả               |
|--------|------------------------------|------|---------------------|
| POST   | /api/v1/auth/register        | ❌   | Đăng ký             |
| POST   | /api/v1/auth/login           | ❌   | Đăng nhập           |
| POST   | /api/v1/auth/refresh-token   | ❌   | Làm mới access token|
| POST   | /api/v1/auth/logout          | ✅   | Đăng xuất           |

### User
| Method | URL                    | Auth  | Mô tả              |
|--------|------------------------|-------|--------------------|
| GET    | /api/v1/me             | ✅ USER | Profile cá nhân  |
| GET    | /api/v1/admin/users    | ✅ ADMIN | Danh sách users |
| GET    | /api/v1/admin/users/{id} | ✅ ADMIN | User theo ID  |

## Ví dụ sử dụng

### Đăng nhập
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### Gọi API có auth
```bash
curl http://localhost:8080/api/v1/me \
  -H "Authorization: Bearer <access_token>"
```

## Error Response Format

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/auth/register",
  "timestamp": "2024-01-01T12:00:00",
  "validationErrors": {
    "email": "Email must be valid",
    "password": "Password must be at least 6 characters"
  }
}
```

## Thêm database thật (PostgreSQL)

1. Bỏ comment dependency PostgreSQL trong `pom.xml`
2. Trong `application.yml`, comment H2 config và bỏ comment PostgreSQL config
3. Cập nhật `username`, `password`, `url` theo database của bạn

## Mở rộng

- **Thêm entity mới**: tạo Entity → Repository → Service (interface + impl) → Controller
- **Thêm role mới**: thêm vào `User.Role` enum và cập nhật `SecurityConfig`
- **Email verification**: thêm Spring Mail dependency và field `verified` vào User
