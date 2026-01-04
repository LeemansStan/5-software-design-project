package be.uantwerpen.sd.project.GroceryList;

import be.uantwerpen.sd.project.Planner.MealSlot;
import be.uantwerpen.sd.project.Recipe.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GroceryListTest {

    private GroceryList gl;

    @BeforeEach
    void setUp() {
        gl = GroceryList.getInstance();
        // Full reset of observable state using only public API
        // 1) Recompute with an empty plan (also clears dismissed)
        gl.onWeekPlanChanged(Collections.emptyMap());
        // 2) Remove whatever remains (manual items) so we start clean
        gl.removeItems(new ArrayList<>(gl.getItems().keySet()));
        assertTrue(gl.getItems().isEmpty(), "Precondition: grocery list should be empty before each test");
    }

    @Test
    void emptyPlanYieldsEmptyItems() {
        gl.onWeekPlanChanged(Collections.emptyMap());
        assertTrue(gl.getItems().isEmpty());
    }

    @Test
    void aggregatesAcrossDaysAndSlots() {
        Map<DayOfWeek, Map<MealSlot, Recipe>> snapshot = new EnumMap<>(DayOfWeek.class);
        snapshot.put(DayOfWeek.MONDAY, new EnumMap<>(MealSlot.class));
        snapshot.put(DayOfWeek.TUESDAY, new EnumMap<>(MealSlot.class));

        Recipe r1 = new Recipe.Builder("Omelet").description("").ingredients(List.of("Eggs", "Milk", "Bread")).build();
        Recipe r2 = new Recipe.Builder("Pancakes").description("").ingredients(List.of("Milk", "Butter")).build();
        Recipe r3 = new Recipe.Builder("Cereal").description("").ingredients(List.of("Milk")).build();

        snapshot.get(DayOfWeek.MONDAY).put(MealSlot.BREAKFAST, r1);
        snapshot.get(DayOfWeek.MONDAY).put(MealSlot.DINNER, r2);
        snapshot.get(DayOfWeek.TUESDAY).put(MealSlot.LUNCH, r3);

        gl.onWeekPlanChanged(snapshot);
        Map<String,Integer> items = gl.getItems();

        assertEquals(1, items.getOrDefault("Eggs", 0));
        assertEquals(1, items.getOrDefault("Bread", 0));
        assertEquals(1, items.getOrDefault("Butter", 0));
        assertEquals(3, items.getOrDefault("Milk", 0));
        assertEquals(4, items.size());
    }

    @Test
    void manualItemsMergeAndMinQuantity() {
        // Plan with two recipes using Milk twice
        Map<DayOfWeek, Map<MealSlot, Recipe>> snapshot = new EnumMap<>(DayOfWeek.class);
        snapshot.put(DayOfWeek.MONDAY, new EnumMap<>(MealSlot.class));
        Recipe r1 = new Recipe.Builder("Porridge").description("").ingredients(List.of("Milk")).build();
        Recipe r2 = new Recipe.Builder("Cereal").description("").ingredients(List.of("Milk")).build();
        snapshot.get(DayOfWeek.MONDAY).put(MealSlot.BREAKFAST, r1);
        snapshot.get(DayOfWeek.MONDAY).put(MealSlot.LUNCH, r2);
        gl.onWeekPlanChanged(snapshot);

        // Manual items
        gl.addManualItem("Milk", 2);      // overlaps -> sums (2 + auto 2 = 4)
        gl.addManualItem("Sugar", 0);     // min quantity becomes 1

        Map<String,Integer> items = gl.getItems();
        assertEquals(4, items.get("Milk"));
        assertEquals(1, items.get("Sugar"));
    }

    @Test
    void removeItemsRemovesManualAndDismissesAuto() {
        // Plan with Milk only
        Map<DayOfWeek, Map<MealSlot, Recipe>> snapshot = new EnumMap<>(DayOfWeek.class);
        snapshot.put(DayOfWeek.MONDAY, new EnumMap<>(MealSlot.class));
        snapshot.get(DayOfWeek.MONDAY).put(MealSlot.BREAKFAST,
                new Recipe.Builder("Shake").description("").ingredients(List.of("Milk")).build());
        gl.onWeekPlanChanged(snapshot);

        // Add a manual-only item
        gl.addManualItem("Eggs", 1);
        assertTrue(gl.getItems().containsKey("Eggs"));
        assertTrue(gl.getItems().containsKey("Milk"));

        // Remove both in one go: Eggs (manual) gets deleted; Milk (auto) is dismissed
        gl.removeItems(List.of("Eggs", "Milk"));

        Map<String,Integer> itemsAfter = gl.getItems();
        assertFalse(itemsAfter.containsKey("Eggs"), "Manual item should be deleted");
        assertFalse(itemsAfter.containsKey("Milk"), "Auto item should be hidden (dismissed)");

        // If plan changes with same items, dismissed auto items stay hidden
        gl.onWeekPlanChanged(snapshot); // same snapshot triggers rebuild, but dismissal persists
        assertFalse(gl.getItems().containsKey("Milk"));
    }

    @Test
    void dismissItemsHidesAutoButKeepsManual() {
        Map<DayOfWeek, Map<MealSlot, Recipe>> snapshot = new EnumMap<>(DayOfWeek.class);
        snapshot.put(DayOfWeek.MONDAY, new EnumMap<>(MealSlot.class));
        snapshot.get(DayOfWeek.MONDAY).put(MealSlot.DINNER,
                new Recipe.Builder("Pasta").description("").ingredients(List.of("Milk")).build());
        gl.onWeekPlanChanged(snapshot);

        gl.addManualItem("Milk", 1); // overlap with auto
        gl.dismissItems(List.of("Milk"));

        Map<String,Integer> items = gl.getItems();
        assertEquals(1, items.getOrDefault("Milk", 0), "Auto part hidden, manual remains");
    }

    @Test
    void dismissedPersistsOnPlanChange() {
        Map<DayOfWeek, Map<MealSlot, Recipe>> snapshot = new EnumMap<>(DayOfWeek.class);
        snapshot.put(DayOfWeek.MONDAY, new EnumMap<>(MealSlot.class));
        snapshot.get(DayOfWeek.MONDAY).put(MealSlot.LUNCH,
                new Recipe.Builder("Cereal").description("").ingredients(List.of("Milk")).build());
        gl.onWeekPlanChanged(snapshot);

        gl.dismissItems(List.of("Milk"));
        assertFalse(gl.getItems().containsKey("Milk"));

        // Changing the plan should keep dismissed items hidden as long as they still exist in the plan
        gl.onWeekPlanChanged(snapshot);
        assertFalse(gl.getItems().containsKey("Milk"));
    }

    @Test
    void normalizationOnManualOperations() {
        gl.addManualItem("  Salt  ", 1);
        assertTrue(gl.getItems().containsKey("Salt"));

        gl.removeItems(List.of("  Salt  "));
        assertFalse(gl.getItems().containsKey("Salt"));
    }
}
