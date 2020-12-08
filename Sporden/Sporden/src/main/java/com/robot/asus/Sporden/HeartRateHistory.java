package com.robot.asus.Sporden;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.RobotUtil;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HeartRateHistory extends RobotActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "HeartRateHistory";
    private static boolean start_back = false;
    private Handler handler = new Handler();

    Fragment fragment;

    String responseData = "";

    ArrayList<String> split_list_1 = new ArrayList<>();
    ArrayList<String> sport_id_list = new ArrayList<>();
    ArrayList<String> show_sport_item = new ArrayList<>();

    ArrayList<String> one_foot_balance_list = new ArrayList<>();
    ArrayList<String> foot_front_back_list = new ArrayList<>();
    ArrayList<String> squat_list = new ArrayList<>();
    //上下拉筋
    ArrayList<String> stretch_top_list = new ArrayList<>();
    ArrayList<String> left_right_stretch_list = new ArrayList<>();
    ArrayList<String> hard_take_list = new ArrayList<>();
    ArrayList<String> swing_hand_list = new ArrayList<>();
    //上下伸展
    ArrayList<String> top_down_stretch_list = new ArrayList<>();
    ArrayList<String> hug_knee_balance_list = new ArrayList<>();
    ArrayList<String> press_knee_list = new ArrayList<>();
    ArrayList<String> sitting_stretch_forward_list = new ArrayList<>();
    ArrayList<String> draw_circle_by_hand_list = new ArrayList<>();
    ArrayList<String> step_up_exercise_list = new ArrayList<>();
    ArrayList<String> arm_stretch_list = new ArrayList<>();
    ArrayList<String> step_right_there_list = new ArrayList<>();
    ArrayList<String> sit_down_stretch_list = new ArrayList<>();
    Map<String, String> map = new HashMap<>();
    String done_list_invoke = "";

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

    public HeartRateHistory () {
        super(robotCallback, robotListenCallback);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_history);
        //保持畫面不讓zenbo臉打斷
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        map.put("1b390f85-dcac-4d87-8a34-58ba814eed80", getString(R.string.ST_extend));
        map.put("29297418-10ce-45c1-871b-267450275532", getString(R.string.MO_step));
        map.put("40f95846-27c6-46cb-a473-e027592ab622", getString(R.string.LR_extend));
        map.put("51d26c54-ff43-463a-82ba-3c181312de33", getString(R.string.FW_shakehands));
        map.put("56c6fbc1-b2cb-4467-9ee3-90344a543551", getString(R.string.UD_stretch));
        map.put("600a6b72-b76d-45ed-8b17-4f0e5a8949ce", getString(R.string.AM_extend));
        map.put("629e77be-35b0-4dbc-af3d-971337457e91", getString(R.string.UD_extension));
        map.put("64aa89f7-dd73-413c-affa-f0541edff152", getString(R.string.BX_exercise));
        map.put("6a6c6542-214b-4a8e-b36c-182e93ccb167", getString(R.string.KE_balance));
        map.put("73257c8d-7673-4bdc-83c1-aba39e8f7feb", getString(R.string.SM_squat));
        map.put("78f06e53-41ec-4177-a972-b813bb74e81f", getString(R.string.FW_footpoint));
        map.put("8ecfe05a-2fe8-422a-9f2a-7e1633786a94", getString(R.string.HN_circle));
        map.put("919b0430-3e18-4364-b2b9-c0274675e93d", getString(R.string.STF_extend));
        map.put("a084e394-fe37-47aa-9bbe-ccbe907257da", getString(R.string.DL));
        map.put("c28b0695-9abd-4fb7-b6f3-75345e72ead2", getString(R.string.SM_balance));
        map.put("dda7102e-f896-4e24-a1eb-c287bcad6a15", getString(R.string.PR_knee));

        Intent intent = getIntent();
        sport_id_list = intent.getStringArrayListExtra("sport_id_list");
        split_list_1 = intent.getStringArrayListExtra("split_list_1");
        one_foot_balance_list = intent.getStringArrayListExtra("one_foot_balance_list");
        foot_front_back_list = intent.getStringArrayListExtra("foot_front_back_list");
        squat_list = intent.getStringArrayListExtra("squat_list");
        stretch_top_list = intent.getStringArrayListExtra("stretch_top_list");
        left_right_stretch_list = intent.getStringArrayListExtra("left_right_stretch_list");
        hard_take_list = intent.getStringArrayListExtra("hard_take_list");
        swing_hand_list = intent.getStringArrayListExtra("swing_hand_list");
        top_down_stretch_list = intent.getStringArrayListExtra("top_down_stretch_list");
        hug_knee_balance_list = intent.getStringArrayListExtra("hug_knee_balance");
        press_knee_list = intent.getStringArrayListExtra("press_knee");
        sitting_stretch_forward_list = intent.getStringArrayListExtra("sitting_stretch_forward");
        draw_circle_by_hand_list = intent.getStringArrayListExtra("draw_circle_by_hand");
        step_up_exercise_list = intent.getStringArrayListExtra("step_up_exercise");
        arm_stretch_list = intent.getStringArrayListExtra("arm_stretch");
        step_right_there_list = intent.getStringArrayListExtra("step_right_there");
        sit_down_stretch_list = intent.getStringArrayListExtra("sit_down_stretch");

        Log.e(TAG, Arrays.toString(sport_id_list.toArray()));

        TextView backtomybord = findViewById(R.id.backtomybord);
        backtomybord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HeartRateHistory.this, Myboard.class);
                startActivity(intent);
            }
        });

        //doInvokeAPI();
        /*doInvokeIndex();

        try {
            while (split_list_1.size() == 0) {
                Thread.sleep(50);
            }
            for (int i = 0; i < split_list_1.size(); i++) {
                doInvokeAPI(split_list_1.get(i));
            }
            while (sport_id_list.size() != split_list_1.size())
                Thread.sleep(100);
            Log.e(TAG, "sport_id_list:" + Arrays.toString(sport_id_list.toArray()));

            Collections.sort(sport_id_list);
            for (int i = 0; i < sport_id_list.size() - 1; i++) {
                if(sport_id_list.get(i).equals("null")){
                    sport_id_list.remove(i);
                    i--;
                }else if(sport_id_list.get(i).equals(sport_id_list.get(i + 1))) {
                    sport_id_list.remove(i);
                    i--;
                }
            }*/
        //Log.e(TAG, "sport_id_list:" + Arrays.toString(sport_id_list.toArray()));
        try {
            if (GetLocale.getLocale().equals("zh")) {
                show_sport_item.add("請選擇");
            } else if (GetLocale.getLocale().equals("en")) {
                show_sport_item.add("Choose one！");
            }
            for (int i = 0; i < sport_id_list.size(); i++) {
                show_sport_item.add(map.get(sport_id_list.get(i)));
            }
            Log.d(TAG, "show_sport_item:" + Arrays.toString(show_sport_item.toArray()));


            Spinner spinner = findViewById(R.id.spinnerTest);
            String[] exercise_category;
            exercise_category = show_sport_item.toArray(new String[0]);
            ArrayAdapter<String> exercise_category_list = new ArrayAdapter<>(HeartRateHistory.this,
                    R.layout.heart_rate_history_spinner,
                    exercise_category);
            spinner.setAdapter(exercise_category_list);
            spinner.setOnItemSelectedListener(this);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
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
        show_sport_item.clear();
        sport_id_list.clear();
        split_list_1.clear();
        one_foot_balance_list.clear();
        foot_front_back_list.clear();
        squat_list.clear();
        stretch_top_list.clear();
        left_right_stretch_list.clear();
        hard_take_list.clear();
        swing_hand_list.clear();
        top_down_stretch_list.clear();
        hug_knee_balance_list.clear();
        press_knee_list.clear();
        sitting_stretch_forward_list.clear();
        draw_circle_by_hand_list.clear();
        step_up_exercise_list.clear();
        arm_stretch_list.clear();
        step_right_there_list.clear();
        sit_down_stretch_list.clear();
    }

    @Override
    protected void onPause() {
        super.onPause();

        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();
        if (GetLocale.getLocale().equals("zh")) {
            switch (parent.getSelectedItem().toString()) {
                case "上下伸展":
                    ChangeFragment2TopDownStretch();
                    break;
                case "左右伸展":
                    ChangeFragment2LeftRightStretch();
                    break;
                case "前後甩手":
                    ChangeFragment2SwingHand();
                    break;
                case "上下拉筋":
                    ChangeFragment2StretchTop();
                    break;
                case "簡易深蹲":
                    ChangeFragment2Squat();
                    break;
                case "前後腳點":
                    ChangeFragment2FootFrontBack();
                    break;
                case "硬舉":
                    ChangeFragment2HardTake();
                    break;
                case "單腳平衡":
                    ChangeFragment2OneFootBalance();
                    break;
                case "坐式伸展":
                    ChangeFragment2SitDownStretch();
                    break;
                case "原地踏步":
                    ChangeFragment2StepRightThere();
                    break;
                case "手臂伸展":
                    ChangeFragment2ArmStretch();
                    break;
                case "踏台運動":
                    ChangeFragment2StepUpExercise();
                    break;
                case "抱膝平衡":
                    ChangeFragment2HugKneeBalance();
                    break;
                case "雙手畫圓操":
                    ChangeFragment2DrawCircleByHand();
                    break;
                case "坐式向前伸展":
                    ChangeFragment2SittingStretchForward();
                    break;
                case "壓膝":
                    ChangeFragment2PressKnee();
                    break;

            }
        } else if (GetLocale.getLocale().equals("en")) {
            switch (parent.getSelectedItem().toString()) {
                case "Extension up and down":
                    ChangeFragment2TopDownStretch();
                    break;
                case "Stretching left and right":
                    ChangeFragment2LeftRightStretch();
                    break;
                case "Shake hand and move front and back":
                    ChangeFragment2SwingHand();
                    break;
                case "Stretching up and down":
                    ChangeFragment2StretchTop();
                    break;
                case "Simple Squat":
                    ChangeFragment2Squat();
                    break;
                case "Foot touch ground and move front and back":
                    ChangeFragment2FootFrontBack();
                    break;
                case "Deadlift":
                    ChangeFragment2HardTake();
                    break;
                case "One foot balance":
                    ChangeFragment2OneFootBalance();
                    break;
                case "Sitting Stretching":
                    ChangeFragment2SitDownStretch();
                    break;
                case "March on the spot":
                    ChangeFragment2StepRightThere();
                    break;
                case "Arm stretching":
                    ChangeFragment2ArmStretch();
                    break;
                case "Step onto the steps":
                    ChangeFragment2StepUpExercise();
                    break;
                case "Knee balance":
                    ChangeFragment2HugKneeBalance();
                    break;
                case "Drawing circles with both hands":
                    ChangeFragment2DrawCircleByHand();
                    break;
                case "Sitting front stretching":
                    ChangeFragment2SittingStretchForward();
                    break;
                case "Press knee":
                    ChangeFragment2PressKnee();
                    break;
            }
        }

    }

    private void ChangeFragment2PressKnee() {
        fragment = new HeartRatePressKneeFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("press_knee", press_knee_list);
        fragment.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.heartRateContainer, fragment);
        ft.commit();
    }

    private void ChangeFragment2SittingStretchForward() {
        fragment = new HeartRateSittingStretchForwardFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("sitting_stretch_forward", sitting_stretch_forward_list);
        fragment.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.heartRateContainer, fragment);
        ft.commit();
    }

    private void ChangeFragment2DrawCircleByHand() {
        fragment = new HeartRateDrawCircleByHandFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("draw_circle_by_hand", draw_circle_by_hand_list);
        fragment.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.heartRateContainer, fragment);
        ft.commit();
    }

    private void ChangeFragment2HugKneeBalance() {
        fragment = new HeartRateHugKneeBalanceFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("hug_knee_balance", hug_knee_balance_list);
        fragment.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.heartRateContainer, fragment);
        ft.commit();
    }

    private void ChangeFragment2StepUpExercise() {
        fragment = new HeartRateStepUpExerciseFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("step_up_exercise", step_up_exercise_list);
        fragment.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.heartRateContainer, fragment);
        ft.commit();
    }

    private void ChangeFragment2ArmStretch() {
        fragment = new HeartRateArmStretchFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("arm_stretch", arm_stretch_list);
        fragment.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.heartRateContainer, fragment);
        ft.commit();
    }

    private void ChangeFragment2StepRightThere() {
        fragment = new HeartRateStepRightThereFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("step_right_there", step_right_there_list);
        fragment.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.heartRateContainer, fragment);
        ft.commit();
    }

    private void ChangeFragment2SitDownStretch() {
        fragment = new HeartRateSitDownStretchFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("sit_down_stretch", sit_down_stretch_list);
        fragment.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.heartRateContainer, fragment);
        ft.commit();
    }

    private void ChangeFragment2OneFootBalance() {
        fragment = new HeartRateOneFootBalanceFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("one_foot_balance", one_foot_balance_list);
        fragment.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.heartRateContainer, fragment);
        ft.commit();
    }

    private void ChangeFragment2FootFrontBack() {
        fragment = new HeartRateFootFrontBackFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("foot_front_back", foot_front_back_list);
        fragment.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.heartRateContainer, fragment);
        ft.commit();
    }

    private void ChangeFragment2Squat() {
        fragment = new HeartRateSquatFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("squat", squat_list);
        fragment.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.heartRateContainer, fragment);
        ft.commit();
    }

    private void ChangeFragment2StretchTop() {
        fragment = new HeartRateStretchTopFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("stretch_top", stretch_top_list);
        fragment.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.heartRateContainer, fragment);
        ft.commit();
    }

    public void ChangeFragment2SwingHand() {
        fragment = new HeartRateSwingHandFragment();
        //HeartRateSwingHandFragment heartRateTestFragment = new HeartRateSwingHandFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("swing_hand", swing_hand_list);
        fragment.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.heartRateContainer, fragment);
        ft.commit();
    }

    public void ChangeFragment2HardTake() {
        fragment = new HeartRateHardTakeFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("hard_take", hard_take_list);
        fragment.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.heartRateContainer, fragment);
        ft.commit();
    }

    private void ChangeFragment2LeftRightStretch() {
        fragment = new LeftRightStretchFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("left_right_stretch", left_right_stretch_list);
        fragment.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.heartRateContainer, fragment);
        ft.commit();
    }

    private void ChangeFragment2TopDownStretch() {
        fragment = new HeartRateTopDownStretchFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("top_down_stretch_list", top_down_stretch_list);
        fragment.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.heartRateContainer, fragment);
        ft.commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    /*if(responseContentStream !=null){
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
        split_list_1.addAll(Arrays.asList(split_list));
        int num = split_list.length - 1;
        for (int ii = 0; ii < split_list.length; ii++) {
            split_list_1.add(split_list[num]);
            num--;
        }
        Log.e(TAG, Arrays.toString(split_list_1.toArray()));

    }*/

    /*if(responseContentStream !=null){
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
        for (int i = 0; i < split_list.length; i++) {
            long split_test_in = Long.parseLong(split_list[i]);
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);
            String in = simpleDateFormat2.format(split_test_in);
            split_list_list.add(in);
        }
        int num = split_list_list.size() - 1;
        for (int ii = 0; ii < split_list_list.size(); ii++) {
            split_list_list_reverse.add(split_list_list.get(num));
            num--;
        }
        Log.d(TAG, "split_reverse:" + Arrays.toString(split_list_list_reverse.toArray()));
    }
    Log.d(TAG,"status code = "+response.getStatusCode()+" "+response.getStatusText());*/


    /*rest api invoke test
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
                        if (!get_list_invoke.equals("")) {
                            Log.d(TAG, "sport_id:" + get_list_invoke);
                            sport_id_list.add(get_list_invoke);
                        } else {
                            sport_id_list.add("null");
                        }

                        String get_time = json_read_invoke.getString("userId");
                        switch (get_list_invoke) {
                            case one_foot_balance:
                                long get_time_1 = Long.parseLong(get_time);
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);
                                String in = simpleDateFormat.format(get_time_1);
                                one_foot_balance_list.add(in);
                                break;
                            case foot_front_back:
                                long get_time_2 = Long.parseLong(get_time);
                                SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);
                                String in2 = simpleDateFormat2.format(get_time_2);
                                foot_front_back_list.add(in2);
                                break;
                            case squat:
                                long get_time_3 = Long.parseLong(get_time);
                                SimpleDateFormat simpleDateFormat3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);
                                String in3 = simpleDateFormat3.format(get_time_3);
                                squat_list.add(in3);
                                break;
                            case stretch_top:
                                long get_time_4 = Long.parseLong(get_time);
                                SimpleDateFormat simpleDateFormat4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);
                                String in4 = simpleDateFormat4.format(get_time_4);
                                stretch_top_list.add(in4);
                                break;
                            case left_right_stretch:
                                long get_time_5 = Long.parseLong(get_time);
                                SimpleDateFormat simpleDateFormat5 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);
                                String in5 = simpleDateFormat5.format(get_time_5);
                                left_right_stretch_list.add(in5);
                                break;
                            case hardtake:
                                long get_time_6 = Long.parseLong(get_time);
                                SimpleDateFormat simpleDateFormat6 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);
                                String in6 = simpleDateFormat6.format(get_time_6);
                                hard_take_list.add(in6);
                                break;
                            case swinghand:
                                long get_time_7 = Long.parseLong(get_time);
                                SimpleDateFormat simpleDateFormat7 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);
                                String in7 = simpleDateFormat7.format(get_time_7);
                                swing_hand_list.add(in7);
                                break;
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
                    Intent intent = new Intent(HeartRateHistory.this, Myboard.class);
                    startActivity(intent);
                    start_back = false;
                }
                Log.d("lxhandler", "HeartRateHistroy");
                handler.postDelayed(this, 50);
            }
        });
    }
}
