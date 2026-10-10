public class Graph {

    private final int vertices;
    private final int[][] adjacency;

    // Create a graph
    public Graph(int vertices) {
        if (vertices < 0) {
            throw new IllegalArgumentException("Number of vertices cannot be negative.");
        }

        this.vertices = vertices;
        adjacency = new int[vertices][vertices];
    }

    // Return number of vertices
    public int getVertices() {
        return vertices;
    }

    // Return adjacency matrix
    public int[][] getAdjacency() {
        return adjacency;
    }

    // Add an undirected edge
    public void addEdge(int source, int destination) {
        checkVertex(source);
        checkVertex(destination);

        adjacency[source][destination] = 1;
        adjacency[destination][source] = 1;
    }

    // Add a directed edge
    public void addDirectedEdge(int source, int destination) {
        checkVertex(source);
        checkVertex(destination);

        adjacency[source][destination] = 1;
    }

    // Add a weighted directed edge
    public void addWeightedDirectedEdge(int source, int destination, int weight) {
        checkVertex(source);
        checkVertex(destination);

        if (weight <= 0) {
            throw new IllegalArgumentException("Weight must be positive.");
        }

        adjacency[source][destination] = weight;
    }

    // Add a weighted undirected edge
    public void addWeightedUndirectedEdge(int source, int destination, int weight) {
        checkVertex(source);
        checkVertex(destination);

        if (weight <= 0) {
            throw new IllegalArgumentException("Weight must be positive.");
        }

        adjacency[source][destination] = weight;
        adjacency[destination][source] = weight;
    }

    // Check whether a vertex is valid
    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= vertices) {
            throw new IllegalArgumentException("Invalid vertex: " + vertex);
        }
    }

    // Display the graph
    public void displayGraph() {
        System.out.println("Adjacency Matrix:");

        for (int i = 0; i < vertices; i++) {
            for (int j = 0; j < vertices; j++) {
                System.out.print(adjacency[i][j] + " ");
            }
            System.out.println();
        }
    }
}