package be.uantwerpen.sd.project.Planner;

public interface MealPlanSubject {
    void addObserver(MealPlanObserver observer);
    void removeObserver(MealPlanObserver observer);
}
