package com.intellijobs.portal;

import java.util.ArrayList;
import java.util.List;

/**
 * Depth-First Search (DFS) Traversal.
 * Explores deeply along branches before backtracking using recursive call stack.
 * Time Complexity: O(V^2) with adjacency matrix, O(V + E) with adjacency list.
 * Space Complexity: O(V).
 */
public class DFS {

    public record Step(int vertex, String label, int depth, String action) {}

    public record TraversalResult(
            List<Integer> order,
            List<String> orderLabels,
            List<Step> steps,
            String explanation
    ) {}

    public static TraversalResult traverse(Graph graph, int start) {
        int n = graph.getVertices();
        if (start < 0 || start >= n) {
            throw new IllegalArgumentException("Invalid start vertex: " + start);
        }

        boolean[] visited = new boolean[n];
        List<Integer> order = new ArrayList<>();
        List<String> orderLabels = new ArrayList<>();
        List<Step> steps = new ArrayList<>();

        dfsRecursive(graph, start, visited, order, orderLabels, steps, 0);

        String explanation = "Depth-First Search (DFS) explores vertical specialization paths to their maximum depth "
                + "before backtracking. In recruitment, this models deep vertical technical specialization pathways.";

        return new TraversalResult(order, orderLabels, steps, explanation);
    }

    private static void dfsRecursive(
            Graph graph, int current, boolean[] visited,
            List<Integer> order, List<String> orderLabels,
            List<Step> steps, int depth) {

        visited[current] = true;
        order.add(current);
        orderLabels.add(graph.getVertexLabel(current));
        steps.add(new Step(current, graph.getVertexLabel(current), depth, "VISIT"));

        int[][] adj = graph.getAdjacency();
        for (int neighbor = 0; neighbor < graph.getVertices(); neighbor++) {
            if (adj[current][neighbor] != 0 && !visited[neighbor]) {
                dfsRecursive(graph, neighbor, visited, order, orderLabels, steps, depth + 1);
                steps.add(new Step(current, graph.getVertexLabel(current), depth, "BACKTRACK_TO"));
            }
        }
    }
}
