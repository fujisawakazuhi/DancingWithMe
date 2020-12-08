package com.robot.asus.Sporden;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.http.HttpMethodName;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.mobileconnectors.apigateway.ApiRequest;
import com.amazonaws.mobileconnectors.apigateway.ApiResponse;
import com.amazonaws.util.IOUtils;
import com.amazonaws.util.StringUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.robot.asus.Sporden.spodenIndex.SpodenIndexClient;
import com.robot.asus.Sporden.spordenREST.SpordenRESTClient;

import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HeartRateHistoryOld extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "HeartRateHistoryOld";
    private SpordenRESTClient apiClient;
    private SpodenIndexClient indexClient;
    String responseData = "";
    String thisdata = "";
    static String[] heart_rate;
    static String[] at_time;
    String total_time_str = "";
    static double total_time = 1;
    int ex_min = 0;
    int ex_sec = 0;

    String[] split_list;
    String[] split_list_2;
    ArrayList<String> split_list_list = new ArrayList<>();
    ArrayList<String> split_list_list_reverse = new ArrayList<>();

    private ListView listView;
    private ListAdapter listAdapter;
    String[] test;
    Boolean test_boolean = false;
    private ProgressDialog progressDialog;

    //rest api invoke index table
    private void doInvokeIndex() {
        // Create components of api request
        final String method = "GET";
        final String path = "/index/object/:user_email";

        //body放要post的資料(userId一定要有)
        final String body = "";
        final byte[] content = body.getBytes(StringUtils.UTF8);

        final Map parameters = new HashMap<>();
        parameters.put("lang", "en_US");
        //這邊放get要搜尋的哪個PK欄位(找哪個帳號的資料)
        parameters.put("user_email", "spordan2018@gmail.com");

        final Map headers = new HashMap<>();

        // Use components to create the api request
        ApiRequest localRequest =
                new ApiRequest(indexClient.getClass().getSimpleName())
                        .withPath(path)
                        .withHttpMethod(HttpMethodName.valueOf(method))
                        .withHeaders(headers)
                        .addHeader("Content-Type", "application/json")
                        .withParameters(parameters);

        // Only set body if it has content.
        if (body.length() > 0) {
            localRequest = localRequest
                    .addHeader("Content-Length", String.valueOf(content.length))
                    .withBody(content);
        }

        final ApiRequest request = localRequest;

        // Make network call on background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG,
                            "Invoking API w/ Request : " +
                                    request.getHttpMethod() + ":" +
                                    request.getPath());

                    final ApiResponse response = indexClient.execute(request);

                    final InputStream responseContentStream = response.getContent();

                    if (responseContentStream != null) {
                        responseData = IOUtils.toString(responseContentStream);
                        Log.d(TAG, "Response : " + responseData);
                        //取json
                        String tmp = responseData.substring(responseData.indexOf("{"), responseData.lastIndexOf("}") + 1);
                        JSONObject json_read = new JSONObject(tmp);
                        String get_list = json_read.getString("done_list");
                        //取[]裡的資料
                        String done_list = get_list.substring(get_list.indexOf("[") + 1, get_list.indexOf("]"));
                        //用,分開，並存成陣列
                        split_list = done_list.split(", ");
                        Log.d(TAG, "" + Arrays.toString(split_list));

                        String temp = split_list[0];
                        long split_test = Long.parseLong(temp);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN);
                        String a = simpleDateFormat.format(split_test);
                        Log.d(TAG, a);
                        split_list_list.add(a);
                        for (int i = 1; i < split_list.length; i++) {
                            long split_test_in = Long.parseLong(split_list[i]);
                            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN);
                            String in = simpleDateFormat2.format(split_test_in);
                            if (!temp.equals(in) && !a.equals(in)) {
                                split_list_list.add(in);
                                temp = in;
                            }
                        }
                        int num = split_list_list.size() - 1;
                        for (int ii = 0; ii < split_list_list.size(); ii++) {
                            split_list_list_reverse.add(split_list_list.get(num));
                            num--;
                        }

                        Log.d(TAG, "這次:" + Arrays.toString(split_list_list_reverse.toArray()));

                    }

                    Log.d(TAG, "這個是:"+ Arrays.toString(test));
                    test_boolean = true;
                    Log.d(TAG, "status code = " + response.getStatusCode() + " " + response.getStatusText());
                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //aws auth init
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {

                    @Override
                    public void onResult(UserStateDetails userStateDetails) {
                        Log.i("INIT", "onResult: " + userStateDetails.getUserState());
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("INIT", "Initialization error.", e);
                    }
                }
        );

        apiClient = new ApiClientFactory()
                .credentialsProvider(AWSMobileClient.getInstance())
                .region("us-east-1")
                .build(SpordenRESTClient.class);

        indexClient = new ApiClientFactory()
                .credentialsProvider(AWSMobileClient.getInstance())
                .region("us-east-1")
                .build(SpodenIndexClient.class);


        setContentView(R.layout.activity_heart_rate_history_old);

        doInvokeAPI();
        doInvokeIndex();
        test = Convert(split_list_list_reverse);
        Log.d(TAG, "nhjkm:"+ Arrays.toString(test));
        listView = findViewById(R.id.heart_rate_listView);
        listAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1,
                test);
        listView.setAdapter(listAdapter);
        //listView.setVisibility(View.VISIBLE);


        Spinner spinner = findViewById(R.id.spinner);
        final String[] exercise_category = {"慢跑", "快跑", "硬舉", "前後甩手"};
        ArrayAdapter<String> exercise_category_list = new ArrayAdapter<>(HeartRateHistoryOld.this,
                android.R.layout.simple_spinner_dropdown_item,
                exercise_category);
        spinner.setAdapter(exercise_category_list);
        spinner.setOnItemSelectedListener(this);

        TextView date = findViewById(R.id.type_exercise);
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LineChart lineChart = findViewById(R.id.line_chart);
                lineChart.setDragEnabled(true);
                lineChart.setSaveEnabled(false);

                ArrayList<Entry> yValues = new ArrayList<>();
                for (int i = 0; i < heart_rate.length; i++) {
                    yValues.add(new Entry(Float.valueOf(at_time[i]), Float.valueOf(heart_rate[i])));
                }
                /*yValues.add(new Entry(11.24f, 68.66f));
                yValues.add(new Entry(13.25f, 77.89f));
                yValues.add(new Entry(15.25f, 75.89f));
                yValues.add(new Entry(16.25f, 73.89f));
                yValues.add(new Entry(17.25f, 74.89f));
                yValues.add(new Entry(19.25f, 76.89f));
                yValues.add(new Entry(20.25f, 78.89f));
                yValues.add(new Entry(23.25f, 79.89f));
                yValues.add(new Entry(24.25f, 83.89f));
                yValues.add(new Entry(25.25f, 87.89f));
                yValues.add(new Entry(29.25f, 91.89f));
                yValues.add(new Entry(30.25f, 88.89f));
                yValues.add(new Entry(31.25f, 90.89f));
                yValues.add(new Entry(34.25f, 93.89f));
                yValues.add(new Entry(35.25f, 92.89f));
                yValues.add(new Entry(37.25f, 89.89f));
                yValues.add(new Entry(38.25f, 91.89f));
                yValues.add(new Entry(40.25f, 95.8f));
                yValues.add(new Entry(42.25f, 96.9f));
                yValues.add(new Entry(43.25f, 97.8f));
                yValues.add(new Entry(44.25f, 96.9f));
                yValues.add(new Entry(45.25f, 93.89f));
                yValues.add(new Entry(47.25f, 95.9f));
                yValues.add(new Entry(50.25f, 94.89f));*/

                float mean = 0;
                for (int i = 0; i < heart_rate.length; i++) {
                    mean += Float.valueOf(heart_rate[i]);
                }
                mean = mean / heart_rate.length;
                LimitLine mean_line;
                mean_line = new LimitLine(mean, "平均");
                mean_line.setLineWidth(2);

                XAxis xAxis = lineChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                //xAxis.setLabelCount(5);
                xAxis.setTextSize(20);
                YAxis yAxis = lineChart.getAxisLeft();
                yAxis.setSpaceTop(20);
                yAxis.setTextSize(20);
                yAxis.addLimitLine(mean_line);
                YAxis yAxis1 = lineChart.getAxisRight();
                yAxis1.setEnabled(false);

                LineDataSet set1 = new LineDataSet(yValues, "心跳");
                set1.setDrawValues(false);
                set1.setHighlightEnabled(true);
                set1.setCircleRadius(1.4f);
                set1.setColor(Color.BLACK);
                set1.setCircleColor(Color.BLACK);

                set1.setFillAlpha(255);

                ArrayList<ILineDataSet> dataSets =
                        new ArrayList<>();
                dataSets.add(set1);

                LineData lineData = new LineData(dataSets);
                lineData.setValueTextSize(20);
                lineChart.getDescription().setEnabled(false);
                lineChart.setData(lineData);
                lineChart.setDrawBorders(true);
                lineChart.setDrawGridBackground(true);
                LinearLayout linearLayout = findViewById(R.id.linear);
                linearLayout.setVisibility(View.VISIBLE);
                TextView max_value = findViewById(R.id.textView);
                max_value.setText(String.format("最大值:%s", set1.getYMax()));
                LinearLayout linearLayout1 = findViewById(R.id.linear_info);
                TextView minimum_heart_rate = findViewById(R.id.min_heart_rate);
                TextView mean_heart_rate = findViewById(R.id.mean_heart_rate);
                mean_heart_rate.setText(String.format(Locale.getDefault(), "平均值:%3.2f", mean));
                minimum_heart_rate.setText(String.format("最小值:%s", set1.getYMin()));
                TextView total_time = findViewById(R.id.total_time);
                total_time.setText(String.format(Locale.getDefault(), "總時間:%d分%d秒", ex_min, ex_sec));
                LinearLayout linearLayout2 = findViewById(R.id.prepare_linear_info);
                linearLayout2.setVisibility(View.INVISIBLE);
                linearLayout1.setVisibility(View.VISIBLE);
            }
        });

        /*Log.d(TAG, "哈哈哈哈哈" + total_time);
        LineChart lineChart = findViewById(R.id.line_chart);
        lineChart.setDragEnabled(true);
        lineChart.setSaveEnabled(false);

        ArrayList<Entry> yValues = new ArrayList<>();
        Log.d(TAG, "這是:" + heart_rate.length);
        for (int i = 0; i <= heart_rate.length - 1; i++) {
            yValues.add(new Entry((Float.valueOf(heart_rate[i])), Float.valueOf(at_time[i])));
        }
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setAxisMinimum(0f);
        //xAxis.setAxisMaximum(60);
        //xAxis.setLabelCount(5);
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setSpaceTop(20);
        YAxis yAxis1 = lineChart.getAxisRight();
        yAxis1.setEnabled(false);

        //yAxis.setAxisMinimum(30);
        //yAxis.setAxisMaximum(180);
        LineDataSet set1 = new LineDataSet(yValues, "Data set 1");

        //set1.setColor(R.color.red);
        //set1.setCircleColor(R.color.red);
        //set1.setValueTextColor(R.color.red);
        //set1.setFillAlpha(110);


        ArrayList<ILineDataSet> dataSets =
                new ArrayList<>();
        dataSets.add(set1);

        LineData lineData = new LineData(dataSets);
        lineData.setValueTextSize(10);
        lineData.setValueTextColor(R.color.red);

        lineChart.setData(lineData);
*/
    }

    //rest api invoke test
    private void doInvokeAPI() {
        // Create components of api request
        final String method = "GET";
        final String path = "/Health/object/:userId";

        //body放要post的資料(userId一定要有)
        //final String body = "{\"userId\":\"test11\"}";
        final String body = "";
        final byte[] content = body.getBytes(StringUtils.UTF8);

        final Map parameters = new HashMap<>();
        parameters.put("lang", "en_US");
        //這邊放get要搜尋的哪個PK欄位1554733942286
        parameters.put("userId", "1555752398330");//1554020360027
        //1554020204151
        //1553954043134
        long time_stop = 1553776560978L;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN);
        String a = simpleDateFormat.format(time_stop);
        Log.d(TAG, "這是:" + a);
        final Map headers = new HashMap<>();

        // Use components to create the api request
        ApiRequest localRequest =
                new ApiRequest(apiClient.getClass().getSimpleName())
                        .withPath(path)
                        .withHttpMethod(HttpMethodName.valueOf(method))
                        .withHeaders(headers)
                        .addHeader("Content-Type", "application/json")
                        .withParameters(parameters);

        // Only set body if it has content.
        if (body.length() > 0) {
            localRequest = localRequest
                    .addHeader("Content-Length", String.valueOf(content.length))
                    .withBody(content);
        }

        final ApiRequest request = localRequest;

        // Make network call on background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG,
                            "Invoking API w/ Request : " +
                                    request.getHttpMethod() + ":" +
                                    request.getPath());

                    final ApiResponse response = apiClient.execute(request);

                    final InputStream responseContentStream = response.getContent();

                    if (responseContentStream != null) {
                        responseData = IOUtils.toString(responseContentStream);
                        Log.d(TAG, "Response : " + responseData);
                        //取json
                        String tmp_invoke = responseData.substring(responseData.indexOf("{"), responseData.lastIndexOf("}") + 1);
                        JSONObject json_read_invoke = new JSONObject(tmp_invoke);
                        String get_list_invoke = json_read_invoke.getString("time_moment");
                        //取[]裡的資料
                        String done_list_invoke = get_list_invoke.substring(get_list_invoke.indexOf("[") + 1, get_list_invoke.indexOf("]"));
                        //用,分開，並存成陣列
                        split_list_2 = done_list_invoke.split(", ");
                        Log.d(TAG, "" + Arrays.toString(split_list_2));
                        /*total_time_str = responseData.substring(responseData.indexOf("zung") + 4, responseData.indexOf("xi"));
                        total_time = Double.valueOf(total_time_str);
                        ex_min = (int) total_time / 60;
                        ex_sec = (int) total_time % 60;
                        total_time_str = responseData.substring(responseData.indexOf("sin") + 4, responseData.indexOf("]tiao"));
                        heart_rate = total_time_str.split(",");
                        total_time_str = responseData.substring(responseData.indexOf("shi[") + 4, responseData.indexOf("]qian"));
                        at_time = total_time_str.split(",");
                        //Log.d(TAG, "這是:" + heart_rate.length);
                        //Log.d(TAG, "總時間" + Double.valueOf(heart_rate[0]));
                        total_time_str = responseData.substring(responseData.indexOf("xi") + 2, responseData.indexOf("\",\""));
                        Log.d(TAG, "" + Arrays.toString(total_time_str.split(" ")));*/
                    }
                    Log.d(TAG, "status code = " + response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    public HeartRateHistoryOld() {

    }

    public HeartRateHistoryOld(String[] heart_rate, String[] at_time, double total_time) {
        HeartRateHistoryOld.heart_rate = heart_rate;
        HeartRateHistoryOld.at_time = at_time;
        HeartRateHistoryOld.total_time = total_time;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private String[] Convert(ArrayList aa) {
        String[] ss = new String[aa.size()];
        for (int i = 0; i < aa.size(); i++) {
            ss[i] = aa.get(i).toString();
        }
        return ss;
    }

}
