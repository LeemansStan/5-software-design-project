package be.uantwerpen.sd.project.Recipe;

import java.util.*;

public class Recipe {
    private final String title;
    private final String description;
    private final List<String> ingredients;
    private final Set<String> tags;

    // Private constructor: wordt alleen door de Builder aangeroepen
    private Recipe(Builder builder) {
        this.title = builder.title;
        this.description = builder.description;
        this.ingredients = Collections.unmodifiableList(new ArrayList<>(builder.ingredients));
        this.tags = Collections.unmodifiableSet(new LinkedHashSet<>(builder.tags));
    }

    public static class Builder {
        private String title;
        private String description = "";
        private List<String> ingredients = new ArrayList<>();
        private Set<String> tags = new LinkedHashSet<>();

        public Builder(String title) {
            if (title == null || title.isBlank()) {
                throw new IllegalArgumentException("Title is required");
            }
            this.title = title.trim();
        }

        public Builder description(String description) {
            this.description = description == null ? "" : description.strip();
            return this;
        }

        public Builder addIngredient(String ingredient) {
            if (ingredient != null && !ingredient.isBlank()) {
                this.ingredients.add(ingredient.strip());
            }
            return this;
        }

        // Handig: in één keer een hele lijst toevoegen
        public Builder ingredients(List<String> ingredients) {
            if (ingredients != null) {
                ingredients.forEach(this::addIngredient);
            }
            return this;
        }

        public Builder addTag(String tag) {
            if (tag != null && !tag.isBlank()) {
                this.tags.add(tag.strip().toLowerCase(Locale.ROOT));
            }
            return this;
        }

        public Builder tags(Collection<String> tags) {
            if (tags != null) {
                tags.forEach(this::addTag);
            }
            return this;
        }

        public Recipe build() {
            if (ingredients.isEmpty()) {
                throw new IllegalStateException("Recipe must have at least one ingredient");
            }
            return new Recipe(this);
        }
    }

    // Alleen GETTERS, geen SETTERS (Immutability)
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<String> getIngredients() { return ingredients; }
    public Set<String> getTags() { return tags; }

    @Override
    public String toString() {
        return "Recipe{" +
                "title='" + title + '\'' +
                ", ingredients=" + ingredients +
                '}';
    }
}