package com.intellijobs.portal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Maximum Bipartite Matching using Augmenting Paths (DFS-based Ford-Fulkerson).
 * Used to optimally match candidates to available job roles based on qualifications
 * and skill compatibility.
 * Time Complexity: O(V * E), Space Complexity: O(V).
 */
public class BipartiteMatching {

    public record MatchPair(
            int candidateIndex,
            String candidateName,
            List<String> candidateSkills,
            int jobIndex,
            int jobId,
            String jobTitle,
            String company,
            String jobSkills
    ) {}

    public record MatchingResult(
            int maxMatches,
            int totalCandidates,
            int totalJobs,
            List<MatchPair> assignments,
            List<String> unmatchedCandidates,
            List<String> augmentingPathLog,
            String explanation
    ) {}

    private final int leftCount;
    private final int rightCount;
    private final int[][] edges;
    private final int[] matchedLeftForRight;
    private final boolean[] visitedRight;
    private final List<String> augmentingPathLog;

    public BipartiteMatching(int leftCount, int rightCount) {
        if (leftCount < 0 || rightCount < 0) {
            throw new IllegalArgumentException("Candidate and job counts cannot be negative.");
        }
        this.leftCount = leftCount;
        this.rightCount = rightCount;
        this.edges = new int[leftCount][rightCount];
        this.matchedLeftForRight = new int[rightCount];
        this.visitedRight = new boolean[rightCount];
        this.augmentingPathLog = new ArrayList<>();
    }

    public void addEdge(int candidate, int job) {
        if (candidate < 0 || candidate >= leftCount || job < 0 || job >= rightCount) {
            throw new IllegalArgumentException("Invalid candidate or job index.");
        }
        edges[candidate][job] = 1;
    }

    public int maximumMatching() {
        Arrays.fill(matchedLeftForRight, -1);
        int totalMatches = 0;

        for (int candidate = 0; candidate < leftCount; candidate++) {
            Arrays.fill(visitedRight, false);
            if (tryMatch(candidate, candidate)) {
                totalMatches++;
            }
        }

        return totalMatches;
    }

    private boolean tryMatch(int candidate, int origin) {
        for (int job = 0; job < rightCount; job++) {
            if (edges[candidate][job] == 1 && !visitedRight[job]) {
                visitedRight[job] = true;

                if (matchedLeftForRight[job] == -1) {
                    matchedLeftForRight[job] = candidate;
                    augmentingPathLog.add(String.format("Augmenting Path: Assigned Candidate #%d to Job #%d", candidate, job));
                    return true;
                } else if (tryMatch(matchedLeftForRight[job], origin)) {
                    augmentingPathLog.add(String.format("Reassigned Job #%d to Candidate #%d after finding alternative for previous holder", job, candidate));
                    matchedLeftForRight[job] = candidate;
                    return true;
                }
            }
        }
        return false;
    }

    public int[] getMatchedLeftForRight() {
        return matchedLeftForRight;
    }

    public List<String> getAugmentingPathLog() {
        return augmentingPathLog;
    }
}
