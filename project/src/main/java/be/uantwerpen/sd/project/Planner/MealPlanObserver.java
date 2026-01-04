package be.uantwerpen.sd.project.Planner;

import be.uantwerpen.sd.project.Recipe.Recipe;

import java.time.DayOfWeek;
import java.util.Map;

/**
 * Observer role for the weekly meal planner. Implementations receive a full
 * immutable snapshot of the current plan whenever something changes.
 * The snapshot maps each day to its planned recipes per slot.
 */
public interface MealPlanObserver {
    /**
     * Called whenever the plan changes. The provided snapshot must not be mutated.
     */
    void onWeekPlanChanged(Map<DayOfWeek, Map<MealSlot, Recipe>> snapshot);
}
