# agentboard-api-restassured

![Java 21](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Maven](https://img.shields.io/badge/build-Maven-blue?logo=apachemaven)
![RestAssured 5.x](https://img.shields.io/badge/RestAssured-5.4.0-green)
![Cucumber 7.x](https://img.shields.io/badge/Cucumber-7.18.0-brightgreen?logo=cucumber)
![Allure 2.x](https://img.shields.io/badge/Allure-2.27.0-yellow)

BDD API test suite for **AgentBoard** — a multi-tenant Kanban board system.  
Tests are written in Gherkin, executed via Cucumber 7 + JUnit Platform, and reported with Allure.

---

## Prerequisites

| Tool   | Version  | Notes                                      |
|--------|----------|--------------------------------------------|
| Java   | 21       | Tested with Amazon Corretto 21             |
| Maven  | 3.9+     | Install via `sdk install maven` or system  |
| Docker | 24+      | Required to run AgentBoard services locally|

---

## Running locally

### 1. Start AgentBoard services

```bash
cd ../agentboard-infra
./scripts/start-local.sh   # starts PostgreSQL:5432
```

Then start the backend services in separate terminals:

```bash
# auth-service → http://localhost:8080
cd ../agentboard-backend
./gradlew :auth-service:bootRun

# board-service → http://localhost:8081
./gradlew :board-service:bootRun
```

### 2. Run all smoke tests

```bash
mvn test -Denv=local
```

### 3. Run a specific tag

```bash
mvn test -Denv=local -Dcucumber.filter.tags="@auth"
mvn test -Denv=local -Dcucumber.filter.tags="@board and @create-work-item"
```

### 4. Generate and open the Allure report

```bash
mvn allure:report
# Report output: target/site/allure-maven-plugin/index.html
```

---

## Environments

Configuration lives in `src/test/resources/environments/`.  
Select an environment with `-Denv=<name>` (default: `local`).

| Profile   | File                       | When to use                       |
|-----------|----------------------------|-----------------------------------|
| `local`   | `environments/local.properties`   | Local dev against localhost       |
| `staging` | `environments/staging.properties` | CI / staging server               |

Staging reads `${AUTH_BASE_URL}` and `${BOARD_BASE_URL}` from environment variables,
so no secrets are committed.

---

## Project architecture

```
src/test/
├── java/com/agentboard/api/
│   ├── config/
│   │   ├── Environment.java         ← Owner @Config — environment properties
│   │   └── RequestSpecFactory.java  ← RestAssured RequestSpec builder (auth / board)
│   ├── hooks/
│   │   └── ApiHooks.java            ← @Before clear context / @After Allure attach
│   ├── steps/
│   │   ├── AuthSteps.java           ← Gherkin steps for auth-service
│   │   └── BoardSteps.java          ← Gherkin steps for board-service
│   ├── support/
│   │   ├── ScenarioContext.java     ← ThreadLocal key/value store
│   │   └── TokenStore.java          ← JWT + tenantId facade over ScenarioContext
│   └── runners/
│       └── CucumberRunner.java      ← JUnit Platform Suite entry point
└── resources/
    ├── features/
    │   ├── auth/
    │   │   ├── register.feature
    │   │   └── login.feature
    │   └── board/
    │       └── work-items.feature
    ├── environments/
    │   ├── local.properties
    │   └── staging.properties
    ├── allure.properties
    └── cucumber.properties
```

### Design pattern: Service Objects

This project does **not** use the Page Object Model (which is DOM-centric).  
Instead it follows the **Service Object** pattern:

- `RequestSpecFactory` is a service object — it encapsulates the HTTP client configuration for each backend service.
- Step definition classes (`AuthSteps`, `BoardSteps`) are thin orchestrators: they call the right spec, store results in `ScenarioContext`, and assert outcomes.
- `ScenarioContext` + `TokenStore` act as the shared state bus between steps, keeping each step class stateless and independently testable.

This keeps step definitions readable as plain prose, hides transport concerns from Gherkin authors, and makes it straightforward to add new services by adding a new `*Spec()` method to `RequestSpecFactory`.

---

## Tag taxonomy

| Tag                  | Meaning                                          |
|----------------------|--------------------------------------------------|
| `@smoke`             | Critical happy-path scenarios — always run in CI |
| `@auth`              | Auth-service scenarios                           |
| `@board`             | Board-service scenarios                          |
| `@create-work-item`  | Work-item creation                               |
| `@list-work-items`   | Work-item listing                                |
| `@update-work-item`  | Work-item status transitions                     |
| `@tenant-selection`  | Multi-tenant token scope scenarios               |
| `@wip`               | In-progress / not yet ready — excluded by default|

---

## CI

GitHub Actions workflow: `.github/workflows/ci.yml`

- Runs on push/PR to `main`
- Supports `workflow_dispatch` with `environment` and `tags` inputs
- Spins up a Postgres 16 service container
- Uploads Allure results and rendered HTML report as artifacts (30-day retention)

---

## Contributing

1. Write the Gherkin scenario in the appropriate `.feature` file first.
2. Run `mvn test` — Cucumber will print pending step snippets for any unimplemented steps.
3. Implement step definitions in `AuthSteps` or `BoardSteps` (or a new `*Steps` class for a new service).
4. Tag new scenarios with at least `@smoke` or a domain tag.
5. Never tag a merged scenario with `@wip`.
