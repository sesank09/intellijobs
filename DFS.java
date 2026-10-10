public class DFS {

    public static void traverse(Graph graph, int start) {
        if (graph == null) {
            System.out.println("Graph cannot be null.");
            return;
        }
        traverse(graph.getAdjacency(), start);
    }

    public static void traverse(int[][] adjacency, int start) {

        int n = adjacency.length;

        if (start < 0 || start >= n) {
            System.out.println("Invalid starting vertex.");
            return;
        }

        boolean[] visited = new boolean[n];

        System.out.print("DFS Traversal: ");

        visit(adjacency, start, visited, n);

        System.out.println();
    }

    private static void visit(
            int[][] adjacency,
            int current,
            boolean[] visited,
            int n) {

        // Mark the current vertex as visited
        visited[current] = true;

        System.out.print(current + " ");

        // Explore each unvisited neighbour
        for (int i = 0; i < n; i++) {

            if (adjacency[current][i] != 0
                    && !visited[i]) {

                visit(adjacency, i, visited, n);
            }
        }
    }
}