package be.uantwerpen.sd.project.Recipe;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Simplified service combining responsibilities of previous RecipeController and RecipeManager.
 * Acts as the single entry point for the View to interact with Recipe data.
 */
public class RecipeService {
    private final Map<UUID, Recipe> byId = new LinkedHashMap<>();

    // CREATE
    public Recipe create(String title, String description, List<String> ingredients, Collection<String> tags) {
        Recipe r = new Recipe(title, description, ingredients, tags);
        byId.put(r.getId(), r);
        return r;
    }

    public void add(Recipe recipe) {
        Objects.requireNonNull(recipe, "recipe");
        byId.put(recipe.getId(), recipe);
    }

    // READ
    public List<Recipe> listAll() {
        return new ArrayList<>(byId.values());
    }

    public Optional<Recipe> findById(UUID id) {
        return Optional.ofNullable(byId.get(id));
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
        return byId.values().stream().filter(predicate).collect(Collectors.toList());
    }

    // UPDATE (returns updated instance)
    public Optional<Recipe> updateTitle(UUID id, String newTitle) {
        return mutate(id, r -> r.setTitle(newTitle));
    }

    public Optional<Recipe> updateDescription(UUID id, String newDescription) {
        return mutate(id, r -> r.setDescription(newDescription));
    }

    public Optional<Recipe> updateIngredients(UUID id, List<String> newIngredients) {
        return mutate(id, r -> r.setIngredients(newIngredients));
    }

    public Optional<Recipe> updateTags(UUID id, Collection<String> newTags) {
        return mutate(id, r -> r.setTags(newTags));
    }

    private Optional<Recipe> mutate(UUID id, java.util.function.Consumer<Recipe> mutator) {
        Recipe r = byId.get(id);
        if (r == null) return Optional.empty();
        mutator.accept(r);
        return Optional.of(r);
    }

    // DELETE
    public boolean remove(UUID id) {
        return byId.remove(id) != null;
    }

    public void clear() {
        byId.clear();
    }
}
