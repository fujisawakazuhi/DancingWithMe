package com.robot.asus.Sporden.WorkingGaming.WorkoutGameData;

import com.robot.asus.Sporden.Model.Workout;

public class GamingActionData {

    private int workoutId;
    private String workoutName;
    private int[] workoutActionList;


    public int getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(int workoutId) {
        this.workoutId = workoutId;
    }

    public String getWorkoutName() {
        return workoutName;
    }

    public void setWorkoutName(String workoutName) {
        this.workoutName = workoutName;
    }

    public int[] getWorkoutActionList() {
        return workoutActionList;
    }

    public void setWorkoutActionList(int[] workoutActionList) {
        this.workoutActionList = workoutActionList;
    }

    public GamingActionData(int workoutId , String workoutName, int[] workoutActionList){

        this.workoutId = workoutId;
        this.workoutName = workoutName;
        this.workoutActionList = workoutActionList;

    }

}
