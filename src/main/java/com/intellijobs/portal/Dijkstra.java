package com.intellijobs.portal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dijkstra's Shortest Path Algorithm for Non-Negative Weighted Graphs.
 * In the Job Portal, calculates the minimum learning effort (in weeks/difficulty)
 * needed to bridge between a candidate's current skillset and target senior roles.
 * Time Complexity: O(V^2), Space Complexity: O(V).
 */
public class Dijkstra {

    public static final int INF = 1000000000;

    public record PathResult(
            int source,
            int target,
            int totalDistance,
            List<Integer> path,
            List<String> pathLabels,
            Map<Integer, Integer> allDistances,
            List<String> relaxationLog,
            String explanation
    ) {}

    public static PathResult shortestPath(Graph graph, int source, int target) {
        int n = graph.getVertices();
        if (source < 0 || source >= n || target < 0 || target >= n) {
            throw new IllegalArgumentException("Invalid source or target vertex.");
        }

        int[][] adj = graph.getAdjacency();
        int[] dist = new int[n];
        int[] parent = new int[n];
        boolean[] visited = new boolean[n];
        List<String> relaxationLog = new ArrayList<>();

        Arrays.fill(dist, INF);
        Arrays.fill(parent, -1);
        dist[source] = 0;

        for (int count = 0; count < n; count++) {
            int u = -1;
            for (int i = 0; i < n; i++) {
                if (!visited[i] && (u == -1 || dist[i] < dist[u])) {
                    u = i;
                }
            }

            if (u == -1 || dist[u] == INF) {
                break;
            }
            visited[u] = true;

            for (int v = 0; v < n; v++) {
                int weight = adj[u][v];
                if (!visited[v] && weight > 0 && dist[u] + weight < dist[v]) {
                    int oldDist = dist[v];
                    dist[v] = dist[u] + weight;
                    parent[v] = u;
                    relaxationLog.add(String.format("Relaxed edge (%s -> %s): updated distance from %s to %d weeks (weight: %d)",
                            graph.getVertexLabel(u), graph.getVertexLabel(v),
                            oldDist == INF ? "INF" : String.valueOf(oldDist), dist[v], weight));
                }
            }
        }

        // Reconstruct shortest path from source to target
        List<Integer> path = new ArrayList<>();
        List<String> pathLabels = new ArrayList<>();
        if (dist[target] != INF) {
            int curr = target;
            while (curr != -1) {
                path.add(0, curr);
                curr = parent[curr];
            }
            for (int node : path) {
                pathLabels.add(graph.getVertexLabel(node));
            }
        }

        Map<Integer, Integer> allDistances = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            allDistances.put(i, dist[i] == INF ? -1 : dist[i]);
        }

        String explanation = dist[target] == INF
                ? String.format("Target role '%s' is not reachable from '%s'.", graph.getVertexLabel(target), graph.getVertexLabel(source))
                : String.format("Dijkstra discovered optimal learning bridge from '%s' to '%s' with minimum total effort of %d weeks across %d milestones.",
                        graph.getVertexLabel(source), graph.getVertexLabel(target), dist[target], path.size());

        return new PathResult(source, target, dist[target] == INF ? -1 : dist[target], path, pathLabels, allDistances, relaxationLog, explanation);
    }
}
