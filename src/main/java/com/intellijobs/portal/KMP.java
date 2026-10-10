package com.intellijobs.portal;

/**
 * Knuth-Morris-Pratt (KMP) Pattern Matching Algorithm.
 * Precomputes the Longest Proper Prefix which is also Suffix (LPS) array.
 * Achieves O(N + M) worst-case time complexity.
 */
public class KMP {

    /**
     * Searches for pattern in text using KMP.
     *
     * @param text    The source string to search within.
     * @param pattern The keyword or pattern to locate.
     * @return 0-based index of the first character match, or -1 if not found.
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

        int[] lps = computeLPS(lowerPattern);

        int i = 0; // index for text
        int j = 0; // index for pattern

        while (i < n) {
            if (lowerText.charAt(i) == lowerPattern.charAt(j)) {
                i++;
                j++;
            }

            if (j == m) {
                return i - j;
            } else if (i < n && lowerText.charAt(i) != lowerPattern.charAt(j)) {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }

        return -1;
    }

    /**
     * Precomputes the LPS (Longest Prefix Suffix) table for the given pattern.
     */
    public static int[] computeLPS(String pattern) {
        int m = pattern.length();
        int[] lps = new int[m];
        int length = 0;
        int i = 1;

        lps[0] = 0;

        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(length)) {
                length++;
                lps[i] = length;
                i++;
            } else {
                if (length != 0) {
                    length = lps[length - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }

        return lps;
    }

    public static boolean contains(String text, String pattern) {
        return search(text, pattern) != -1;
    }
}
