package com.robot.asus.Sporden;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotUtil;
import com.asus.robotframework.API.SpeakConfig;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import android.widget.ImageButton;
import com.robot.asus.Sporden.spodenIndex.SpodenIndexClient;
import com.robot.asus.Sporden.spordenREST.SpordenRESTClient;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static java.lang.Thread.sleep;

/*
94e18f97-2b82-4eef-87d4-1ee898fab94e  有跑
8252b83e-55e5-4427-a8a3-1e5fa313127e  快跑
ec9accfc-cd50-4dbf-8300-7fb4565f9ce7  慢跑
521c8f94-f774-4eeb-9273-831c68fe6aa1  約跑
ed3c00f6-f2fc-49a1-b109-343d10047e68  胡玲
a084e394-fe37-47aa-9bbe-ccbe907257da  硬舉
51d26c54-ff43-463a-82ba-3c181312de33  前後甩手
 */

public class Myboard extends RobotActivity {

    private static final String TAG = "MyBoard";
    private SpordenRESTClient apiClient;
    private SpodenIndexClient indexClient;
    private String acc_email;
    String responseData = "";
    ArrayList<String> split_list_1 = new ArrayList<>();
    String[] split_list;
    ArrayList<String> sport_id_list = new ArrayList<>();
    ArrayList<String> time_list = new ArrayList<>();
    ArrayList<String> sport_name_list = new ArrayList<>();
    ArrayList<String> time_sort = new ArrayList<>();

    final String sit_down_stretch = "1b390f85-dcac-4d87-8a34-58ba814eed80";
    final String step_right_there = "29297418-10ce-45c1-871b-267450275532";
    final String left_right_stretch = "40f95846-27c6-46cb-a473-e027592ab622";
    final String swinghand = "51d26c54-ff43-463a-82ba-3c181312de33";
    final String stretch_top = "56c6fbc1-b2cb-4467-9ee3-90344a543551";
    final String arm_stretch = "600a6b72-b76d-45ed-8b17-4f0e5a8949ce";
    final String top_down_stretch = "629e77be-35b0-4dbc-af3d-971337457e91";
    final String step_up_exercise = "64aa89f7-dd73-413c-affa-f0541edff152";
    final String hug_knee_balance = "6a6c6542-214b-4a8e-b36c-182e93ccb167";
    final String squat = "73257c8d-7673-4bdc-83c1-aba39e8f7feb";
    final String foot_front_back = "78f06e53-41ec-4177-a972-b813bb74e81f";
    final String draw_circle_by_hand = "8ecfe05a-2fe8-422a-9f2a-7e1633786a94";
    final String sitting_stretch_forward = "919b0430-3e18-4364-b2b9-c0274675e93d";
    final String hardtake = "a084e394-fe37-47aa-9bbe-ccbe907257da";
    final String one_foot_balance = "c28b0695-9abd-4fb7-b6f3-75345e72ead2";
    final String press_knee = "dda7102e-f896-4e24-a1eb-c287bcad6a15";

    ArrayList<String> hug_knee_balance_list = new ArrayList<>();
    ArrayList<String> press_knee_list = new ArrayList<>();
    ArrayList<String> sitting_stretch_forward_list = new ArrayList<>();
    ArrayList<String> draw_circle_by_hand_list = new ArrayList<>();
    ArrayList<String> step_up_exercise_list = new ArrayList<>();
    ArrayList<String> arm_stretch_list = new ArrayList<>();
    ArrayList<String> step_right_there_list = new ArrayList<>();
    ArrayList<String> sit_down_stretch_list = new ArrayList<>();
    ArrayList<String> one_foot_balance_list = new ArrayList<>();
    ArrayList<String> foot_front_back_list = new ArrayList<>();
    ArrayList<String> squat_list = new ArrayList<>();
    ArrayList<String> stretch_top_list = new ArrayList<>();
    ArrayList<String> left_right_stretch_list = new ArrayList<>();
    ArrayList<String> hard_take_list = new ArrayList<>();
    ArrayList<String> swing_hand_list = new ArrayList<>();
    ArrayList<String> top_down_stretch_list = new ArrayList<>();

    ArrayList<String> num_list = new ArrayList<>();

    HashMap<String, String> map = new HashMap<>();

    private static int num = 0;
    private static int count = 0;
    int countOnThread = 0;

    TextView exercise_history;

    int check_in_index = 0;
    int insure = 0;
    ArrayList<String> insure_num = new ArrayList<>();
    ArrayList<String> date_list = new ArrayList<>();
    HashMap<String, Double> map_cal = new HashMap<>();

    Handler handler = new Handler();
    private ProgressBar progressBar;
    private static boolean start_heart = false;
    private static boolean start_calories = false;
    private static boolean start_histroy = false;
    private static boolean start_back = false;
    private Handler handler2 = new Handler();

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
            if(sIntentionID.equals("MyBoard")) {
                //取這個plan的那個要得變數
                String sSluResultButton = RobotUtil.queryListenResultJson(jsonObject, "數據", null);
                Log.d(TAG, "Result Button = " + sSluResultButton);

                //要和concept的instance一樣(這是把變數拿來比對的條件式)
                if(sSluResultButton.equals("心跳")) {
                    start_heart = true;
                } else if (sSluResultButton.equals("卡路里")) {
                    start_calories = true;
                } else if (sSluResultButton.equals("歷史資料")) {
                    start_histroy = true;
                }
            }

            //如果是這個plan
            if(sIntentionID.equals("BackToMain")) {
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

    public Myboard () {
        super(robotCallback, robotListenCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myboard);
        //保持畫面不讓zenbo臉打斷
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TextView backtomain = findViewById(R.id.backtomain);
        backtomain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Myboard.this, MainActivity.class);
                startActivity(intent);
                robotAPI.robot.stopSpeakAndListen();
                System.exit(0);
            }
        });

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

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (acct != null) {
            //取得google帳戶資訊
            acc_email = acct.getEmail();
        }

        TextView heart_rate_history = findViewById(R.id.HeartRate);
        heart_rate_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                init();
                                progressBar.setVisibility(View.VISIBLE);
                                if (GetLocale.getLocale().equals("zh")) {
                                    Toast.makeText(getApplicationContext(), "請稍等...", Toast.LENGTH_LONG).show();
                                } else if (GetLocale.getLocale().equals("en")){
                                    Toast.makeText(getApplicationContext(), "Wait a Minute...", Toast.LENGTH_LONG).show();
                                }

                            }
                        });
                        doHistory();
                        /*
                        try {
                            while (sport_id_list.size() != split_list_1.size()) {
                                sleep(99);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/

                    }

                }).start();
            }

        });
        num = 0;
        exercise_history = findViewById(R.id.history_data);
        exercise_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        check_in_index = 0;
                        exercise_history.setClickable(false);
                        insure = 0;
                        count = 0;
                        //map.clear();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                init();
                                progressBar.setVisibility(View.VISIBLE);
                                if (GetLocale.getLocale().equals("zh")) {
                                    Toast.makeText(getApplicationContext(), "請稍等...", Toast.LENGTH_LONG).show();
                                } else if (GetLocale.getLocale().equals("en")) {
                                    Toast.makeText(getApplicationContext(), "Wait a Minute...", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        doExerciseHistory();

                        /*try {
                            while (count != time_list.size())
                                Thread.sleep(200);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/

                    }
                }).start();
            }
        });

        TextView history_calorie = findViewById(R.id.Calorie);
        history_calorie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                init();
                                progressBar.setVisibility(View.VISIBLE);
                                if (GetLocale.getLocale().equals("zh")) {
                                    Toast.makeText(getApplicationContext(), "請稍等...", Toast.LENGTH_LONG).show();
                                } else if (GetLocale.getLocale().equals("en")) {
                                    Toast.makeText(getApplicationContext(), "Wait a Minute...", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        doCaloriePic();
                        /*try {
                            while (date_list.size() != num) {
                                Thread.sleep(99);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/

                    }

                }).start();

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        split_list_1.clear();
        sport_id_list.clear();
        left_right_stretch_list.clear();
        swing_hand_list.clear();
        one_foot_balance_list.clear();
        foot_front_back_list.clear();
        squat_list.clear();
        stretch_top_list.clear();
        hard_take_list.clear();
        time_list.clear();
        sport_name_list.clear();
        hug_knee_balance_list.clear();
        press_knee_list.clear();
        sitting_stretch_forward_list.clear();
        draw_circle_by_hand_list.clear();
        step_up_exercise_list.clear();
        arm_stretch_list.clear();
        step_right_there_list.clear();
        date_list.clear();
        sit_down_stretch_list.clear();
        if (progressBar != null)
            progressBar.setVisibility(View.GONE);
        exercise_history.setClickable(true);

        //robotAPI.robot.speakAndListen("你想要查看什麼紀錄呢?", new SpeakConfig().timeout(20));
        if (GetLocale.getLocale().equals("zh")) {
            robotAPI.robot.speak("你想要查看什麼紀錄呢?");
        } else if (GetLocale.getLocale().equals("en")) {
            robotAPI.robot.speak("Which record do you want to see?");
        }

        runTimerForDDE();
    }

    @Override
    protected void onPause() {
        super.onPause();

        handler2.removeCallbacksAndMessages(null);
        handler.removeCallbacksAndMessages(null);
        robotAPI.robot.stopSpeakAndListen();
    }

    private void init() {
        this.progressBar = findViewById(R.id.circular_progressBar);
    }

    private void doExerciseHistory() {
        int times_count = 0;

        try {
            sport_id_list.clear();
            time_list.clear();
            doInvokeIndexfor();
            /*while (check_in_index != -1) {
                sleep(20);
            }


            Log.e(TAG, "time_list_size:" + time_list.size());
            for (int i = 0; i < time_list.size(); i++) {
                doInvokeAPIfor(time_list.get(i));
                Log.e(TAG, "這是第" + (i + 1) + "次");
            }

            while (insure_num.size() != time_list.size()) {
                sleep(500);
            }

            //while (sport_id_list.size() != time_list.size())
            //   Thread.sleep(150);


            //Log.e(TAG, "sportlist=" + Arrays.toString(sport_id_list.toArray()));

            /*for (int k = 0; k < time_list.size(); k++) {
                long get_time_1 = Long.parseLong(time_list.get(k));
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String in = simpleDateFormat.format(get_time_1);
                time_list.set(k, in);
                times_count++;
            }while (times_count!=time_list.size())
                Thread.sleep(20);
            */
            /*Log.e(TAG, "times_count:" + times_count);

            /*for (String key : map.keySet()) {
                long get_time_1 = Long.parseLong(key);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String in = simpleDateFormat.format(get_time_1);
                map.put(key, in);
            }*/


            /*for (String key : map.keySet()) {
                //Log.e(TAG, "這有嬤"+map.get(time_list.get(j)));
                count++;
                switch (map.get(key)) {
                    case left_right_stretch:
                        map.put(key, "左右伸展");
                        Log.e(TAG, "左右伸展 時間:" + key);
                        break;
                    case swinghand:
                        map.put(key, "前後甩手");
                        Log.e(TAG, "前後甩手 時間." + key);
                        break;
                    case stretch_top:
                        map.put(key, "上下拉筋");
                        Log.e(TAG, "左右伸展 時間." + key);
                        break;
                    case squat:
                        map.put(key, "簡易深蹲");
                        Log.e(TAG, "簡易深蹲 時間." + key);
                        break;
                    case foot_front_back:
                        map.put(key, "前後腳點");
                        Log.e(TAG, "前後腳點 時間." + key);
                        break;
                    case hardtake:
                        map.put(key, "硬舉");
                        Log.e(TAG, "硬舉 時間." + key);
                        break;
                    case one_foot_balance:
                        map.put(key, "單腳平衡");
                        Log.e(TAG, "單腳平衡 時間." + key);
                        break;
                    case top_down_stretch:
                        map.put(key, "上下伸展");
                        Log.e(TAG, "上下伸展 時間." + key);
                        break;
                    case sit_down_stretch:
                        map.put(key, "坐式伸展");
                        Log.e(TAG, "坐式伸展 時間." + key);
                        break;
                    case step_right_there:
                        map.put(key, "原地踏步");
                        Log.e(TAG, "原地踏步 時間." + key);
                        break;
                    case arm_stretch:
                        map.put(key, "手臂伸展");
                        Log.e(TAG, "手臂伸展 時間." + key);
                        break;
                    case step_up_exercise:
                        map.put(key, "踏台運動");
                        Log.e(TAG, "踏台運動 時間." + key);
                        break;
                    case hug_knee_balance:
                        map.put(key, "抱膝平衡");
                        Log.e(TAG, "抱膝平衡 時間." + key);
                        break;
                    case draw_circle_by_hand:
                        map.put(key, "雙手畫圓操");
                        Log.e(TAG, "雙手畫圓操 時間." + key);
                        break;
                    case sitting_stretch_forward:
                        map.put(key, "坐式向前伸展");
                        Log.e(TAG, "坐式向前伸展 時間." + key);
                        break;
                    case press_knee:
                        map.put(key, "壓膝");
                        Log.e(TAG, "壓膝 時間." + key);
                        break;
                }
            }
            for(String key: map.keySet()){
                time_sort.add(key);
            }
            Collections.sort(time_sort);
            Collections.reverse(time_sort);
            Log.e(TAG, "count=" + count);


            Intent intent = new Intent();
            intent.putExtra("time_list", time_list);
            Log.e(TAG, "time=" + time_list);
            intent.putExtra("sport_id_list", sport_id_list);
            intent.putExtra("sport_name_list", sport_name_list);
            intent.putExtra("map", map);
            intent.putExtra("timesort", time_sort);
            intent.setClass(Myboard.this, ExerciseHistory.class);
            startActivity(intent);*/
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private void doInvokeIndexfor() {
        // Create components of api request
        final String method = "GET";
        final String path = "/index/object/:user_email";

        //body放要post的資料(userId一定要有)
        final String body = "";
        final byte[] content = body.getBytes(StringUtils.UTF8);

        final Map parameters = new HashMap<>();
        parameters.put("lang", "en_US");
        //這邊放get要搜尋的哪個PK欄位(找哪個帳號的資料)
        parameters.put("user_email", acc_email);

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
                        String done_list = get_list.substring(get_list.indexOf("[") + 1, get_list.indexOf("]"));
                        String[] split_list = done_list.split(", ");
                        Log.d(TAG, "split_list" + Arrays.toString(split_list));
                        time_list.addAll(Arrays.asList(split_list));
                        Collections.sort(time_list);
                        Collections.reverse(time_list);
                        Log.e(TAG, Arrays.toString(time_list.toArray()));

                        for (int i = 0; i < time_list.size() - 1; i++) {
                            if (time_list.get(i).equals(time_list.get(i + 1))) {
                                time_list.remove(i);
                                i--;
                            }
                        }

                        //check_in_index = -1;

                        /*while (check_in_index != -1) {
                            sleep(20);
                        }*/

                        insure_num.clear();


                        Log.e(TAG, "time_list_size:" + time_list.size());
                        for (int i = 0; i < time_list.size(); i++) {
                            doInvokeAPIfor(time_list.get(i));
                            Log.e(TAG, "這是第" + (i + 1) + "次");
                            sleep(150);
                        }

                        while (count != time_list.size()) {
                            sleep(300);
                        }

                        //while (sport_id_list.size() != time_list.size())
                        //   Thread.sleep(150);


                        //Log.e(TAG, "sportlist=" + Arrays.toString(sport_id_list.toArray()));

            /*for (int k = 0; k < time_list.size(); k++) {
                long get_time_1 = Long.parseLong(time_list.get(k));
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String in = simpleDateFormat.format(get_time_1);
                time_list.set(k, in);
                times_count++;
            }while (times_count!=time_list.size())
                Thread.sleep(20);
            */
                        //Log.e(TAG, "times_count:" + times_count);

            /*for (String key : map.keySet()) {
                long get_time_1 = Long.parseLong(key);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String in = simpleDateFormat.format(get_time_1);
                map.put(key, in);
            }*/


                                                                        /**有加運動的話這邊要改
                                                                                                                          * **
                                                                                                                   * **
                                                                                                           * */
                        for (String key : map.keySet()) {
                            //Log.e(TAG, "這有嬤"+map.get(time_list.get(j)));
                            count++;
                            switch (map.get(key)) {
                                case left_right_stretch:
                                    map.put(key, getString(R.string.LR_extend));
                                    Log.e(TAG, "左右伸展 時間:" + key);
                                    break;
                                case swinghand:
                                    map.put(key, getString(R.string.FW_shakehands));
                                    Log.e(TAG, "前後甩手 時間." + key);
                                    break;
                                case stretch_top:
                                    map.put(key, getString(R.string.UD_stretch));
                                    Log.e(TAG, "左右伸展 時間." + key);
                                    break;
                                case squat:
                                    map.put(key, getString(R.string.SM_squat));
                                    Log.e(TAG, "簡易深蹲 時間." + key);
                                    break;
                                case foot_front_back:
                                    map.put(key, getString(R.string.FW_footpoint));
                                    Log.e(TAG, "前後腳點 時間." + key);
                                    break;
                                case hardtake:
                                    map.put(key, getString(R.string.DL));
                                    Log.e(TAG, "硬舉 時間." + key);
                                    break;
                                case one_foot_balance:
                                    map.put(key, getString(R.string.SM_balance));
                                    Log.e(TAG, "單腳平衡 時間." + key);
                                    break;
                                case top_down_stretch:
                                    map.put(key, getString(R.string.UD_extension));
                                    Log.e(TAG, "上下伸展 時間." + key);
                                    break;
                                case sit_down_stretch:
                                    map.put(key, getString(R.string.ST_extend));
                                    Log.e(TAG, "坐式伸展 時間." + key);
                                    break;
                                case step_right_there:
                                    map.put(key, getString(R.string.MO_step));
                                    Log.e(TAG, "原地踏步 時間." + key);
                                    break;
                                case arm_stretch:
                                    map.put(key, getString(R.string.AM_extend));
                                    Log.e(TAG, "手臂伸展 時間." + key);
                                    break;
                                case step_up_exercise:
                                    map.put(key, getString(R.string.BX_exercise));
                                    Log.e(TAG, "踏台運動 時間." + key);
                                    break;
                                case hug_knee_balance:
                                    map.put(key, getString(R.string.KE_balance));
                                    Log.e(TAG, "抱膝平衡 時間." + key);
                                    break;
                                case draw_circle_by_hand:
                                    map.put(key, getString(R.string.HN_circle));
                                    Log.e(TAG, "雙手畫圓操 時間." + key);
                                    break;
                                case sitting_stretch_forward:
                                    map.put(key, getString(R.string.STF_extend));
                                    Log.e(TAG, "坐式向前伸展 時間." + key);
                                    break;
                                case press_knee:
                                    map.put(key, getString(R.string.PR_knee));
                                    Log.e(TAG, "壓膝 時間." + key);
                                    break;
                            }
                        }
                        for (String key : map.keySet()) {
                            time_sort.add(key);
                        }
                        Collections.sort(time_sort);
                        Collections.reverse(time_sort);
                        Log.e(TAG, "count=" + count);


                        Intent intent = new Intent();
                        intent.putExtra("time_list", time_list);
                        Log.e(TAG, "time=" + time_list);
                        intent.putExtra("sport_id_list", sport_id_list);
                        intent.putExtra("sport_name_list", sport_name_list);
                        intent.putExtra("map", map);
                        intent.putExtra("timesort", time_sort);
                        intent.setClass(Myboard.this, ExerciseHistory.class);
                        startActivity(intent);

                    }

                    Log.d(TAG, "status code = " + response.getStatusCode() + " " + response.getStatusText());
                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();

    }


    private void doInvokeAPIfor(String date) {
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
                        Log.d(TAG, "Responsef : " + responseData + ", c : " + (count+=1));
                        //insure_num.add(responseData);
                        //Log.d(TAG, Integer.toString(insure_num.size()));
                        //取json
                        if (responseData != null) {
                            String tmp_invoke = responseData.substring(responseData.indexOf("{"), responseData.lastIndexOf("}") + 1);
                            JSONObject json_read_invoke = new JSONObject(tmp_invoke);
                            String get_list_invoke = json_read_invoke.getString("sport_id");
                            //取[]裡的資料
                            //String done_list_invoke = get_list_invoke.substring(get_list_invoke.indexOf("[") + 1, get_list_invoke.indexOf("]"));

                            String get_time = json_read_invoke.getString("userId");
                            long get_time_1 = Long.parseLong(get_time);
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                            String in = simpleDateFormat.format(get_time_1);
                            map.put(in, get_list_invoke);

                            if (!get_list_invoke.equals("")) {
                                sport_id_list.add(get_list_invoke);
                            } else {
                                sport_id_list.add("null");
                            }

                            Log.e(TAG, "userId = " + get_time);
                        }
                    }
                    Log.d(TAG, "status code = " + response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
    }


    private void doHistory() {
        split_list_1.clear();
        sport_id_list.clear();
        time_list.clear();
        num_list.clear();
        //處理無重複的done_list串列 split_list_1為結果
        doInvokeIndex();

        /*try {
            while (split_list_1.size() == 0) {
                sleep(50);
            }
            for (int i = 0; i < split_list_1.size(); i++) {
                doInvokeAPI(split_list_1.get(i));
            }
            //sport_id_list為每一次健康數據紀錄的運動id
            while (sport_id_list.size() != split_list_1.size())
                sleep(100);

            Collections.sort(sport_id_list);
            for (int i = 0; i < sport_id_list.size() - 1; i++) {
                if (sport_id_list.get(i).equals("null")) {
                    sport_id_list.remove(i);
                    i--;
                } else if (sport_id_list.get(i).equals(sport_id_list.get(i + 1))) {
                    sport_id_list.remove(i);
                    i--;
                }
            }
            Log.e(TAG, "sportlist" + Arrays.toString(sport_id_list.toArray()));
            Intent intent = new Intent();
            intent.putExtra("sport_id_list", sport_id_list);
            intent.putExtra("split_list_1", split_list_1);
            intent.putExtra("one_foot_balance_list", one_foot_balance_list);
            intent.putExtra("foot_front_back_list", foot_front_back_list);
            intent.putExtra("squat_list", squat_list);
            intent.putExtra("stretch_top_list", stretch_top_list);
            intent.putExtra("left_right_stretch_list", left_right_stretch_list);
            intent.putExtra("hard_take_list", hard_take_list);
            intent.putExtra("swing_hand_list", swing_hand_list);
            intent.putExtra("top_down_stretch_list", top_down_stretch_list);
            intent.putExtra("hug_knee_balance", hug_knee_balance_list);
            intent.putExtra("press_knee", press_knee_list);
            intent.putExtra("sitting_stretch_forward", sitting_stretch_forward_list);
            intent.putExtra("draw_circle_by_hand", draw_circle_by_hand_list);
            intent.putExtra("step_up_exercise", step_up_exercise_list);
            intent.putExtra("arm_stretch", arm_stretch_list);
            intent.putExtra("step_right_there", step_right_there_list);
            intent.putExtra("sit_down_stretch", sit_down_stretch_list);

            intent.setClass(Myboard.this, HeartRateHistory.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }*/
    }

    private void doCaloriePic() {
        time_list.clear();
        date_list.clear();
        num_list.clear();
        //將done_list時間id處理出來加入time_list
        doInvokeIndexforCalPic();

        /*try {
            while (time_list.size() == 0) {
                Thread.sleep(50);
            }
            for (int i = 0; i < time_list.size(); i++) {
                doInvokeAPIforCalPic(time_list.get(i));
            }
            while (num != time_list.size())
                Thread.sleep(200);

            //求map裡面的值
            for (String key : date_list) {
                Log.e(TAG, key + " : " + map.get(key));
            }
            Collections.sort(date_list);

            Intent intent = new Intent();
            intent.putExtra("date_list", date_list);
            intent.putExtra("map_cal", map_cal);
            intent.setClass(Myboard.this, CaloriePicture.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }*/

    }


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
        parameters.put("user_email", acc_email);

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
                        Log.d(TAG, "split_list" + Arrays.toString(split_list));
                        split_list_1 = new ArrayList<>();
                        //split_list_1.addAll(Arrays.asList(split_list));
                        int num1 = split_list.length - 1;
                        for (int ii = 0; ii < split_list.length; ii++) {
                            split_list_1.add(split_list[num1]);
                            num1--;
                        }
                        //移除重複done_list
                        for (int i = 0; i < split_list_1.size() - 1; i++) {
                            if (split_list_1.get(i).equals(split_list_1.get(i + 1))) {
                                split_list_1.remove(i);
                                i--;
                            }
                        }
                        Log.d(TAG, Arrays.toString(split_list_1.toArray()));

                        /*while (split_list_1.size() == 0) {
                            sleep(50);
                        }*/

                        num = 0;
                        int test_num = 0;
                        count = 0;
                        countOnThread = 0;

                        for (int i = 0; i < split_list_1.size(); i++) {
                            test_num += 1;
                            Log.d(TAG, "numtest :" + test_num);
                            doInvokeAPI(split_list_1.get(i));
                            sleep(50);
                        }

                        Log.d(TAG, "split_list: " + split_list_1.size());
                        while (num_list.size() != split_list_1.size()) {
                            Log.d(TAG, "num under: " + num);
                                sleep(100);
                                Log.d(TAG,"sleep");
                        }

                        Collections.sort(sport_id_list);
                        for (int i = 0; i < sport_id_list.size() - 1; i++) {
                            if (sport_id_list.get(i).equals("null")) {
                                sport_id_list.remove(i);
                                i--;
                            } else if (sport_id_list.get(i).equals(sport_id_list.get(i + 1))) {
                                sport_id_list.remove(i);
                                i--;
                            }
                        }
                        Log.e(TAG, "sportlist" + Arrays.toString(sport_id_list.toArray()));
                        Intent intent = new Intent();
                        intent.putExtra("sport_id_list", sport_id_list);
                        intent.putExtra("split_list_1", split_list_1);
                        intent.putExtra("one_foot_balance_list", one_foot_balance_list);
                        intent.putExtra("foot_front_back_list", foot_front_back_list);
                        intent.putExtra("squat_list", squat_list);
                        intent.putExtra("stretch_top_list", stretch_top_list);
                        intent.putExtra("left_right_stretch_list", left_right_stretch_list);
                        intent.putExtra("hard_take_list", hard_take_list);
                        intent.putExtra("swing_hand_list", swing_hand_list);
                        intent.putExtra("top_down_stretch_list", top_down_stretch_list);
                        intent.putExtra("hug_knee_balance", hug_knee_balance_list);
                        intent.putExtra("press_knee", press_knee_list);
                        intent.putExtra("sitting_stretch_forward", sitting_stretch_forward_list);
                        intent.putExtra("draw_circle_by_hand", draw_circle_by_hand_list);
                        intent.putExtra("step_up_exercise", step_up_exercise_list);
                        intent.putExtra("arm_stretch", arm_stretch_list);
                        intent.putExtra("step_right_there", step_right_there_list);
                        intent.putExtra("sit_down_stretch", sit_down_stretch_list);

                        intent.setClass(Myboard.this, HeartRateHistory.class);
                        startActivity(intent);
                    }

                    Log.d(TAG, "status code = " + response.getStatusCode() + " " + response.getStatusText());
                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();

    }

    private void doInvokeAPI(String date) {
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

        count += 1;
        Log.d(TAG, "ResponseofHealth before : " + count);

        // Make network call on background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    countOnThread += 1;
                    Log.d("ResponseofHealth ; ", "OnThread " + Integer.toString(countOnThread));
                    Log.d("ResponseofHealth : ",
                            "Invoking API w/ Request : " +
                                    request.getHttpMethod() + ":" +
                                    request.getPath() + countOnThread);
                    ApiResponse response = apiClient.execute(request);

                    InputStream responseContentStream = response.getContent();

                    Log.d("ResponseofHealth : ", "response : " + response + ", stream: " + responseContentStream.toString() + (num + 1));

                    if (responseContentStream != null) {
                        responseData = IOUtils.toString(responseContentStream);
                        Log.d(TAG, "ResponseofHealth : " + responseData);
                        num+=1;
                        Log.d(TAG, "ResponseofHealth num : " + num);
                        num_list.add("1");
                        //取json
                        if (responseData != null) {
                            String tmp_invoke = responseData.substring(responseData.indexOf("{"), responseData.lastIndexOf("}") + 1);
                            JSONObject json_read_invoke = new JSONObject(tmp_invoke);
                            String get_list_invoke = json_read_invoke.getString("sport_id");
                            //取[]裡的資料
                            //String done_list_invoke = get_list_invoke.substring(get_list_invoke.indexOf("[") + 1, get_list_invoke.indexOf("]"));
                            if (!get_list_invoke.equals("")) {
                                Log.e(TAG, "sport_id:" + get_list_invoke);
                                sport_id_list.add(get_list_invoke);
                            } else {
                                sport_id_list.add("null");
                                Log.d(TAG, "sport_id null");
                            }

                            String get_time = json_read_invoke.getString("userId");



                            //將各個運動的運動時間歸類到他的list
                            switch (get_list_invoke) {
                                case one_foot_balance:
                                    one_foot_balance_list.add(date_converted(get_time));
                                    break;
                                case foot_front_back:
                                    foot_front_back_list.add(date_converted(get_time));
                                    break;
                                case squat:
                                    squat_list.add(date_converted(get_time));
                                    break;
                                case stretch_top:
                                    stretch_top_list.add(date_converted(get_time));
                                    break;
                                case left_right_stretch:
                                    left_right_stretch_list.add(date_converted(get_time));
                                    break;
                                case hardtake:
                                    hard_take_list.add(date_converted(get_time));
                                    break;
                                case swinghand:
                                    swing_hand_list.add(date_converted(get_time));
                                    break;
                                case top_down_stretch:
                                    top_down_stretch_list.add(date_converted(get_time));
                                    break;
                                case hug_knee_balance:
                                    hug_knee_balance_list.add(date_converted(get_time));
                                    break;
                                case press_knee:
                                    press_knee_list.add(date_converted(get_time));
                                    break;
                                case sitting_stretch_forward:
                                    sitting_stretch_forward_list.add(date_converted(get_time));
                                    break;
                                case draw_circle_by_hand:
                                    draw_circle_by_hand_list.add(date_converted(get_time));
                                    break;
                                case step_up_exercise:
                                    step_up_exercise_list.add(date_converted(get_time));
                                    break;
                                case arm_stretch:
                                    arm_stretch_list.add(date_converted(get_time));
                                    break;
                                case step_right_there:
                                    step_right_there_list.add(date_converted(get_time));
                                    break;
                                case sit_down_stretch:
                                    sit_down_stretch_list.add(date_converted(get_time));
                                    break;
                            }
                        }
                    }
                    Log.d(TAG, "ResponseofHealth status code " + num + " = " + response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.d("ResponseofHealth", exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    private String date_converted(String date) {
        long get_time = Long.parseLong(date);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return simpleDateFormat.format(get_time);
    }

    private void doInvokeIndexforCalPic() {
        //split_list_list_reverse.clear();
        //split_list_list.clear();
        // Create components of api request
        final String method = "GET";
        final String path = "/index/object/:user_email";

        //body放要post的資料(userId一定要有)
        final String body = "";
        final byte[] content = body.getBytes(StringUtils.UTF8);

        final Map parameters = new HashMap<>();
        parameters.put("lang", "en_US");
        //這邊放get要搜尋的哪個PK欄位(找哪個帳號的資料)
        parameters.put("user_email", acc_email);

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
                        String done_list = get_list.substring(get_list.indexOf("[") + 1, get_list.indexOf("]"));
                        split_list = done_list.split(", ");
                        Log.d(TAG, "split_list" + Arrays.toString(split_list));
                        time_list.addAll(Arrays.asList(split_list));
                        Collections.sort(time_list);
                        Log.e(TAG, Arrays.toString(time_list.toArray()));

                        //將重複的時間刪除
                        for (int i = 0; i < time_list.size() - 1; i++) {
                            if (time_list.get(i).equals(time_list.get(i + 1))) {
                                time_list.remove(i);
                                i--;
                            }
                        }

                        num = 0;
                        count = 0;

                        //用time_list去找這個帳戶做過的運動的卡路里資料
                        for (int i = 0; i < time_list.size(); i++) {
                            //速度太快似乎會有重複回傳值計算累加的可能性，加長回傳時間好像可以避免
                            Thread.sleep(130);
                            doInvokeAPIforCalPic(time_list.get(i));
                        }

                        while (num_list.size() != time_list.size()) {
                            Thread.sleep(200);
                        }

                        //求map裡面的值
                        for (String key : date_list) {
                            Log.e(TAG, key + " : " + map.get(key));
                        }
                        Collections.sort(date_list);

                        Intent intent = new Intent();
                        intent.putExtra("date_list", date_list);
                        intent.putExtra("map_cal", map_cal);
                        intent.setClass(Myboard.this, CaloriePicture.class);
                        startActivity(intent);
                    }

                    Log.d(TAG, "status code = " + response.getStatusCode() + " " + response.getStatusText());
                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();

    }

    private void doInvokeAPIforCalPic(String date) {
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

                    count += 1;
                    Log.d(TAG, "ResponseofHealth before : " + count);

                    InputStream responseContentStream = response.getContent();

                    if (responseContentStream != null) {
                        responseData = IOUtils.toString(responseContentStream);
                        Log.d(TAG, "ResponseofHealth : " + responseData);
                        num += 1;
                        num_list.add("1");
                        Log.d(TAG, "ResponseofHealth num: " + Integer.toString(num));
                        //取json
                        String tmp_invoke = responseData.substring(responseData.indexOf("{"), responseData.lastIndexOf("}") + 1);
                        JSONObject json_read_invoke = new JSONObject(tmp_invoke);
                        String get_list_invoke = json_read_invoke.getString("calories");
                        get_list_invoke = String.format(Locale.getDefault(), "%3.1f", Double.valueOf(get_list_invoke));
                        Double calorie = Double.valueOf(get_list_invoke);
                        //將unixtime轉正式格式的日期
                        String get_time_long = json_read_invoke.getString("userId");
                        long split_test_in = Long.parseLong(get_time_long);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String in = simpleDateFormat.format(split_test_in);
                        Log.d("responseofhealthDATA", date_list.toString() + " date: " + in + " calories: " + calorie + " num: " + num);
                        //如果date_list有東西
                        if (date_list.size() != 0) {
                            //如果date_list有重複的日期(同一天)
                            if (date_list.contains((in))) {
                                //將該筆資料卡路里加上去，形成累積一天的卡路里，並放入該天的map
                                Double temp = map_cal.get(in);
                                temp = temp + calorie;
                                String temp_string = String.format(Locale.getDefault(), "%3.1f", temp);
                                map_cal.put(in, Double.valueOf(temp_string));
                            } else {
                                //將正式日期時間加入date_list，將正式日期、卡路里加入map_cal
                                date_list.add(in);
                                map_cal.put(in, calorie);
                            }
                        } else {
                            //將正式日期時間加入date_list，將正式日期、卡路里加入map_cal
                            date_list.add(in);
                            map_cal.put(in, calorie);
                        }
                    }
                    Log.d(TAG, "status code = " + response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();

    }

    private void runTimerForDDE() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (start_heart) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    init();
                                    progressBar.setVisibility(View.VISIBLE);
                                    if (GetLocale.getLocale().equals("zh")) {
                                        Toast.makeText(getApplicationContext(), "請稍等...", Toast.LENGTH_LONG).show();
                                    } else if (GetLocale.getLocale().equals("en")) {
                                        Toast.makeText(getApplicationContext(), "Wait a Minute...", Toast.LENGTH_LONG).show();
                                    }

                                }
                            });
                            doHistory();
                            try {
                                while (sport_id_list.size() != split_list_1.size()) {
                                    sleep(99);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }

                    }).start();
                    start_heart = false;
                } else if (start_calories) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    init();
                                    progressBar.setVisibility(View.VISIBLE);
                                    if (GetLocale.getLocale().equals("zh")) {
                                        Toast.makeText(getApplicationContext(), "請稍等...", Toast.LENGTH_LONG).show();
                                    } else if (GetLocale.getLocale().equals("en")) {
                                        Toast.makeText(getApplicationContext(), "Wait a Minute...", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                            doCaloriePic();
                            try {
                                while (date_list.size() != num) {
                                    sleep(99);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }

                    }).start();
                    start_calories = false;
                } else if (start_histroy) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    init();
                                    progressBar.setVisibility(View.VISIBLE);
                                    if (GetLocale.getLocale().equals("zh")) {
                                        Toast.makeText(getApplicationContext(), "請稍等...", Toast.LENGTH_LONG).show();
                                    } else if (GetLocale.getLocale().equals("en")) {
                                        Toast.makeText(getApplicationContext(), "Wait a Minute...", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                            doExerciseHistory();
                            try {
                                while (sport_id_list.size() != split_list_1.size()) {
                                    sleep(99);
                                }
                                Log.d(TAG, "sport_id:" + Arrays.toString(sport_id_list.toArray()));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }

                    }).start();
                    start_histroy = false;
                } else if (start_back) {
                    Intent intent = new Intent(Myboard.this, MainActivity.class);
                    startActivity(intent);
                    start_back = false;
                    robotAPI.robot.stopSpeakAndListen();
                    System.exit(0);
                }
                Log.d("lxhandler", "MyBoard");
                handler.postDelayed(this, 50);
            }
        });
    }
}
