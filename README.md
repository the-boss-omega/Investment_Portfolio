# Investment Portfolio

Spring Boot app for managing an investment portfolio.

## Persistence

The app now stores users, accounts, and positions in the configured JDBC database.

- Main application default: MySQL-style connection settings (`jdbc:mysql://localhost:3306/portfolio`) unless overridden in `.env.json` or environment variables
- Test profile: in-memory H2 database for repeatable automated tests

## Seeded users

These users are created automatically on startup if they do not already exist:

- `demo@demo.com` / `demo123`
- `dbcheck@demo.com` / `dbcheck123`

## Quick check

1. Start the app with a reachable MySQL database configured
2. Log in with `dbcheck@demo.com`
3. Call `/api/portfolio`
4. Call `/api/db/save`
5. Call `/api/db/accounts/{userId}`
