
# TicTacToe – Recruitment Task

## Runtime Requirements
- Java 21
- Maven
- Docker (required for local DB and integration tests)

## Running the Application
To start the application locally, simply run:

```bash
docker compose up
```
The application uses PostgreSQL managed via Docker Compose.
> **Note:** Docker is required to run both the application and integration tests, as they rely on Testcontainers.

---

## Testing
- Unit tests use standard JUnit and Mockito.
- Integration tests use **Testcontainers** to spin up real PostgreSQL instances.

You can run all tests with:

```bash
mvn clean verify
```

---

## API Testing
For your convenience, I’ve included a preconfigured HTTP file:
```
local-endpoints.http
```

You can open this in IntelliJ or another supported IDE to quickly test endpoints.

