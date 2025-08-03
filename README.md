# QuantCrux - Quantitative Finance Platform

A professional-grade full-stack quantitative finance platform built with React, Spring Boot, and PostgreSQL.

## Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- PostgreSQL 12+
- Maven 3.6+

### Database Setup
1. Create PostgreSQL database:
```bash
createdb quantcrux
```

2. Run the database schemas:
```bash
psql -d quantcrux -f supabase/migrations/create_user_management_fixed.sql
psql -d quantcrux -f supabase/migrations/create_strategy_schema_simple.sql
```

### Backend Setup
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend will run on: http://localhost:8080

### Frontend Setup
```bash
npm install
npm run dev
```

Frontend will run on: http://localhost:5173

## Demo Credentials

- **Admin**: admin@quantcrux.com / password
- **Portfolio Manager**: john.pm@quantcrux.com / password  
- **Researcher**: alice.research@quantcrux.com / password
- **Client**: bob.client@quantcrux.com / password

## Module Status

✅ **Authentication & User Management** - Complete
- JWT authentication with refresh tokens
- Role-based access control (4 roles)
- Secure password hashing
- Session management
- Professional dark-themed UI

🔄 **Coming Next**:
- Strategy Builder
- Backtesting Engine
- Product Builder
- Trade Desk
- Portfolio Management
- Risk Analytics

## Tech Stack

**Frontend**: React 18, TypeScript, Tailwind CSS, Axios
**Backend**: Spring Boot 3.2, Spring Security, JPA/Hibernate
**Database**: PostgreSQL with proper schemas and RLS
**Authentication**: JWT with refresh tokens, BCrypt password hashing

## Architecture

This is a monolithic application designed for local development and deployment. All modules are integrated within a single codebase for simplicity and performance.

## API Documentation

### Auth Endpoints
- `POST /api/auth/login` - User authentication
- `POST /api/auth/register` - User registration  
- `POST /api/auth/refresh` - Token refresh
- `GET /api/auth/profile` - Get user profile
- `POST /api/auth/logout` - User logout

### Security
- Role-based access control at API and UI level
- JWT tokens with secure secret rotation
- PostgreSQL Row Level Security (RLS)
- CORS configuration for local development