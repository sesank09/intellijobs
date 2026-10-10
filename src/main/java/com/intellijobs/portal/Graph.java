package com.intellijobs.portal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adjacency Matrix Graph Representation supporting unweighted, weighted, directed,
 * and undirected edges with descriptive vertex labels.
 */
public class Graph {

    private final int vertices;
    private final int[][] adjacency;
    private final Map<Integer, String> vertexLabels;

    public Graph(int vertices) {
        if (vertices < 0) {
            throw new IllegalArgumentException("Number of vertices cannot be negative.");
        }
        this.vertices = vertices;
        this.adjacency = new int[vertices][vertices];
        this.vertexLabels = new HashMap<>();
        for (int i = 0; i < vertices; i++) {
            vertexLabels.put(i, "Vertex " + i);
        }
    }

    public int getVertices() {
        return vertices;
    }

    public int[][] getAdjacency() {
        return adjacency;
    }

    public void setVertexLabel(int vertex, String label) {
        checkVertex(vertex);
        vertexLabels.put(vertex, label);
    }

    public String getVertexLabel(int vertex) {
        checkVertex(vertex);
        return vertexLabels.getOrDefault(vertex, "Vertex " + vertex);
    }

    public Map<Integer, String> getVertexLabels() {
        return Collections.unmodifiableMap(vertexLabels);
    }

    public void addEdge(int source, int destination) {
        checkVertex(source);
        checkVertex(destination);
        adjacency[source][destination] = 1;
        adjacency[destination][source] = 1;
    }

    public void addDirectedEdge(int source, int destination) {
        checkVertex(source);
        checkVertex(destination);
        adjacency[source][destination] = 1;
    }

    public void addWeightedUndirectedEdge(int source, int destination, int weight) {
        checkVertex(source);
        checkVertex(destination);
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight must be positive.");
        }
        adjacency[source][destination] = weight;
        adjacency[destination][source] = weight;
    }

    public void addWeightedDirectedEdge(int source, int destination, int weight) {
        checkVertex(source);
        checkVertex(destination);
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight must be positive.");
        }
        adjacency[source][destination] = weight;
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= vertices) {
            throw new IllegalArgumentException("Invalid vertex: " + vertex);
        }
    }

    public List<Integer> getNeighbors(int vertex) {
        checkVertex(vertex);
        List<Integer> neighbors = new ArrayList<>();
        for (int i = 0; i < vertices; i++) {
            if (adjacency[vertex][i] > 0) {
                neighbors.add(i);
            }
        }
        return neighbors;
    }
}
