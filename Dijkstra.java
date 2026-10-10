public class Dijkstra {

    private static final int INF = 1000000000;

    public static void shortestPaths(Graph graph, int source) {

        int n = graph.getVertices();

        if (source < 0 || source >= n) {
            System.out.println("Invalid source vertex.");
            return;
        }

        int[][] adjacency = graph.getAdjacency();

        int[] distance = new int[n];
        boolean[] visited = new boolean[n];

        // Initialize distances
        for (int i = 0; i < n; i++) {
            distance[i] = INF;
        }

        distance[source] = 0;

        // Find shortest paths
        for (int count = 0; count < n; count++) {

            int current = -1;

            // Find the unvisited vertex with the smallest distance
            for (int i = 0; i < n; i++) {

                if (!visited[i]
                        && distance[i] != INF
                        && (current == -1
                        || distance[i] < distance[current])) {

                    current = i;
                }
            }

            // Stop when no reachable vertex remains
            if (current == -1) {
                break;
            }

            visited[current] = true;

            // Update distances to neighbouring vertices
            for (int next = 0; next < n; next++) {

                int weight = adjacency[current][next];

                if (!visited[next] && weight > 0
                        && distance[current] + weight < distance[next]) {

                    distance[next] =
                            distance[current] + weight;
                }
            }
        }

        // Display shortest distances
        System.out.println(
                "\nShortest distances from vertex " + source + ":"
        );

        for (int i = 0; i < n; i++) {

            if (distance[i] == INF) {

                System.out.println(
                        source + " -> " + i + " : Unreachable"
                );

            } else {

                System.out.println(
                        source + " -> " + i + " : " + distance[i]
                );
            }
        }
    }
}