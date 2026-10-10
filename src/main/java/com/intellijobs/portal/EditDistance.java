package com.intellijobs.portal;

/**
 * Levenshtein Edit Distance using Dynamic Programming.
 * Measures the minimum number of single-character edits (insertions, deletions, or substitutions)
 * required to transform word1 into word2.
 * Used for approximate/fuzzy search on job titles and required skills.
 */
public class EditDistance {

    /**
     * Calculates Levenshtein Distance between two strings.
     * Time Complexity: O(M * N), Space Complexity: O(M * N).
     */
    public static int distance(String word1, String word2) {
        if (word1 == null && word2 == null) {
            return 0;
        }
        if (word1 == null) {
            return word2.length();
        }
        if (word2 == null) {
            return word1.length();
        }

        String s1 = word1.toLowerCase().trim();
        String s2 = word2.toLowerCase().trim();

        int m = s1.length();
        int n = s2.length();

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    int insert = dp[i][j - 1];
                    int delete = dp[i - 1][j];
                    int replace = dp[i - 1][j - 1];
                    dp[i][j] = 1 + Math.min(insert, Math.min(delete, replace));
                }
            }
        }

        return dp[m][n];
    }

    /**
     * Fuzzy matching helper: checks if any token in text is within maxDistance of query,
     * or if the overall query is contained as an approximate substring.
     */
    public static boolean fuzzyMatch(String text, String query, int maxDistance) {
        if (text == null || query == null || query.isBlank()) {
            return false;
        }

        String q = query.trim().toLowerCase();
        String t = text.trim().toLowerCase();

        // Exact substring match always satisfies fuzzy search
        if (t.contains(q)) {
            return true;
        }

        // Split text into tokens (words and skill phrases)
        String[] tokens = t.split("[\\s,;|./()\\-]+");
        int allowed = Math.max(1, maxDistance);

        for (String token : tokens) {
            String clean = token.trim();
            if (clean.isEmpty()) {
                continue;
            }

            // Adaptive distance threshold based on token length
            int tokenThreshold = clean.length() <= 3 ? 1 : allowed;
            if (distance(clean, q) <= tokenThreshold) {
                return true;
            }
        }

        // Also check if entire query is close to any skill substring
        if (q.length() >= 4 && t.length() >= q.length()) {
            for (int i = 0; i <= t.length() - q.length(); i++) {
                String sub = t.substring(i, i + q.length());
                if (distance(sub, q) <= allowed) {
                    return true;
                }
            }
        }

        return false;
    }

    public static double similarity(String s1, String s2) {
        if (s1 == null && s2 == null) return 1.0;
        if (s1 == null || s2 == null) return 0.0;
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;
        int dist = distance(s1, s2);
        return Math.max(0.0, 1.0 - (double) dist / maxLen);
    }
}
