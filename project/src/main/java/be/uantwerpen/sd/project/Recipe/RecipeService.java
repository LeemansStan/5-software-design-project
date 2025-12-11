package be.uantwerpen.sd.project.Recipe;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Simplified service combining responsibilities of previous RecipeController and RecipeManager.
 * Acts as the single entry point for the View to interact with Recipe data.
 */
public class RecipeService {
    private final List<Recipe> all = new ArrayList<>();

    // CREATE
    public Recipe create(String title, String description, List<String> ingredients, Collection<String> tags) {
        Recipe r = new Recipe(title, description, ingredients, tags);
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

    // UPDATE (returns updated instance)
    public Optional<Recipe> updateTitle(Recipe recipe, String newTitle) {
        return mutate(recipe, r -> r.setTitle(newTitle));
    }

    public Optional<Recipe> updateDescription(Recipe recipe, String newDescription) {
        return mutate(recipe, r -> r.setDescription(newDescription));
    }

    public Optional<Recipe> updateIngredients(Recipe recipe, List<String> newIngredients) {
        return mutate(recipe, r -> r.setIngredients(newIngredients));
    }

    public Optional<Recipe> updateTags(Recipe recipe, Collection<String> newTags) {
        return mutate(recipe, r -> r.setTags(newTags));
    }

    private Optional<Recipe> mutate(Recipe recipe, java.util.function.Consumer<Recipe> mutator) {
        if (recipe == null) return Optional.empty();
        // Ensure it is the same instance we manage; if not present, refuse to mutate
        if (!all.contains(recipe)) return Optional.empty();
        mutator.accept(recipe);
        return Optional.of(recipe);
    }

    // DELETE
    public boolean remove(Recipe recipe) {
        return all.remove(recipe);
    }

    public void clear() {
        all.clear();
    }
}
