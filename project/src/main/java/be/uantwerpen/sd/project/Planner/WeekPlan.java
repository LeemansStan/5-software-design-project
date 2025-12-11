package be.uantwerpen.sd.project.Planner;

import java.time.DayOfWeek;
import java.util.*;

/**
 * In-memory model representing a single week plan (Mon–Sun).
 * Keeps the active meal slots and the selected recipe per day/slot.
 */
public class WeekPlan implements MealPlanSubject {
    private final EnumMap<DayOfWeek, DailyPlan> days = new EnumMap<>(DayOfWeek.class);
    private EnumSet<MealSlot> activeSlots = EnumSet.of(MealSlot.BREAKFAST, MealSlot.LUNCH, MealSlot.DINNER, MealSlot.SNACKS);

    private final List<MealPlanObserver> observers = new ArrayList<>();

    public WeekPlan() {
        for (DayOfWeek d : DayOfWeek.values()) {
            days.put(d, new DailyPlan());
        }
    }

    @Override
    public void addObserver(MealPlanObserver observer) {
        if (observer == null) return;
        observers.add(observer);
        // Immediately send a snapshot so observers start in sync
        observer.onWeekPlanChanged(snapshot());
    }

    @Override
    public void removeObserver(MealPlanObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        Map<DayOfWeek, Map<MealSlot, UUID>> snap = snapshot();
        for (MealPlanObserver o : observers) {
            o.onWeekPlanChanged(snap);
        }
    }

    public Set<MealSlot> getActiveSlots() {
        return EnumSet.copyOf(activeSlots);
    }

    public void setActiveSlots(Set<MealSlot> slots) {
        if (slots == null || slots.isEmpty()) {
            throw new IllegalArgumentException("At least one meal slot must be active");
        }
        this.activeSlots = EnumSet.copyOf(slots);
        notifyObservers();
    }

    public DailyPlan getDay(DayOfWeek day) {
        return days.get(Objects.requireNonNull(day, "day"));
    }

    public Optional<UUID> getRecipeId(DayOfWeek day, MealSlot slot) {
        return getDay(day).get(slot);
    }

    public void setRecipe(DayOfWeek day, MealSlot slot, UUID recipeId) {
        getDay(day).set(slot, recipeId);
        notifyObservers();
    }

    public void clear(DayOfWeek day, MealSlot slot) {
        getDay(day).clear(slot);
        notifyObservers();
    }

    public Map<DayOfWeek, Map<MealSlot, UUID>> snapshot() {
        Map<DayOfWeek, Map<MealSlot, UUID>> snap = new EnumMap<>(DayOfWeek.class);
        for (Map.Entry<DayOfWeek, DailyPlan> e : days.entrySet()) {
            snap.put(e.getKey(), e.getValue().asMap());
        }
        return Collections.unmodifiableMap(snap);
    }
}
