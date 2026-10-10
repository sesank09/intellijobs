package com.intellijobs.portal;

import java.util.List;

public record SkillMatch(
        Job job,
        double score,
        List<String> matchedSkills,
        List<String> missingSkills,
        double percentage,
        String explanation,
        String formula) {

    public SkillMatch(Job job, double score, List<String> matchedSkills, List<String> missingSkills) {
        this(
                job,
                score,
                matchedSkills,
                missingSkills,
                Math.round(score * 1000.0) / 10.0,
                String.format("%d of %d required skills matched (%.1f%% overlap)",
                        matchedSkills.size(),
                        matchedSkills.size() + missingSkills.size(),
                        score * 100.0),
                String.format("J(A, B) = |%d| / |%d| = %.2f",
                        matchedSkills.size(),
                        matchedSkills.size() + missingSkills.size(),
                        score)
        );
    }
}