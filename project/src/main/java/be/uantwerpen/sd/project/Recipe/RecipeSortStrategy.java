package be.uantwerpen.sd.project.Recipe;

import java.util.List;

/**
 * Strategy interface used by {@link be.uantwerpen.sd.project.Recipe.RecipeService}
 * to sort the in-memory list of recipes without hard-coding a single policy.
 */
public interface RecipeSortStrategy {
    /**
     * Sort the given mutable list in-place.
     * Implementations should define a stable ordering where possible.
     */
    void sort(List<Recipe> recipes);
}

