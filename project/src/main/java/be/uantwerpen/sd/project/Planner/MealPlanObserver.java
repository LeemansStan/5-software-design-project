package be.uantwerpen.sd.project.Planner;

import java.time.DayOfWeek;
import java.util.Map;
import java.util.UUID;


public interface MealPlanObserver {
    void onWeekPlanChanged(Map<DayOfWeek, Map<MealSlot, UUID>> snapshot);
}
