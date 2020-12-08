package com.robot.asus.Sporden.Model;

public class MemberModel {
    private static boolean isCompleted;
    private static boolean isChanged;

    public MemberModel() {

    }

    public void setCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public void setChanged (boolean isChanged) {
        this.isChanged = isChanged;
    }

    public boolean getCompleted() {
        return isCompleted;
    }

    public boolean getChanged() {
        return isChanged;
    }

}
