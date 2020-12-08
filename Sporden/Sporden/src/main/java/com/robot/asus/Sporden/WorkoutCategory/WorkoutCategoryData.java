package com.robot.asus.Sporden.WorkoutCategory;

public class WorkoutCategoryData {

    private String name;
    private String description;
    private String filmsrc;

    public static final WorkoutCategoryData[] workouts = {
            new WorkoutCategoryData("跟我一起蹲0！",
                    "解說解說解說解說解說解說解說解說解說解說解說",""),
            new WorkoutCategoryData("前後來回夠趣味1！",
                    "解說解說解說解說解說解說解說解說解說解說解說",""),
            new WorkoutCategoryData("前後來回夠趣味2！",
                    "解說解說解說解說解說解說解說解說解說解說解說",""),
            new WorkoutCategoryData("前後來回夠趣味3！",
                    "解說解說解說解說解說解說解說解說解說解說解說",""),
    };

    //Each Workout has a name and description
    private WorkoutCategoryData(String name, String description, String filmsrc) {
        this.name = name;
        this.description = description;
        this.filmsrc = filmsrc;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getFilmsrc() {return filmsrc;}

    public String toString() {
        return this.name;
    }

}
