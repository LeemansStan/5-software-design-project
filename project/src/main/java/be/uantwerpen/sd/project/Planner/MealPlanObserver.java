package be.uantwerpen.sd.project.Planner;

import java.time.DayOfWeek;
import java.util.Map;
import java.util.UUID;

/**
 * Observer for changes in the weekly meal plan.
 * Implementations will be notified with an immutable snapshot of the current plan.
 */
public interface MealPlanObserver {
    void onWeekPlanChanged(Map<DayOfWeek, Map<MealSlot, UUID>> snapshot);
}
