package com.intellijobs.portal;

/**
 * Rabin-Karp Rolling Hash Pattern Matching Algorithm.
 * Computes hash values in O(1) per sliding window.
 * Performs explicit character verification upon hash match to safely resolve collisions.
 */
public class RabinKarp {

    private static final int PRIME = 1000000007;
    private static final int BASE = 256;

    /**
     * Searches for pattern in text using Rabin-Karp rolling hash.
     *
     * @param text    The source string.
     * @param pattern The search pattern.
     * @return 0-based index of first match, or -1 if not found.
     */
    public static int search(String text, String pattern) {
        if (text == null || pattern == null) {
            return -1;
        }

        String lowerText = text.toLowerCase();
        String lowerPattern = pattern.toLowerCase();

        int n = lowerText.length();
        int m = lowerPattern.length();

        if (m == 0) {
            return 0;
        }
        if (m > n) {
            return -1;
        }

        long patternHash = 0;
        long textHash = 0;
        long h = 1;

        // Calculate BASE^(m-1) % PRIME
        for (int i = 0; i < m - 1; i++) {
            h = (h * BASE) % PRIME;
        }

        // Calculate initial hash values for pattern and first window
        for (int i = 0; i < m; i++) {
            patternHash = (BASE * patternHash + lowerPattern.charAt(i)) % PRIME;
            textHash = (BASE * textHash + lowerText.charAt(i)) % PRIME;
        }

        // Slide window over text
        for (int i = 0; i <= n - m; i++) {
            // Check character match if hash matches (to handle hash collision)
            if (patternHash == textHash) {
                boolean match = true;
                for (int j = 0; j < m; j++) {
                    if (lowerText.charAt(i + j) != lowerPattern.charAt(j)) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return i;
                }
            }

            // Compute rolling hash for next window
            if (i < n - m) {
                textHash = (textHash - lowerText.charAt(i) * h) % PRIME;
                textHash = (textHash * BASE + lowerText.charAt(i + m)) % PRIME;
                if (textHash < 0) {
                    textHash += PRIME;
                }
            }
        }

        return -1;
    }

    public static boolean contains(String text, String pattern) {
        return search(text, pattern) != -1;
    }
}
