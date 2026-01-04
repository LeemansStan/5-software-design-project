package be.uantwerpen.sd.project.Planner;

/**
 * Subject role in the Observer pattern for the weekly meal planner.
 * Implementations notify observers whenever the plan (active slots or
 * scheduled recipes) changes.
 */
public interface MealPlanSubject {
    /** Register a new observer; implementations may push an initial snapshot. */
    void addObserver(MealPlanObserver observer);
    /** Unregister a previously added observer. */
    void removeObserver(MealPlanObserver observer);
}
