import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class JobPortal {

    private static class Job {
        private final int id;
        private final String title;
        private final String company;
        private final String location;
        private final String education;
        private final String experience;
        private final String salary;
        private final String skills;
        private final String description;

        Job(int id, String title, String company, String location,
            String education, String experience, String salary,
            String skills, String description) {
            this.id = id;
            this.title = title;
            this.company = company;
            this.location = location;
            this.education = education;
            this.experience = experience;
            this.salary = salary;
            this.skills = skills;
            this.description = description;
        }

        String getSkills() {
            return skills;
        }

        String getSearchText() {
            return title + " " + company + " " + location + " "
                + education + " " + experience + " " + salary + " "
                + skills + " " + description;
        }

        void display() {
            System.out.println("Job ID: " + id);
            System.out.println("Title: " + title);
            System.out.println("Company: " + company);
            System.out.println("Location: " + location);
            System.out.println("Education: " + education);
            System.out.println("Experience: " + experience);
            System.out.println("Salary: " + salary);
            System.out.println("Skills: " + skills);
            System.out.println("Description: " + description);
        }
    }

    private ArrayList<Job> jobs;

    private static class RabinKarp {
        static int search(String text, String pattern) {
            if (text == null || pattern == null) {
                return -1;
            }

            if (pattern.isEmpty()) {
                return 0;
            }

            if (pattern.length() > text.length()) {
                return -1;
            }

            final long base = 911382629L;
            final long mod = 1_000_000_007L;

            long patternHash = 0L;
            long windowHash = 0L;
            long power = 1L;

            for (int i = 0; i < pattern.length(); i++) {
                power = (i == 0) ? 1L : (power * base) % mod;
                patternHash = (patternHash * base + pattern.charAt(i)) % mod;
                windowHash = (windowHash * base + text.charAt(i)) % mod;
            }

            for (int i = 0; i <= text.length() - pattern.length(); i++) {
                if (windowHash == patternHash && text.startsWith(pattern, i)) {
                    return i;
                }

                if (i + pattern.length() < text.length()) {
                    windowHash = (windowHash - text.charAt(i) * power) % mod;
                    if (windowHash < 0) {
                        windowHash += mod;
                    }
                    windowHash = (windowHash * base + text.charAt(i + pattern.length())) % mod;
                }
            }

            return -1;
        }
    }

    private static class ZAlgorithm {
        static int search(String text, String pattern) {
            if (text == null || pattern == null) {
                return -1;
            }

            if (pattern.isEmpty()) {
                return 0;
            }

            if (pattern.length() > text.length()) {
                return -1;
            }

            String combined = pattern + "\u0000" + text;
            int[] z = new int[combined.length()];
            int left = 0;
            int right = 0;

            for (int i = 1; i < combined.length(); i++) {
                if (i <= right) {
                    z[i] = Math.min(right - i + 1, z[i - left]);
                }

                while (i + z[i] < combined.length()
                    && combined.charAt(z[i]) == combined.charAt(i + z[i])) {
                    z[i]++;
                }

                if (i + z[i] - 1 > right) {
                    left = i;
                    right = i + z[i] - 1;
                }

                if (z[i] == pattern.length()) {
                    return i - pattern.length() - 1;
                }
            }

            return -1;
        }
    }

    // Constructor
    public JobPortal() {
        jobs = new ArrayList<>();
        loadJobs();
    }

    // Load jobs from data/jobs.csv
    private void loadJobs() {
        try (BufferedReader reader =
                 new BufferedReader(new FileReader("data/jobs.csv"))) {

            String line;

            // Skip the CSV header
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 9);

                if (data.length == 9) {
                    try {
                        int id = Integer.parseInt(data[0].trim());

                        Job job = new Job(
                            id,
                            data[1].trim(),
                            data[2].trim(),
                            data[3].trim(),
                            data[4].trim(),
                            data[5].trim(),
                            data[6].trim(),
                            data[7].trim(),
                            data[8].trim()
                        );

                        jobs.add(job);

                    } catch (NumberFormatException e) {
                        System.out.println("Skipping invalid job ID.");
                    }
                }
            }

            System.out.println("Jobs loaded successfully: " + jobs.size());

        } catch (IOException e) {
            System.out.println("Could not load data/jobs.csv.");
            System.out.println("Loading sample jobs instead.");
            loadSampleJobs();
        }

        if (jobs.isEmpty()) {
            loadSampleJobs();
        }
    }

    // Load sample jobs if the CSV is unavailable or empty
    private void loadSampleJobs() {
        jobs.add(new Job(
            1, "Java Developer", "Tech Solutions",
            "Hyderabad", "B.Tech CSE", "0-2 years",
            "6-10 LPA", "Java, SQL, Spring Boot",
            "Develop and maintain Java applications"
        ));

        jobs.add(new Job(
            2, "Data Analyst", "DataWorks",
            "Bengaluru", "B.Tech or related degree",
            "0-2 years", "5-8 LPA",
            "Python, SQL, Excel",
            "Analyze data and prepare reports"
        ));

        jobs.add(new Job(
            3, "Web Developer", "WebTech",
            "Hyderabad", "B.Tech CSE", "0-2 years",
            "4-7 LPA", "HTML, CSS, JavaScript",
            "Develop and maintain websites"
        ));
    }

    // Display all available jobs
    public void displayAllJobs() {
        if (jobs.isEmpty()) {
            System.out.println("No jobs available.");
            return;
        }

        System.out.println("\n========== ALL JOBS ==========");

        for (Job job : jobs) {
            job.display();
            System.out.println("------------------------------");
        }

        System.out.println("Total jobs: " + jobs.size());
    }

    // Search jobs using KMP, Rabin-Karp, or Z-Algorithm
    public void searchJobs(String keyword, int method) {
        if (keyword == null || keyword.trim().isEmpty()) {
            System.out.println("Please enter a search keyword.");
            return;
        }

        keyword = keyword.trim();
        int matches = 0;

        System.out.println("\nSearching for: " + keyword);

        for (Job job : jobs) {
            String text = job.getSearchText();
            int position = -1;

            if (method == 1) {
                position = kmpSearch(text, keyword);
            } else if (method == 2) {
                position = RabinKarp.search(text, keyword);
            } else if (method == 3) {
                position = ZAlgorithm.search(text, keyword);
            } else {
                System.out.println("Invalid search method.");
                return;
            }

            if (position != -1) {
                job.display();
                System.out.println("------------------------------");
                matches++;
            }
        }

        if (matches == 0) {
            System.out.println("No matching jobs found.");
        } else {
            System.out.println("Matching jobs found: " + matches);
        }
    }

    private int kmpSearch(String text, String pattern) {
        int[] lps = new int[pattern.length()];
        for (int i = 1, length = 0; i < pattern.length();) {
            if (pattern.charAt(i) == pattern.charAt(length)) {
                lps[i++] = ++length;
            } else if (length > 0) {
                length = lps[length - 1];
            } else {
                lps[i++] = 0;
            }
        }

        for (int i = 0, j = 0; i < text.length();) {
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                if (j == pattern.length()) {
                    return i - j;
                }
            } else if (j > 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }
        return -1;
    }

    // Search for similar skills using Edit Distance
    public void fuzzySearch(String skill, int maxDistance) {
        if (skill == null || skill.trim().isEmpty()) {
            System.out.println("Please enter a skill.");
            return;
        }

        if (maxDistance < 0) {
            System.out.println("Maximum distance cannot be negative.");
            return;
        }

        skill = skill.trim();
        int matches = 0;

        System.out.println("\nFuzzy skill search: " + skill);

        for (Job job : jobs) {
            String jobSkills = job.getSkills();

            // Separate skills using commas or semicolons
            String[] skills = jobSkills.split("[,;|]");

            int minimumDistance = Integer.MAX_VALUE;
            String nearestSkill = "";

            for (String currentSkill : skills) {
                currentSkill = currentSkill.trim();

                if (currentSkill.isEmpty()) {
                    continue;
                }

                int distance = EditDistance.distance(
                    skill,
                    currentSkill
                );

                if (distance < minimumDistance) {
                    minimumDistance = distance;
                    nearestSkill = currentSkill;
                }
            }

            if (minimumDistance <= maxDistance) {
                job.display();

                System.out.println("Closest skill: " + nearestSkill);
                System.out.println("Edit Distance: " + minimumDistance);
                System.out.println("------------------------------");

                matches++;
            }
        }

        if (matches == 0) {
            System.out.println("No similar skills or jobs found.");
        } else {
            System.out.println("Matching jobs found: " + matches);
        }
    }

    // Compare candidate skills with job requirements
    public void compareSkills(String candidateSkills, String jobSkills) {
        double percentage =
            JaccardSimilarity.similarityPercent(
                candidateSkills,
                jobSkills
            );

        System.out.printf(
            "\nCandidate Skills: %s%n", candidateSkills
        );

        System.out.printf(
            "Required Skills: %s%n", jobSkills
        );

        System.out.printf(
            "Skill Similarity: %.2f%%%n", percentage
        );
    }

    // Demonstrate graph algorithms
    public void runGraphDemo(int choice) {
        if (choice == 1) {
            runTraversalDemo();
        } else if (choice == 2) {
            runDijkstraDemo();
        } else if (choice == 3) {
            runMatchingDemo();
        } else {
            System.out.println("Invalid graph option.");
        }
    }

    // BFS and DFS demonstration
    private void runTraversalDemo() {
        Graph graph = new Graph(5);

        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        graph.addEdge(1, 3);
        graph.addEdge(2, 4);

        System.out.println("\n========== GRAPH TRAVERSAL ==========");

        graph.displayGraph();

        BFS.traverse(graph, 0);
        DFS.traverse(graph, 0);
    }

    // Dijkstra shortest-path demonstration
    private void runDijkstraDemo() {
        Graph graph = new Graph(5);

        graph.addWeightedUndirectedEdge(0, 1, 4);
        graph.addWeightedUndirectedEdge(0, 2, 2);
        graph.addWeightedUndirectedEdge(1, 2, 1);
        graph.addWeightedUndirectedEdge(1, 3, 5);
        graph.addWeightedUndirectedEdge(2, 3, 8);
        graph.addWeightedUndirectedEdge(2, 4, 10);
        graph.addWeightedUndirectedEdge(3, 4, 2);

        System.out.println("\n========== DIJKSTRA ALGORITHM ==========");

        Dijkstra.shortestPaths(graph, 0);
    }

    // Bipartite matching demonstration
    private void runMatchingDemo() {
        String[] candidates = {
            "Candidate A",
            "Candidate B",
            "Candidate C"
        };

        String[] jobRoles = {
            "Java Developer",
            "Data Analyst",
            "Web Developer"
        };

        BipartiteMatching matching =
            new BipartiteMatching(candidates.length, jobRoles.length);

        // Candidate A can apply for Java Developer or Data Analyst
        matching.addEdge(0, 0);
        matching.addEdge(0, 1);

        // Candidate B can apply for Data Analyst
        matching.addEdge(1, 1);

        // Candidate C can apply for Web Developer
        matching.addEdge(2, 2);

        System.out.println("\n========== BIPARTITE MATCHING ==========");

        int totalMatches = matching.maximumMatching();

        matching.displayMatches(candidates, jobRoles);

        System.out.println("Maximum number of matches: " + totalMatches);
    }

    private static class Graph {
        private final int vertices;
        private final List<List<Integer>> adjacencyList;
        private final Map<Integer, Map<Integer, Integer>> weightedEdges;

        Graph(int vertices) {
            if (vertices < 0) {
                throw new IllegalArgumentException("Vertex count cannot be negative.");
            }
            this.vertices = vertices;
            this.adjacencyList = new ArrayList<>();
            this.weightedEdges = new HashMap<>();
            for (int i = 0; i < vertices; i++) {
                adjacencyList.add(new ArrayList<>());
                weightedEdges.put(i, new HashMap<>());
            }
        }

        public int getVertices() {
            return vertices;
        }

        public List<Integer> getNeighbors(int vertex) {
            if (vertex < 0 || vertex >= vertices) {
                throw new IndexOutOfBoundsException("Invalid vertex: " + vertex);
            }
            return adjacencyList.get(vertex);
        }

        public void addEdge(int u, int v) {
            if (u < 0 || u >= vertices || v < 0 || v >= vertices) {
                throw new IndexOutOfBoundsException("Invalid edge endpoints.");
            }
            if (!adjacencyList.get(u).contains(v)) {
                adjacencyList.get(u).add(v);
            }
            if (!adjacencyList.get(v).contains(u)) {
                adjacencyList.get(v).add(u);
            }
        }

        public void addWeightedUndirectedEdge(int u, int v, int weight) {
            if (u < 0 || u >= vertices || v < 0 || v >= vertices) {
                throw new IndexOutOfBoundsException("Invalid edge endpoints.");
            }
            adjacencyList.get(u).add(v);
            adjacencyList.get(v).add(u);
            weightedEdges.get(u).put(v, weight);
            weightedEdges.get(v).put(u, weight);
        }

        public int getWeight(int u, int v) {
            if (u < 0 || u >= vertices || v < 0 || v >= vertices) {
                throw new IndexOutOfBoundsException("Invalid vertex.");
            }
            return weightedEdges.getOrDefault(u, Collections.emptyMap()).getOrDefault(v, Integer.MAX_VALUE);
        }

        public void displayGraph() {
            System.out.println("Adjacency List:");
            for (int i = 0; i < vertices; i++) {
                System.out.println(i + " -> " + adjacencyList.get(i));
            }
        }
    }

    private static class BFS {
        static void traverse(Graph graph, int start) {
            if (graph == null || start < 0 || start >= graph.getVertices()) {
                System.out.println("Invalid BFS start vertex.");
                return;
            }

            boolean[] visited = new boolean[graph.getVertices()];
            Queue<Integer> queue = new LinkedList<>();
            visited[start] = true;
            queue.add(start);

            System.out.println("\nBFS traversal from " + start + ":");
            while (!queue.isEmpty()) {
                int vertex = queue.poll();
                System.out.print(vertex + " ");

                for (int neighbor : graph.getNeighbors(vertex)) {
                    if (!visited[neighbor]) {
                        visited[neighbor] = true;
                        queue.add(neighbor);
                    }
                }
            }
            System.out.println();
        }
    }

    private static class DFS {
        static void traverse(Graph graph, int start) {
            if (graph == null || start < 0 || start >= graph.getVertices()) {
                System.out.println("Invalid DFS start vertex.");
                return;
            }

            boolean[] visited = new boolean[graph.getVertices()];
            System.out.println("\nDFS traversal from " + start + ":");
            dfs(graph, start, visited);
            System.out.println();
        }

        private static void dfs(Graph graph, int vertex, boolean[] visited) {
            visited[vertex] = true;
            System.out.print(vertex + " ");

            for (int neighbor : graph.getNeighbors(vertex)) {
                if (!visited[neighbor]) {
                    dfs(graph, neighbor, visited);
                }
            }
        }
    }

    private static class Dijkstra {
        private static class Node {
            int vertex;
            int distance;

            Node(int vertex, int distance) {
                this.vertex = vertex;
                this.distance = distance;
            }
        }

        static void shortestPaths(Graph graph, int source) {
            int vertices = graph.getVertices();
            int[] dist = new int[vertices];
            Arrays.fill(dist, Integer.MAX_VALUE);
            dist[source] = 0;

            PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(node -> node.distance));
            pq.add(new Node(source, 0));

            while (!pq.isEmpty()) {
                Node current = pq.poll();
                int u = current.vertex;

                if (current.distance > dist[u]) {
                    continue;
                }

                for (int v : graph.getNeighbors(u)) {
                    int weight = graph.getWeight(u, v);
                    if (weight == Integer.MAX_VALUE) {
                        continue;
                    }

                    int newDistance = dist[u] + weight;
                    if (newDistance < dist[v]) {
                        dist[v] = newDistance;
                        pq.add(new Node(v, newDistance));
                    }
                }
            }

            System.out.println("Shortest paths from source " + source + ":");
            for (int i = 0; i < vertices; i++) {
                System.out.println("Vertex " + i + ": " + (dist[i] == Integer.MAX_VALUE ? "INF" : dist[i]));
            }
        }
    }

    private static class BipartiteMatching {
        private final int leftSize;
        private final int rightSize;
        private final List<List<Integer>> adjacency;
        private int[] matchRight;

        BipartiteMatching(int leftSize, int rightSize) {
            this.leftSize = leftSize;
            this.rightSize = rightSize;
            this.adjacency = new ArrayList<>();
            for (int i = 0; i < leftSize; i++) {
                adjacency.add(new ArrayList<>());
            }
            this.matchRight = new int[rightSize];
            Arrays.fill(this.matchRight, -1);
        }

        void addEdge(int leftVertex, int rightVertex) {
            if (leftVertex < 0 || leftVertex >= leftSize || rightVertex < 0 || rightVertex >= rightSize) {
                throw new IndexOutOfBoundsException("Invalid bipartite edge.");
            }
            adjacency.get(leftVertex).add(rightVertex);
        }

        int maximumMatching() {
            int matching = 0;
            for (int leftVertex = 0; leftVertex < leftSize; leftVertex++) {
                boolean[] seen = new boolean[rightSize];
                if (findAugmentingPath(leftVertex, seen)) {
                    matching++;
                }
            }
            return matching;
        }

        private boolean findAugmentingPath(int leftVertex, boolean[] seen) {
            for (int rightVertex : adjacency.get(leftVertex)) {
                if (seen[rightVertex]) {
                    continue;
                }
                seen[rightVertex] = true;

                if (matchRight[rightVertex] == -1 || findAugmentingPath(matchRight[rightVertex], seen)) {
                    matchRight[rightVertex] = leftVertex;
                    return true;
                }
            }
            return false;
        }

        void displayMatches(String[] candidates, String[] jobs) {
            System.out.println("Matched candidate-job pairs:");
            for (int i = 0; i < rightSize; i++) {
                if (matchRight[i] != -1) {
                    System.out.println(candidates[matchRight[i]] + " -> " + jobs[i]);
                }
            }
        }
    }

    private static class EditDistance {
        static int distance(String source, String target) {
            if (source == null || target == null) {
                return Integer.MAX_VALUE;
            }

            source = source.trim();
            target = target.trim();

            if (source.isEmpty() && target.isEmpty()) {
                return 0;
            }

            if (source.isEmpty()) {
                return target.length();
            }

            if (target.isEmpty()) {
                return source.length();
            }

            int[][] dp = new int[source.length() + 1][target.length() + 1];

            for (int i = 0; i <= source.length(); i++) {
                dp[i][0] = i;
            }

            for (int j = 0; j <= target.length(); j++) {
                dp[0][j] = j;
            }

            for (int i = 1; i <= source.length(); i++) {
                for (int j = 1; j <= target.length(); j++) {
                    int cost = (source.charAt(i - 1) == target.charAt(j - 1)) ? 0 : 1;
                    dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                    );
                }
            }

            return dp[source.length()][target.length()];
        }
    }

    private static class JaccardSimilarity {
        static double similarityPercent(String first, String second) {
            if (first == null || second == null) {
                return 0.0;
            }

            Set<String> set1 = tokenize(first);
            Set<String> set2 = tokenize(second);

            if (set1.isEmpty() && set2.isEmpty()) {
                return 100.0;
            }

            Set<String> union = new HashSet<>(set1);
            union.addAll(set2);

            if (union.isEmpty()) {
                return 0.0;
            }

            Set<String> intersection = new HashSet<>(set1);
            intersection.retainAll(set2);

            return (intersection.size() * 100.0) / union.size();
        }

        private static Set<String> tokenize(String skills) {
            if (skills == null || skills.trim().isEmpty()) {
                return new HashSet<>();
            }

            String[] tokens = skills.toLowerCase().split("[\\s,;|]+");
            Set<String> set = new HashSet<>();
            for (String token : tokens) {
                if (!token.trim().isEmpty()) {
                    set.add(token.trim());
                }
            }
            return set;
        }
    }
}