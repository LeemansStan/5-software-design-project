package be.uantwerpen.sd.project.Recipe;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Service that manages all recipes in-memory.
 *
 * Design notes:
 * - Uses the Recipe.Builder to enforce immutability when creating new instances.
 * - Update operations create a new Recipe and replace the old reference in the list.
 * - Provides simple searching and sorting using the Strategy pattern.
 */
public class RecipeService {
    private final List<Recipe> all = new ArrayList<>();

    // CREATE: Gebruikt nu de Builder
    public Recipe create(String title, String description, List<String> ingredients, Collection<String> tags) {
        Recipe r = new Recipe.Builder(title)
                .description(description)
                .ingredients(ingredients)
                .tags(tags)
                .build();
        all.add(r);
        return r;
    }

    public void add(Recipe recipe) {
        Objects.requireNonNull(recipe, "recipe");
        all.add(recipe);
    }

    // READ
    public List<Recipe> listAll() {
        return new ArrayList<>(all);
    }

    public List<Recipe> searchByTitle(String query) {
        if (query == null || query.isBlank()) return listAll();
        String q = query.toLowerCase(Locale.ROOT);
        return filter(r -> r.getTitle().toLowerCase(Locale.ROOT).contains(q));
    }

    public List<Recipe> searchByTag(String tag) {
        if (tag == null || tag.isBlank()) return Collections.emptyList();
        String t = tag.toLowerCase(Locale.ROOT);
        return filter(r -> r.getTags().contains(t));
    }

    private List<Recipe> filter(Predicate<Recipe> predicate) {
        return all.stream().filter(predicate).collect(Collectors.toList());
    }

    // UPDATE: Omdat Recipe onveranderlijk is, maken we steeds een nieuwe versie
    public Optional<Recipe> updateTitle(Recipe old, String newTitle) {
        if (old == null || !all.contains(old)) return Optional.empty();
        Recipe updated = new Recipe.Builder(newTitle)
                .description(old.getDescription())
                .ingredients(old.getIngredients())
                .tags(old.getTags())
                .build();
        return replace(old, updated);
    }

    public Optional<Recipe> updateDescription(Recipe old, String newDescription) {
        if (old == null || !all.contains(old)) return Optional.empty();
        Recipe updated = new Recipe.Builder(old.getTitle())
                .description(newDescription)
                .ingredients(old.getIngredients())
                .tags(old.getTags())
                .build();
        return replace(old, updated);
    }

    /**
     * Hulpmethode om een oud object te vervangen door een nieuwe versie.
     * Nodig voor de ViewApp om 'Callback Hell' te voorkomen.
     */
    public Optional<Recipe> replace(Recipe oldRecipe, Recipe newRecipe) {
        int index = all.indexOf(oldRecipe);
        if (index >= 0) {
            all.set(index, newRecipe);
            return Optional.of(newRecipe);
        }
        return Optional.empty();
    }

    // STRATEGY PATTERN: Sorteren
    public void sortRecipes(RecipeSortStrategy strategy) {
        if (strategy == null) return;
        strategy.sort(all);
    }

    // DELETE
    public boolean remove(Recipe recipe) {
        return all.remove(recipe);
    }

    public void clear() {
        all.clear();
    }
}