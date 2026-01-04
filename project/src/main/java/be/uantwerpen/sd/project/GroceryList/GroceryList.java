package be.uantwerpen.sd.project.GroceryList;

import be.uantwerpen.sd.project.Planner.MealPlanObserver;
import be.uantwerpen.sd.project.Planner.*;
import be.uantwerpen.sd.project.Recipe.Recipe;

import java.time.DayOfWeek;
import java.util.*;

/**
 * Grocery list that automatically aggregates ingredients for all recipes
 * currently planned in the week plan. Implemented using the Observer pattern.
 *
 * Behavior notes:
 * - Manual items are user-managed and always visible until explicitly removed.
 * - Auto items (from the plan) can be dismissed in a quantity-aware way: when
 *   you "refresh" (remove) current auto items, their present quantities are
 *   recorded as a baseline. If you plan the same recipe again later, only the
 *   new quantities beyond the baseline will show up again (fresh count).
 */
public class GroceryList implements MealPlanObserver {

    private static GroceryList INSTANCE;

    // Aggregated auto items (from planner) and their counts
    private final Map<String, Integer> items = new LinkedHashMap<>();

    // Manually added items and their quantities
    private final Map<String, Integer> manualItems = new LinkedHashMap<>();

    // Quantity-based dismissals: how many units of an auto item have been dismissed (baseline)
    private final Map<String, Integer> dismissedCounts = new LinkedHashMap<>();

    private GroceryList(){ }

    public static synchronized GroceryList getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GroceryList();
        }
        return INSTANCE;
    }

    @Override
    public synchronized void onWeekPlanChanged(Map<DayOfWeek, Map<MealSlot, Recipe>> snapshot) {
        // Re-aggregate auto items from snapshot
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
        // Keep only dismissals that still exist; clamp dismissed to available auto count
        dismissedCounts.keySet().retainAll(next.keySet());
        for (Map.Entry<String, Integer> e : new ArrayList<>(dismissedCounts.entrySet())) {
            String k = e.getKey();
            int dismissed = Math.max(0, e.getValue() == null ? 0 : e.getValue());
            int auto = Math.max(0, next.getOrDefault(k, 0));
            if (auto <= 0 || dismissed <= 0) {
                if (auto <= 0) dismissedCounts.remove(k);
                else dismissedCounts.put(k, 0);
            } else if (dismissed > auto) {
                dismissedCounts.put(k, auto);
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
     * Merges manual items with auto-aggregated items, and subtracts dismissed
     * baseline quantities from auto items. Never shows negative values.
     */
    public synchronized Map<String, Integer> getItems() {
        LinkedHashMap<String, Integer> merged = new LinkedHashMap<>();
        // Manual items first
        for (Map.Entry<String, Integer> e : manualItems.entrySet()) {
            merged.put(e.getKey(), Math.max(1, e.getValue() == null ? 1 : e.getValue()));
        }
        // Auto items with quantity-based dismissals
        for (Map.Entry<String, Integer> e : items.entrySet()) {
            String k = e.getKey();
            int auto = Math.max(0, e.getValue() == null ? 0 : e.getValue());
            int dismissed = Math.max(0, dismissedCounts.getOrDefault(k, 0));
            int visible = Math.max(0, auto - dismissed);
            if (visible <= 0) continue; // fully covered by baseline
            merged.merge(k, visible, Integer::sum);
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
     * Otherwise it is considered an auto item and its current auto quantity is recorded
     * as dismissed (baseline). Future increases beyond the baseline will show up again.
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
                int auto = Math.max(0, items.getOrDefault(key, 0));
                if (auto > 0) {
                    // Set baseline to at least current auto count
                    int prev = Math.max(0, dismissedCounts.getOrDefault(key, 0));
                    dismissedCounts.put(key, Math.max(prev, auto));
                }
            }
        }
        // Immediate effect: getItems() reflects new baseline.
    }

    /**
     * Backward compatible: dismiss auto items only (manual items unaffected).
     * Works like remove for auto items, recording the current auto quantity as baseline.
     */
    public synchronized void dismissItems(Collection<String> names) {
        if (names == null || names.isEmpty()) return;
        for (String n : names) {
            if (n == null) continue;
            String key = normalize(n);
            if (!key.isEmpty()) {
                int auto = Math.max(0, items.getOrDefault(key, 0));
                if (auto > 0) {
                    int prev = Math.max(0, dismissedCounts.getOrDefault(key, 0));
                    dismissedCounts.put(key, Math.max(prev, auto));
                }
            }
        }
    }

    /**
     * Convenience API: treat all current auto items as completed by setting the
     * baseline to the current auto aggregation. Manual items remain unchanged.
     */
    public synchronized void setBaselineToCurrentAuto() {
        for (Map.Entry<String, Integer> e : items.entrySet()) {
            String k = e.getKey();
            int auto = Math.max(0, e.getValue() == null ? 0 : e.getValue());
            if (auto <= 0) continue;
            int prev = Math.max(0, dismissedCounts.getOrDefault(k, 0));
            dismissedCounts.put(k, Math.max(prev, auto));
        }
        // Also purge dismissals for non-existing keys (safety)
        dismissedCounts.keySet().retainAll(items.keySet());
    }

    @Override
    public String toString() {
        return "GroceryList{" + getItems() + '}';
    }
}
