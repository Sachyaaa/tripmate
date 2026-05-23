# TripMate

**A full-stack group trip planner** — plan itineraries, split expenses, and settle debts with a single invite link.

[![Live Demo](https://img.shields.io/badge/Live%20Demo-Vercel-black?logo=vercel)](https://YOUR_VERCEL_URL)
[![Backend](https://img.shields.io/badge/API-Render-46e3b7?logo=render)](https://YOUR_RENDER_URL)
[![Built with Claude Code](https://img.shields.io/badge/Built%20with-Claude%20Code-blueviolet?logo=anthropic)](https://claude.ai/code)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB?logo=react)](https://react.dev/)

---

## Features

- **JWT Authentication** — register, log in, stateless token-based sessions
- **Trip Management** — create trips with emoji covers, generate shareable invite links
- **Itinerary Builder** — add days and activities; drag-and-drop to reorder
- **Expense Splitting** — log expenses, auto-split among selected members
- **Smart Debt Settlement** — greedy algorithm (max/min-heap) minimizes the number of transactions to settle all debts
- **PDF Export** — one-click download of the full trip report (members, itinerary, expenses, balances, settlement plan)
- **Multi-member Support** — invite members via a unique token link; each member gets a color identity

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.2 · Spring Security · Spring Data JPA · Java 17 |
| Auth | JWT (jjwt 0.11.5) — stateless, `OncePerRequestFilter` |
| Database | MySQL (TiDB Serverless) · Hibernate `ddl-auto=update` |
| PDF | iText 5 |
| Frontend | React 18 · Vite 5 · React Router 6 · Axios |
| UI / UX | Plain CSS (CSS variables) · react-beautiful-dnd · recharts · react-hot-toast |
| Deployment | Render (Docker) · Vercel · TiDB Serverless |
| Container | Multi-stage Dockerfile (Maven build → JRE runtime) |

---

## Built with Agentic AI

This project was scaffolded, debugged, and deployed end-to-end using **[Claude Code](https://claude.ai/code)** — Anthropic's agentic coding tool.

The entire development workflow — architecture decisions, code generation across 20+ files, CORS and SSL configuration, multi-stage Docker setup, and deployment troubleshooting on Render + TiDB Serverless — was driven through natural-language prompts to an AI agent. This demonstrates practical **prompt engineering** and the ability to leverage agentic AI for complex, production-quality software delivery.

> "Agentic AI development" is not just autocomplete — it means directing an AI agent through multi-step tasks: reading files, editing code, resolving errors, and shipping to production.

---

## Local Setup

### Prerequisites
- Java 17, Maven 3.9+
- Node.js 18+
- MySQL (local or TiDB Serverless)

### Backend

```bash
cd tripmate-backend

# Create a local env file
cp src/main/resources/application.properties src/main/resources/application-local.properties
# Edit application-local.properties with your DB credentials

mvn spring-boot:run -Dspring-boot.run.profiles=local
# API runs at http://localhost:8080
```

### Frontend

```bash
cd tripmate-frontend
npm install
cp .env.example .env
# Edit .env: VITE_API_URL=http://localhost:8080
npm run dev
# UI runs at http://localhost:5173
```

---

## Project Structure

```
tripmate/
├── tripmate-backend/       Spring Boot 3.2 API
│   ├── src/main/java/com/tripmate/
│   │   ├── config/         SecurityConfig, CORS
│   │   ├── controller/     Auth, Trip, Day, Expense, Export
│   │   ├── entity/         7 JPA entities (UUID keys)
│   │   ├── service/        Business logic, settlement algorithm
│   │   └── repository/     Spring Data JPA repositories
│   └── Dockerfile
├── tripmate-frontend/      React 18 + Vite SPA
│   └── src/
│       ├── pages/          Login, Register, Trips, TripDashboard, JoinTrip
│       ├── components/     Itinerary, Expenses, SettleUp, Members tabs
│       ├── api/            Axios client with JWT interceptor
│       └── context/        AuthContext
└── render.yaml             Render deployment config
```

---

## API Overview

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Create account |
| POST | `/api/auth/login` | Get JWT token |
| GET/POST | `/api/trips` | List / create trips |
| POST | `/api/trips/join/:token` | Join via invite link |
| GET/POST | `/api/trips/:id/days` | Manage itinerary days |
| POST | `/api/days/:id/items` | Add activity to a day |
| PUT | `/api/days/:id/reorder` | Drag-drop reorder |
| GET/POST | `/api/trips/:id/expenses` | List / add expenses |
| GET | `/api/trips/:id/settlements` | Compute settlement plan |
| POST | `/api/trips/:id/settle` | Mark debts as paid |
| GET | `/api/trips/:id/export/pdf` | Download PDF report |

---

## Deployment

- **Backend**: Render (free tier, Docker runtime) — auto-deploys from `render.yaml`
- **Database**: TiDB Serverless (MySQL-compatible, free forever) — SSL required in JDBC URL
- **Frontend**: Vercel — auto-deploys from `tripmate-frontend/`

> Tip: Use [UptimeRobot](https://uptimerobot.com) to ping the backend every 10 minutes and prevent Render free-tier cold starts.

Resume bullet points (copy-paste ready for your Projects section):


TripMate — Group Trip Planner
github.com/Sachyaaa/tripmate  |  Live: YOUR_VERCEL_URL

• Engineered end-to-end using Claude Code (Anthropic) — demonstrates agentic AI
  development: directing an AI agent through architecture, code generation, debugging,
  and cloud deployment via natural-language prompts
• Full-stack: Spring Boot 3.2 (Java 17) REST API + React 18 SPA; JWT stateless auth,
  role-based access, 7 JPA entities
• Implemented greedy settlement algorithm (max/min-heap, integer arithmetic) to
  minimize the number of transactions needed to settle group expenses
• PDF trip report generation (iText 5); drag-and-drop itinerary reordering
  (react-beautiful-dnd)
• Containerized with multi-stage Docker; deployed on Render + Vercel + TiDB
  Serverless — permanently live at zero cost
Skills section additions:

AI/Tools: Claude Code, Agentic AI Development, Prompt Engineering
Backend: Spring Boot, Spring Security, JWT, JPA/Hibernate
DevOps: Docker, Render, Vercel, Multi-stage Docker builds
