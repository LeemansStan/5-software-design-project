package be.uantwerpen.sd.project.Recipe;

import java.util.*;


public class Recipe {
    private String title;
    private String description;
    private List<String> ingredients;
    private Set<String> tags;

    public Recipe(String title, String description, List<String> ingredients, Collection<String> tags) {
        validateTitle(title);
        this.title = title.trim();
        this.description = description == null ? "" : description.strip();
        this.ingredients = sanitizeIngredients(ingredients);
        this.tags = sanitizeTags(tags);
    }

    private static void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("title is required");
        }
    }

    private static List<String> sanitizeIngredients(List<String> ingredients) {
        if (ingredients == null) throw new IllegalArgumentException("ingredients cannot be null");
        List<String> cleaned = new ArrayList<>();
        for (String s : ingredients) {
            if (s == null) continue;
            String c = s.strip();
            if (!c.isEmpty()) cleaned.add(c);
        }
        if (cleaned.isEmpty()) throw new IllegalArgumentException("ingredients cannot be empty");
        return Collections.unmodifiableList(cleaned);
    }

    private static Set<String> sanitizeTags(Collection<String> tags) {
        if (tags == null) return Collections.emptySet();
        Set<String> cleaned = new LinkedHashSet<>();
        for (String t : tags) {
            if (t == null) continue;
            String c = t.strip();
            if (!c.isEmpty()) cleaned.add(c.toLowerCase(Locale.ROOT));
        }
        return Collections.unmodifiableSet(cleaned);
    }

// getters en setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        validateTitle(title);
        this.title = title.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description.strip();
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = sanitizeIngredients(ingredients);
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Collection<String> tags) {
        this.tags = sanitizeTags(tags);
    }


    @Override
    public String toString() {
        return "Recipe{" +
                "title='" + title + '\'' +
                ", description='" + (description.length() > 60 ? description.substring(0, 57) + "..." : description) + '\'' +
                ", ingredients=" + ingredients +
                ", tags=" + tags +
                '}';
    }
}
