package com.intellijobs.portal;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JobService {

    private static final Pattern CATEGORY_DATA = Pattern.compile(
            "data analyst|machine learning|data scientist|artificial intelligence|\\bai\\b|analytics|business intelligence|computer vision|nlp");
    private static final Pattern CATEGORY_WEB = Pattern.compile(
            "frontend|front-end|web developer|ui developer|react|html|css|javascript|full stack");
    private static final Pattern CATEGORY_CLOUD = Pattern.compile(
            "cloud|devops|aws|azure|docker|kubernetes|infrastructure");
    private static final Pattern CATEGORY_SECURITY = Pattern.compile(
            "cyber|security|penetration|soc analyst|information security");

    private static final Pattern SALARY_PATTERN = Pattern.compile("(\\d+)(?:-(\\d+))?\\s*LPA", Pattern.CASE_INSENSITIVE);

    @Value("${jobportal.data-file:data/jobs.csv}")
    private String dataFile;

    private List<Job> jobs = List.of();

    @PostConstruct
    void loadJobs() throws IOException {
        List<Job> loadedJobs = new ArrayList<>();
        Path path = Path.of(dataFile);

        if (!Files.exists(path)) {
            // Check fallback locations
            if (Files.exists(Path.of("data/jobs.csv"))) {
                path = Path.of("data/jobs.csv");
            } else if (Files.exists(Path.of("../data/jobs.csv"))) {
                path = Path.of("../data/jobs.csv");
            }
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            reader.readLine(); // Header
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                // Support tab-delimited, with comma fallback
                String[] fields = line.split("\\t", 9);
                if (fields.length != 9) {
                    fields = line.split(",", 9);
                }

                if (fields.length == 9) {
                    try {
                        loadedJobs.add(new Job(
                                Integer.parseInt(fields[0].trim()),
                                fields[1].trim(),
                                fields[2].trim(),
                                fields[3].trim(),
                                fields[4].trim(),
                                fields[5].trim(),
                                fields[6].trim(),
                                fields[7].trim(),
                                fields[8].trim(),
                                categoryFor(fields)));
                    } catch (NumberFormatException ignored) {
                        // Skip malformed data
                    }
                }
            }
        }

        if (loadedJobs.isEmpty()) {
            throw new IllegalStateException("No valid jobs found in " + path.toAbsolutePath());
        }
        jobs = List.copyOf(loadedJobs);
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public Optional<Job> getJobById(int id) {
        return jobs.stream().filter(job -> job.id() == id).findFirst();
    }

    /**
     * Searches and filters jobs using string matching algorithms.
     */
    public List<Job> searchJobs(String keyword, String algorithm, String location, String category, String sort) {
        List<Job> results = new ArrayList<>();
        String normalizedAlgo = algorithm == null ? "kmp" : algorithm.toLowerCase().replace("-", "").trim();

        for (Job job : jobs) {
            boolean matchesKeyword = true;
            if (keyword != null && !keyword.isBlank()) {
                String targetText = job.searchText();
                String query = keyword.trim();

                switch (normalizedAlgo) {
                    case "rabinkarp":
                        matchesKeyword = RabinKarp.contains(targetText, query);
                        break;
                    case "zalgorithm":
                    case "z":
                        matchesKeyword = ZAlgorithm.contains(targetText, query);
                        break;
                    case "editdistance":
                    case "fuzzy":
                        matchesKeyword = EditDistance.fuzzyMatch(targetText, query, 2);
                        break;
                    case "kmp":
                    default:
                        matchesKeyword = KMP.contains(targetText, query);
                        break;
                }
            }

            if (!matchesKeyword) {
                continue;
            }

            // Location filter
            if (location != null && !location.isBlank() && !location.equalsIgnoreCase("all")) {
                if (!job.location().toLowerCase().contains(location.trim().toLowerCase())) {
                    continue;
                }
            }

            // Category filter
            if (category != null && !category.isBlank() && !category.equalsIgnoreCase("all")) {
                if (!job.category().equalsIgnoreCase(category.trim())) {
                    continue;
                }
            }

            results.add(job);
        }

        // Sorting
        if ("salary".equalsIgnoreCase(sort)) {
            results.sort(Comparator.comparingInt(this::extractMaxSalary).reversed());
        } else if ("title".equalsIgnoreCase(sort)) {
            results.sort(Comparator.comparing(Job::title));
        }

        return results;
    }

    /**
     * Matches candidate skills against jobs using Jaccard Similarity.
     */
    public List<SkillMatch> matchSkills(String candidateSkills) {
        Set<String> candidateTokens = JaccardSimilarity.tokenizeSkills(candidateSkills);
        if (candidateTokens.isEmpty()) {
            return List.of();
        }

        List<SkillMatch> matches = new ArrayList<>();

        for (Job job : jobs) {
            Set<String> requiredTokens = JaccardSimilarity.tokenizeSkills(job.skills());

            List<String> matchedSkills = candidateTokens.stream()
                    .filter(requiredTokens::contains)
                    .toList();

            List<String> missingSkills = requiredTokens.stream()
                    .filter(skill -> !candidateTokens.contains(skill))
                    .toList();

            int union = candidateTokens.size() + requiredTokens.size() - matchedSkills.size();
            double score = union == 0 ? 0.0 : (double) matchedSkills.size() / union;

            matches.add(new SkillMatch(job, score, matchedSkills, missingSkills));
        }

        matches.sort(Comparator.comparingDouble(SkillMatch::score).reversed()
                .thenComparing(match -> match.job().title()));

        return matches;
    }

    /**
     * Interactive Algorithm Lab: Executes a single string matching algorithm and measures real execution time.
     */
    public Map<String, Object> runAlgorithmSearch(String pattern, String algorithm, Integer threshold) {
        if (pattern == null || pattern.isBlank()) {
            return Map.of("error", "Pattern cannot be empty.");
        }

        String query = pattern.trim();
        String normalizedAlgo = algorithm == null ? "kmp" : algorithm.toLowerCase().replace("-", "").trim();
        int maxDist = threshold != null && threshold > 0 ? threshold : 2;

        List<Job> matchedJobs = new ArrayList<>();
        long startTime = System.nanoTime();

        for (Job job : jobs) {
            String text = job.searchText();
            boolean match = false;

            switch (normalizedAlgo) {
                case "rabinkarp":
                    match = RabinKarp.contains(text, query);
                    break;
                case "zalgorithm":
                case "z":
                    match = ZAlgorithm.contains(text, query);
                    break;
                case "editdistance":
                case "fuzzy":
                    match = EditDistance.fuzzyMatch(text, query, maxDist);
                    break;
                case "kmp":
                default:
                    match = KMP.contains(text, query);
                    break;
            }

            if (match) {
                matchedJobs.add(job);
            }
        }

        long durationNano = System.nanoTime() - startTime;
        double durationMs = Math.round((durationNano / 1_000_000.0) * 1000.0) / 1000.0;

        String algoName;
        String timeComplexity;
        String spaceComplexity;
        String explanation;

        switch (normalizedAlgo) {
            case "rabinkarp":
                algoName = "Rabin-Karp Rolling Hash";
                timeComplexity = "O(N + M) average, O(N * M) worst-case";
                spaceComplexity = "O(1)";
                explanation = "Rabin-Karp slides an O(1) rolling hash window over the text. When the rolling hash matches patternHash, it performs character verification to eliminate collisions.";
                break;
            case "zalgorithm":
            case "z":
                algoName = "Z-Algorithm (Z-Array)";
                timeComplexity = "O(N + M)";
                spaceComplexity = "O(N + M)";
                explanation = "Z-Algorithm constructs the string S = Pattern + '$' + Text and builds the Z-array where Z[i] represents the length of the longest substring starting at i matching pattern prefix.";
                break;
            case "editdistance":
            case "fuzzy":
                algoName = "Edit Distance (Levenshtein DP)";
                timeComplexity = "O(N * M)";
                spaceComplexity = "O(N * M)";
                explanation = "Edit Distance calculates the minimum insertions, deletions, and substitutions to transform query into token via Dynamic Programming table. Ideal for handling candidate typos.";
                break;
            case "kmp":
            default:
                algoName = "Knuth-Morris-Pratt (KMP)";
                timeComplexity = "O(N + M)";
                spaceComplexity = "O(M)";
                explanation = "KMP preprocesses the pattern to construct the Longest Prefix Suffix (LPS) table, skipping redundant comparisons upon mismatches without ever backtracking the text pointer.";
                break;
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("algorithm", algoName);
        response.put("algorithmKey", normalizedAlgo);
        response.put("pattern", query);
        response.put("recordsProcessed", jobs.size());
        response.put("matchesFound", matchedJobs.size());
        response.put("executionTimeNano", durationNano);
        response.put("executionTimeMs", durationMs);
        response.put("timeComplexity", timeComplexity);
        response.put("spaceComplexity", spaceComplexity);
        response.put("explanation", explanation);
        response.put("matchedJobs", matchedJobs);

        return response;
    }

    /**
     * Interactive Algorithm Lab Benchmark: Runs all 4 string matching algorithms on identical data
     * and records true measured nanoseconds for academic performance comparison.
     */
    public Map<String, Object> runAlgorithmBenchmark(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            return Map.of("error", "Search pattern cannot be empty.");
        }

        String query = pattern.trim();
        List<Map<String, Object>> benchmarks = new ArrayList<>();

        // 1. KMP
        long startKmp = System.nanoTime();
        int kmpMatches = 0;
        for (Job job : jobs) {
            if (KMP.contains(job.searchText(), query)) kmpMatches++;
        }
        long kmpNano = System.nanoTime() - startKmp;

        benchmarks.add(createBenchmarkEntry(
                "Knuth-Morris-Pratt (KMP)",
                "kmp",
                kmpMatches,
                kmpNano,
                "O(N + M)",
                "O(M)",
                "Precomputes LPS table; zero backtracking on text pointer. Ideal for fixed pattern search."
        ));

        // 2. Rabin-Karp
        long startRk = System.nanoTime();
        int rkMatches = 0;
        for (Job job : jobs) {
            if (RabinKarp.contains(job.searchText(), query)) rkMatches++;
        }
        long rkNano = System.nanoTime() - startRk;

        benchmarks.add(createBenchmarkEntry(
                "Rabin-Karp",
                "rabinkarp",
                rkMatches,
                rkNano,
                "O(N + M) avg",
                "O(1)",
                "Polynomial rolling hash with character collision check. Excellent for multi-pattern hashing."
        ));

        // 3. Z-Algorithm
        long startZ = System.nanoTime();
        int zMatches = 0;
        for (Job job : jobs) {
            if (ZAlgorithm.contains(job.searchText(), query)) zMatches++;
        }
        long zNano = System.nanoTime() - startZ;

        benchmarks.add(createBenchmarkEntry(
                "Z-Algorithm",
                "zalgorithm",
                zMatches,
                zNano,
                "O(N + M)",
                "O(N + M)",
                "Z-box prefix matching over concatenated string. Optimal linear pattern occurrence detection."
        ));

        // 4. Edit Distance (Fuzzy)
        long startEd = System.nanoTime();
        int edMatches = 0;
        for (Job job : jobs) {
            if (EditDistance.fuzzyMatch(job.searchText(), query, 2)) edMatches++;
        }
        long edNano = System.nanoTime() - startEd;

        benchmarks.add(createBenchmarkEntry(
                "Edit Distance (Levenshtein)",
                "editdistance",
                edMatches,
                edNano,
                "O(N * M)",
                "O(N * M)",
                "Dynamic programming recurrence. Tolerates typos and approximate candidate input."
        ));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pattern", query);
        result.put("recordsProcessed", jobs.size());
        result.put("benchmarks", benchmarks);
        result.put("note", "Execution times measured directly in Java Virtual Machine using System.nanoTime().");

        return result;
    }

    private Map<String, Object> createBenchmarkEntry(
            String name, String key, int matches, long nano, String timeComp, String spaceComp, String notes) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("key", key);
        map.put("matchesFound", matches);
        map.put("timeNano", nano);
        map.put("timeMs", Math.round((nano / 1_000_000.0) * 1000.0) / 1000.0);
        map.put("timeMicro", Math.round((nano / 1_000.0) * 10.0) / 10.0);
        map.put("timeComplexity", timeComp);
        map.put("spaceComplexity", spaceComp);
        map.put("notes", notes);
        return map;
    }

    /**
     * Graph Algorithms Demonstration module.
     */
    public Map<String, Object> runGraphDemo(String type, Integer source, Integer target) {
        String cleanType = type == null ? "bfs" : type.toLowerCase().trim();
        Map<String, Object> response = new LinkedHashMap<>();

        switch (cleanType) {
            case "bfs": {
                Graph graph = buildSkillPathwayGraph();
                int src = (source != null && source >= 0 && source < graph.getVertices()) ? source : 0;
                BFS.TraversalResult result = BFS.traverse(graph, src);

                response.put("algorithm", "Breadth-First Search (BFS)");
                response.put("scenario", "Skill Prerequisite & Curriculum Pathway Traversal");
                response.put("startVertex", src);
                response.put("startLabel", graph.getVertexLabel(src));
                response.put("traversalOrder", result.order());
                response.put("traversalLabels", result.orderLabels());
                response.put("levels", result.levels());
                response.put("steps", result.steps());
                response.put("vertexLabels", graph.getVertexLabels());
                response.put("adjacencyMatrix", graph.getAdjacency());
                response.put("explanation", result.explanation());
                response.put("timeComplexity", "O(V + E)");
                response.put("spaceComplexity", "O(V)");
                break;
            }

            case "dfs": {
                Graph graph = buildSpecializationGraph();
                int src = (source != null && source >= 0 && source < graph.getVertices()) ? source : 0;
                DFS.TraversalResult result = DFS.traverse(graph, src);

                response.put("algorithm", "Depth-First Search (DFS)");
                response.put("scenario", "AI Domain Mastery & Deep Vertical Specialization");
                response.put("startVertex", src);
                response.put("startLabel", graph.getVertexLabel(src));
                response.put("traversalOrder", result.order());
                response.put("traversalLabels", result.orderLabels());
                response.put("steps", result.steps());
                response.put("vertexLabels", graph.getVertexLabels());
                response.put("adjacencyMatrix", graph.getAdjacency());
                response.put("explanation", result.explanation());
                response.put("timeComplexity", "O(V + E)");
                response.put("spaceComplexity", "O(V)");
                break;
            }

            case "dijkstra": {
                Graph graph = buildLearningRoadmapGraph();
                int src = (source != null && source >= 0 && source < graph.getVertices()) ? source : 0;
                int tgt = (target != null && target >= 0 && target < graph.getVertices()) ? target : 6;
                Dijkstra.PathResult result = Dijkstra.shortestPath(graph, src, tgt);

                response.put("algorithm", "Dijkstra's Shortest Path Algorithm");
                response.put("scenario", "Optimal Skill Bridge (Minimum Effort in Learning Weeks)");
                response.put("sourceVertex", src);
                response.put("targetVertex", tgt);
                response.put("sourceLabel", graph.getVertexLabel(src));
                response.put("targetLabel", graph.getVertexLabel(tgt));
                response.put("totalEffortWeeks", result.totalDistance());
                response.put("shortestPath", result.path());
                response.put("pathLabels", result.pathLabels());
                response.put("allDistances", result.allDistances());
                response.put("relaxationLog", result.relaxationLog());
                response.put("vertexLabels", graph.getVertexLabels());
                response.put("adjacencyMatrix", graph.getAdjacency());
                response.put("explanation", result.explanation());
                response.put("timeComplexity", "O(V^2) or O((V + E) log V)");
                response.put("spaceComplexity", "O(V)");
                break;
            }

            case "bipartite": {
                List<Candidate> candidates = List.of(
                        new Candidate("Alice Sharma", List.of("Java", "Spring Boot", "SQL")),
                        new Candidate("Bob Rao", List.of("Python", "Machine Learning", "Statistics")),
                        new Candidate("Charlie Verma", List.of("HTML", "CSS", "JavaScript", "React")),
                        new Candidate("Diana Nair", List.of("AWS", "Linux", "Docker", "CI/CD")),
                        new Candidate("Ethan Gupta", List.of("Java", "React", "SQL", "Spring Boot"))
                );

                List<Job> targetJobs = List.of(
                        findJobById(101).orElse(jobs.get(0)), // Java Developer
                        findJobById(106).orElse(jobs.get(5)), // Machine Learning Engineer
                        findJobById(103).orElse(jobs.get(2)), // Frontend Developer
                        findJobById(112).orElse(jobs.get(11)), // DevOps Engineer
                        findJobById(109).orElse(jobs.get(8))  // Full Stack Developer
                );

                BipartiteMatching matching = new BipartiteMatching(candidates.size(), targetJobs.size());

                // Build bipartite edges based on skill qualification overlap
                List<Map<String, Object>> edgeList = new ArrayList<>();
                for (int c = 0; c < candidates.size(); c++) {
                    Candidate cand = candidates.get(c);
                    for (int j = 0; j < targetJobs.size(); j++) {
                        Job jb = targetJobs.get(j);
                        double sim = JaccardSimilarity.similarity(String.join(", ", cand.skills()), jb.skills());
                        if (sim >= 0.25) { // Compatible qualification edge
                            matching.addEdge(c, j);
                            edgeList.add(Map.of(
                                    "candidateIndex", c,
                                    "candidateName", cand.name(),
                                    "jobIndex", j,
                                    "jobTitle", jb.title(),
                                    "similarity", Math.round(sim * 100.0)
                            ));
                        }
                    }
                }

                int totalMatches = matching.maximumMatching();
                int[] assignments = matching.getMatchedLeftForRight();

                List<BipartiteMatching.MatchPair> matchPairs = new ArrayList<>();
                boolean[] candidateMatched = new boolean[candidates.size()];

                for (int j = 0; j < targetJobs.size(); j++) {
                    int c = assignments[j];
                    if (c != -1) {
                        candidateMatched[c] = true;
                        Candidate cand = candidates.get(c);
                        Job jb = targetJobs.get(j);
                        matchPairs.add(new BipartiteMatching.MatchPair(
                                c, cand.name(), cand.skills(),
                                j, jb.id(), jb.title(), jb.company(), jb.skills()
                        ));
                    }
                }

                List<String> unmatched = new ArrayList<>();
                for (int c = 0; c < candidates.size(); c++) {
                    if (!candidateMatched[c]) {
                        unmatched.add(candidates.get(c).name());
                    }
                }

                response.put("algorithm", "Maximum Bipartite Matching");
                response.put("scenario", "Optimal Candidate-to-Job Role Allocation (Augmenting Paths)");
                response.put("maxMatches", totalMatches);
                response.put("totalCandidates", candidates.size());
                response.put("totalJobs", targetJobs.size());
                response.put("assignments", matchPairs);
                response.put("unmatchedCandidates", unmatched);
                response.put("compatibilityEdges", edgeList);
                response.put("augmentingPathLog", matching.getAugmentingPathLog());
                response.put("candidates", candidates);
                response.put("targetJobs", targetJobs);
                response.put("explanation", "Augmenting Path algorithm maximizes the number of placed candidates by finding alternating augmenting paths and reassigning matching edges.");
                response.put("timeComplexity", "O(V * E)");
                response.put("spaceComplexity", "O(V)");
                break;
            }

            default:
                response.put("error", "Unknown graph demo algorithm: " + type);
        }

        return response;
    }

    private Optional<Job> findJobById(int id) {
        return jobs.stream().filter(j -> j.id() == id).findFirst();
    }

    private record Candidate(String name, List<String> skills) {}

    // Graphs for demos:
    private Graph buildSkillPathwayGraph() {
        Graph g = new Graph(8);
        g.setVertexLabel(0, "Programming Foundations");
        g.setVertexLabel(1, "OOP & Data Structures");
        g.setVertexLabel(2, "Web & REST APIs");
        g.setVertexLabel(3, "Relational Databases & SQL");
        g.setVertexLabel(4, "Spring Boot Backend");
        g.setVertexLabel(5, "Frontend Architecture");
        g.setVertexLabel(6, "Cloud & DevOps (Docker/AWS)");
        g.setVertexLabel(7, "Enterprise Microservices");

        g.addEdge(0, 1);
        g.addEdge(0, 2);
        g.addEdge(1, 3);
        g.addEdge(2, 4);
        g.addEdge(2, 5);
        g.addEdge(3, 4);
        g.addEdge(4, 6);
        g.addEdge(4, 7);
        g.addEdge(6, 7);
        return g;
    }

    private Graph buildSpecializationGraph() {
        Graph g = new Graph(8);
        g.setVertexLabel(0, "Mathematics & Statistics");
        g.setVertexLabel(1, "Data Analysis & SQL");
        g.setVertexLabel(2, "Machine Learning Core");
        g.setVertexLabel(3, "Deep Learning Foundations");
        g.setVertexLabel(4, "Computer Vision (CNN/OpenCV)");
        g.setVertexLabel(5, "NLP & Text Processing");
        g.setVertexLabel(6, "Large Language Models & GenAI");
        g.setVertexLabel(7, "Production MLOps");

        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(3, 5);
        g.addEdge(5, 6);
        g.addEdge(6, 7);
        return g;
    }

    private Graph buildLearningRoadmapGraph() {
        Graph g = new Graph(7);
        g.setVertexLabel(0, "Junior Developer");
        g.setVertexLabel(1, "Backend API Developer");
        g.setVertexLabel(2, "Data Associate");
        g.setVertexLabel(3, "Machine Learning Engineer");
        g.setVertexLabel(4, "Full Stack Engineer");
        g.setVertexLabel(5, "DevOps Specialist");
        g.setVertexLabel(6, "AI & Cloud Architect");

        g.addWeightedUndirectedEdge(0, 1, 4);  // 4 weeks
        g.addWeightedUndirectedEdge(0, 2, 3);  // 3 weeks
        g.addWeightedUndirectedEdge(1, 3, 6);  // 6 weeks
        g.addWeightedUndirectedEdge(2, 3, 5);  // 5 weeks
        g.addWeightedUndirectedEdge(1, 4, 4);  // 4 weeks
        g.addWeightedUndirectedEdge(1, 5, 5);  // 5 weeks
        g.addWeightedUndirectedEdge(4, 5, 3);  // 3 weeks
        g.addWeightedUndirectedEdge(3, 6, 7);  // 7 weeks
        g.addWeightedUndirectedEdge(5, 6, 6);  // 6 weeks
        return g;
    }

    private int extractMaxSalary(Job job) {
        if (job.salary() == null) return 0;
        Matcher m = SALARY_PATTERN.matcher(job.salary());
        if (m.find()) {
            if (m.group(2) != null) {
                return Integer.parseInt(m.group(2));
            }
            return Integer.parseInt(m.group(1));
        }
        return 0;
    }

    private static String categoryFor(String[] fields) {
        String text = String.join(" ", fields).toLowerCase(Locale.ROOT);
        if (CATEGORY_SECURITY.matcher(text).find()) {
            return "Cybersecurity";
        }
        if (CATEGORY_CLOUD.matcher(text).find()) {
            return "Cloud & DevOps";
        }
        if (CATEGORY_DATA.matcher(text).find()) {
            return "Data & AI";
        }
        if (CATEGORY_WEB.matcher(text).find()) {
            return "Web Development";
        }
        return "Software";
    }
}