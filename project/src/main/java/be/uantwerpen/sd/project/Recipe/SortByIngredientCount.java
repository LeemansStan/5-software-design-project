package be.uantwerpen.sd.project.Recipe;

import java.util.Comparator;
import java.util.List;

/**
 * Recipe sorting strategy that orders recipes by the number of ingredients (fewest first).
 */
public class SortByIngredientCount implements RecipeSortStrategy {
    @Override
    public void sort(List<Recipe> recipes) {
        // Sort by ingredient count ascending (fewest first)
        recipes.sort(Comparator.comparingInt(r -> r.getIngredients().size()));
    }
}