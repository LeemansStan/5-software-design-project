package be.uantwerpen.sd.project.Planner;

public enum MealSlot {
    BREAKFAST("B"),
    LUNCH("L"),
    DINNER("D"),
    SNACKS("S");

    private final String displayName;

    MealSlot(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
