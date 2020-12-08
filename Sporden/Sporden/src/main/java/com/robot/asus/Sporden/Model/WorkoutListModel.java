package com.robot.asus.Sporden.Model;

import android.support.v7.widget.RecyclerView;
import android.widget.ListAdapter;

import com.robot.asus.Sporden.Adapter.SportListAdapter;
import com.robot.asus.Sporden.Adapter.WorkoutListAdapter;
import com.robot.asus.Sporden.R;

public class WorkoutListModel {
    private String name;
    private String img;

    // 建構子
    public WorkoutListModel(String name, String img) {
        this.img =img;
        this.name = name;
    }

    //測試資料
    public static final WorkoutListModel[] workoutListArray = {
            new WorkoutListModel("腹部運動", "111"),
    };

    public String getImg() {
        return img;
    }

    public String getName() {
        return name;
    }

}
