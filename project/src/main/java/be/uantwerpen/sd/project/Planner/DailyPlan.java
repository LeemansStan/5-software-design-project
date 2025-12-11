package be.uantwerpen.sd.project.Planner;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;


public class DailyPlan {
    private final Map<MealSlot, UUID> bySlot = new EnumMap<>(MealSlot.class);

    public Optional<UUID> get(MealSlot slot) {
        return Optional.ofNullable(bySlot.get(slot));
    }

    public void set(MealSlot slot, UUID recipeId) {
        Objects.requireNonNull(slot, "slot");
        bySlot.put(slot, recipeId);
    }

    public void clear(MealSlot slot) {
        if (slot != null) bySlot.remove(slot);
    }

    public Map<MealSlot, UUID> asMap() {
        return Map.copyOf(bySlot);
    }
}
