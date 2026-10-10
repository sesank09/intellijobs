package com.intellijobs.portal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Breadth-First Search (BFS) Traversal.
 * Visits vertices level-by-level using a FIFO Queue.
 * Time Complexity: O(V^2) with adjacency matrix, O(V + E) with adjacency list.
 * Space Complexity: O(V).
 */
public class BFS {

    public record Step(int current, String label, int level, List<Integer> queueState) {}

    public record TraversalResult(
            List<Integer> order,
            List<String> orderLabels,
            Map<Integer, Integer> levels,
            List<Step> steps,
            String explanation
    ) {}

    public static TraversalResult traverse(Graph graph, int start) {
        int n = graph.getVertices();
        if (start < 0 || start >= n) {
            throw new IllegalArgumentException("Invalid start vertex: " + start);
        }

        int[][] adj = graph.getAdjacency();
        boolean[] visited = new boolean[n];
        Queue<Integer> queue = new ArrayDeque<>();
        List<Integer> order = new ArrayList<>();
        List<String> orderLabels = new ArrayList<>();
        Map<Integer, Integer> levels = new LinkedHashMap<>();
        List<Step> steps = new ArrayList<>();

        visited[start] = true;
        levels.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            int current = queue.poll();
            order.add(current);
            orderLabels.add(graph.getVertexLabel(current));
            int currentLevel = levels.get(current);

            for (int neighbor = 0; neighbor < n; neighbor++) {
                if (adj[current][neighbor] != 0 && !visited[neighbor]) {
                    visited[neighbor] = true;
                    levels.put(neighbor, currentLevel + 1);
                    queue.add(neighbor);
                }
            }
            steps.add(new Step(current, graph.getVertexLabel(current), currentLevel, new ArrayList<>(queue)));
        }

        String explanation = "Breadth-First Search (BFS) explores all neighboring skill competencies at level k "
                + "before proceeding to level k+1. In career pathways, this models foundational breadth-first curriculum progression.";

        return new TraversalResult(order, orderLabels, levels, steps, explanation);
    }
}
