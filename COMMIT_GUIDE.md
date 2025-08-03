# QuantCrux - Git Commit Guide

This document lists all files that need to be committed to complete the QuantCrux implementation.

## Files Created/Modified During Development

### Database Migrations
```bash
# New migration files (need to be committed)
supabase/migrations/20250803165800_fix_jsonb_columns.sql
supabase/migrations/20250803171200_fix_enum_columns.sql
supabase/migrations/20250803180000_create_backtesting_schema.sql
supabase/migrations/20250803190000_create_product_builder_schema.sql
```

### Backend Java Files

#### Models
```bash
backend/src/main/java/com/quantcrux/model/User.java
backend/src/main/java/com/quantcrux/model/UserRole.java
backend/src/main/java/com/quantcrux/model/AccountStatus.java
backend/src/main/java/com/quantcrux/model/UserSession.java
backend/src/main/java/com/quantcrux/model/Strategy.java
backend/src/main/java/com/quantcrux/model/StrategyStatus.java
backend/src/main/java/com/quantcrux/model/StrategyVersion.java
backend/src/main/java/com/quantcrux/model/StrategySignal.java
backend/src/main/java/com/quantcrux/model/SignalType.java
backend/src/main/java/com/quantcrux/model/Backtest.java
backend/src/main/java/com/quantcrux/model/BacktestStatus.java
backend/src/main/java/com/quantcrux/model/BacktestTrade.java
backend/src/main/java/com/quantcrux/model/MarketData.java
backend/src/main/java/com/quantcrux/model/Product.java
backend/src/main/java/com/quantcrux/model/ProductType.java
backend/src/main/java/com/quantcrux/model/ProductStatus.java
backend/src/main/java/com/quantcrux/model/PricingModel.java
backend/src/main/java/com/quantcrux/model/ProductVersion.java
backend/src/main/java/com/quantcrux/model/ProductPricing.java
backend/src/main/java/com/quantcrux/model/ProductPayoff.java
```

#### DTOs
```bash
backend/src/main/java/com/quantcrux/dto/ApiResponse.java
backend/src/main/java/com/quantcrux/dto/LoginRequest.java
backend/src/main/java/com/quantcrux/dto/RegisterRequest.java
backend/src/main/java/com/quantcrux/dto/RefreshTokenRequest.java
backend/src/main/java/com/quantcrux/dto/JwtResponse.java
backend/src/main/java/com/quantcrux/dto/UserProfileResponse.java
backend/src/main/java/com/quantcrux/dto/StrategyRequest.java
backend/src/main/java/com/quantcrux/dto/StrategyResponse.java
backend/src/main/java/com/quantcrux/dto/SignalEvaluationRequest.java
backend/src/main/java/com/quantcrux/dto/SignalEvaluationResponse.java
backend/src/main/java/com/quantcrux/dto/BacktestRequest.java
backend/src/main/java/com/quantcrux/dto/BacktestResponse.java
backend/src/main/java/com/quantcrux/dto/ProductRequest.java
backend/src/main/java/com/quantcrux/dto/ProductResponse.java
```

#### Services
```bash
backend/src/main/java/com/quantcrux/service/UserService.java
backend/src/main/java/com/quantcrux/service/StrategyService.java
backend/src/main/java/com/quantcrux/service/BacktestService.java
backend/src/main/java/com/quantcrux/service/MarketDataService.java
backend/src/main/java/com/quantcrux/service/ProductService.java
```

#### Controllers
```bash
backend/src/main/java/com/quantcrux/controller/AuthController.java
backend/src/main/java/com/quantcrux/controller/StrategyController.java
backend/src/main/java/com/quantcrux/controller/BacktestController.java
backend/src/main/java/com/quantcrux/controller/ProductController.java
```

#### Repositories
```bash
backend/src/main/java/com/quantcrux/repository/UserRepository.java
backend/src/main/java/com/quantcrux/repository/UserSessionRepository.java
backend/src/main/java/com/quantcrux/repository/StrategyRepository.java
backend/src/main/java/com/quantcrux/repository/StrategyVersionRepository.java
backend/src/main/java/com/quantcrux/repository/StrategySignalRepository.java
backend/src/main/java/com/quantcrux/repository/BacktestRepository.java
backend/src/main/java/com/quantcrux/repository/BacktestTradeRepository.java
backend/src/main/java/com/quantcrux/repository/MarketDataRepository.java
backend/src/main/java/com/quantcrux/repository/ProductRepository.java
backend/src/main/java/com/quantcrux/repository/ProductVersionRepository.java
backend/src/main/java/com/quantcrux/repository/ProductPricingRepository.java
backend/src/main/java/com/quantcrux/repository/ProductPayoffRepository.java
```

#### Security & Config
```bash
backend/src/main/java/com/quantcrux/security/JwtProvider.java
backend/src/main/java/com/quantcrux/security/JwtAuthenticationFilter.java
backend/src/main/java/com/quantcrux/security/UserPrincipal.java
backend/src/main/java/com/quantcrux/config/SecurityConfig.java
backend/src/main/java/com/quantcrux/config/JpaConfig.java
backend/src/main/java/com/quantcrux/config/PostgreSQLEnumType.java
```

#### Main Application
```bash
backend/src/main/java/com/quantcrux/QuantcruxBackendApplication.java
```

### Frontend React/TypeScript Files

#### Core App Files
```bash
src/App.tsx
src/main.tsx
src/index.css
```

#### Context & Hooks
```bash
src/context/AuthContext.tsx
src/hooks/useAuth.ts
```

#### Services
```bash
src/services/authService.ts
src/services/strategyService.ts
src/services/backtestService.ts
src/services/productService.ts
```

#### Pages
```bash
src/pages/Dashboard.tsx
src/pages/LoginPage.tsx
src/pages/RegisterPage.tsx
src/pages/StrategiesPage.tsx
src/pages/StrategyBuilderPage.tsx
src/pages/BacktestsPage.tsx
src/pages/BacktestResultsPage.tsx
src/pages/ProductsPage.tsx
```

#### Components
```bash
src/components/LoadingSpinner.tsx
src/components/BacktestModal.tsx
```

### Configuration Files
```bash
package.json
tsconfig.json
tsconfig.app.json
tsconfig.node.json
vite.config.ts
tailwind.config.js
postcss.config.js
eslint.config.js
```

### Backend Configuration
```bash
backend/pom.xml
backend/src/main/resources/application.yml
```

### Documentation
```bash
README.md
COMMIT_GUIDE.md (this file)
```

## Git Commands to Commit All Changes

### 1. Check Status
```bash
git status
```

### 2. Add All New Files
```bash
# Add all new files
git add .

# Or add specific directories
git add src/
git add backend/src/
git add supabase/migrations/
```

### 3. Commit with Descriptive Messages

#### Option A: Single Large Commit
```bash
git commit -m "feat: Complete QuantCrux implementation with all modules

- ✅ Authentication & User Management (JWT, RBAC)
- ✅ Strategy Builder (JSON-based strategy configuration)
- ✅ Backtesting Engine (Historical simulation with metrics)
- ✅ Product Builder (Structured financial products)

Backend:
- Spring Boot 3.2 with PostgreSQL
- JWT authentication with refresh tokens
- Role-based access control (4 roles)
- Comprehensive REST APIs
- Advanced financial algorithms

Frontend:
- React 18 with TypeScript
- Professional dark theme UI
- Interactive charts with Recharts
- Real-time updates and notifications
- Responsive design

Database:
- PostgreSQL with proper schemas
- Row Level Security (RLS)
- Migration-based schema management
- Optimized indexes and constraints"
```

#### Option B: Separate Commits by Module
```bash
# Commit authentication module
git add backend/src/main/java/com/quantcrux/model/User* backend/src/main/java/com/quantcrux/controller/Auth* src/pages/Login* src/pages/Register* src/context/Auth* src/services/auth*
git commit -m "feat: Implement authentication & user management module"

# Commit strategy builder
git add backend/src/main/java/com/quantcrux/model/Strategy* backend/src/main/java/com/quantcrux/controller/Strategy* src/pages/Strateg* src/services/strategy*
git commit -m "feat: Implement strategy builder module with JSON configuration"

# Commit backtesting engine
git add backend/src/main/java/com/quantcrux/model/Backtest* backend/src/main/java/com/quantcrux/controller/Backtest* src/pages/Backtest* src/services/backtest* src/components/Backtest*
git commit -m "feat: Implement backtesting engine with historical simulation"

# Commit product builder
git add backend/src/main/java/com/quantcrux/model/Product* backend/src/main/java/com/quantcrux/controller/Product* src/pages/Product* src/services/product*
git commit -m "feat: Implement product builder for structured financial products"

# Commit database migrations
git add supabase/migrations/
git commit -m "feat: Add database migrations for all modules"

# Commit configuration and documentation
git add *.json *.js *.ts *.yml README.md
git commit -m "feat: Add configuration files and documentation"
```

### 4. Push to GitHub
```bash
git push origin main
# or
git push origin master
```

## Verification Commands

### Check what files are staged
```bash
git diff --cached --name-only
```

### Check what files are untracked
```bash
git ls-files --others --exclude-standard
```

### View commit history
```bash
git log --oneline -10
```

## Notes

1. **Migration Files**: Make sure to run the database migrations after pulling:
   ```bash
   psql -d quantcrux -f supabase/migrations/20250803165800_fix_jsonb_columns.sql
   psql -d quantcrux -f supabase/migrations/20250803171200_fix_enum_columns.sql
   psql -d quantcrux -f supabase/migrations/20250803180000_create_backtesting_schema.sql
   psql -d quantcrux -f supabase/migrations/20250803190000_create_product_builder_schema.sql
   ```

2. **Dependencies**: Run `npm install` after pulling to install new dependencies

3. **Backend**: Run `mvn clean install` in the backend directory

4. **Environment**: Ensure PostgreSQL is running and configured properly

## File Count Summary
- **Backend Java Files**: ~50+ files
- **Frontend React/TS Files**: ~15+ files  
- **Database Migrations**: 4 new files
- **Configuration Files**: ~10 files
- **Documentation**: 2 files

Total: **80+ files** need to be committed to complete the QuantCrux implementation.