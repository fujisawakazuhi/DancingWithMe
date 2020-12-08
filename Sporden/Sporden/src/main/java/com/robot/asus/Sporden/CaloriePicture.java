package com.robot.asus.Sporden;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.RobotUtil;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class CaloriePicture extends RobotActivity {
    private static final String TAG = "CaloriePicture";
    BarChart barChart;

    String responseData = "";

    ArrayList<String> time_list = new ArrayList<>();
    ArrayList<String> date_list = new ArrayList<>();

    HashMap<String, Double> map = new HashMap<>();
    private Handler handler = new Handler();
    private static boolean start_back = false;

    public static RobotCallback robotCallback = new RobotCallback() {
        @Override
        public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
            super.onResult(cmd, serial, err_code, result);
        }

        @Override
        public void onStateChange(int cmd, int serial, RobotErrorCode err_code, RobotCmdState state) {
            super.onStateChange(cmd, serial, err_code, state);
        }

        @Override
        public void initComplete() {
            super.initComplete();

        }
    };

    public static RobotCallback.Listen robotListenCallback = new RobotCallback.Listen() {
        @Override
        public void onFinishRegister() {

        }

        @Override
        public void onVoiceDetect(JSONObject jsonObject) {

        }

        @Override
        public void onSpeakComplete(String s, String s1) {

        }

        @Override
        public void onEventUserUtterance(JSONObject jsonObject) {
            String text;
            text = "onEventUserUtterance: " + jsonObject.toString();
            Log.d(TAG, text);
        }

        @Override
        public void onResult(JSONObject jsonObject) {
            String text;
            text = "onResult: " + jsonObject.toString();
            Log.d(TAG, text);

            String sIntentionID = RobotUtil.queryListenResultJson(jsonObject, "IntentionId");
            Log.d(TAG, "Intention Id = " + sIntentionID);

            //如果是這個plan
            if(sIntentionID.equals("BackToBoard")) {
                //取這個plan的那個要得變數
                String sSluResultButton = RobotUtil.queryListenResultJson(jsonObject, "返回", null);
                Log.d(TAG, "Result Button = " + sSluResultButton);

                //要和concept的instance一樣(這是把變數拿來比對的條件式)
                if(sSluResultButton.equals("返回")) {
                    start_back = true;
                }
            }
        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };

    public CaloriePicture () {
        super(robotCallback, robotListenCallback);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calorie_pic);
        //保持畫面不讓zenbo臉打斷
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        barChart = findViewById(R.id.calorie_barChart);

        Intent intent = getIntent();
        date_list = intent.getStringArrayListExtra("date_list");
        map = (HashMap<String, Double>) intent.getSerializableExtra("map_cal");

        initPic();

        TextView backtomybord = findViewById(R.id.backtomybord);
        backtomybord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CaloriePicture.this, Myboard.class);
                startActivity(intent);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        robotAPI.robot.setExpression(RobotFace.HIDEFACE);

        runTimerForDDE();
    }

    @Override
    protected void onStop() {
        super.onStop();
        map.clear();
    }

    @Override
    protected void onPause() {
        super.onPause();

        handler.removeCallbacksAndMessages(null);
    }

    private void initPic(){
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        for(int i = 0; i <date_list.size(); i++) {
            barEntries.add(new BarEntry(i, Float.valueOf(String.valueOf(map.get(date_list.get(i))))));
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Calories");
        barDataSet.setColor(getResources().getColor(R.color.barchartgreen));
        String[] months = date_list.toArray(new String[0]);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new MyXAixFormatter(months));
        xAxis.setTextSize(20);
        xAxis.setGranularity(1);
        xAxis.setAxisLineColor(R.color.black);

        YAxis yAxis = barChart.getAxisRight();
        yAxis.setEnabled(false);
        YAxis yAxis1 = barChart.getAxisLeft();
        yAxis1.setGridColor(R.color.black);
        yAxis1.setTextSize(40);
        yAxis1.setAxisMinimum(0);

        BarData barData = new BarData(barDataSet);
        barChart.getDescription().setEnabled(false);
        barChart.setData(barData);
        barChart.setFilterTouchesWhenObscured(true);
        barChart.setDrawBorders(true);
    }

    private void runTimerForDDE() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (start_back) {
                    Intent intent = new Intent(CaloriePicture.this, Myboard.class);
                    startActivity(intent);
                    start_back = false;
                }
                Log.d("lxhandler", "CaloriePicture");
                handler.postDelayed(this, 50);
            }
        });
    }

    public class MyXAixFormatter implements IAxisValueFormatter {

        private String[] mValues;

        MyXAixFormatter(String[] values) {
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return mValues[(int)value];
        }
    }


}
