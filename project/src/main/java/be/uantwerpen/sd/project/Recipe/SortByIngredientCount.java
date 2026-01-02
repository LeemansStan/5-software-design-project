package be.uantwerpen.sd.project.Recipe;

import java.util.Comparator;
import java.util.List;

public class SortByIngredientCount implements RecipeSortStrategy {
    @Override
    public void sort(List<Recipe> recipes) {
        // Sorteer op aantal ingrediënten (minste eerst)
        recipes.sort(Comparator.comparingInt(r -> r.getIngredients().size()));
    }
}