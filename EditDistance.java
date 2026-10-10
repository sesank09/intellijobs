public class EditDistance {

    public static int distance(String word1, String word2) {

        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        int m = word1.length();
        int n = word2.length();

        int[][] dp = new int[m + 1][n + 1];

        // Initialize the first column
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }

        // Initialize the first row
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }

        // Calculate edit distance
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {

                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    int insert = dp[i][j - 1];
                    int delete = dp[i - 1][j];
                    int replace = dp[i - 1][j - 1];

                    dp[i][j] = 1 + Math.min(
                        insert,
                        Math.min(delete, replace)
                    );
                }
            }
        }

        return dp[m][n];
    }

    public static void main(String[] args) {

        String word1 = "Developer";
        String word2 = "Devloper";

        int result = distance(word1, word2);

        System.out.println("Word 1: " + word1);
        System.out.println("Word 2: " + word2);
        System.out.println("Edit Distance: " + result);
    }
}
