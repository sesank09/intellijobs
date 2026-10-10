package com.intellijobs.portal;

/**
 * Immutable Job entity record representing a career opportunity.
 */
public record Job(
        int id,
        String title,
        String company,
        String location,
        String qualification,
        String experience,
        String salary,
        String skills,
        String description,
        String category) {

    public String searchText() {
        return (title + " " + company + " " + location + " " + qualification + " " + experience + " " + salary + " " + skills + " " + description + " " + category).toLowerCase();
    }

    public String getEmploymentType() {
        if (experience != null && experience.toLowerCase().contains("intern")) {
            return "Internship";
        }
        return "Full-time";
    }

    public String getApplyUrl() {
        return "https://careers." + company.toLowerCase().replaceAll("[^a-z0-9]", "") + ".com/jobs/" + id;
    }
}