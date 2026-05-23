# TripMate Frontend

React 18 + Vite SPA for the TripMate group trip planner.

## Prerequisites

- Node.js 18+
- Backend running at `http://localhost:8080`

## Setup

```bash
# Install dependencies
npm install

# Copy env file and set the API URL
cp .env.example .env
# Edit .env if your backend runs on a different port
```

## Run

```bash
npm run dev
# Opens at http://localhost:5173
```

## Build for production

```bash
npm run build
# Output in /dist
```

## Pages

| Route | Description |
|-------|-------------|
| `/` | Landing page |
| `/login` | Sign in |
| `/register` | Create account |
| `/trips` | All my trips (auth required) |
| `/trips/:id` | Trip dashboard — Itinerary · Expenses · Settle Up · Members |
| `/join/:token` | Accept an invite link |

## Tech notes

- **Auth**: JWT stored in `localStorage`, attached via Axios interceptor. Redirects to `/login` on 401.
- **Drag-and-drop**: `react-beautiful-dnd` for itinerary item reordering within a day. React's `StrictMode` is intentionally disabled in `main.jsx` for compatibility.
- **Toast notifications**: `react-hot-toast` for all success/error feedback.
- **PDF download**: Calls `GET /api/trips/:id/export/pdf` and triggers a browser download.

## Environment variables

| Variable | Default | Description |
|----------|---------|-------------|
| `VITE_API_URL` | `http://localhost:8080` | Backend base URL |
