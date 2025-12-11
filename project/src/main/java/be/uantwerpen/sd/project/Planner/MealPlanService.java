package be.uantwerpen.sd.project.Planner;

import java.time.DayOfWeek;
import java.util.*;


public class MealPlanService {
    private final WeekPlan weekPlan = new WeekPlan();

    public WeekPlan getWeekPlan() { return weekPlan; }

    public Set<MealSlot> getActiveSlots() { return weekPlan.getActiveSlots(); }

    public void setActiveSlots(Set<MealSlot> slots) {
        if (slots == null || slots.isEmpty()) {
            throw new IllegalArgumentException("At least one active slot is required");
        }
        weekPlan.setActiveSlots(slots);
    }

    public Optional<UUID> getRecipeId(DayOfWeek day, MealSlot slot) {
        return weekPlan.getRecipeId(day, slot);
    }

    public void setRecipe(DayOfWeek day, MealSlot slot, UUID recipeId) {
        Objects.requireNonNull(day, "day");
        Objects.requireNonNull(slot, "slot");
        weekPlan.setRecipe(day, slot, recipeId);
    }

    public void clear(DayOfWeek day, MealSlot slot) {
        weekPlan.clear(day, slot);
    }
}
