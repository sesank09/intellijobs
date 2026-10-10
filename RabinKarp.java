public class RabinKarp {

    private static final int PRIME = 101;
    private static final int BASE = 256;

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

        long patternHash = 0;
        long textHash = 0;
        long h = 1;

        // Calculate BASE^(m-1) % PRIME
        for (int i = 0; i < m - 1; i++) {
            h = (h * BASE) % PRIME;
        }

        // Calculate initial hash values
        for (int i = 0; i < m; i++) {
            patternHash =
                (BASE * patternHash + pattern.charAt(i)) % PRIME;

            textHash =
                (BASE * textHash + text.charAt(i)) % PRIME;
        }

        // Slide the pattern over the text
        for (int i = 0; i <= n - m; i++) {

            // If hash values match, compare actual characters
            if (patternHash == textHash) {

                boolean found = true;

                for (int j = 0; j < m; j++) {
                    if (text.charAt(i + j) != pattern.charAt(j)) {
                        found = false;
                        break;
                    }
                }

                if (found) {
                    return i;
                }
            }

            // Calculate hash for the next window
            if (i < n - m) {

                textHash =
                    (textHash - text.charAt(i) * h) % PRIME;

                textHash =
                    (textHash * BASE + text.charAt(i + m)) % PRIME;

                // Keep hash non-negative
                if (textHash < 0) {
                    textHash += PRIME;
                }
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