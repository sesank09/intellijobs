public class BFS {

    public static void traverse(Graph graph, int start) {
        if (graph == null) {
            System.out.println("Graph cannot be null.");
            return;
        }
        traverseInternal(graph.getVertices(), graph.getAdjacency(), start);
    }

    public static void traverse(Object graph, int start) {
        if (graph instanceof Graph g) {
            traverse(g, start);
            return;
        }
        final int n;
        final int[][] adjacency;
        try {
            n = (Integer) graph.getClass().getMethod("getVertices").invoke(graph);
            adjacency = (int[][]) graph.getClass().getMethod("getAdjacency").invoke(graph);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Graph must provide getVertices() and getAdjacency().", e);
        }
        traverseInternal(n, adjacency, start);
    }

    private static void traverseInternal(int n, int[][] adjacency, int start) {

        if (start < 0 || start >= n) {
            System.out.println("Invalid starting vertex.");
            return;
        }

        boolean[] visited = new boolean[n];
        int[] queue = new int[n];

        int front = 0;
        int rear = 0;

        visited[start] = true;
        queue[rear] = start;
        rear++;

        System.out.print("BFS Traversal: ");

        while (front < rear) {

            int current = queue[front];
            front++;

            System.out.print(current + " ");

            for (int i = 0; i < n; i++) {

                if (adjacency[current][i] != 0
                        && !visited[i]) {

                    visited[i] = true;

                    queue[rear] = i;
                    rear++;
                }
            }
        }

        System.out.println();
    }
}