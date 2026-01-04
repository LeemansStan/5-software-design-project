package be.uantwerpen.sd.project.Planner;

import be.uantwerpen.sd.project.Recipe.Recipe;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


/**
 * Model for a single day in the week plan.
 * Holds at most one recipe per {@link MealSlot}.
 */
public class DailyPlan {
    private final Map<MealSlot, Recipe> bySlot = new EnumMap<>(MealSlot.class);

    /** Return the recipe currently planned for the given slot (if any). */
    public Optional<Recipe> get(MealSlot slot) {
        return Optional.ofNullable(bySlot.get(slot));
    }

    /** Set or replace the recipe for the given slot (null allowed via {@link #clear(MealSlot)}). */
    public void set(MealSlot slot, Recipe recipe) {
        Objects.requireNonNull(slot, "slot");
        bySlot.put(slot, recipe);
    }

    /** Remove any recipe assigned to the given slot. */
    public void clear(MealSlot slot) {
        if (slot != null) bySlot.remove(slot);
    }

    /**
     * Return a read-only snapshot view of the day plan.
     * Keys are the slots, values are the currently assigned recipes (nullable in map not exposed).
     */
    public Map<MealSlot, Recipe> asMap() {
        return Map.copyOf(bySlot);
    }
}
