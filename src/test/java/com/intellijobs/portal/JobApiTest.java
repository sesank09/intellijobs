package com.intellijobs.portal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JobApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Loads all jobs from project CSV dataset")
    void loadsJobsFromTheProjectDataset() throws Exception {
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(60))
                .andExpect(jsonPath("$[0].title").value("Java Developer"));
    }

    @Test
    @DisplayName("Retrieves single job by ID")
    void retrievesJobById() throws Exception {
        mockMvc.perform(get("/api/jobs/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.title").value("Java Developer"))
                .andExpect(jsonPath("$.company").value("Northstar Labs"));
    }

    @Test
    @DisplayName("Returns 404 for non-existent job ID")
    void returns404ForUnknownJob() throws Exception {
        mockMvc.perform(get("/api/jobs/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Ranks jobs by whole skill overlap via Jaccard Similarity")
    void ranksJobsByWholeSkillOverlap() throws Exception {
        mockMvc.perform(post("/api/jobs/match")
                        .contentType("application/json")
                        .content("{\"skills\":\"Java, SQL, Spring Boot\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].job.title").value("Java Developer"))
                .andExpect(jsonPath("$[0].score").value(1.0))
                .andExpect(jsonPath("$[0].matchedSkills.length()").value(3))
                .andExpect(jsonPath("$[0].missingSkills.length()").value(0))
                .andExpect(jsonPath("$[0].percentage").value(100.0));
    }

    @Test
    @DisplayName("Rejects empty skill list with 400 Bad Request")
    void rejectsAnEmptySkillList() throws Exception {
        mockMvc.perform(post("/api/jobs/match")
                        .contentType("application/json")
                        .content("{\"skills\":\"  \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Searches jobs with KMP algorithm and location filter")
    void searchJobsWithKmpAndLocation() throws Exception {
        mockMvc.perform(get("/api/jobs")
                        .param("keyword", "Python")
                        .param("algorithm", "kmp")
                        .param("location", "Bengaluru"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));
    }

    @Test
    @DisplayName("Searches jobs with Edit Distance fuzzy matching")
    void searchJobsWithFuzzyMatching() throws Exception {
        mockMvc.perform(get("/api/jobs")
                        .param("keyword", "Pithon")
                        .param("algorithm", "editdistance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));
    }

    @Test
    @DisplayName("Executes Algorithm Lab single search with measured nano metrics")
    void executesAlgorithmLabSearch() throws Exception {
        mockMvc.perform(post("/api/algorithms/search")
                        .contentType("application/json")
                        .content("{\"pattern\":\"Developer\",\"algorithm\":\"rabinkarp\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithmKey").value("rabinkarp"))
                .andExpect(jsonPath("$.recordsProcessed").value(60))
                .andExpect(jsonPath("$.matchesFound").value(greaterThan(0)))
                .andExpect(jsonPath("$.executionTimeNano").value(greaterThan(0)));
    }

    @Test
    @DisplayName("Executes Algorithm Lab benchmark comparison over all 4 algorithms")
    void executesAlgorithmLabBenchmark() throws Exception {
        mockMvc.perform(post("/api/algorithms/compare")
                        .contentType("application/json")
                        .content("{\"pattern\":\"Engineer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.benchmarks.length()").value(4))
                .andExpect(jsonPath("$.recordsProcessed").value(60));
    }

    @Test
    @DisplayName("Executes Graph BFS demonstration")
    void executesGraphBfsDemo() throws Exception {
        mockMvc.perform(get("/api/graph/demo").param("type", "bfs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithm").value("Breadth-First Search (BFS)"))
                .andExpect(jsonPath("$.traversalOrder.length()").value(8));
    }

    @Test
    @DisplayName("Executes Graph DFS demonstration")
    void executesGraphDfsDemo() throws Exception {
        mockMvc.perform(get("/api/graph/demo").param("type", "dfs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithm").value("Depth-First Search (DFS)"))
                .andExpect(jsonPath("$.traversalOrder.length()").value(8));
    }

    @Test
    @DisplayName("Executes Graph Dijkstra shortest path demonstration")
    void executesGraphDijkstraDemo() throws Exception {
        mockMvc.perform(get("/api/graph/demo")
                        .param("type", "dijkstra")
                        .param("source", "0")
                        .param("target", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithm").value("Dijkstra's Shortest Path Algorithm"))
                .andExpect(jsonPath("$.totalEffortWeeks").value(greaterThan(0)))
                .andExpect(jsonPath("$.shortestPath.length()").value(greaterThan(1)));
    }

    @Test
    @DisplayName("Executes Graph Maximum Bipartite Matching demonstration")
    void executesGraphBipartiteDemo() throws Exception {
        mockMvc.perform(get("/api/graph/demo").param("type", "bipartite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithm").value("Maximum Bipartite Matching"))
                .andExpect(jsonPath("$.maxMatches").value(greaterThan(0)))
                .andExpect(jsonPath("$.assignments.length()").value(greaterThan(0)));
    }

    @Test
    @DisplayName("Returns dataset stats")
    void returnsPortalStats() throws Exception {
        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalJobs").value(60));
    }
}