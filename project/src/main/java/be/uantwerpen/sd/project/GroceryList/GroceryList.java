package be.uantwerpen.sd.project.GroceryList;

import be.uantwerpen.sd.project.Planner.MealPlanObserver;
import be.uantwerpen.sd.project.Planner.*;

import java.time.DayOfWeek;
import java.util.*;
import java.util.function.Function;

/**
 * Grocery list that automatically aggregates ingredients for all recipes
 * currently planned in the week plan. Implemented using the Observer pattern.
 */
public class GroceryList implements MealPlanObserver {

    private static GroceryList INSTANCE;

    // Resolves a recipe ID to its list of ingredients
    // Return Optional.empty() if recipe not found
    private Function<UUID, Optional<List<String>>> ingredientResolver = id -> Optional.empty();

    // Aggregated items and their counts
    private final Map<String, Integer> items = new LinkedHashMap<>();

    private GroceryList(){ }

    public static synchronized GroceryList getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GroceryList();
        }
        return INSTANCE;
    }

    /**
     * Provide a resolver that maps recipe IDs to their ingredient lists.
     */
    public void setIngredientResolver(Function<UUID, Optional<List<String>>> resolver) {
        this.ingredientResolver = Objects.requireNonNull(resolver, "resolver");
    }

    /**
     * Observer callback: recompute the grocery items based on the provided snapshot.
     */
    @Override
    public synchronized void onWeekPlanChanged(Map<DayOfWeek, Map<MealSlot, UUID>> snapshot) {
        Map<String, Integer> next = new LinkedHashMap<>();
        if (snapshot != null) {
            for (Map<MealSlot, UUID> day : snapshot.values()) {
                for (UUID recipeId : day.values()) {
                    if (recipeId == null) continue;
                    ingredientResolver.apply(recipeId).ifPresent(list -> {
                        for (String raw : list) {
                            String ing = normalize(raw);
                            if (ing.isEmpty()) continue;
                            next.merge(ing, 1, Integer::sum);
                        }
                    });
                }
            }
        }
        items.clear();
        items.putAll(next);
    }

    private String normalize(String s) {
        if (s == null) return "";
        String t = s.strip();
        // very light normalization; could be extended with unit parsing later
        return t;
    }

    /**
     * Returns an immutable view of the current grocery items with counts.
     */
    public synchronized Map<String, Integer> getItems() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(items));
    }

    /**
     * Convenience: formatted lines suitable for display.
     */
    public synchronized List<String> asDisplayLines() {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, Integer> e : items.entrySet()) {
            int count = e.getValue();
            lines.add(count > 1 ? (e.getKey() + " x" + count) : e.getKey());
        }
        return lines;
    }

    @Override
    public String toString() {
        return "GroceryList{" + items + '}';
    }
}
