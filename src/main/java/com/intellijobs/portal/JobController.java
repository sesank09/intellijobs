package com.intellijobs.portal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    /**
     * Retrieve all jobs or search & filter by keyword, algorithm, location, category, and sort.
     */
    @GetMapping("/jobs")
    public List<Job> getJobs(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "algorithm", required = false, defaultValue = "kmp") String algorithm,
            @RequestParam(name = "location", required = false) String location,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "sort", required = false, defaultValue = "recommended") String sort) {

        if ((keyword == null || keyword.isBlank())
                && (location == null || location.isBlank() || "all".equalsIgnoreCase(location))
                && (category == null || category.isBlank() || "all".equalsIgnoreCase(category))
                && ("recommended".equalsIgnoreCase(sort) || sort == null || sort.isBlank())) {
            return jobService.getJobs();
        }

        return jobService.searchJobs(keyword, algorithm, location, category, sort);
    }

    /**
     * Retrieve single job by ID.
     */
    @GetMapping("/jobs/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable("id") int id) {
        return jobService.getJobById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Candidate Skill Matcher endpoint using Jaccard Similarity.
     */
    @PostMapping("/jobs/match")
    public List<SkillMatch> matchSkills(@RequestBody SkillMatchRequest request) {
        if (request == null || request.skills() == null || request.skills().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter at least one skill.");
        }
        return jobService.matchSkills(request.skills());
    }

    /**
     * Interactive Algorithm Lab: Execute a selected string matching algorithm.
     */
    @PostMapping("/algorithms/search")
    public ResponseEntity<Map<String, Object>> searchAlgorithm(@RequestBody AlgorithmSearchRequest request) {
        String pattern = request.pattern() != null ? request.pattern() : request.query();
        if (pattern == null || pattern.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search pattern cannot be empty.");
        }

        Map<String, Object> result = jobService.runAlgorithmSearch(
                pattern,
                request.algorithm(),
                request.threshold()
        );
        return ResponseEntity.ok(result);
    }

    /**
     * Interactive Algorithm Lab: Run academic benchmark comparing all 4 string matching algorithms.
     */
    @PostMapping("/algorithms/compare")
    public ResponseEntity<Map<String, Object>> compareAlgorithms(@RequestBody AlgorithmCompareRequest request) {
        String pattern = request.pattern() != null ? request.pattern() : request.query();
        if (pattern == null || pattern.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search pattern cannot be empty.");
        }

        Map<String, Object> result = jobService.runAlgorithmBenchmark(pattern);
        return ResponseEntity.ok(result);
    }

    /**
     * Interactive Graph Algorithms Demonstration (BFS, DFS, Dijkstra, Bipartite Matching).
     */
    @GetMapping("/graph/demo")
    public ResponseEntity<Map<String, Object>> runGraphDemo(
            @RequestParam(name = "type", defaultValue = "bfs") String type,
            @RequestParam(name = "source", required = false) Integer source,
            @RequestParam(name = "target", required = false) Integer target) {

        Map<String, Object> result = jobService.runGraphDemo(type, source, target);
        return ResponseEntity.ok(result);
    }

    /**
     * Metadata & summary stats.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<Job> allJobs = jobService.getJobs();
        Map<String, Long> categoryCounts = new LinkedHashMap<>();
        Map<String, Long> locationCounts = new LinkedHashMap<>();

        for (Job job : allJobs) {
            categoryCounts.put(job.category(), categoryCounts.getOrDefault(job.category(), 0L) + 1);
            locationCounts.put(job.location(), locationCounts.getOrDefault(job.location(), 0L) + 1);
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalJobs", allJobs.size());
        stats.put("categories", categoryCounts);
        stats.put("locations", locationCounts);
        stats.put("supportedAlgorithms", List.of("KMP", "Rabin-Karp", "Z-Algorithm", "Edit Distance"));
        stats.put("supportedGraphAlgorithms", List.of("BFS", "DFS", "Dijkstra", "Bipartite Matching"));

        return ResponseEntity.ok(stats);
    }

    public record AlgorithmSearchRequest(String query, String pattern, String algorithm, Integer threshold) {}
    public record AlgorithmCompareRequest(String query, String pattern) {}
}