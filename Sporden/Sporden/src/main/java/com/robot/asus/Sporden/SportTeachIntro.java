package com.robot.asus.Sporden;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class SportTeachIntro extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport_teach_intro);
        //保持畫面不讓zenbo臉打斷
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //到時候抓資料填入對應資料
        ImageView imgIntro = (ImageView)findViewById(R.id.sport_teach_intro_pic);
        // set img

        TextView textIntroName = (TextView)findViewById(R.id.sport_teach_intro_name);
        //textIntroName.setText(??);
        TextView textIntroIntensity = (TextView)findViewById(R.id.sport_teach_intro_intensity);
        //textIntroName.setText(??);
        TextView textIntroTime = (TextView)findViewById(R.id.sport_teach_intro_time);
        //textIntroName.setText(??);
        TextView textIntroCategory = (TextView)findViewById(R.id.sport_teach_intro_category);
        //textIntroName.setText(??);
        TextView textIntroEquipment = (TextView)findViewById(R.id.sport_teach_intro_equipment);
        //textIntroName.setText(??);
        TextView textIntro = (TextView)findViewById(R.id.sport_teach_intro);
        //textIntroName.setText(??);

        //按鈕，看要連到哪
        ImageButton button = findViewById(R.id.sport_teach_intro_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),SportSearchAndSort.class);
                startActivity(intent);
            }
        });
    }
}
