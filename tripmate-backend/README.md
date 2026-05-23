# TripMate Backend

Spring Boot 3.x REST API for the TripMate group trip planner. See the [root README](../README.md) for full project context, tech stack, and deployment details.

## Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.0+

## Setup

### 1. Create MySQL database

```sql
CREATE DATABASE tripmate;
```

### 2. Generate a JWT secret (Base64-encoded 256-bit key)

```bash
openssl rand -base64 32
```

### 3. Set environment variables

**Linux / macOS (bash/zsh):**
```bash
export DB_URL=jdbc:mysql://localhost:3306/tripmate?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
export DB_USER=root
export DB_PASS=yourpassword
export JWT_SECRET=your-base64-encoded-secret-here
# Optional — defaults to http://localhost:5173
export FRONTEND_URL=http://localhost:5173
```

**Windows (PowerShell):**
```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/tripmate?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USER="root"
$env:DB_PASS="yourpassword"
$env:JWT_SECRET="your-base64-encoded-secret-here"
```

> **Note:** `allowPublicKeyRetrieval=true` is required when connecting to MySQL 8 without SSL (local dev). Remove it in production and use SSL instead.

### 4. Run

```bash
mvn spring-boot:run
```

The API starts at `http://localhost:8080`.

## API Reference

### Auth
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/register` | No | Register → JWT |
| POST | `/api/auth/login` | No | Login → JWT |
| GET | `/api/auth/me` | Yes | Current user |

### Trips
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/trips` | Yes | List my trips |
| POST | `/api/trips` | Yes | Create trip |
| GET | `/api/trips/{id}` | Yes | Trip detail |
| PUT | `/api/trips/{id}` | Yes | Update trip |
| DELETE | `/api/trips/{id}` | Yes | Delete trip (creator only) |
| GET | `/api/trips/join/{token}` | No | Trip preview by invite token |
| POST | `/api/trips/join/{token}` | Yes | Join trip |
| GET | `/api/trips/{id}/invite` | Yes | Get invite URL |

### Itinerary
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/trips/{id}/days` | Yes | Get all days with items |
| POST | `/api/trips/{id}/days` | Yes | Add a day |
| POST | `/api/days/{id}/items` | Yes | Add item to day |
| PUT | `/api/items/{id}` | Yes | Edit item |
| DELETE | `/api/items/{id}` | Yes | Delete item |
| PUT | `/api/days/{id}/reorder` | Yes | Batch reorder items |

### Expenses
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/trips/{id}/expenses` | Yes | List expenses |
| POST | `/api/trips/{id}/expenses` | Yes | Add expense |
| PUT | `/api/expenses/{id}` | Yes | Edit expense |
| DELETE | `/api/expenses/{id}` | Yes | Delete expense |
| GET | `/api/trips/{id}/balances` | Yes | Per-member net balance |
| GET | `/api/trips/{id}/settlements` | Yes | Minimum settlement transactions |
| PUT | `/api/settlements/mark-paid` | Yes | Mark splits as paid |

### Export
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/trips/{id}/export/pdf` | Yes | Download trip PDF summary |

## Quick curl test

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com","password":"secret123"}'

# Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"secret123"}' | jq -r .token)

# Create trip
curl -X POST http://localhost:8080/api/trips \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Goa 2025","coverEmoji":"🏖️"}'
```
