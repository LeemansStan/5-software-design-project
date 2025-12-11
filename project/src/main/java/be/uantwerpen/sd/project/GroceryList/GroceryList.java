package be.uantwerpen.sd.project.GroceryList;

import be.uantwerpen.sd.project.Planner.MealPlanObserver;
import be.uantwerpen.sd.project.Planner.*;
import be.uantwerpen.sd.project.Recipe.Recipe;

import java.time.DayOfWeek;
import java.util.*;

/**
 * Grocery list that automatically aggregates ingredients for all recipes
 * currently planned in the week plan. Implemented using the Observer pattern.
 */
public class GroceryList implements MealPlanObserver {

    private static GroceryList INSTANCE;

    // Aggregated items and their counts
    private final Map<String, Integer> items = new LinkedHashMap<>();

    // User-dismissed (checked/removed) items that should be hidden until plan changes
    private final Set<String> dismissed = new LinkedHashSet<>();

    private GroceryList(){ }

    public static synchronized GroceryList getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GroceryList();
        }
        return INSTANCE;
    }

    @Override
    public synchronized void onWeekPlanChanged(Map<DayOfWeek, Map<MealSlot, Recipe>> snapshot) {
        // When the plan changes, reset dismissed selections; the UI can re-dismiss as needed
        dismissed.clear();
        Map<String, Integer> next = new LinkedHashMap<>();
        if (snapshot != null) {
            for (Map<MealSlot, Recipe> day : snapshot.values()) {
                for (Recipe recipe : day.values()) {
                    if (recipe == null) continue;
                    List<String> list = recipe.getIngredients();
                    for (String raw : list) {
                        String ing = normalize(raw);
                        if (ing.isEmpty()) continue;
                        if (dismissed.contains(ing)) continue; // hide dismissed items until user resets
                        next.merge(ing, 1, Integer::sum);
                    }
                }
            }
        }
        items.clear();
        items.putAll(next);
    }

    private String normalize(String s) {
        if (s == null) return "";
        String t = s.strip();
        return t;
    }

    /**
     * Returns an immutable view of the current grocery items with counts.
     */
    public synchronized Map<String, Integer> getItems() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(items));
    }


    /**
     * Mark provided ingredient names as dismissed, so they are hidden from the list.
     * Also removes them from the current in-memory items view.
     */
    public synchronized void dismissItems(Collection<String> names) {
        if (names == null || names.isEmpty()) return;
        for (String n : names) {
            if (n == null) continue;
            String key = normalize(n);
            if (!key.isEmpty()) {
                dismissed.add(key);
            }
        }
        // Remove from current list immediately
        items.keySet().removeAll(dismissed);
    }


    @Override
    public String toString() {
        return "GroceryList{" + items + '}';
    }
}
