package com.robot.asus.Sporden.WorkoutCategory;

import android.Manifest;
import android.app.Activity;
//import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.amazonaws.amplify.generated.graphql.ListExercisesQuery;
import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.mobileconnectors.apigateway.ApiRequest;
import com.amazonaws.mobileconnectors.apigateway.ApiResponse;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.util.IOUtils;
import com.amazonaws.util.StringUtils;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.asus.robotframework.API.MotionControl;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.RobotUtil;
import com.asus.robotframework.API.SpeakConfig;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.robot.asus.Sporden.GetLocale;
import com.robot.asus.Sporden.Model.ClientFactory;
import com.robot.asus.Sporden.Model.Workout;
import com.robot.asus.Sporden.R;
import com.robot.asus.Sporden.WorkingGaming.WorkoutGaming;
import com.robot.asus.Sporden.WorkingGaming.WorkoutGamingDirection;
import com.robot.asus.Sporden.spordenREST.SpordenRESTClient;
import com.robot.asus.robotactivity.RobotActivity;
import com.shashank.sony.fancytoastlib.FancyToast;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import io.supercharge.shimmerlayout.ShimmerLayout;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;
import static com.robot.asus.robotactivity.RobotActivity.robotAPI;

public class WorkoutGameDetail extends RobotActivity {
    private static final String TAG = "GameDetailDialogue";
    public final static String DOMAIN = "2532C682CCD447C7AEAE1830C7DC2219";
    public static boolean start_gaming = false;
    private static boolean start_back = false;
    private long workoutId;
    private VideoView videoView;
    private TextView textback;
    private String intent_name;
    private String intent_id;
    private String get_intro;
    private String get_ex_disease;
    private String get_voiceIntro;
    private String intent_category;
    private TextView description;
    private int cacheTimes;
    private static int iCurrentCommandSerial;

    private SpordenRESTClient apiClient;
    private String email;
    private Boolean stateChecked, isGetActionList;
    private double user_height;
    private double user_weight;
    private int user_age;
    private String user_gender, videoUri, videouri;


    private String WorkoutName;
    private String methodClass;
    private String WorkoutActionListString;
    private String[] WorkoutActionList;
    private ArrayList<Integer> WorkoutActionArrayListInt = new ArrayList<>();
    private int[] WorkoutActionListInt;


    private String user_disease;
    private final Handler handler = new Handler();
    //private final Handler handler2 = new Handler();
    private String[] split_disease;
    private static boolean isOverEx = false;
    private static boolean isOverUser = false;
    private String fromSearch;
    private String translate_en_dis;


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
            if (sIntentionID.equals("WorkoutGameDetail")) {
                //取這個plan的那個要得變數
                String sSluResultButton = RobotUtil.queryListenResultJson(jsonObject, "開始", null);
                Log.d(TAG, "Result Button = " + sSluResultButton);

                //要和concept的instance一樣(這是把變數拿來比對的條件式)
                if (sSluResultButton.equals("開始")) {
                    start_gaming = true;
                }
            } else if (sIntentionID.equals("BackToDetial")) {
                //取這個plan的那個要得變數
                String sSluResultButton = RobotUtil.queryListenResultJson(jsonObject, "返回", null);
                Log.d(TAG, "Result Button = " + sSluResultButton);

                //要和concept的instance一樣(這是把變數拿來比對的條件式)
                if (sSluResultButton.equals("返回")) {
                    start_back = true;
                }
            }
        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };

    public WorkoutGameDetail() {
        super(robotCallback, robotListenCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_game_detail);
        //保持畫面不讓zenbo臉打斷
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //螢幕HOME鍵隱藏
        controlBarSetting();

        ShimmerLayout shimmerText = (ShimmerLayout) findViewById(R.id.shimmer_text);
        shimmerText.startShimmerAnimation();



        //頭部位置校正
        robotAPI.motion.moveHead(0,30, MotionControl.SpeedLevel.Head.L2);


        if (savedInstanceState != null) {
            workoutId = savedInstanceState.getLong("workoutId");
        }

        Intent intent = getIntent();
        intent_name = intent.getStringExtra("sport_name");
        intent_id = intent.getStringExtra("sport_id");
        get_voiceIntro = intent.getStringExtra("sport_voice");
        intent_category = intent.getStringExtra("sport_category");
        //fromSearch = intent.getStringExtra("bySearch");
        Log.d("intennnntfra", intent_name + " and " + intent_id + "and" + get_voiceIntro);
        if (intent_category != null) {
            Log.d("fuckyou", intent_category);
        }

        /*View fragmentContainer = findViewById(R.id.fragment_container);

        WorkoutGameDetailFragment details = new WorkoutGameDetailFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        //details.setWorkout(id);
        ft.replace(R.id.fragment_container, details);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();*/

        // Create the client (測試REST API)
        apiClient = new ApiClientFactory()
                .credentialsProvider(AWSMobileClient.getInstance())
                .region("us-east-1")
                .build(SpordenRESTClient.class);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (acct != null) {
            //取得google帳戶資訊
            email = acct.getEmail();
        }

        backSetup();

        //尋找這個運動的資訊
        //queryExercise();
        //尋找這個使用者的資訊
        queryUserImfor();

        //監聽疾病提醒
        //runTimerforDisease();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // close faical
        robotAPI.robot.setExpression(RobotFace.HIDEFACE);

        // jump dialog domain
        //robotAPI.robot.jumpToPlan(DOMAIN, "WorkoutGameDetail");

        //robotAPI.robot.speakAndListen(get_voiceIntro, new SpeakConfig().timeout(20).speed(145).pitch(100));
        robotAPI.robot.speakAndListen(get_voiceIntro, new SpeakConfig().speed(145).pitch(100).timeout(20));

        //監聽DDE
        runTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();

        robotAPI.robot.stopSpeakAndListen();

        handler.removeCallbacksAndMessages(null);

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        videoPlayer();

    }

    @Override
    public void onStop() {
        super.onStop();

        videoView.stopPlayback();

        Log.d("fuckerGame", "Stop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("fuckerGame", "Destroy");
    }

    @Override
    public void finish() {
        super.finish();

        Log.d("fuckerGame", "finish");
        //get_ex_disease = null;
    }


    @Override
    public void onStart() {
        super.onStart();

        //videoPlayer();
        cacheTimes = 0;

        TextView title = (TextView) findViewById(R.id.textTitle);
        //Workout workout = Workout.workouts[0];
        title.setText(intent_name);
        if (GetLocale.getLocale().equals("en")) {
            title.setTextSize(70);
        }
        description = (TextView) findViewById(R.id.textDescription);
        //Log.d("intennnntfra2", get_intro);


        TextView button = (TextView) findViewById(R.id.startworkoutgame);

        //開始運動按鈕
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get是否開啟手表APP，並做判斷接著要做什麼




                /**  記得打開!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! **/
                doCheckAppState();
                //doPostStartAndEmail();
                //Intent intent = new Intent(getApplicationContext(), WorkoutGaming.class);
                //startActivity(intent);
                /** 記得把上面三行刪掉!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! **/

                    /*
                    ///沒手錶時測試用 上面的doCheckAppState()要記得註解掉
                    Intent intent = new Intent();
                    intent.setClassName(getApplicationContext(),"com.robot.asus.Sporden.WorkingGaming."+methodClass);
                    intent.putExtra("WorkoutName",WorkoutName);
                    intent.putExtra("WorkoutActionListInt",WorkoutActionListInt);
                    startActivity(intent);

                    */
            }
        });


    }


    public void setWorkout(long id) {
        this.workoutId = id;
    }

    public void videoPlayer() {
        videoView = (VideoView) findViewById(R.id.videoView);
        Uri uri = Uri.parse(videoUri);
        videoView.setVideoURI(uri);
        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
                mp.setVolume(0f, 0f);
            }
        });
    }

    public void backSetup() {

        textback = (TextView) findViewById(R.id.textback);
        textback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void queryExercise() {
        // Get the client instance
        AWSAppSyncClient awsAppSyncClient = ClientFactory.getInstance(getApplicationContext());

        awsAppSyncClient.query(ListExercisesQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK) /**小心這邊出問題**/
                .enqueue(exerciseSearchCallback);
    }

    private GraphQLCall.Callback<ListExercisesQuery.Data> exerciseSearchCallback = new GraphQLCall.Callback<ListExercisesQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListExercisesQuery.Data> response) {
            //用迴圈顯示每一個item，並逐一比對找這個運動是哪一個，找到就結束迴圈
            for (int i = 0; i < response.data().listExercises().items().size(); i++) {
                if (GetLocale.getLocale().equals("zh")) {
                    if (response.data().listExercises().items().get(i).name().equals(intent_name)) {
                        get_intro = response.data().listExercises().items().get(i).textInfo();
                        WorkoutName = response.data().listExercises().items().get(i).name();
                        methodClass = response.data().listExercises().items().get(i).method();
                        WorkoutActionListString = response.data().listExercises().items().get(i).script();
                        get_ex_disease = response.data().listExercises().items().get(i).disease();
                        videouri = response.data().listExercises().items().get(i).videoUri();
                        //get_voiceIntro = response.data().listExercises().items().get(i).voiceInfo();
                        break;
                    }
                } else if (GetLocale.getLocale().equals("en")) {
                    if (response.data().listExercises().items().get(i).enName().equals(intent_name)) {
                        get_intro = response.data().listExercises().items().get(i).enTextInfo();
                        WorkoutName = response.data().listExercises().items().get(i).enName();
                        methodClass = response.data().listExercises().items().get(i).method();
                        WorkoutActionListString = response.data().listExercises().items().get(i).script();
                        get_ex_disease = response.data().listExercises().items().get(i).disease();
                        videouri = response.data().listExercises().items().get(i).videoUri();
                        //get_voiceIntro = response.data().listExercises().items().get(i).voiceInfo();
                        break;
                    }
                }
            }

            if (videouri == null) {

                videoUri = "android.resource://" + "com.robot.asus.Sporden" + "/raw/shakehand";
            } else {

                videoUri = "android.resource://" + "com.robot.asus.Sporden" + "/raw/" + videouri;
            }


            if (WorkoutActionListString != null && WorkoutActionListString.length() != 0) {

                isGetActionList = true;
                Log.d("YifanTest", isGetActionList + "1");
                WorkoutActionList = WorkoutActionListString.substring(WorkoutActionListString.indexOf("[") + 1,
                        WorkoutActionListString.lastIndexOf("]")).split(",");
                WorkoutActionArrayListInt.clear();
                for (int j = 0; j < WorkoutActionList.length; j++) {
                    WorkoutActionArrayListInt.add(Integer.parseInt(WorkoutActionList[j]));
                }
                WorkoutActionListInt = new int[WorkoutActionArrayListInt.size()];
                for (int w = 0; w < WorkoutActionArrayListInt.size(); w++) {
                    WorkoutActionListInt[w] = WorkoutActionArrayListInt.get(w);
                    Log.d("testYifan", WorkoutActionListInt[w] + "");
                }
                Log.d("testYifan", WorkoutName + " " + WorkoutActionArrayListInt.toString() + " " + methodClass);
            } else {
                Log.d("YifanTest", isGetActionList + "2");
                isGetActionList = false;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    description.setText(get_intro);
                    if (cacheTimes == 0) {
                        videoPlayer();
                        cacheTimes++;
                    }


                    if (get_ex_disease != null) {
                        Log.d("fucker", get_ex_disease);
                        if (split_disease != null) {
                            for (int j = 0; j < split_disease.length; j++) {
                                if (split_disease[j].equals(get_ex_disease)) {
                                    if (GetLocale.getLocale().equals("zh")) {
                                        robotAPI.robot.speak("這項運動較不適合有" + get_ex_disease + "的人唷，請斟酌考慮是否要開始運動");
                                        FancyToast.makeText(getApplicationContext(), "這項運動較不適合有" + get_ex_disease + "的人唷，請斟酌考慮是否要開始運動", FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                                        break;
                                        //因為目前只有一個疾病篩選，之後要拿掉
                                    } else if (GetLocale.getLocale().equals("en")) {
                                        if (get_ex_disease.equals("膝蓋疼痛")) {
                                            translate_en_dis = "Knee Pain";
                                        } else if (get_ex_disease.equals("手腕疼痛")) {
                                            translate_en_dis = "Wrist Pain";
                                        } else if (get_ex_disease.equals("骨質疏鬆")) {
                                            translate_en_dis = "Osteoporosis";
                                        } else if (get_ex_disease.equals("糖尿病")) {
                                            translate_en_dis = "diabetes";
                                        }
                                        robotAPI.robot.speak("This exercise is not suitable for the people having " + translate_en_dis + ", please consider whether you want to start exercising.");
                                        FancyToast.makeText(getApplicationContext(), "This exercise is less suitable for the people having " + translate_en_dis + ", please consider whether you want to start exercising.", FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                                        break;
                                        //因為目前只有一個疾病篩選，之後要拿掉
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
        }
    };

    //取得目前登入user的資料
    private void queryUserImfor() {
        // Get the client instance
        AWSAppSyncClient awsAppSyncClient = ClientFactory.getInstance(getApplicationContext());

        awsAppSyncClient.query(ListUsersQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST) /**小心這邊出問題**/
                .enqueue(userSearchCallback);
    }

    private GraphQLCall.Callback<ListUsersQuery.Data> userSearchCallback = new GraphQLCall.Callback<ListUsersQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListUsersQuery.Data> response) {
            //用迴圈顯示每一個item，並逐一比對找這個運動是哪一個，找到就結束迴圈
            for (int i = 0; i < response.data().listUsers().items().size(); i++) {
                //如果搜尋到的email跟現在登入的email一樣
                if (response.data().listUsers().items().get(i).email().equals(email)) {
                    user_height = response.data().listUsers().items().get(i).height();
                    user_weight = response.data().listUsers().items().get(i).weight();
                    user_age = response.data().listUsers().items().get(i).age();
                    user_gender = response.data().listUsers().items().get(i).gender();
                    user_disease = response.data().listUsers().items().get(i).disease();
                    break;
                }
            }
            if (user_disease != null) {
                String sl = user_disease.substring(user_disease.indexOf("[") + 1, user_disease.lastIndexOf("]"));
                split_disease = sl.split(", ");
            }
            queryExercise();
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
        }
    };


    //rest api invoking (把開始的布林值和目前使用者的email post上去)
    private void doPostStartAndEmail() {
        // Create components of api request
        final String method = "POST";
        final String path = "/Health";

        //body放要post的資料(userId一定要有)
        final String body = "{\"userId\":\"dansmocheckforstartsend\", \"email\":\"" + email + "\", \"height\":\"" + user_height + "\", \"weight\":\"" + user_weight + "\", \"age\":\"" + user_age + "\", \"gender\":\"" + user_gender + "\", \"sport_id\":\"" + intent_id + "\", \"isChecked\":\"true\"}";
        final byte[] content = body.getBytes(StringUtils.UTF8);

        final Map parameters = new HashMap<>();
        //parameters.put("lang", "en_US");
        //這邊放get要搜尋的哪個PK欄位
        //parameters.put("userId","AJSI");

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
                        final String responseData = IOUtils.toString(responseContentStream);
                        Log.d(TAG, "Response : " + responseData);
                    }

                    Log.d(TAG, "status code = " + response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    private void doCheckAppState() {
        // Create components of api request
        //Post的path為/watch，GET的為/watch/object/:userId
        final String method = "GET";
        final String path = "/Health/object/:userId";

        //body放要post的資料(userId一定要有)，get時則為空字串
        final String body = "";

        final byte[] content = body.getBytes(StringUtils.UTF8);

        final Map parameters = new HashMap<>();
        //post時留這邊就好
        //GET時這邊放要搜尋的哪個PK欄位
        parameters.put("userId", "isStartstartednajsistar");

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
                        final String responseData = IOUtils.toString(responseContentStream);
                        //responseData為傳回之json轉成字串，在這邊寫回傳資料的應用
                        Log.d(TAG, "Response : " + responseData);
                        //一開始從網路接收通常為String型態,tmp為接收到的String,為避免串流內有其他資料只需抓取{}間的內容
                        String tmp2 = responseData.substring(responseData.indexOf("{"), responseData.lastIndexOf("}") + 1);
                        JSONObject json_read2 = new JSONObject(tmp2);
                        stateChecked = json_read2.getBoolean("isStarted");
                        Log.d(TAG, "Response for state : " + Boolean.toString(stateChecked));


                        //拿到手錶是否開啟後，利用判斷式先判斷有沒有取得相機權限，接著確認是不是有開啟手錶APP
                        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED) {
                            Log.d("changepage", "success");
                            //如果有開啟
                            if (stateChecked) {

                                if (isGetActionList) {


                                    doPostStartAndEmail();
                                    Intent intent = new Intent();
                                    intent.setClassName(getApplicationContext(), "com.robot.asus.Sporden.WorkingGaming." + methodClass);
                                    intent.putExtra("WorkoutName", WorkoutName);
                                    intent.putExtra("WorkoutActionListInt", WorkoutActionListInt);
                                    intent.putExtra("VideoUri", videouri);
                                    startActivity(intent);
                                } else {

                                    Log.d("YifanTest", "此動作的腳本尚未建立，請改做別的運動");
                                    if (GetLocale.getLocale().equals("zh")) {
                                        robotAPI.robot.speak("此動作的腳本尚未建立，請改做別的運動");
                                    } else if (GetLocale.getLocale().equals("en")) {
                                        robotAPI.robot.speak("The script for this exercise has not been created yet. Please change to another exercise.");
                                    }
                                    // FancyToast.makeText(getApplicationContext(), "此動作的腳本尚未建立，請改做別的運動", FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                                }
                            } else {
                                //Toast.makeText(getContext(), "請開啟手錶APP", Toast.LENGTH_LONG).show();
                                //zenbo speak
                                Log.d("YifanTest", "請打開手錶APP才能開始運動唷!");
                                if (GetLocale.getLocale().equals("zh")) {
                                    robotAPI.robot.speak("請打開手錶APP才能開始運動唷!");
                                } else if (GetLocale.getLocale().equals("en")) {
                                    robotAPI.robot.speak("Please open the watch app to start exercise!");
                                }
                            }
                        } else {
                            if (GetLocale.getLocale().equals("zh")) {
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        "請開啟相機權限", Toast.LENGTH_LONG);
                                toast.show();
                            } else if (GetLocale.getLocale().equals("en")) {
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        "Please turn on camera permissions", Toast.LENGTH_LONG);
                                toast.show();
                            }
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

    public void runTimer() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d("lxhandler", "WorkoutGameDetail");
                if (start_gaming) {
                    //檢查手錶有沒有開，有開的話就傳送使用者資訊並開始運動，沒開就擋住
                    /**  記得打開!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! **/
                    doCheckAppState();
                    //doPostStartAndEmail();
                    //Intent intent = new Intent(getApplicationContext(), WorkoutGaming.class);
                    //startActivity(intent);
                    /** 記得把上面三行刪掉!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! **/
                    start_gaming = false;
                } else if (start_back) {
                    if (intent_category == null) {
                        Intent intent = new Intent(getApplicationContext(), WorkoutSearchActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(getApplicationContext(), WorkoutCategoryDetial.class);
                        intent.putExtra("category", intent_category);
                        startActivity(intent);
                        finish();
                    }
                    start_back = false;
                }
                handler.postDelayed(this, 10);
            }
        });
    }

    /*public void runTimerforDisease() {
        handler2.post(new Runnable() {
            @Override
            public void run() {
                Log.d("testtt","judl");
                if (isOverUser) {
                    //有些運動沒有排除疾病，如果不是空的才做
                    //if (get_ex_disease != null) {
                    queryExercise();
                    //}
                    isOverUser = false;
                    handler2.removeCallbacksAndMessages(null);
                }
                handler2.postDelayed(this, 300);
            }
        });
    }*/


    public void controlBarSetting() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );


    }
}





