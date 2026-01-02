package be.uantwerpen.sd.project.Recipe;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class RecipeTest {

    private RecipeService recipeService;

    @BeforeEach
    void setUp() {
        // Voor elke test starten we met een schone lei
        recipeService = new RecipeService();
    }

    @Test
    void testBuilderCreatesCorrectRecipe() {
        Recipe recipe = new Recipe.Builder("Pasta Carbonara")
                .description("Klassiek recept")
                .addIngredient("Ei")
                .addIngredient("Pancetta")
                .addTag("Italiaans")
                .build();

        assertEquals("Pasta Carbonara", recipe.getTitle());
        assertEquals("Klassiek recept", recipe.getDescription());
        assertTrue(recipe.getIngredients().contains("Ei"));
        assertTrue(recipe.getTags().contains("italiaans")); // Builder zet tags naar lowercase
    }

    @Test
    void testBuilderShouldFailWithoutTitle() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Recipe.Builder(""); // Lege titel mag niet
        });
    }

    @Test
    void testBuilderShouldFailWithoutIngredients() {
        assertThrows(IllegalStateException.class, () -> {
            new Recipe.Builder("Lege Soep").build(); // Geen addIngredient() aangeroepen
        });
    }

    @Test
    void testRecipeIsImmutable() {
        Recipe recipe = new Recipe.Builder("Test")
                .addIngredient("Iets")
                .build();

        // Als iemand probeert de lijst direct aan te passen, moet dit een fout geven
        assertThrows(UnsupportedOperationException.class, () -> {
            recipe.getIngredients().add("Nieuw ingrediënt");
        });
    }

    @Test
    void testRecipeServiceUpdateTitle() {
        // 1. Maak recept
        Recipe oud = recipeService.create("Oude Titel", "Beschrijving", List.of("Ingrediënt"), List.of("Tag"));

        // 2. Update titel
        Optional<Recipe> geüpdatetOpt = recipeService.updateTitle(oud, "Nieuwe Titel");

        // 3. Controles
        assertTrue(geüpdatetOpt.isPresent());
        Recipe nieuw = geüpdatetOpt.get();

        assertEquals("Nieuwe Titel", nieuw.getTitle());
        assertEquals("Beschrijving", nieuw.getDescription()); // Beschrijving moet gelijk blijven

        // Controleer of de service het oude recept echt heeft vervangen in de lijst
        assertEquals(1, recipeService.listAll().size());
        assertEquals("Nieuwe Titel", recipeService.listAll().get(0).getTitle());
    }
}