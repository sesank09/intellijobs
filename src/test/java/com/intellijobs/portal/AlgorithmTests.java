package com.intellijobs.portal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AlgorithmTests {

    // --- 1. KMP TESTS ---
    @Test
    @DisplayName("KMP finds pattern at expected index")
    void testKmpSearch() {
        String text = "Software Engineer at TechCorp";
        assertEquals(9, KMP.search(text, "Engineer"));
        assertEquals(0, KMP.search(text, "software"));
        assertEquals(-1, KMP.search(text, "NonExistent"));
        assertTrue(KMP.contains(text, "techcorp"));
        assertFalse(KMP.contains(text, "finance"));
    }

    @Test
    @DisplayName("KMP LPS table computation is mathematically correct")
    void testKmpLps() {
        int[] lps = KMP.computeLPS("aabaabaa");
        assertArrayEquals(new int[]{0, 1, 0, 1, 2, 3, 4, 5}, lps);
    }

    // --- 2. RABIN-KARP TESTS ---
    @Test
    @DisplayName("Rabin-Karp correctly finds pattern and safely handles collisions")
    void testRabinKarpSearch() {
        String text = "Senior Java Developer with Spring Boot experience";
        assertEquals(7, RabinKarp.search(text, "Java"));
        assertEquals(27, RabinKarp.search(text, "Spring"));
        assertEquals(-1, RabinKarp.search(text, "Golang"));
        assertTrue(RabinKarp.contains(text, "developer"));
        assertFalse(RabinKarp.contains(text, "c++"));
    }

    // --- 3. Z-ALGORITHM TESTS ---
    @Test
    @DisplayName("Z-Algorithm correctly finds pattern using Z-array")
    void testZAlgorithmSearch() {
        String text = "Python and Data Science Machine Learning";
        assertEquals(0, ZAlgorithm.search(text, "Python"));
        assertEquals(24, ZAlgorithm.search(text, "Machine"));
        assertEquals(-1, ZAlgorithm.search(text, "Rust"));
        assertTrue(ZAlgorithm.contains(text, "Data"));
        assertFalse(ZAlgorithm.contains(text, "Ruby"));
    }

    // --- 4. EDIT DISTANCE TESTS ---
    @Test
    @DisplayName("Edit Distance correctly computes Levenshtein Distance via DP")
    void testEditDistance() {
        assertEquals(0, EditDistance.distance("developer", "developer"));
        assertEquals(1, EditDistance.distance("devloper", "developer")); // 1 insertion
        assertEquals(1, EditDistance.distance("python", "pithon"));       // 1 substitution
        assertEquals(1, EditDistance.distance("doker", "docker"));        // 1 insertion ('c')
        assertEquals(3, EditDistance.distance("kitten", "sitting"));
    }

    @Test
    @DisplayName("Edit Distance fuzzy match retrieves approximate keywords")
    void testFuzzyMatch() {
        String text = "Looking for a Python and Django developer";
        assertTrue(EditDistance.fuzzyMatch(text, "pithon", 2));
        assertTrue(EditDistance.fuzzyMatch(text, "djangoo", 1));
        assertFalse(EditDistance.fuzzyMatch(text, "reactjs", 2));
    }

    // --- 5. JACCARD SIMILARITY TESTS ---
    @Test
    @DisplayName("Jaccard Similarity calculates |A ∩ B| / |A ∪ B| accurately")
    void testJaccardSimilarity() {
        String candidate = "Java, SQL, Spring Boot";
        String job = "Java, SQL, Spring Boot";
        assertEquals(1.0, JaccardSimilarity.similarity(candidate, job), 0.001);

        String candidate2 = "Java, SQL";
        String job2 = "Java, Python, Docker";
        // A = {java, sql}, B = {java, python, docker}
        // Intersect = {java} (1), Union = {java, sql, python, docker} (4) -> 1/4 = 0.25
        assertEquals(0.25, JaccardSimilarity.similarity(candidate2, job2), 0.001);

        // Empty cases
        assertEquals(0.0, JaccardSimilarity.similarity("", "Java"), 0.001);
        assertEquals(1.0, JaccardSimilarity.similarity("", ""), 0.001);
    }

    // --- 6. GRAPH BFS & DFS TESTS ---
    @Test
    @DisplayName("BFS visits level-by-level in expected order")
    void testBfsTraversal() {
        Graph g = new Graph(4);
        g.addEdge(0, 1);
        g.addEdge(0, 2);
        g.addEdge(1, 3);

        BFS.TraversalResult result = BFS.traverse(g, 0);
        assertEquals(List.of(0, 1, 2, 3), result.order());
        assertEquals(0, result.levels().get(0));
        assertEquals(1, result.levels().get(1));
        assertEquals(1, result.levels().get(2));
        assertEquals(2, result.levels().get(3));
    }

    @Test
    @DisplayName("DFS visits depth-first in expected order")
    void testDfsTraversal() {
        Graph g = new Graph(4);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(0, 3);

        DFS.TraversalResult result = DFS.traverse(g, 0);
        assertEquals(List.of(0, 1, 2, 3), result.order());
    }

    // --- 7. DIJKSTRA TESTS ---
    @Test
    @DisplayName("Dijkstra finds shortest path and minimum effort correctly")
    void testDijkstraShortestPath() {
        Graph g = new Graph(4);
        g.setVertexLabel(0, "A");
        g.setVertexLabel(1, "B");
        g.setVertexLabel(2, "C");
        g.setVertexLabel(3, "D");

        g.addWeightedUndirectedEdge(0, 1, 2);
        g.addWeightedUndirectedEdge(1, 3, 3);
        g.addWeightedUndirectedEdge(0, 2, 6);
        g.addWeightedUndirectedEdge(2, 3, 1);

        // Path A -> B -> D costs 2 + 3 = 5.
        // Path A -> C -> D costs 6 + 1 = 7.
        Dijkstra.PathResult result = Dijkstra.shortestPath(g, 0, 3);
        assertEquals(5, result.totalDistance());
        assertEquals(List.of(0, 1, 3), result.path());
        assertEquals(List.of("A", "B", "D"), result.pathLabels());
    }

    // --- 8. BIPARTITE MATCHING TESTS ---
    @Test
    @DisplayName("Bipartite Matching finds maximum cardinality matching")
    void testBipartiteMatching() {
        // 3 candidates, 3 jobs
        BipartiteMatching bm = new BipartiteMatching(3, 3);
        bm.addEdge(0, 0); // Candidate 0 can do Job 0
        bm.addEdge(0, 1); // Candidate 0 can do Job 1
        bm.addEdge(1, 1); // Candidate 1 can do Job 1
        bm.addEdge(2, 2); // Candidate 2 can do Job 2

        int maxMatches = bm.maximumMatching();
        assertEquals(3, maxMatches);
        // Candidate 1 gets Job 1, Candidate 0 gets Job 0, Candidate 2 gets Job 2
        int[] assignments = bm.getMatchedLeftForRight();
        assertEquals(0, assignments[0]);
        assertEquals(1, assignments[1]);
        assertEquals(2, assignments[2]);
    }
}
