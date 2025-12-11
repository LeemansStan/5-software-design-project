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

    // Aggregated items (from planner) and their counts
    private final Map<String, Integer> items = new LinkedHashMap<>();

    // Manually added items and their quantities
    private final Map<String, Integer> manualItems = new LinkedHashMap<>();

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
     * Merges manual items with auto-aggregated items, and hides dismissed auto items.
     */
    public synchronized Map<String, Integer> getItems() {
        LinkedHashMap<String, Integer> merged = new LinkedHashMap<>();
        // Manual items first
        for (Map.Entry<String, Integer> e : manualItems.entrySet()) {
            merged.put(e.getKey(), Math.max(1, e.getValue() == null ? 1 : e.getValue()));
        }
        // Auto items, skipping dismissed, summing counts when overlapping
        for (Map.Entry<String, Integer> e : items.entrySet()) {
            String k = e.getKey();
            if (dismissed.contains(k)) continue;
            int v = Math.max(1, e.getValue() == null ? 1 : e.getValue());
            merged.merge(k, v, Integer::sum);
        }
        return Collections.unmodifiableMap(merged);
    }

    /**
     * Add or increase a manually added item with the given quantity (>=1).
     */
    public synchronized void addManualItem(String name, int quantity) {
        String key = normalize(name);
        if (key.isEmpty()) return;
        int qty = Math.max(1, quantity);
        manualItems.merge(key, qty, Integer::sum);
    }

    /**
     * Remove items from the list. If an item exists in manual items, it is deleted there.
     * Otherwise it is considered an auto item and will be dismissed until the plan changes.
     */
    public synchronized void removeItems(Collection<String> names) {
        if (names == null || names.isEmpty()) return;
        for (String n : names) {
            if (n == null) continue;
            String key = normalize(n);
            if (key.isEmpty()) continue;
            if (manualItems.containsKey(key)) {
                manualItems.remove(key);
            } else {
                dismissed.add(key);
            }
        }
        // Immediate effect: nothing else needed; getItems() respects dismissed and manual state.
    }

    /**
     * Backward compatible: dismiss auto items only (manual items unaffected).
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
    }

    @Override
    public String toString() {
        return "GroceryList{" + getItems() + '}';
    }
}
