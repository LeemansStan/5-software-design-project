package be.uantwerpen.sd.project;

import be.uantwerpen.sd.project.Planner.*;
import be.uantwerpen.sd.project.Recipe.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

public class ViewApp extends Application {
    private final RecipeService controller = new RecipeService();
    private final MealPlanService mealController = new MealPlanService();

    // View state
    private final ObservableList<Recipe> recipes = FXCollections.observableArrayList();
    private final ListView<Recipe> listView = new ListView<>(recipes);

    // Form controls
    private TextField titleField;
    private TextArea descriptionArea;
    private TextArea ingredientsArea;
    private TextField tagsField;
    private TextField searchField;
    private Label statusLabel;

    // Weekly planner UI state
    private GridPane plannerGrid;
    private final Map<DayOfWeek, Map<MealSlot, ComboBox<Recipe>>> plannerCells = new EnumMap<>(DayOfWeek.class);
    private final Map<MealSlot, CheckBox> slotToggles = new EnumMap<>(MealSlot.class);

    // Grocery List UI state
    private VBox groceryRoot;
    private VBox groceryItemsBox;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Recipe Manager");

        // Seed a few demo recipes
        seedDemoData();
        refreshList();

        // Wire GroceryList to observe WeekPlan and resolve ingredients from RecipeController
        be.uantwerpen.sd.project.GroceryList.GroceryList grocery = be.uantwerpen.sd.project.GroceryList.GroceryList.getInstance();
        grocery.setIngredientResolver(id -> controller.findById(id).map(Recipe::getIngredients));
        mealController.getWeekPlan().addObserver(grocery);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Left: list of recipes
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Recipe item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String tags = item.getTags().stream().limit(3).collect(Collectors.joining(", "));
                    setText(item.getTitle() + (tags.isEmpty() ? "" : "  [" + tags + "]"));
                }
            }
        });
        listView.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> populateForm(sel));
        root.setLeft(wrapWithTitle(listView, "Recipes"));

        // Center: editor + weekly planner tabs
        GridPane form = createForm();
        TabPane tabs = new TabPane();
        Tab editorTab = new Tab("Recipe Editor", form);
        editorTab.setClosable(false);
        Tab plannerTab = new Tab("Weekly Planner", createPlannerPane());
        plannerTab.setClosable(false);
        Tab groceryTab = new Tab("Grocery List", createGroceryPane());
        groceryTab.setClosable(false);
        tabs.getTabs().addAll(editorTab, plannerTab, groceryTab);
        root.setCenter(tabs);

        // Keep Grocery tab synced with plan changes
        mealController.getWeekPlan().addObserver(snap -> Platform.runLater(this::rebuildGroceryUI));

        // Top: search bar
        HBox searchBar = createSearchBar();
        root.setTop(searchBar);

        // Bottom: status bar
        statusLabel = new Label("Ready");
        BorderPane.setMargin(statusLabel, new Insets(6, 0, 0, 0));
        root.setBottom(statusLabel);

        Scene scene = new Scene(root, 960, 560);
        stage.setScene(scene);
        stage.show();
    }

    private VBox wrapWithTitle(Control control, String title) {
        Label l = new Label(title);
        l.setStyle("-fx-font-weight: bold; -fx-padding: 0 0 6 0;");
        VBox box = new VBox(6, l, control);
        VBox.setVgrow(control, Priority.ALWAYS);
        box.setPrefWidth(320);
        return box;
    }

    // ========== Weekly Planner UI ==========
    private VBox createPlannerPane() {
        // Slot toggles
        HBox toggles = new HBox(12);
        toggles.setAlignment(Pos.CENTER_LEFT);
        toggles.setPadding(new Insets(0, 0, 10, 0));
        slotToggles.clear();
        for (MealSlot slot : MealSlot.values()) {
            CheckBox cb = new CheckBox(slot.getDisplayName());
            cb.setSelected(mealController.getActiveSlots().contains(slot));
            cb.selectedProperty().addListener((obs, was, is) -> onToggleSlot(slot, is));
            slotToggles.put(slot, cb);
            toggles.getChildren().add(cb);
        }

        plannerGrid = new GridPane();
        plannerGrid.setHgap(8);
        plannerGrid.setVgap(6);
        plannerGrid.setPadding(new Insets(0, 0, 0, 10));

        rebuildPlannerGrid();
        refreshPlannerSelections();

        VBox root = new VBox(6, toggles, plannerGrid);
        VBox.setVgrow(plannerGrid, Priority.ALWAYS);
        return root;
    }

    private void onToggleSlot(MealSlot slot, boolean active) {
        Set<MealSlot> current = new LinkedHashSet<>(mealController.getActiveSlots());
        if (active) current.add(slot); else current.remove(slot);
        if (current.isEmpty()) {
            // Keep at least one slot
            slotToggles.get(slot).setSelected(true);
            info("At least one meal slot must be active");
            return;
        }
        mealController.setActiveSlots(current);
        rebuildPlannerGrid();
        refreshPlannerSelections();
    }

    private void rebuildPlannerGrid() {
        plannerGrid.getChildren().clear();
        plannerCells.clear();

        // Header row: empty corner + days Mon..Sun
        int row = 0;
        plannerGrid.add(new Label(""), 0, row);
        int col = 1;
        for (DayOfWeek d : daysMonToSun()) {
            Label lbl = new Label(shortDay(d));
            lbl.setStyle("-fx-font-weight: bold");
            plannerGrid.add(lbl, col++, row);
        }
        row++;

        // Rows for active slots
        for (MealSlot slot : mealController.getActiveSlots()) {
            plannerGrid.add(new Label(slot.getDisplayName()), 0, row);
            col = 1;
            for (DayOfWeek d : daysMonToSun()) {
                ComboBox<Recipe> combo = createRecipeComboBox();
                Button clearBtn = new Button("×");
                clearBtn.setMinWidth(28);
                clearBtn.setOnAction(e -> {
                    mealController.clear(d, slot);
                    combo.getSelectionModel().clearSelection();
                    status("Cleared " + slot.getDisplayName() + " on " + d);
                });
                HBox cell = new HBox(4, combo, clearBtn);
                plannerGrid.add(cell, col++, row);
                plannerCells.computeIfAbsent(d, k -> new EnumMap<>(MealSlot.class)).put(slot, combo);
            }
            row++;
        }
    }

    private ComboBox<Recipe> createRecipeComboBox() {
        ComboBox<Recipe> combo = new ComboBox<>(recipes);
        combo.setPrefWidth(180);
        combo.setConverter(new StringConverter<>() {
            @Override public String toString(Recipe r) { return r == null ? "" : r.getTitle(); }
            @Override public Recipe fromString(String s) { return null; }
        });
        combo.valueProperty().addListener((obs, old, val) -> {
            // Listener set per cell later with context (day, slot) via userData
        });
        return combo;
    }

    private List<DayOfWeek> daysMonToSun() {
        return List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    }

    private String shortDay(DayOfWeek d) {
        switch (d) {
            case MONDAY: return "Mon";
            case TUESDAY: return "Tue";
            case WEDNESDAY: return "Wed";
            case THURSDAY: return "Thu";
            case FRIDAY: return "Fri";
            case SATURDAY: return "Sat";
            case SUNDAY: return "Sun";
            default: return d.name();
        }
    }

    private void refreshPlannerSelections() {
        // Make sure each ComboBox shows the correct selection from the model.
        for (Map.Entry<DayOfWeek, Map<MealSlot, ComboBox<Recipe>>> e : plannerCells.entrySet()) {
            DayOfWeek day = e.getKey();
            for (Map.Entry<MealSlot, ComboBox<Recipe>> ce : e.getValue().entrySet()) {
                MealSlot slot = ce.getKey();
                ComboBox<Recipe> combo = ce.getValue();
                combo.setItems(recipes);
                Optional<UUID> rid = mealController.getRecipeId(day, slot);
                if (rid.isPresent()) {
                    Recipe r = controller.findById(rid.get()).orElse(null);
                    combo.getSelectionModel().select(r);
                } else {
                    combo.getSelectionModel().clearSelection();
                }
                // Attach listener with proper context
                combo.valueProperty().addListener((obs, old, val) -> {
                    if (val != null) {
                        mealController.setRecipe(day, slot, val.getId());
                        status("Planned '" + val.getTitle() + "' for " + shortDay(day) + " (" + slot.getDisplayName() + ")");
                    }
                });
            }
        }
    }

    private GridPane createForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(0, 0, 0, 10));

        int r = 0;
        grid.add(new Label("Title"), 0, r);
        titleField = new TextField();
        grid.add(titleField, 1, r++);

        grid.add(new Label("Description"), 0, r);
        descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(4);
        grid.add(descriptionArea, 1, r++);

        grid.add(new Label("Ingredients (one per line)"), 0, r);
        ingredientsArea = new TextArea();
        ingredientsArea.setPrefRowCount(6);
        grid.add(ingredientsArea, 1, r++);

        grid.add(new Label("Tags (comma-separated)"), 0, r);
        tagsField = new TextField();
        grid.add(tagsField, 1, r++);

        HBox buttons = new HBox(10);
        Button addBtn = new Button("Add");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");
        Button clearBtn = new Button("Clear");
        buttons.getChildren().addAll(addBtn, updateBtn, deleteBtn, clearBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);
        grid.add(buttons, 1, r);

        addBtn.setOnAction(e -> onAdd());
        updateBtn.setOnAction(e -> onUpdate());
        deleteBtn.setOnAction(e -> onDelete());
        clearBtn.setOnAction(e -> clearForm());

        return grid;
    }

    private HBox createSearchBar() {
        searchField = new TextField();
        searchField.setPromptText("Search by title");
        Button searchBtn = new Button("Search");
        Button resetBtn = new Button("Reset");
        HBox box = new HBox(8, new Label("Search:"), searchField, searchBtn, resetBtn);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(0, 0, 10, 0));

        searchBtn.setOnAction(e -> doSearch());
        resetBtn.setOnAction(e -> { searchField.clear(); refreshList(); });
        searchField.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) doSearch(); });
        return box;
    }

    private void doSearch() {
        String q = searchField.getText();
        List<Recipe> result = controller.searchByTitle(q);
        recipes.setAll(result);
        // when recipe list changes, update planner selections in case references moved
        refreshPlannerSelections();
        status("Found " + result.size() + " recipe(s) for '" + (q == null ? "" : q) + "'");
    }

    private void populateForm(Recipe r) {
        if (r == null) {
            clearForm();
            return;
        }
        titleField.setText(r.getTitle());
        descriptionArea.setText(r.getDescription());
        ingredientsArea.setText(String.join("\n", r.getIngredients()));
        tagsField.setText(String.join(", ", r.getTags()));
    }

    private void clearForm() {
        listView.getSelectionModel().clearSelection();
        titleField.clear();
        descriptionArea.clear();
        ingredientsArea.clear();
        tagsField.clear();
        status("Form cleared");
    }

    private void onAdd() {
        try {
            Recipe r = controller.create(
                    valueOrThrow(titleField.getText(), "Title is required"),
                    defaultString(descriptionArea.getText()),
                    parseIngredients(ingredientsArea.getText()),
                    parseTags(tagsField.getText())
            );
            recipes.add(r);
            listView.getSelectionModel().select(r);
            status("Added recipe: " + r.getTitle());
        } catch (IllegalArgumentException ex) {
            error("Cannot add recipe: " + ex.getMessage());
        }
    }

    private void onUpdate() {
        Recipe sel = listView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            info("Select a recipe to update");
            return;
        }
        try {
            controller.updateTitle(sel.getId(), valueOrThrow(titleField.getText(), "Title is required"));
            controller.updateDescription(sel.getId(), defaultString(descriptionArea.getText()));
            controller.updateIngredients(sel.getId(), parseIngredients(ingredientsArea.getText()));
            controller.updateTags(sel.getId(), parseTags(tagsField.getText()));
            // Refresh selection to show any formatted changes
            listView.refresh();
            status("Updated recipe: " + sel.getTitle());
        } catch (IllegalArgumentException ex) {
            error("Cannot update recipe: " + ex.getMessage());
        }
    }

    private void onDelete() {
        Recipe sel = listView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            info("Select a recipe to delete");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Recipe");
        alert.setHeaderText("Delete '" + sel.getTitle() + "'?");
        alert.setContentText("This action cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean removed = controller.remove(sel.getId());
            if (removed) {
                recipes.remove(sel);
                clearForm();
                status("Deleted recipe: " + sel.getTitle());
            } else {
                error("Failed to delete recipe");
            }
        }
    }

    private void refreshList() {
        recipes.setAll(controller.listAll());
    }

    // Helpers
    private static String valueOrThrow(String v, String message) {
        if (v == null || v.trim().isEmpty()) throw new IllegalArgumentException(message);
        return v.trim();
    }

    private static String defaultString(String v) { return v == null ? "" : v.strip(); }

    private static List<String> parseIngredients(String text) {
        if (text == null) return List.of();
        List<String> list = Arrays.stream(text.split("\r?\n"))
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        return list;
    }

    private static Collection<String> parseTags(String text) {
        if (text == null || text.isBlank()) return List.of();
        return Arrays.stream(text.split(","))
                .map(s -> s.replace(";", ","))
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void status(String msg) {
        statusLabel.setText(msg);
    }

    private void info(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
        status(msg);
    }

    private void error(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
        status(msg);
    }

    private void seedDemoData() {
        controller.create(
                "Spaghetti Aglio e Olio",
                "Classic Italian pasta with garlic, olive oil, and chili flakes.",
                List.of("spaghetti", "garlic", "olive oil", "chili flakes", "parsley", "salt"),
                List.of("vegetarian", "quick", "budget")
        );
        controller.create(
                "Chicken Curry",
                "Creamy chicken curry with coconut milk.",
                Arrays.asList("chicken", "onion", "garlic", "ginger", "curry paste", "coconut milk", "salt"),
                List.of("dinner")
        );
    }

    // ========== Grocery List UI ==========
    private VBox createGroceryPane() {
        groceryItemsBox = new VBox(4);
        ScrollPane scroll = new ScrollPane(groceryItemsBox);
        scroll.setFitToWidth(true);

        Button refreshBtn = new Button("Refresh (remove checked)");
        Button selectAllBtn = new Button("Select All");
        Button deselectAllBtn = new Button("Deselect All");
        HBox controls = new HBox(8, refreshBtn, selectAllBtn, deselectAllBtn);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(0, 0, 6, 0));

        refreshBtn.setOnAction(e -> {
            // Collect checked items and ask GroceryList to dismiss them
            List<String> toRemove = new ArrayList<>();
            for (javafx.scene.Node n : groceryItemsBox.getChildren()) {
                if (n instanceof CheckBox) {
                    CheckBox cb = (CheckBox) n;
                    if (cb.isSelected()) {
                        Object key = cb.getUserData();
                        if (key != null) toRemove.add(key.toString());
                    }
                }
            }
            be.uantwerpen.sd.project.GroceryList.GroceryList.getInstance().dismissItems(toRemove);
            rebuildGroceryUI();
            status("Removed " + toRemove.size() + " item(s) from grocery list");
        });
        selectAllBtn.setOnAction(e -> {
            for (javafx.scene.Node n : groceryItemsBox.getChildren()) {
                if (n instanceof CheckBox) ((CheckBox) n).setSelected(true);
            }
        });
        deselectAllBtn.setOnAction(e -> {
            for (javafx.scene.Node n : groceryItemsBox.getChildren()) {
                if (n instanceof CheckBox) ((CheckBox) n).setSelected(false);
            }
        });

        groceryRoot = new VBox(6, controls, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        rebuildGroceryUI();
        return groceryRoot;
    }

    private void rebuildGroceryUI() {
        if (groceryItemsBox == null) return;
        Map<String, Integer> items = be.uantwerpen.sd.project.GroceryList.GroceryList.getInstance().getItems();
        groceryItemsBox.getChildren().clear();
        if (items.isEmpty()) {
            Label empty = new Label("No items. Plan recipes to populate the list.");
            empty.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
            groceryItemsBox.getChildren().add(empty);
            return;
        }
        for (Map.Entry<String, Integer> e : items.entrySet()) {
            String name = e.getKey();
            int count = e.getValue();
            String label = count > 1 ? (name + " x" + count) : name;
            CheckBox cb = new CheckBox(label);
            cb.setUserData(name); // store raw key
            groceryItemsBox.getChildren().add(cb);
        }
    }
}