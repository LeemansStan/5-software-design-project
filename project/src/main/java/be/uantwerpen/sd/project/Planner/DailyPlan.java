package be.uantwerpen.sd.project.Planner;

import be.uantwerpen.sd.project.Recipe.Recipe;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


public class DailyPlan {
    private final Map<MealSlot, Recipe> bySlot = new EnumMap<>(MealSlot.class);

    public Optional<Recipe> get(MealSlot slot) {
        return Optional.ofNullable(bySlot.get(slot));
    }

    public void set(MealSlot slot, Recipe recipe) {
        Objects.requireNonNull(slot, "slot");
        bySlot.put(slot, recipe);
    }

    public void clear(MealSlot slot) {
        if (slot != null) bySlot.remove(slot);
    }

    public Map<MealSlot, Recipe> asMap() {
        return Map.copyOf(bySlot);
    }
}
