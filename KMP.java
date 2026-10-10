public class KMP {

    // Search for a pattern inside a text
    public static int search(String text, String pattern) {

        if (text == null || pattern == null) {
            return -1;
        }

        text = text.toLowerCase();
        pattern = pattern.toLowerCase();

        int n = text.length();
        int m = pattern.length();

        if (m == 0) {
            return 0;
        }

        if (m > n) {
            return -1;
        }

        int[] lps = new int[m];

        // Create the LPS array
        computeLPS(pattern, lps);

        int i = 0;
        int j = 0;

        // Search for the pattern
        while (i < n) {

            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
            }

            if (j == m) {
                return i - j;
            }

            else if (i < n && text.charAt(i) != pattern.charAt(j)) {

                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }

        return -1;
    }

    // Calculate the LPS array
    private static void computeLPS(String pattern, int[] lps) {

        int length = 0;
        int i = 1;

        lps[0] = 0;

        while (i < pattern.length()) {

            if (pattern.charAt(i) == pattern.charAt(length)) {
                length++;
                lps[i] = length;
                i++;
            }

            else {

                if (length != 0) {
                    length = lps[length - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
    }

    // Test the algorithm
    public static void main(String[] args) {

        String text = "Java Developer Required";
        String pattern = "Developer";

        int result = search(text, pattern);

        if (result != -1) {
            System.out.println("Pattern found at index: " + result);
        } else {
            System.out.println("Pattern not found.");
        }
    }
}
