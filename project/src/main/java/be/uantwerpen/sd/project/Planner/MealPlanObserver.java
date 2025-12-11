package be.uantwerpen.sd.project.Planner;

import be.uantwerpen.sd.project.Recipe.Recipe;

import java.time.DayOfWeek;
import java.util.Map;


public interface MealPlanObserver {
    void onWeekPlanChanged(Map<DayOfWeek, Map<MealSlot, Recipe>> snapshot);
}
