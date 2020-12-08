package com.robot.asus.Sporden;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
import com.robot.asus.Sporden.spordenREST.SpordenRESTClient;

import org.json.JSONObject;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HeartRateStretchTopFragment extends Fragment implements AdapterView.OnItemClickListener{
    View v;

    private static final String TAG = "StretchTopFragment";

    private SpordenRESTClient apiClient;
    String responseData = "";
    String[] total_time_string;
    String total_time_str;
    double total_time = 1;
    int ex_min = 0;
    int ex_sec = 0;
    String get_calorie;
    boolean isHeart_rate_time = false;

    ArrayList<String> askrun = new ArrayList<>();


    private ListView listView;
    private ListAdapter listAdapter;

    ArrayList<String> date_time_list = new ArrayList<>();

    String[] date_list;
    String[] heart_rate_time;
    String[] heart_rate_test;

    long milliseconds = 0L;

    LineChart lineChart;
    LinearLayout linearLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_heartrate_interface, container, false);

        //aws auth init
        AWSMobileClient.getInstance().initialize(getActivity(), new Callback<UserStateDetails>() {

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

        //doInvokeAPI();

        lineChart = v.findViewById(R.id.line_chart_test);
        linearLayout = v.findViewById(R.id.linear_test);

        Bundle bundle = getArguments();
        if (bundle != null) {
            askrun = bundle.getStringArrayList("stretch_top");
        }


        Collections.sort(askrun);
        for(int i = 0; i < askrun.size()-1; i++){
            if (askrun.get(i).equals(askrun.get(i+1))) {
                askrun.remove(i);
                i--;
            }
        }
        Collections.reverse(askrun);
        date_list = askrun.toArray(new String[0]);
        Log.d(TAG, "鄭列:" + Arrays.toString(date_list));
        listView = v.findViewById(R.id.heartrate_test_list);
        listAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,
                askrun);
        listView.setAdapter(listAdapter);

        /*test_listView = (ExpandableListView)v.findViewById(R.id.ExpListView);

        test_listAdapter =  new ExpandableListAdapter(getContext(), listDataHeader, listHash);
        test_listView.setAdapter(test_listAdapter);*/

        listView.setOnItemClickListener(this);

        return v;
    }


    //rest api invoke test
    private void doInvokeAPI(Long date) {
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
        parameters.put("userId", String.valueOf(date));//1554020360027
        //1554020204151
        //1553954043134

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
                        Log.d(TAG, "get_list_invoke:" + get_list_invoke);
                        //取[]裡的資料
                        String done_list_invoke = get_list_invoke.substring(get_list_invoke.indexOf("[") + 1, get_list_invoke.indexOf("]"));
                        Log.d(TAG, "done_list_invoke:" + done_list_invoke);
                        //用,分開，並存成陣列
                        String[] split_list1 = done_list_invoke.split(", ");
                        Log.d(TAG, "split_list_1:" + Arrays.toString(split_list1));
                        heart_rate_time = split_list1;
                        isHeart_rate_time = true;

                        String get_list_invoke2 = json_read_invoke.getString("heartRate");
                        Log.d(TAG, "get_list_invoke2:" + get_list_invoke2);
                        //取[]裡的資料
                        String done_list_invoke2 = get_list_invoke2.substring(get_list_invoke2.indexOf("[") + 1, get_list_invoke2.indexOf("]"));
                        //用,分開，並存成陣列
                        heart_rate_test = done_list_invoke2.split(", ");

                        String get_list_invoke3 = json_read_invoke.getString("total_time");
                        Log.d(TAG, "get_list_invoke3:" + get_list_invoke3);

                        get_calorie = json_read_invoke.getString("calories");
                        Log.e(TAG, get_calorie);
                        //用,分開，並存成陣列
                        total_time_string = get_list_invoke3.split("/");
                        total_time_str = total_time_string[0];
                        total_time = Double.valueOf(total_time_str);
                        ex_min = (int) total_time / 60;
                        ex_sec = (int) total_time % 60;
                        //Log.d(TAG, "" + Arrays.toString(total_time_str.split(" ")));

                    }
                    Log.d(TAG, "status code = " + response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        heart_rate_test= new String[0];
        heart_rate_time = new String[0];
        milliseconds = 0L;
        Log.d(TAG, "你所選的位置:" + parent.getItemAtPosition(position).toString());
        String string_date = parent.getItemAtPosition(position).toString();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);
        try {
            Date d = f.parse(string_date);
            milliseconds = d.getTime();
            Log.d(TAG, "你所選的位置:" + String.valueOf(milliseconds));
            doInvokeAPI(milliseconds);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            while (heart_rate_time.length == 0) {
                Log.d(TAG, "等喔");
                Thread.sleep(50);
            }
            lineChart.clear();
            lineChart.setDragEnabled(true);
            lineChart.setSaveEnabled(false);
            //Log.d(TAG, "heartratetimeonitemclick:" + Arrays.toString(heart_rate_time));
            //Log.d(TAG, "heartratetimeonitemclicklength:" + "and" + Arrays.toString(heart_rate_test));

            ArrayList<Entry> yValues = new ArrayList<>();
            for (int i = 0; i < heart_rate_time.length; i++) {
                yValues.add(new Entry(Float.valueOf(heart_rate_time[i]), Float.valueOf(heart_rate_test[i])));
            }
            Log.d(TAG, "這裡呢:" + Arrays.toString(yValues.toArray()));

            float mean = 0;
            for (String aHeart_rate_test : heart_rate_test) {
                mean += Float.valueOf(aHeart_rate_test);
            }
            mean = mean / heart_rate_test.length;
            LimitLine mean_line;
            mean_line = new LimitLine(mean, getString(R.string.AV));
            mean_line.setLineWidth(2);

            XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            //xAxis.setLabelCount(5);
            xAxis.setTextSize(20);
            YAxis yAxis = lineChart.getAxisLeft();
            yAxis.removeAllLimitLines();
            yAxis.setSpaceTop(20);
            yAxis.setTextSize(20);
            yAxis.addLimitLine(mean_line);
            YAxis yAxis1 = lineChart.getAxisRight();
            yAxis1.setEnabled(false);

            LineDataSet set1 = new LineDataSet(yValues, getString(R.string.HR));
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
            lineChart.setNoDataText("請點選日期已取得圖片");
            lineChart.getDescription().setEnabled(false);
            lineChart.setData(lineData);
            lineChart.setDrawBorders(true);
            lineChart.setDrawGridBackground(true);

            linearLayout.setVisibility(View.VISIBLE);
            TextView max_value = v.findViewById(R.id.max_value_test);
            max_value.setText(String.format(getString(R.string.MAX)+"：%s", set1.getYMax()));
            LinearLayout linearLayout1 = v.findViewById(R.id.linear_info_test);
            TextView minimum_heart_rate = v.findViewById(R.id.min_heart_rate_test);
            TextView mean_heart_rate = v.findViewById(R.id.mean_heart_rate_test);
            mean_heart_rate.setText(String.format(Locale.getDefault(), getString(R.string.AVM)+"：%3.2f", mean));
            minimum_heart_rate.setText(String.format(getString(R.string.MIN)+"：%s", set1.getYMin()));
            TextView total_time = v.findViewById(R.id.total_time_test);
            total_time.setText(String.format(Locale.getDefault(), getString(R.string.sp_T)+"：%d "+getString(R.string.minute)+" %d "+getString(R.string.sec), ex_min, ex_sec));
            TextView ex_time = v.findViewById(R.id.ex_time_test);
            ex_time.setText(String.format(Locale.getDefault(), getString(R.string.DATE)+"：%s", string_date));
            TextView calorie = v.findViewById(R.id.calorie);
            double calorie_ = Double.valueOf(get_calorie);
            calorie.setText(String.format(Locale.getDefault(), getString(R.string.BUR_CAL)+"：%3.1f " + getString(R.string.cal), calorie_));
            LinearLayout linearLayout2 = v.findViewById(R.id.prepare_linear_info_test);
            linearLayout2.setVisibility(View.INVISIBLE);
            linearLayout1.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Log.e(TAG, "" + e);
        }
    }
}
