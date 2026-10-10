package com.intellijobs.portal;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Jaccard Similarity Algorithm for Candidate-to-Job Skill Matching.
 * Calculates J(A, B) = |A ∩ B| / |A ∪ B|.
 * Handles empty sets gracefully and normalizes multi-word skill phrases.
 */
public class JaccardSimilarity {

    /**
     * Splits and normalizes skills by comma, semicolon, or pipe delimiter.
     * Preserves multi-word skill phrases like "Spring Boot", "Machine Learning", "Data Structures".
     */
    public static Set<String> tokenizeSkills(String skills) {
        if (skills == null || skills.isBlank()) {
            return Collections.emptySet();
        }

        Set<String> set = new LinkedHashSet<>();
        for (String item : skills.split("[,;|]")) {
            String normalized = item.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
            if (!normalized.isEmpty()) {
                set.add(normalized);
            }
        }

        return set;
    }

    /**
     * Calculates Jaccard similarity score between 0.0 and 1.0.
     */
    public static double similarity(String skills1, String skills2) {
        Set<String> set1 = tokenizeSkills(skills1);
        Set<String> set2 = tokenizeSkills(skills2);

        if (set1.isEmpty() && set2.isEmpty()) {
            return 1.0;
        }
        if (set1.isEmpty() || set2.isEmpty()) {
            return 0.0;
        }

        int intersection = 0;
        for (String s : set1) {
            if (set2.contains(s)) {
                intersection++;
            }
        }

        int union = set1.size() + set2.size() - intersection;
        return union == 0 ? 0.0 : (double) intersection / union;
    }

    /**
     * Calculates Jaccard similarity as a percentage (0.0 to 100.0).
     */
    public static double similarityPercent(String skills1, String skills2) {
        return similarity(skills1, skills2) * 100.0;
    }
}
