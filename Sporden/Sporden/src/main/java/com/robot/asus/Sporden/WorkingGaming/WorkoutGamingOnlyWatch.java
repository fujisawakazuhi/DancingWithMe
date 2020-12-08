package com.robot.asus.Sporden.WorkingGaming;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.amazonaws.http.HttpMethodName;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.mobileconnectors.apigateway.ApiRequest;
import com.amazonaws.mobileconnectors.apigateway.ApiResponse;
import com.amazonaws.util.IOUtils;
import com.amazonaws.util.StringUtils;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.Utility;
import com.asus.robotframework.API.VisionConfig;
import com.asus.robotframework.API.results.DetectFaceResult;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.dinuscxj.progressbar.CircleProgressBar;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.robot.asus.Sporden.CameraPreview;
import com.robot.asus.Sporden.MainActivity;
import com.robot.asus.Sporden.Model.Workout;
import com.robot.asus.Sporden.R;
import com.robot.asus.Sporden.WorkingGaming.WorkoutGameData.ActionDetecter;
import com.robot.asus.Sporden.WorkingGaming.WorkoutGameData.DetectPersonXYZ;
import com.robot.asus.Sporden.spodenIndex.SpodenIndexClient;
import com.robot.asus.Sporden.spordenREST.SpordenRESTClient;
import com.robot.asus.robotactivity.RobotActivity;
import com.willy.ratingbar.RotationRatingBar;
import com.willy.ratingbar.ScaleRatingBar;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WorkoutGamingOnlyWatch extends RobotActivity {

    private Context context;

    public static final int TYPE_CAPACITY_TOUCH = Utility.SensorType.CAPACITY_TOUCH;

    private TextView textTimes,textDirection;


    //sensor
    private TextView textOrientation, textisShake, textShakeTime;
    private SensorManager mSensorManager;
    private Sensor mSensorCapacityTouch;
    //sensor

    //Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference docRef = db.collection("Watch").document("watchState");
    Map<String, Object> FirebaseData;
    //Firebase


    private static VideoView videoView;
    private String WorkoutName,VideoUri;
    private ImageView cancleGame;

    private Button endButton;


    private static int timeCount, ActionTime, watchOrientation, scriptNum,completeTimes;
    private double smallscore, score, totalscore;


    private static int[] WorkoutActionList;


    private static ArrayList<Integer> script;

    private SoundPool soundPool;
    private int correctsound, completesound, checksound,totalCompleteTimes;

    final Handler handler = new Handler();

    private RotationRatingBar rotationRatingBar;
    private final String TAG = "WorkoutGaming";
    private SpordenRESTClient apiClient;

    private CircleProgressBar mLineProgressBar;
    private ValueAnimator animator;

    private MediaPlayer mp = new MediaPlayer();

    private SpodenIndexClient indexClient;
    private String acc_email;
    private static String get_list;
    private static boolean isPostFalse = false;
    private static boolean isPostEnd = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_gaming_only_watch);

        indexClient = new ApiClientFactory()
                .credentialsProvider(AWSMobileClient.getInstance())
                .region("us-east-1")
                .build(SpodenIndexClient.class);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (acct != null) {
            //取得google帳戶資訊
            acc_email = acct.getEmail();
        }

        //sensortest

        // sensor manager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // sensors
        mSensorCapacityTouch = mSensorManager.getDefaultSensor(TYPE_CAPACITY_TOUCH);

        mLineProgressBar = (CircleProgressBar) findViewById(R.id.line_progress);


        progressSet();
        script = new ArrayList();
        WorkoutName="沒抓到運動名稱";

        Intent intent = getIntent();
        WorkoutName = intent.getStringExtra("WorkoutName");
        WorkoutActionList = intent.getIntArrayExtra("WorkoutActionListInt");
        VideoUri = intent.getStringExtra("VideoUri");



        completeTimes=0;
        timeCount = 0;
        ActionTime = 0;
        scriptNum = -1;
        watchOrientation = -1;
        totalCompleteTimes=WorkoutActionList.length;
        totalscore=0;

        score = 100 / (double) totalCompleteTimes ;

        for (int i : WorkoutActionList) {
            script.add(i);
        }


        soundinitializer();


        context = getApplicationContext();


        ScaleRatingBar ratingBar = new ScaleRatingBar(this);
        ratingBar.setClickable(false);

        rotationRatingBar = findViewById(R.id.rotationratingbar_main);
        rotationRatingBar.setClickable(false);
        rotationRatingBar.setNumStars(WorkoutActionList.length);
        rotationRatingBar.setRating(0);


        textTimes = findViewById(R.id.textTimes);
        endButton = findViewById(R.id.end);
        textisShake = findViewById(R.id.textisShake);
        textOrientation = findViewById(R.id.textOrientation);
        textShakeTime = findViewById(R.id.textShakeTime);
        cancleGame = findViewById(R.id.cancleGame);
        textDirection = findViewById(R.id.textDirection);


        timeCount = 1000;



        //影像
        CameraPreview mPreview = new CameraPreview(this);
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        // Create the client (測試REST API)
        apiClient = new ApiClientFactory()
                .credentialsProvider(AWSMobileClient.getInstance())
                .region("us-east-1")
                .build(SpordenRESTClient.class);



        actionInitSpeaker();
        actionStarter();

        getFirebase();
        videoPlayer();

        //跑handler
        TimeCounter();

        //getAws();
        cancleGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancleGameDelay();
            }
        });

        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionEnd();
            }
        });


    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onRestartt", "resume");


        mSensorManager.registerListener(listenerCapacityTouch, mSensorCapacityTouch, SensorManager.SENSOR_DELAY_UI);
    }
    @Override
    protected void onRestart(){
        super.onRestart();
        robotAPI.robot.speak(getString(R.string.exit_game));
    }
    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(listenerCapacityTouch);

        if (mp != null) {
            mp.stop();
            mp.release();
            mp=null;
        }

    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.d("onRestartt", "stop");
        handler.removeCallbacksAndMessages(null);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onRestartt", "Destroy");
        handler.removeCallbacksAndMessages(null);

        if (mp != null) {
            mp.stop();
            mp.release();
            mp=null;
        }


    }


    SensorEventListener listenerCapacityTouch = new SensorEventListener() {
        //Sensor

        @Override
        public void onSensorChanged(SensorEvent event) {

            Log.d("asdfg", event.values[0] + "");
            if (event.values[0] == 1) {
                robotAPI.robot.speak("靠");
                Log.d("asdfg", "進入");

            } else if (event.values[0] == 3) {
                robotAPI.robot.speak("哦吼");
            }


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    public WorkoutGamingOnlyWatch() {

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

            // String a =RobotCmdState.getCommandState(cmd).toString();

            // Log.d("RobotStateTest",a);
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


    public void actionStarter() {

        scriptNum++;
        ActionTime = 0;
        textShakeTime.setText(String.valueOf(ActionTime));

        actionNameSpeaker(script.get(scriptNum));
        YoYo.with(Techniques.BounceIn)
                .duration(700)
                .repeat(0)
                .playOn(textDirection);

    }


    //完成所有
    public void actionEnd() {


        robotAPI.robot.speak(getString(R.string.finish_game));
        Log.d("scoreget",totalscore+"");
        effectSound(2);

        changeActivityDelay();
    }
    public void actionNotEnd() {



        robotAPI.robot.speak(getString(R.string.sorry_timeup));

        changeActivityDelay();
    }


    public void changeActivityDelay() {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(getApplicationContext(), WorkoutGamingResult.class);
                intent.putExtra("score", totalscore);
                startActivity(intent);


            }
        }, 100);
    }
    public void cancleGameDelay() {


        final Handler handlerC = new Handler();
        handlerC.post(new Runnable() {
            @Override
            public void run() {
                if (isPostFalse && isPostEnd) {
                    isPostEnd = false;
                    isPostFalse = false;
                    startActivity(new Intent(WorkoutGamingOnlyWatch.this, MainActivity.class));
                    System.exit(0);
                    handlerC.removeCallbacksAndMessages(null);
                }
                handlerC.postDelayed(this, 300);
            }
        });

        robotAPI.robot.speak("掰掰");

        doPostEnd();
        doInvokeIndex();
    }

    public void TimeCounter() {

        //計算運動總時間

        handler.post(new Runnable() {
            @Override
            public void run() {

                timeCount--;

                //1000秒時跳到actionEnd()

                if (watchOrientation == script.get(scriptNum)) {
                    ActionTime++;
                    textTimes.setText(String.valueOf(ActionTime));
                    textShakeTime.setText(String.valueOf(ActionTime));
                    animator.resume();

                    if (ActionTime == 8) {
                        completeTimes++;

                        if (scriptNum == WorkoutActionList.length - 1) {
                            totalscore = totalscore + score;
                            rotationRatingBar.setRating(completeTimes);
                            actionEnd();

                        } else {
                            actionStarter();
                            totalscore = totalscore + score;
                            rotationRatingBar.setRating(completeTimes);
                        }

                    }
                } else {

                    animator.pause();

                }

                if (timeCount == 0) {
                    actionNotEnd();

                }


                handler.postDelayed(this, 1000);
            }
        });
    }

    public void actionNameSpeaker(int i) {


        switch (i) {
            default:
                textDirection.setText(getString(R.string.plez_mv));
                robotAPI.robot.speak(getString(R.string.plez_mv));
                break;
            case 1:
                textDirection.setText(getString(R.string.up_stretch));
                robotAPI.robot.speak(getString(R.string.up_stretch));
                break;
            case 3:
                textDirection.setText(getString(R.string.down_stretch));
                robotAPI.robot.speak(getString(R.string.down_stretch));
                break;
        }
    }


    public void effectSound(int i) {
        switch (i) {
            case (1):
                soundPool.play(correctsound, 1, 1, 0, 0, 1.0F);
                break;

            case (2):
                soundPool.play(completesound, 1.0F, 1.0F, 0, 0, 1.0F);
                break;
            case (3):
                soundPool.play(checksound, 1.0F, 1.0F, 0, 0, 1.0F);
                break;
        }
    }

    public void soundinitializer() {
        mp = MediaPlayer.create(this, R.raw.workoutmusic1);
        mp.start();
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 5);
        correctsound = soundPool.load(this, R.raw.correct, 1);
        completesound = soundPool.load(this, R.raw.complete, 1);
        checksound = soundPool.load(this, R.raw.actioncheck2, 1);

    }

    public void videoPlayer() {
        videoView = (VideoView) findViewById(R.id.videoView);
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/raw/"+VideoUri);
       // Uri uri = Uri.parse("android.resource://" + getPackageName() + "/raw/squat");
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
    public void actionInitSpeaker() {

        robotAPI.robot.speak(getString(R.string.welcome_to) + WorkoutName);
        robotAPI.robot.speak(getString(R.string.plez_finish) + totalCompleteTimes + getString(R.string.times));
        robotAPI.robot.speak(getString(R.string.ready));

    }

    public void progressSet() {
        animator = ValueAnimator.ofInt(0, 100);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int progress = (int) animation.getAnimatedValue();
                mLineProgressBar.setProgress(progress);
            }
        });
        animator.setDuration(8000);
        animator.setRepeatCount(totalCompleteTimes-1);
        animator.start();
        animator.pause();
    }


    //以下為資料庫連線--------------------------------------------------------


    public void getFirebase() {


        //firebase
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    Log.d("TAG", source + " data: " + snapshot.getData());
                    FirebaseData = snapshot.getData();
                    textisShake.setText(FirebaseData.get("isShake").toString());
                    textOrientation.setText(FirebaseData.get("orientation").toString());
                    watchOrientation = ((Number) FirebaseData.get("orientation")).intValue();

                } else {
                    Log.d("TAG", source + " data: null");
                }
            }
        });
        //firebase

    }
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
                        isPostEnd = true;
                    }

                    Log.d(TAG, "status code = " + response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
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
                        String responseData = IOUtils.toString(responseContentStream);
                        Log.d(TAG, "Response : " + responseData);
                        //取json
                        String tmp = responseData.substring(responseData.indexOf("{"), responseData.lastIndexOf("}") + 1);
                        JSONObject json_read = new JSONObject(tmp);
                        //取done_list陣列格式
                        get_list = json_read.getString("done_list");
                        doPostFalse();
                    }

                    Log.d(TAG, "status code = " + response.getStatusCode() + " " + response.getStatusText());
                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    private void doPostFalse() {
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
                        String responseData = IOUtils.toString(responseContentStream);
                        Log.d(TAG, "Response : " + responseData);
                        isPostFalse = true;
                    }

                    Log.d(TAG, "status code = " + response.getStatusCode() + " " + response.getStatusText());
                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
    }


}



