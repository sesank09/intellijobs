import java.util.HashSet;
import java.util.Set;

public class JaccardSimilarity {

    public static double similarity(String skills1, String skills2) {

        if (skills1 == null) {
            skills1 = "";
        }

        if (skills2 == null) {
            skills2 = "";
        }

        // Convert skills into lowercase words
        String[] words1 = skills1.toLowerCase().trim().split("[\\s,;]+");
        String[] words2 = skills2.toLowerCase().trim().split("[\\s,;]+");

        Set<String> set1 = new HashSet<>();
        Set<String> set2 = new HashSet<>();

        // Store unique skills
        for (String word : words1) {
            if (!word.isEmpty()) {
                set1.add(word);
            }
        }

        for (String word : words2) {
            if (!word.isEmpty()) {
                set2.add(word);
            }
        }

        // Handle empty skill lists
        if (set1.isEmpty() && set2.isEmpty()) {
            return 1.0;
        }

        if (set1.isEmpty() || set2.isEmpty()) {
            return 0.0;
        }

        // Count common skills
        int intersection = 0;

        for (String skill : set1) {
            if (set2.contains(skill)) {
                intersection++;
            }
        }

        // Find total unique skills
        int union = set1.size() + set2.size() - intersection;

        // Calculate Jaccard similarity
        return (double) intersection / union;
    }

    public static double similarityPercent(String skills1, String skills2) {
        return similarity(skills1, skills2) * 100;
    }

    public static void main(String[] args) {

        String candidateSkills = "Java, SQL, HTML, Python";
        String jobSkills = "Java, SQL, Python, CSS";

        double result = similarityPercent(candidateSkills, jobSkills);

        System.out.printf("Skill Similarity: %.2f%%%n", result);
    }
}