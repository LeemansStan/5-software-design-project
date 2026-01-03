package be.uantwerpen.sd.project.Integration;

import be.uantwerpen.sd.project.GroceryList.GroceryList;
import be.uantwerpen.sd.project.Planner.MealPlanService;
import be.uantwerpen.sd.project.Planner.MealSlot;
import be.uantwerpen.sd.project.Planner.WeekPlan;
import be.uantwerpen.sd.project.Recipe.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test that wires the core design elements together:
 * - WeekPlan/MealPlanService (Subject)
 * - GroceryList (Observer)
 * - Recipe domain objects

 * It validates end-to-end behavior via the observer notifications without
 * directly calling GroceryList's observer method.
 */
public class FullDesignIntegrationTest {

    private MealPlanService mealService;
    private WeekPlan weekPlan;
    private GroceryList groceryList;

    @BeforeEach
    void setUp() {
        mealService = new MealPlanService();
        weekPlan = mealService.getWeekPlan();
        groceryList = GroceryList.getInstance();

        // Ensure GroceryList observes the WeekPlan like in the app wiring
        weekPlan.addObserver(groceryList);

        // Reset GroceryList state using only its public API (as done in unit tests)
        groceryList.onWeekPlanChanged(Collections.emptyMap());
        groceryList.removeItems(new ArrayList<>(groceryList.getItems().keySet()));
        assertTrue(groceryList.getItems().isEmpty(), "Precondition: grocery list should start empty");
    }

    @Test
    void endToEnd_AggregatesAcrossDaysAndSlots_viaObserver() {
        Recipe omelet = new Recipe.Builder("Omelet")
                .description("")
                .ingredients(List.of("Eggs", "Milk", "Bread"))
                .build();
        Recipe pancakes = new Recipe.Builder("Pancakes")
                .description("")
                .ingredients(List.of("Milk", "Butter"))
                .build();
        Recipe cereal = new Recipe.Builder("Cereal")
                .description("")
                .ingredients(List.of("Milk"))
                .build();

        mealService.setRecipe(DayOfWeek.MONDAY, MealSlot.BREAKFAST, omelet);
        mealService.setRecipe(DayOfWeek.MONDAY, MealSlot.DINNER, pancakes);
        mealService.setRecipe(DayOfWeek.TUESDAY, MealSlot.LUNCH, cereal);

        Map<String, Integer> items = groceryList.getItems();
        assertEquals(1, items.getOrDefault("Eggs", 0));
        assertEquals(1, items.getOrDefault("Bread", 0));
        assertEquals(1, items.getOrDefault("Butter", 0));
        assertEquals(3, items.getOrDefault("Milk", 0));
        assertEquals(4, items.size());
    }

    @Test
    void endToEnd_DismissalResetsWhenActiveSlotsChange() {
        // Put one recipe to create an auto item
        Recipe pasta = new Recipe.Builder("Pasta")
                .description("")
                .ingredients(List.of("Milk"))
                .build();
        mealService.setRecipe(DayOfWeek.WEDNESDAY, MealSlot.DINNER, pasta);
        assertTrue(groceryList.getItems().containsKey("Milk"));

        // Dismiss the auto item and ensure it's hidden
        groceryList.dismissItems(List.of("Milk"));
        assertFalse(groceryList.getItems().containsKey("Milk"));

        // Changing the active slots triggers an observer notification in WeekPlan
        Set<MealSlot> nextSlots = EnumSet.of(MealSlot.BREAKFAST, MealSlot.DINNER);
        mealService.setActiveSlots(nextSlots);

        // After plan change, dismissed set should be cleared and item visible again
        assertTrue(groceryList.getItems().containsKey("Milk"));
    }

    @Test
    void endToEnd_ClearingAPlanCellUpdatesGroceryList() {
        Recipe salad = new Recipe.Builder("Salad")
                .description("")
                .ingredients(List.of("Lettuce", "Tomato"))
                .build();

        mealService.setRecipe(DayOfWeek.FRIDAY, MealSlot.LUNCH, salad);
        assertTrue(groceryList.getItems().keySet().containsAll(List.of("Lettuce", "Tomato")));

        // Clear the plan cell and verify grocery list updates accordingly
        mealService.clear(DayOfWeek.FRIDAY, MealSlot.LUNCH);
        Map<String, Integer> items = groceryList.getItems();
        assertFalse(items.containsKey("Lettuce"));
        assertFalse(items.containsKey("Tomato"));
    }
}
