package com.intellijobs.portal;

/**
 * Z-Algorithm for Linear Time Pattern Matching.
 * Constructs the Z-array for string S = pattern + delimiter + text.
 * Z[i] is the length of longest prefix match starting at position i.
 * Runs in O(N + M) time and O(N + M) space.
 */
public class ZAlgorithm {

    /**
     * Searches for pattern in text using Z-Algorithm.
     *
     * @param text    The source string.
     * @param pattern The search pattern.
     * @return 0-based index of first match in text, or -1 if not found.
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

        // Concatenate pattern, delimiter and text
        String combined = lowerPattern + "\u0000" + lowerText;
        int length = combined.length();
        int[] z = calculateZ(combined);

        // Find where Z[i] matches pattern length
        for (int i = m + 1; i < length; i++) {
            if (z[i] >= m) {
                return i - m - 1;
            }
        }

        return -1;
    }

    /**
     * Computes the Z-array for the combined string.
     */
    public static int[] calculateZ(String str) {
        int length = str.length();
        int[] z = new int[length];
        int left = 0;
        int right = 0;

        for (int i = 1; i < length; i++) {
            if (i <= right) {
                z[i] = Math.min(right - i + 1, z[i - left]);
            }

            while (i + z[i] < length && str.charAt(z[i]) == str.charAt(i + z[i])) {
                z[i]++;
            }

            if (i + z[i] - 1 > right) {
                left = i;
                right = i + z[i] - 1;
            }
        }

        return z;
    }

    public static boolean contains(String text, String pattern) {
        return search(text, pattern) != -1;
    }
}
