public class ZAlgorithm {

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

        // Combine pattern, separator, and text
        String combined = pattern + "\u0000" + text;

        int length = combined.length();
        int[] z = new int[length];

        int left = 0;
        int right = 0;

        // Calculate the Z-array
        for (int i = 1; i < length; i++) {

            if (i <= right) {
                z[i] = Math.min(right - i + 1, z[i - left]);
            }

            while (i + z[i] < length &&
                   combined.charAt(z[i]) ==
                   combined.charAt(i + z[i])) {

                z[i]++;
            }

            if (i + z[i] - 1 > right) {
                left = i;
                right = i + z[i] - 1;
            }
        }

        // Find the pattern inside the original text
        for (int i = m + 1; i < length; i++) {

            if (z[i] >= m) {
                return i - m - 1;
            }
        }

        return -1;
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