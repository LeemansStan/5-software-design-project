package be.uantwerpen.sd.project.Recipe;

import java.util.Comparator;
import java.util.List;

public class SortByTitle implements RecipeSortStrategy {
    @Override
    public void sort(List<Recipe> recipes) {
        // Sorteer op titel (A-Z), negeer hoofdletters
        recipes.sort(Comparator.comparing(Recipe::getTitle, String.CASE_INSENSITIVE_ORDER));
    }
}