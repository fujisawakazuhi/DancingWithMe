package com.robot.asus.Sporden;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.RobotUtil;
import com.robot.asus.Sporden.Adapter.ExerciseHistoryAdapter;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ExerciseHistory extends RobotActivity {
    private static final String TAG = "ExerciseHistory";

    String responseData = "";

    String[] split_list;
    ArrayList<String> time_list = new ArrayList<>();
    ArrayList<String> sport_id_list = new ArrayList<>();
    ArrayList<String> sport_name_list = new ArrayList<>();
    ArrayList<HistoryData> datalist = new ArrayList<>();
    ArrayList<String> timesort = new ArrayList<>();

    HashMap<String, String> map = new HashMap<>();

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

    public ExerciseHistory () {
        super(robotCallback, robotListenCallback);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_history);
        //保持畫面不讓zenbo臉打斷
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();
        time_list = intent.getStringArrayListExtra("time_list");
        sport_id_list = intent.getStringArrayListExtra("sport_id_list");
        sport_name_list = intent.getStringArrayListExtra("sport_name_list");
        map = (HashMap<String, String>)intent.getSerializableExtra("map");
        timesort = intent.getStringArrayListExtra("timesort");

        ListView listView = findViewById(R.id.history_listView);

        /*for (int i = 0; i < time_list.size(); i++) {
            HistoryData data = new HistoryData(time_list.get(i), sport_name_list.get(i));
            datalist.add(data);
        }*/
        for(int i=0; i< timesort.size(); i++){
            HistoryData data = new HistoryData(timesort.get(i), map.get(timesort.get(i)));
            datalist.add(data);
        }
        ExerciseHistoryAdapter adapter = new ExerciseHistoryAdapter(this, R.layout.adapter_exercise_history, datalist);
        listView.setAdapter(adapter);
        Log.e(TAG, "sport_name_list"+ Arrays.toString(sport_name_list.toArray()));

        TextView backtomybord = findViewById(R.id.backtomybord);
        backtomybord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExerciseHistory.this, Myboard.class);
                startActivity(intent);
            }
        });


    }

    @Override
    protected void onStop() {
        super.onStop();
        sport_name_list.clear();
        sport_id_list.clear();
        time_list.clear();
        datalist.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();

        robotAPI.robot.setExpression(RobotFace.HIDEFACE);

        runTimerForDDE();
    }

    @Override
    protected void onPause() {
        super.onPause();

        handler.removeCallbacksAndMessages(null);
    }

    /*private void doInvokeAPI(String date) {
        // Create components of api request
        String method = "GET";
        String path = "/Health/object/:userId";

        //body放要post的資料(userId一定要有)
        //final String body = "{\"userId\":\"test11\"}";
        String body = "";
        byte[] content = body.getBytes(StringUtils.UTF8);

        Map parameters = new HashMap<>();
        parameters.put("lang", "en_US");
        //這邊放get要搜尋的哪個PK欄位
        parameters.put("userId", date);

        Map headers = new HashMap<>();

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
                    ApiResponse response = apiClient.execute(request);

                    InputStream responseContentStream = response.getContent();

                    if (responseContentStream != null) {
                        responseData = IOUtils.toString(responseContentStream);
                        Log.d(TAG, "Response : " + responseData);
                        //取json
                        String tmp_invoke = responseData.substring(responseData.indexOf("{"), responseData.lastIndexOf("}") + 1);
                        JSONObject json_read_invoke = new JSONObject(tmp_invoke);
                        String get_list_invoke = json_read_invoke.getString("sport_id");
                        //取[]裡的資料
                        //String done_list_invoke = get_list_invoke.substring(get_list_invoke.indexOf("[") + 1, get_list_invoke.indexOf("]"));

                        String get_time = json_read_invoke.getString("userId");
                        long get_time_1 = Long.parseLong(get_time);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);
                        String in = simpleDateFormat.format(get_time_1);
                        map.put(in, get_list_invoke);

                        if (!get_list_invoke.equals("")) {
                            sport_id_list.add(get_list_invoke);
                        } else {
                            sport_id_list.add("null");
                        }

                    }
                    Log.d(TAG, "status code = " + response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
    }*/

    private void runTimerForDDE() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (start_back) {
                    Intent intent = new Intent(ExerciseHistory.this, Myboard.class);
                    startActivity(intent);
                    start_back = false;
                }
                Log.d("lxhandler", "ExerciseHistroy");
                handler.postDelayed(this, 50);
            }
        });
    }
}
