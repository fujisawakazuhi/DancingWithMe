package com.robot.asus.Sporden.WorkoutCategory;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.asus.robotframework.API.RobotCallback;
import com.robot.asus.Sporden.R;
import com.robot.asus.robotactivity.RobotActivity;


public class WorkoutCategoryDetialFragment extends Fragment {


    private TextView text1;
    private int id;
    private ImageView img1;

    public static final String ID = "ID";

    public static final WorkoutCategoryDetialFragment newInstance(int id) {
        Log.d("doIHi", "127");
        WorkoutCategoryDetialFragment fragment = new WorkoutCategoryDetialFragment();
        Bundle bdl = new Bundle();
        bdl.putInt(ID, id);
        fragment.setArguments(bdl);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_workout_detial_category, container, false);


    }

    public void onStart() {
        super.onStart();
        View view = getView();
        Log.d("doIHi", "onstart");
        //text1 = (TextView)view.findViewById(R.id.textDescription);
        //id = WorkoutCategory.fList.get()
        id = getArguments().getInt(ID);
        Log.d("textT", id + "");
        text1 = view.findViewById(R.id.text1);
        WorkoutCategoryDetialData data = WorkoutCategoryDetialData.workoutDetails[id];
        text1.setText(data.getDescription());

        img1 = (ImageView) view.findViewById(R.id.image1);
        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), WorkoutGameDetail.class);
                startActivity(intent);
            }
        });

        //text1.setText(data.getName());

    }

    public void setCategoryId(int id) {
        this.id = id;
    }

}


