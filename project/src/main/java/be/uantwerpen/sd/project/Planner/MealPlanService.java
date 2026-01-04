package be.uantwerpen.sd.project.Planner;

import be.uantwerpen.sd.project.Recipe.Recipe;

import java.time.DayOfWeek;
import java.util.*;

/**
 * Application-facing service that wraps the in-memory {@link WeekPlan} model.
 *
 * Responsibilities:
 * - Exposes a small API to read/write the weekly plan used by the UI.
 * - Guards business rules (e.g., recipe meal-tag must match the selected slot).
 * - Delegates observer notifications to the underlying model.
 */
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

    public Optional<Recipe> getRecipe(DayOfWeek day, MealSlot slot) {
        return weekPlan.getRecipe(day, slot);
    }

    /**
     * Assign a recipe to a specific day/slot in the week plan.
     * Applies a business rule: if the recipe has meal-type tags
     * (breakfast/lunch/dinner/snack(s)), the slot must match.
     */
    public void setRecipe(DayOfWeek day, MealSlot slot, Recipe recipe) {
        Objects.requireNonNull(day, "day");
        Objects.requireNonNull(slot, "slot");
        if (recipe != null) {
            enforceSlotTagCompatibility(slot, recipe);
        }
        weekPlan.setRecipe(day, slot, recipe);
    }

    /**
     * Guard that enforces slot compatibility based on meal-type tags.
     * If the recipe contains any of the tags: breakfast, lunch, dinner, snack(s),
     * it can only be placed in the matching slot. Recipes without these tags
     * can be planned in any slot.
     *
     * Throws IllegalArgumentException if the placement is invalid.
     */
    private void enforceSlotTagCompatibility(MealSlot slot, Recipe recipe) {
        Set<String> tags = recipe.getTags();
        if (tags == null || tags.isEmpty()) return; // no restriction
        // normalize
        boolean hasBreakfast = tags.contains("breakfast");
        boolean hasLunch = tags.contains("lunch");
        boolean hasDinner = tags.contains("dinner");
        boolean hasSnack = tags.contains("snack") || tags.contains("snacks");

        // If any of the meal-type tags are present, enforce matching slot for each present tag
        if (hasBreakfast && slot != MealSlot.BREAKFAST) {
            throw new IllegalArgumentException("Recipe tagged for breakfast can only be planned in the Breakfast slot");
        }
        if (hasLunch && slot != MealSlot.LUNCH) {
            throw new IllegalArgumentException("Recipe tagged for lunch can only be planned in the Lunch slot");
        }
        if (hasDinner && slot != MealSlot.DINNER) {
            throw new IllegalArgumentException("Recipe tagged for dinner can only be planned in the Dinner slot");
        }
        if (hasSnack && slot != MealSlot.SNACKS) {
            throw new IllegalArgumentException("Recipe tagged for snack can only be planned in the Snacks slot");
        }
    }

    public void clear(DayOfWeek day, MealSlot slot) {
        weekPlan.clear(day, slot);
    }

    /**
     * Replace all occurrences of the given recipe currently planned in the week with the new recipe.
     * Useful after updating a recipe (immutability creates a new instance) so the planner stays in sync.
     */
    public void replaceRecipeReferences(Recipe oldRecipe, Recipe newRecipe) {
        weekPlan.replaceRecipeReferences(oldRecipe, newRecipe);
    }
}
