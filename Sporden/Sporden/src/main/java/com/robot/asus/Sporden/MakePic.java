package com.robot.asus.Sporden;

import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Arrays;

public class MakePic extends HeartRateHistoryOld {

    private static final String TAG = "MakePic";

    String[] heart_rate;
    String[] at_time;
    double total_time = 1;

    public MakePic() {
        super();
        this.heart_rate = HeartRateHistoryOld.heart_rate;
        this.at_time = HeartRateHistoryOld.at_time;
        this.total_time = HeartRateHistoryOld.total_time;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makepic);

        Log.d(TAG, "有"+ Arrays.toString(heart_rate) +"..."+ Arrays.toString(at_time)
        +"..."+total_time);

        LineChart lineChart = findViewById(R.id.line_chart);

        //lineChart.setOnChartGestureListener(this);
        //lineChart.setOnChartValueSelectedListener(this);

        lineChart.setDragEnabled(true);
        lineChart.setSaveEnabled(false);


        ArrayList<Entry> yValues = new ArrayList<>();

        for(int i = 0; i <heart_rate.length;i++) {
            yValues.add(new Entry(Float.valueOf(at_time[i]), Float.valueOf(heart_rate[i])));
        }


        XAxis xAxis = lineChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setDrawGridLines(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setAxisMinimum(0f);
        //xAxis.setAxisMaximum(60);
        xAxis.setTextSize(20);
        xAxis.setTextColor(R.color.black);
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setSpaceTop(20);
        //yAxis.setZeroLineWidth(5);
        yAxis.setTextSize(20);
        YAxis yAxis1 = lineChart.getAxisRight();
        yAxis1.setEnabled(false);
        //yAxis.setAxisMinimum(30);
        //yAxis.setAxisMaximum(180);
        LineDataSet set1 = new LineDataSet(yValues, "Data set 1");

        Description description = lineChart.getDescription();
        description.setEnabled(false);


        set1.setFillAlpha(110);

        ArrayList<ILineDataSet> dataSets =
                new ArrayList<>();
        dataSets.add(set1);

        LineData lineData = new LineData(dataSets);
        lineData.setValueTextSize(10);

        lineChart.setData(lineData);

    }




}