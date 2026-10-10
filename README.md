# Intelligent Job Portal Using String Matching and Graph Algorithms

An academic, production-grade web application built for the **DSA-3 (Data Structures & Algorithms)** project. The platform demonstrates how real Java data structures and algorithmic paradigms power an intelligent job discovery, candidate skill-matching, and career navigation system.

---

## 📌 Project Overview

- **Title**: Intelligent Job Portal Using String Matching and Graph Algorithms
- **Backend**: Java 17, Spring Boot 3.5.x (REST API Architecture)
- **Frontend**: Responsive HTML5, Vanilla CSS3 (Deep Purple Design System), Vanilla JavaScript (native `fetch()` API)
- **Data Storage**: Local tab-delimited `data/jobs.csv` dataset containing 60 verified job records across engineering domains
- **Build System**: Maven Wrapper (`mvnw` / `mvnw.cmd`)
- **Port**: `http://localhost:8080/`

---

## 🏛 Application Architecture

```
                                  [ Browser Client ]
                       (HTML5 + Vanilla CSS3 + Vanilla JavaScript)
                                        │
                         HTTP REST API (fetch) │ JSON
                                        ▼
                            [ Spring Boot REST API ]
                       (com.intellijobs.portal.JobController)
                                        │
                                        ▼
                                [ JobService Layer ]
                                        │
           ┌────────────────────────────┼────────────────────────────┐
           ▼                            ▼                            ▼
  [ String Algorithms ]        [ Set Theory Matching ]       [ Graph Algorithms ]
  • KMP.java                   • JaccardSimilarity.java      • Graph.java
  • RabinKarp.java                                           • BFS.java
  • ZAlgorithm.java                                          • DFS.java
  • EditDistance.java                                        • Dijkstra.java
           │                                                 • BipartiteMatching.java
           └────────────────────────────┬────────────────────────────┘
                                        ▼
                                [ Local Storage ]
                             data/jobs.csv (60 Jobs)
```

The frontend is served directly by the embedded Tomcat server through Spring Boot's static resource handlers, unifying both client and API on a single origin (`http://localhost:8080`).

---

## 🧠 Implemented DSA Algorithms & Complexity

| Algorithm | Category | Best Case Time | Worst Case Time | Space Complexity | Practical Role in Job Portal |
|---|---|---|---|---|---|
| **Knuth-Morris-Pratt (KMP)** | String Matching | $O(N)$ | $O(N + M)$ | $O(M)$ | Prefix-function (LPS) pattern search over descriptions with zero text backtracking. |
| **Rabin-Karp** | String Matching | $O(N + M)$ | $O(N \cdot M)$ | $O(1)$ | Polynomial rolling hash with character verification upon hash collision. |
| **Z-Algorithm** | String Matching | $O(N + M)$ | $O(N + M)$ | $O(N + M)$ | Linear-time Z-box prefix matching on concatenated pattern and text. |
| **Edit Distance (Levenshtein)** | Dynamic Programming | $O(M \cdot N)$ | $O(M \cdot N)$ | $O(M \cdot N)$ | Approximate/fuzzy matching that tolerates applicant spelling typos. |
| **Jaccard Similarity** | Set Theory | $O(\|A\| + \|B\|)$ | $O(\|A\| + \|B\|)$ | $O(\|A\| + \|B\|)$ | Calculates $\frac{\|A \cap B\|}{\|A \cup B\|} \times 100\%$ overlap between applicant skills and role requirements. |
| **Breadth-First Search (BFS)** | Graph Traversal | $O(V + E)$ | $O(V + E)$ | $O(V)$ | Level-by-level skill prerequisite and competency curriculum progression. |
| **Depth-First Search (DFS)** | Graph Traversal | $O(V + E)$ | $O(V + E)$ | $O(V)$ | Deep vertical domain specialization and recursive skill hierarchy. |
| **Dijkstra's Algorithm** | Shortest Path | $O((V+E)\log V)$ | $O(V^2)$ | $O(V)$ | Calculates minimum learning effort (in weeks) to bridge candidate to target role. |
| **Maximum Bipartite Matching** | Network Flow | $O(V \cdot E)$ | $O(V \cdot E)$ | $O(V)$ | Optimally assigns candidate applicant sets to open job positions using Augmenting Paths. |

*Notation: $N$ = length of text, $M$ = length of pattern, $V$ = vertices/nodes, $E$ = edges/prerequisites, $A, B$ = tokenized skill sets.*

---

## 🌐 REST API Endpoints

### 1. Job Listings & Search
- **`GET /api/jobs`**
  - **Query Parameters**:
    - `keyword` *(optional)*: Pattern to search for in jobs.
    - `algorithm` *(optional, default: `kmp`)*: `kmp`, `rabinkarp`, `zalgorithm`, `editdistance`.
    - `location` *(optional)*: Filter by location (e.g. `Hyderabad`, `Bengaluru`, `Remote`).
    - `category` *(optional)*: Filter by domain (e.g. `Software`, `Data & AI`, `Web Development`, `Cloud & DevOps`, `Cybersecurity`).
    - `sort` *(optional, default: `recommended`)*: `recommended`, `salary`, `title`.
  - **Response**: `List<Job>` (Array of 60 JSON objects).

- **`GET /api/jobs/{id}`**
  - Returns complete job details, qualification requirements, salary, and company metadata for a single job ID.

### 2. Candidate Skill Matcher
- **`POST /api/jobs/match`**
  - **Request Body**:
    ```json
    {
      "skills": "Java, SQL, Spring Boot, REST API"
    }
    ```
  - **Response**: Array of `SkillMatch` objects ranked descending by similarity score:
    ```json
    [
      {
        "job": { "id": 101, "title": "Java Developer", "company": "Northstar Labs", ... },
        "score": 1.0,
        "percentage": 100.0,
        "matchedSkills": ["java", "sql", "spring boot", "rest api"],
        "missingSkills": [],
        "explanation": "4 of 4 required skills matched (100.0% overlap)",
        "formula": "J(A, B) = |4| / |4| = 1.00"
      }
    ]
    ```

### 3. Algorithm Lab (Interactive Benchmarks)
- **`POST /api/algorithms/search`**
  - **Request Body**:
    ```json
    {
      "pattern": "Developer",
      "algorithm": "kmp"
    }
    ```
  - **Response**: Returns execution metrics measured via `System.nanoTime()`:
    - `executionTimeNano`: Raw JVM execution time in nanoseconds.
    - `executionTimeMs`: Execution time in milliseconds.
    - `recordsProcessed`: 60 records scanned.
    - `matchesFound`: Number of matching job records.
    - `explanation`: Detailed explanation of algorithm mechanics.
    - `matchedJobs`: List of matching job listings.

- **`POST /api/algorithms/compare`**
  - **Request Body**:
    ```json
    {
      "pattern": "Engineer"
    }
    ```
  - **Response**: Runs all 4 string matching algorithms concurrently across the same dataset and returns side-by-side comparative benchmark metrics.

### 4. Graph Theory Demonstrations
- **`GET /api/graph/demo`**
  - **Query Parameters**:
    - `type`: `bfs`, `dfs`, `dijkstra`, `bipartite`.
    - `source` *(optional, for BFS/DFS/Dijkstra)*: Starting node index.
    - `target` *(optional, for Dijkstra)*: Destination node index.
  - **Response**: Graph adjacency matrix, traversal sequence, level mappings, relaxation logs, and augmenting path assignments.

### 5. Portal Statistics
- **`GET /api/stats`**
  - Returns total jobs count, category breakdown, and location distributions.

---

## 📁 Project Directory Structure

```
Project/
├── data/
│   └── jobs.csv                       # Local dataset (60 verified jobs)
├── frontend/
│   ├── index.html                     # Responsive single-page application UI
│   ├── style.css                      # Modern deep purple design system
│   └── script.js                      # Vanilla JS fetch() API client
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/intellijobs/portal/
│   │   │       ├── IntelligentJobPortalApplication.java   # Spring Boot entry point
│   │   │       ├── JobPortalApplication.java              # Compatibility alias
│   │   │       ├── Job.java                               # Job entity record
│   │   │       ├── JobController.java                     # REST Controller endpoints
│   │   │       ├── JobService.java                        # Service & dataset loader
│   │   │       ├── KMP.java                               # Knuth-Morris-Pratt matcher
│   │   │       ├── RabinKarp.java                         # Rolling hash matcher
│   │   │       ├── ZAlgorithm.java                        # Z-array matcher
│   │   │       ├── EditDistance.java                      # DP Levenshtein distance
│   │   │       ├── JaccardSimilarity.java                 # Skill set similarity
│   │   │       ├── Graph.java                             # Adjacency matrix graph
│   │   │       ├── BFS.java                               # Breadth-first traversal
│   │   │       ├── DFS.java                               # Depth-first traversal
│   │   │       ├── Dijkstra.java                          # Shortest path algorithm
│   │   │       ├── BipartiteMatching.java                 # Max bipartite matching
│   │   │       ├── SkillMatch.java                        # Match result record
│   │   │       └── SkillMatchRequest.java                 # Skill match request model
│   │   └── resources/
│   │       ├── application.properties                     # Spring Boot configuration
│   │       └── static/                                    # Static classpath fallback
│   └── test/
│       └── java/
│           └── com/intellijobs/portal/
│               ├── AlgorithmTests.java                    # Unit tests for all 8 DSA algorithms
│               └── JobApiTest.java                        # End-to-end Spring Boot API tests
├── mvnw                               # Maven wrapper Unix script
├── mvnw.cmd                           # Maven wrapper Windows batch script
├── .mvn/                              # Maven wrapper configuration & jar
├── pom.xml                            # Project Object Model dependencies
└── README.md                          # Project documentation
```

---

## 🚀 Execution & Verification Instructions

### Prerequisites
- **Java**: OpenJDK 17 or higher (`java -version`).
- **Maven**: Use the included Maven Wrapper (`mvnw.cmd` on Windows, `./mvnw` on macOS/Linux).

### 1. Compile the Project
```powershell
.\mvnw.cmd compile
```

### 2. Run the Full Test Suite
Executes 25 comprehensive tests (11 DSA algorithm unit tests and 14 Spring Boot integration tests):
```powershell
.\mvnw.cmd test
```

### 3. Start the Web Application
```powershell
.\mvnw.cmd spring-boot:run
```

Once started, open your web browser and navigate to:
```
http://localhost:8080/
```

---

## 🧪 Verification Summary

| Test Category | Suite | Test Count | Status |
|---|---|---|---|
| String Matching & DP | `AlgorithmTests` | 5 | ✅ PASS (KMP, LPS, Rabin-Karp, Z-Algorithm, Edit Distance) |
| Set Theory Matching | `AlgorithmTests` | 2 | ✅ PASS (Jaccard similarity, empty set handling) |
| Graph Theory | `AlgorithmTests` | 4 | ✅ PASS (BFS levels, DFS recursion, Dijkstra weights, Bipartite matching) |
| REST API & Controller | `JobApiTest` | 14 | ✅ PASS (Search, filters, 404 handling, benchmarks, graph demos) |
| **Total** | **All Tests** | **25** | **✅ 100% PASS (0 Failures, 0 Errors)** |