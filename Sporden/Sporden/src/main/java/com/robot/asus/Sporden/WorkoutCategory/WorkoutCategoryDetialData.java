package com.robot.asus.Sporden.WorkoutCategory;

public class WorkoutCategoryDetialData {

    private String name;
    private String description;
    private String filmsrc;

    public static final WorkoutCategoryDetialData[] workoutDetails = {
            new WorkoutCategoryDetialData("跟我一起蹲0！",
                    "解說解說解說解說解說解說解說解說解說解說解說",""),
            new WorkoutCategoryDetialData("前後來回夠趣味1！",
                    "解說解說解說解說解說解說解說解說解說解說解說",""),
            new WorkoutCategoryDetialData("前後來回夠趣味2！",
                    "解說解說解說解說解說解說解說解說解說解說解說",""),
            new WorkoutCategoryDetialData("前後來回夠趣味3！",
                    "解說解說解說解說解說解說解說解說解說解說解說",""),
    };

    //Each Workout has a name and description
    private WorkoutCategoryDetialData(String name, String description, String filmsrc) {
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
