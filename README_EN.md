# Auth Performance Comparison

🇹🇷 **[Türkçe Oku](README.md)**

This project aims to compare the performance of 3 different authentication strategies using Spring Boot. The goal is to observe the impact of database queries, Redis caching, and Stateless JWT usage on system latency and capacity (RPS) in real-time.

## 🚀 Strategies

The project tests 3 different scenarios:

1.  **Database Strategy** (`/api/v1/test/db`)
    *   `userId` is extracted from the JWT for each request.
    *   The user is verified by querying the database (`users` table).
    *   **Advantage:** Most up-to-date data.
    *   **Disadvantage:** High latency, database bottleneck.

2.  **Redis Cache Strategy** (`/api/v1/test/redis`)
    *   Redis is checked first for each request.
    *   If data is not in Redis (Cache Miss), it is fetched from the database and written to Redis.
    *   **Advantage:** Much faster than the database.
    *   **Disadvantage:** Requires extra infrastructure cost and cache invalidation management.

3.  **Stateless JWT Strategy** (`/api/v1/test/stateless`)
    *   Information such as `username` and `role` (Claims) is carried within the token.
    *   The server performs only signature verification without querying any storage (DB or Redis).
    *   **Advantage:** Zero Network I/O, highest speed and scalability.
    *   **Disadvantage:** Token revocation is difficult, token size may increase.

## 🛠️ Setup and Execution

### Requirements
*   Java 17+
*   Docker & Docker Compose
*   K6 (For benchmarking)

### 1. Set Up Infrastructure
Run the following command in the project directory to start PostgreSQL and Redis on Docker:

```bash
docker-compose up -d
```
*   **Postgres:** Port `5438` (User: `postgres`, Pass: `pass`)
*   **Redis:** Port `6388`

### 2. Start the Application
The application will seed **10,000 test users** into the database on first startup.

```bash
mvn spring-boot:run
```
The application will run at `http://localhost:8080`.

### 3. Generate Test Tokens
You need valid JWT tokens to perform tests. Go to the following address in your browser:

`http://localhost:8080/api/v1/test/generate-tokens?userId=1`

The returned JSON will contain two tokens:
*   `minimal`: For DB and Redis tests (contains only ID).
*   `full`: For Stateless test (contains role and email info).

Copy these tokens and paste them into the relevant places in the `perf-test.js` file (It may be done automatically, please check).

## 🧪 Performance Test (Benchmark)

We use **K6** as the testing tool.

1.  Open a terminal in the project directory.
2.  Open `perf-test.js` and uncomment the scenario you want to test (`testDb`, `testRedis`, or `testStateless`).
3.  Start the test:

```powershell
& "C:\Program Files\k6\k6.exe" run perf-test.js
```

### Actual Test Results (Local Environment)

The following results were obtained with PostgreSQL/Redis running on Docker and K6 running on the local machine (50 VUs, 1m 40s):

| Strategy | RPS (Requests/Sec) | Average Latency |
| :--- | :--- | :--- |
| **Stateless** | ~2,966 | 12.18ms |
| **Redis** | ~2,243 | 16.96ms |
| **Database** | ~1,365 | 29.00ms |

**Analysis:**
*   **Stateless** can handle **more than 2 times** the requests compared to the database method.
*   **Redis** provided approximately **65% performance increase** compared to the database.
*   **Database** method is the slowest because each request involves disk-based reading (or database cache lookup) and network I/O costs.

## 📂 Project Structure

*   `src/main/java/.../controller/TestController.java`: Where the test endpoints are located.
*   `src/main/java/.../util/JwtUtil.java`: Token generation and verification logic.
*   `src/main/java/.../config/RedisConfig.java`: Redis connection settings.
*   `perf-test.js`: K6 load test scenario file.
*   `docker-compose.yml`: Infrastructure configuration.
