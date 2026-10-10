public class BipartiteMatching {

    private int leftCount;
    private int rightCount;
    private int[][] edges;
    private int[] matchedLeftForRight;
    private boolean[] visitedRight;

    // Constructor
    public BipartiteMatching(int leftCount, int rightCount) {

        if (leftCount < 0 || rightCount < 0) {
            throw new IllegalArgumentException(
                    "Number of candidates and jobs cannot be negative."
            );
        }

        this.leftCount = leftCount;
        this.rightCount = rightCount;

        edges = new int[leftCount][rightCount];
        matchedLeftForRight = new int[rightCount];
    }

    // Connect a candidate to a suitable job
    public void addEdge(int candidate, int job) {

        if (candidate < 0 || candidate >= leftCount
                || job < 0 || job >= rightCount) {

            throw new IllegalArgumentException(
                    "Invalid candidate or job index."
            );
        }

        edges[candidate][job] = 1;
    }

    // Find maximum candidate-job assignments
    public int maximumMatching() {

        for (int i = 0; i < rightCount; i++) {
            matchedLeftForRight[i] = -1;
        }

        int totalMatches = 0;

        for (int candidate = 0; candidate < leftCount; candidate++) {

            visitedRight = new boolean[rightCount];

            if (tryMatch(candidate)) {
                totalMatches++;
            }
        }

        return totalMatches;
    }

    // Find a suitable job for a candidate
    private boolean tryMatch(int candidate) {

        for (int job = 0; job < rightCount; job++) {

            if (edges[candidate][job] == 1
                    && !visitedRight[job]) {

                visitedRight[job] = true;

                if (matchedLeftForRight[job] == -1
                        || tryMatch(matchedLeftForRight[job])) {

                    matchedLeftForRight[job] = candidate;
                    return true;
                }
            }
        }

        return false;
    }

    // Display final assignments
    public void displayMatches(
            String[] candidateNames,
            String[] jobNames) {

        System.out.println("\nCandidate-Job Assignments:");

        for (int job = 0; job < rightCount; job++) {

            if (matchedLeftForRight[job] != -1) {

                int candidate = matchedLeftForRight[job];

                String candidateName =
                        candidateNames != null
                        && candidate < candidateNames.length
                        ? candidateNames[candidate]
                        : "Candidate " + candidate;

                String jobName =
                        jobNames != null && job < jobNames.length
                        ? jobNames[job]
                        : "Job " + job;

                System.out.println(
                        candidateName + " -> " + jobName
                );
            }
        }
    }
}