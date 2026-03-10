# Database Integration Summary

This patch adds JDBC persistence for portfolio accounts and positions.

## Added API Endpoints (`/api`)

- `POST /db/save` - saves all in-memory accounts for the authenticated user.
- `GET /db/accounts/{userId}` - loads accounts directly from DB for a user.
- `POST /db/reload/{userId}` - replaces in-memory accounts with DB data.
- `POST /db/add-position` - adds/updates one position and persists immediately.
- `POST /db/update-position` - updates one position and persists immediately.
- `DELETE /db/position/{provider}/{symbol}` - deletes one position and persists immediately.

## New Classes

- `src/main/java/com/example/portfolio/PortfolioDatabaseFunctions.java`
- `src/main/java/com/example/portfolio/AddPositionRequest.java`
- `src/main/java/com/example/portfolio/UpdatePositionRequest.java`

## Config Changes

- Added dependencies in `build.gradle`:
  - `spring-boot-starter-jdbc`
  - `mysql-connector-j`
  - `jackson-databind`
- Added datasource placeholders in `src/main/resources/application.properties`.
- `.env.json` is loaded at startup in `PortfolioApplication`.
- `.env.json` is ignored in `.gitignore`.

## `.env.json` format

```json
{
  "DB_URL": "jdbc:mysql://localhost:3306/portfolio?serverTimezone=UTC",
  "DB_USERNAME": "root",
  "DB_PASSWORD": "",
  "DB_DRIVER": "com.mysql.cj.jdbc.Driver"
}
```

