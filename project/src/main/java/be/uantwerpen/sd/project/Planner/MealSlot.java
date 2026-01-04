package be.uantwerpen.sd.project.Planner;

/**
 * Enumeration of the four meal slots that can appear in a weekly plan.
 * Each slot has a short display label used by the UI.
 */
public enum MealSlot {
    BREAKFAST("B"),
    LUNCH("L"),
    DINNER("D"),
    SNACKS("S");

    private final String displayName;

    MealSlot(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Short label shown in the planner grid header.
     */
    public String getDisplayName() {
        return displayName;
    }
}
