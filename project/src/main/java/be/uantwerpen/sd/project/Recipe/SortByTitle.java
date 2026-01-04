package be.uantwerpen.sd.project.Recipe;

import java.util.Comparator;
import java.util.List;

/**
 * Recipe sorting strategy that orders recipes by title (A–Z), case-insensitive.
 */
public class SortByTitle implements RecipeSortStrategy {
    @Override
    public void sort(List<Recipe> recipes) {
        // Sort by title (A–Z), ignore case
        recipes.sort(Comparator.comparing(Recipe::getTitle, String.CASE_INSENSITIVE_ORDER));
    }
}