package be.uantwerpen.sd.project.Planner;

import be.uantwerpen.sd.project.Recipe.Recipe;

import java.time.DayOfWeek;
import java.util.*;

/**
 * In-memory model representing a single week plan (Mon–Sun).
 * Keeps the active meal slots and the selected recipe per day/slot.
 *
 * Observer semantics:
 * - Observers receive a full immutable snapshot on every change (active slots, set/clear recipe).
 * - Adding an observer pushes an initial snapshot so UIs start in sync.
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
        Map<DayOfWeek, Map<MealSlot, Recipe>> snap = snapshot();
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

    public Optional<Recipe> getRecipe(DayOfWeek day, MealSlot slot) {
        return getDay(day).get(slot);
    }

    public void setRecipe(DayOfWeek day, MealSlot slot, Recipe recipe) {
        getDay(day).set(slot, recipe);
        notifyObservers();
    }

    public void clear(DayOfWeek day, MealSlot slot) {
        getDay(day).clear(slot);
        notifyObservers();
    }

    /**
     * Replace all references of a specific Recipe instance that are currently
     * planned anywhere in the week with another Recipe instance. Observers are
     * notified once after all replacements are applied.
     */
    public void replaceRecipeReferences(Recipe oldRecipe, Recipe newRecipe) {
        if (oldRecipe == null || newRecipe == null || oldRecipe == newRecipe) return;
        boolean changed = false;
        for (DayOfWeek d : DayOfWeek.values()) {
            DailyPlan dp = getDay(d);
            for (MealSlot slot : MealSlot.values()) {
                Optional<Recipe> maybe = dp.get(slot);
                if (maybe.isPresent() && maybe.get() == oldRecipe) {
                    dp.set(slot, newRecipe); // direct set, no notify here
                    changed = true;
                }
            }
        }
        if (changed) notifyObservers();
    }

    public Map<DayOfWeek, Map<MealSlot, Recipe>> snapshot() {
        Map<DayOfWeek, Map<MealSlot, Recipe>> snap = new EnumMap<>(DayOfWeek.class);
        for (Map.Entry<DayOfWeek, DailyPlan> e : days.entrySet()) {
            snap.put(e.getKey(), e.getValue().asMap());
        }
        return Collections.unmodifiableMap(snap);
    }
}
