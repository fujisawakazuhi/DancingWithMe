package com.robot.asus.Sporden.WorkingGaming;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.asus.robotframework.API.MotionControl;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.SpeakConfig;
import com.asus.robotframework.API.Utility;
import com.asus.robotframework.API.results.DetectFaceResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.race604.drawable.wave.WaveDrawable;
import com.robot.asus.Sporden.GetLocale;
import com.robot.asus.Sporden.MainActivity;
import com.robot.asus.Sporden.R;
import com.robot.asus.Sporden.spodenIndex.SpodenIndexClient;
import com.robot.asus.Sporden.spordenREST.SpordenRESTClient;
import com.robot.asus.robotactivity.RobotActivity;
import com.shashank.sony.fancytoastlib.FancyToast;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class WorkoutGamingResult extends RobotActivity {
    private String TAG = "WorkoutGamingResult";
    private SpordenRESTClient apiClient;
    //private FerrisWheelView ferrisWheelView;


    private ImageView mImageView;
    private WaveDrawable mWaveDrawable;
    private LinearLayout heartClick;

    private TextView textTitle;
    private LinearLayout linearLayout2;
    private double intent_score;
    private static int iCurrentCommandSerial;
    private static int iCurrentSpeakSerial;
    private ImageView gohome;
    private int timecount;
    private static boolean isHideFace;

    private SpodenIndexClient indexClient;
    private String acc_email;
    String responseData = "";
    String[] split_list;
    private String last_one;
    private String average_HR = "";
    private String calorie = "";
    private TextView average_hr;
    private TextView final_calorie;
    private final Handler handler = new Handler();
    private static String get_list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_gaming_result);
        //保持畫面不讓zenbo臉打斷
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        textTitle = findViewById(R.id.textTitle);
        if (GetLocale.getLocale().equals("en")) {
            textTitle.setTextSize(50);
        }
        linearLayout2 = findViewById(R.id.linerlayout2);
        gohome = findViewById(R.id.gohome);

        average_hr = findViewById(R.id.average_hr);
        final_calorie = findViewById(R.id.final_calorie);


        //若沒講話導致沒hideface的除錯，一定秒數後強制hideface
        timecount = 0;
        isHideFace = false;
        TimeCounter();

        //ferrisWheelView = findViewById(R.id.ferrisWheelView);

        //ferrisWheelView.startAnimation();

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

        indexClient = new ApiClientFactory()
                .credentialsProvider(AWSMobileClient.getInstance())
                .region("us-east-1")
                .build(SpodenIndexClient.class);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (acct != null) {
            //取得google帳戶資訊
            acc_email = acct.getEmail();
        }

        // Create the client
        apiClient = new ApiClientFactory()
                .credentialsProvider(AWSMobileClient.getInstance())
                .region("us-east-1")
                .build(SpordenRESTClient.class);

        //textTitle = findViewById(R.id.textTitle);
        linearLayout2 = findViewById(R.id.linerlayout2);
        gohome = findViewById(R.id.gohome);

        //將結束布林傳上資料庫
        doPostEnd();

        //叫Zenbo講話的一些設定
        SpeakConfig config = new SpeakConfig();
        config.pitch(190).speed(69);
        // robotAPI.robot.speak("很棒馬，好厲害哦", config);


        //ShimmerEffect
        ShimmerLayout shimmerText = (ShimmerLayout) findViewById(R.id.shimmer_text);
        shimmerText.startShimmerAnimation();

        setWavePic();

        //取intent那邊的成績資料
        Intent intent = getIntent();
        intent_score = intent.getDoubleExtra("score", 0);
        Log.d("intennnnt", Double.toString(intent_score));

        //zenbo依分數給予回饋
        //zenboFeedback();

        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    last_one = -1;
                    long now = System.currentTimeMillis();
                    Log.e(TAG, "time_mnoew" + now);
                    //Thread.sleep(2000);
                    while (now - last_one > 40000) {
                        doInvokeIndex();
                        Thread.sleep(3000);
                    }
                    //Thread.sleep(500);
                    Log.e(TAG, "last:" + last_one);
                    //if(last_one!=0)
                        doInvokeAPIfor(String.valueOf(last_one));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start(); */


        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    doInvokeIndex();
                    if (last_one != null) {
                        doInvokeAPIfor(last_one);
                        handler.removeCallbacksAndMessages(null);
                    }
                    handler.postDelayed(this, 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //ferrisWheelView = findViewById(R.id.ferrisWheelView);

        //ferrisWheelView.startAnimation();

        heartClick = findViewById(R.id.heratRateLiner);
        heartClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), HeartRateInGameResult.class);
                // startActivity(i);
                Log.d("123y", "c");
            }
        });


        linearLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //FancyToast.makeText(getApplicationContext(), "Hello World !"
                // , FancyToast.LENGTH_LONG, FancyToast.DEFAULT, true).show();

                /*YoYo.with(Techniques.FadeInUp)
                        .duration(1400)
                        .repeat(0)
                        .playOn(linearLayout2);*/
            }
        });

        //取intent那邊的成績資料
        intent_score = intent.getDoubleExtra("score", 0);
        Log.d("intennnnt", Double.toString(intent_score));

        //zenbo依分數給予回饋
        zenboFeedback();

        gohome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                robotAPI.robot.speak("Bye!");
                //finish();
                startActivity(new Intent(WorkoutGamingResult.this, MainActivity.class));
                //Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                //startActivity(intent);
                System.exit(0);
            }
        });


    }

    @Override
    protected void onStop() {
        super.onStop();

        handler.removeCallbacksAndMessages(null);
        mImageView = null;
        mWaveDrawable = null;
        gohome = null;
    }

    public void setWavePic() {
        //圖片波動效果

        mImageView = (ImageView) findViewById(R.id.imageHeart);
        mWaveDrawable = new WaveDrawable(this, R.drawable.heart);
        mImageView.setImageDrawable(mWaveDrawable);
        mWaveDrawable.setLevel(5500);


        mImageView = (ImageView) findViewById(R.id.imageCalorie);
        mWaveDrawable = new WaveDrawable(this, R.drawable.calorie);
        mImageView.setImageDrawable(mWaveDrawable);
        mWaveDrawable.setWaveAmplitude(20);
        mWaveDrawable.setLevel(4000);

    }


    public WorkoutGamingResult() {

        super(robotCallback, robotListenCallback);
    }

    public static RobotCallback robotCallback = new RobotCallback() {
        @Override
        public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
            super.onResult(cmd, serial, err_code, result);
        }

        @Override
        public void onStateChange(int cmd, int serial, RobotErrorCode err_code, RobotCmdState state) {
            super.onStateChange(cmd, serial, err_code, state);


            if (serial == iCurrentSpeakSerial && state != RobotCmdState.ACTIVE) {
                //如果講完話，就將臉結束，並結束動作，Zenbo還要走回來
                Log.d("RobotDevSample", "command: " + iCurrentSpeakSerial + " SUCCEED");
                isHideFace = true;
                robotAPI.robot.setExpression(RobotFace.HIDEFACE);
                robotAPI.cancelCommandBySerial(iCurrentCommandSerial);
                robotAPI.motion.moveBody(0.7f, 0, 0, MotionControl.SpeedLevel.Body.L1);
            }
        }

        @Override
        public void initComplete() {
            super.initComplete();

        }

        @Override
        public void onDetectFaceResult(List<DetectFaceResult> resultList) {
            super.onDetectFaceResult(resultList);


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

        }

        @Override
        public void onResult(JSONObject jsonObject) {

        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }


    };

    //rest api invoking (把結束的布林值post上去)
    private void doPostEnd() {
        // Create components of api request
        final String method = "POST";
        final String path = "/Health";

        //body放要post的資料(userId一定要有)
        final String body = "{\"userId\":\"dansmocheckforstartsend\", \"endChecked\":\"true\"}";
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

    private void zenboFeedback() {
        if (intent_score == 0) {
            iCurrentSpeakSerial = robotAPI.robot.speak(getString(R.string.score_0));
            iCurrentCommandSerial = robotAPI.utility.playEmotionalAction(RobotFace.WORRIED, 25);
        } else if (intent_score == 50) {
            iCurrentSpeakSerial = robotAPI.robot.speak(getString(R.string.score_50));
            iCurrentCommandSerial = robotAPI.utility.playEmotionalAction(RobotFace.SERIOUS, Utility.PlayAction.Dance_s_1_loop);
        } else if (50 < intent_score && intent_score < 75) {
            iCurrentSpeakSerial = robotAPI.robot.speak(getString(R.string.score_75));
            iCurrentCommandSerial = robotAPI.utility.playEmotionalAction(RobotFace.DOUBTING, Utility.PlayAction.Dance_b_1_loop);
        } else if (75 <= intent_score && intent_score < 100) {
            iCurrentSpeakSerial = robotAPI.robot.speak(getString(R.string.score_90));
            iCurrentCommandSerial = robotAPI.utility.playEmotionalAction(RobotFace.ACTIVE, Utility.PlayAction.Dance_3_loop);
        } else if (intent_score == 100) {
            iCurrentSpeakSerial = robotAPI.robot.speak(getString(R.string.score_100));
            iCurrentCommandSerial = robotAPI.utility.playEmotionalAction(RobotFace.HAPPY, Utility.PlayAction.Dance_2_loop);
        }
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
                        Boolean isNew = json_read.getBoolean("newData");
                        if (isNew && isNew != null) {
                            //取done_list陣列格式
                            get_list = json_read.getString("done_list");
                            //取[]裡的資料
                            String done_list = get_list.substring(get_list.indexOf("[") + 1, get_list.indexOf("]"));
                            //用,分開，並存成陣列
                            split_list = done_list.split(", ");
                            //最新的值
                            last_one = split_list[split_list.length - 1];
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
                        Log.d(TAG, "Response : " + responseData);
                        //取json
                        String tmp_invoke = responseData.substring(responseData.indexOf("{"), responseData.lastIndexOf("}") + 1);
                        JSONObject json_read_invoke = new JSONObject(tmp_invoke);
                        calorie = json_read_invoke.getString("calories");
                        average_HR = json_read_invoke.getString("averageHR");
                        Log.e(TAG, "cal:" + calorie + ", aveHR:" + average_HR);

                        //此為給一執行緒來讓背景執行，並給main thread來執行，跟上面的handler分開，分配在不同thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //設定娶回來的健康數據
                                average_hr.setText(String.format(Locale.getDefault(), getString(R.string.average_HR)+": %3.1f BPM", Double.valueOf(average_HR)));
                                final_calorie.setText(String.format(Locale.getDefault(), getString(R.string.burned_cal)+"：%3.1f cal", Double.valueOf(calorie)));

                            }
                        });

                        //將newData轉乘false
                        doPostIndexNewFalse();
                    }
                    Log.d(TAG, "status code = " + response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    private void doPostIndexNewFalse() {
        // Create components of api request
        final String method = "POST";
        final String path = "/index";

        //body放要post的資料(userId一定要有)
        final String body = "{\"user_email\":\"" + acc_email + "\", \"done_list\":\"" + get_list + "\", \"newData\":\"false\"}";
        final byte[] content = body.getBytes(StringUtils.UTF8);

        final Map parameters = new HashMap<>();
        //parameters.put("lang", "en_US");
        //這邊放get要搜尋的哪個PK欄位(找哪個帳號的資料)
        //parameters.put("user_email", acc_email);

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
                    }

                    Log.d(TAG, "status code = " + response.getStatusCode() + " " + response.getStatusText());
                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    public void TimeCounter() {

        //計算運動總時間

        handler.post(new Runnable() {
            @Override
            public void run() {

                timecount++;

                if (timecount == 30 && !isHideFace) {

                    robotAPI.robot.setExpression(RobotFace.HIDEFACE);
                    robotAPI.cancelCommandBySerial(iCurrentCommandSerial);
                    robotAPI.motion.moveBody(0.7f, 0, 0, MotionControl.SpeedLevel.Body.L1);

                }

                handler.postDelayed(this, 1000);
            }
        });
    }

}
