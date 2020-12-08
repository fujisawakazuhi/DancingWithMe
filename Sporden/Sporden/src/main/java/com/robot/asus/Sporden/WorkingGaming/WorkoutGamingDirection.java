package com.robot.asus.Sporden.WorkingGaming;

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
import com.asus.robotframework.API.MotionControl;
import com.asus.robotframework.API.RobotAPI;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.SpeakConfig;
import com.asus.robotframework.API.Utility;
import com.asus.robotframework.API.VisionConfig;
import com.asus.robotframework.API.results.DetectFaceResult;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.robot.asus.Sporden.CameraPreview;
import com.robot.asus.Sporden.MainActivity;
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


public class WorkoutGamingDirection extends RobotActivity {

    private Context context;

    public static final int TYPE_CAPACITY_TOUCH = Utility.SensorType.CAPACITY_TOUCH;

    private static TextView textface, textTimes, textTimeCount, textDirection;
    private double smallscore, score, totalscore;

    //sensor
    private TextView textOrientation, textisShake;
    private SensorManager mSensorManager;
    private Sensor mSensorCapacityTouch;
    //sensor

    //Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference docRef = db.collection("Watch").document("watchState");
    Map<String, Object> FirebaseData;
    //Firebase


    private static VideoView videoView;
    private String actionName, VideoUri;

    private Button endButton;

    private static String WorkoutName;

    private static int d1, d2, completeTimes, timeCount, timeCountAction, watchOrientation, totalCompleteTimes;
    private static int pointInt, isGetCount;

    private static int Logtimes;
    private static ImageView cancleGame, isGetPersonIcon;


    private static int times, CorrectOrientation, backTime;
    private static int stageNum;
    private static int scriptNum;

    private static int[] WorkoutActionList;


    //相機相關
    private static FrameLayout preview;
    private static CameraPreview mPreview;

    private static boolean isrunning, isgetFace, isStarted, isrestart, NeedCahngeGetFaceIcon, isCompleteStartRobotMovement, isShake = false;


    private static ArrayList<Integer> script;


    private static DetectPersonXYZ[] answer;
    private static ActionDetecter actionDetecter;

    private SoundPool soundPool;
    private int correctsound, completesound, checksound;
    private static int iCurrentCommandSerial;

    final Handler handler = new Handler();

    private RotationRatingBar rotationRatingBar;
    private final String TAG = "WorkoutGaming";
    private SpordenRESTClient apiClient;
    private MediaPlayer mp = new MediaPlayer();
    private SpodenIndexClient indexClient;
    private String acc_email;
    private static String get_list;
    private static boolean isPostFalse = false;
    private static boolean isPostEnd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_gaming);


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


        //ZenboRobotMove
        iCurrentCommandSerial = robotAPI.motion.moveBody(-0.7f, 0, 0, MotionControl.SpeedLevel.Body.L1);


        actionName = "簡易深蹲";

        soundinitializer();


        context = getApplicationContext();


        Logtimes = 1;


        ScaleRatingBar ratingBar = new ScaleRatingBar(this);
        ratingBar.setClickable(false);

        rotationRatingBar = findViewById(R.id.rotationratingbar_main);
        rotationRatingBar.setClickable(false);
        rotationRatingBar.setNumStars(5);
        rotationRatingBar.setRating(0);


        textface = findViewById(R.id.textface);
        textTimes = findViewById(R.id.textTimes);
        textDirection = findViewById(R.id.textDirection);

        //textTimeCount = findViewById(R.id.TimeCount);
        endButton = findViewById(R.id.end);
        textisShake = findViewById(R.id.textisShake);
        textOrientation = findViewById(R.id.textOrientation);
        cancleGame = findViewById(R.id.cancleGame);
        isGetPersonIcon = findViewById(R.id.isGetPersonIcon);
        preview = findViewById(R.id.camera_preview);
        mPreview = new CameraPreview(this);

        backTime = 3000;
        stageNum = 0;
        scriptNum = 0;
        watchOrientation = -1;
        times = 0;
        isGetCount = 0;
        pointInt = 0;
        completeTimes = 0;
        timeCount = 1000;
        totalCompleteTimes = 0;
        score = 0;
        totalscore = 0;
        smallscore = 0;

        NeedCahngeGetFaceIcon = true;
        isrunning = false;
        isgetFace = false;
        isCompleteStartRobotMovement = false;
        isStarted = true;

        Intent intent = getIntent();
        WorkoutName = intent.getStringExtra("WorkoutName");
        WorkoutActionList = intent.getIntArrayExtra("WorkoutActionListInt");
        VideoUri = intent.getStringExtra("VideoUri");

        script = new ArrayList();

        //影像


        // Create the client (測試REST API)
        apiClient = new ApiClientFactory()
                .credentialsProvider(AWSMobileClient.getInstance())
                .region("us-east-1")
                .build(SpordenRESTClient.class);


        for (int i : WorkoutActionList) {
            script.add(i);
            if (i == 0) {
                totalCompleteTimes++;
                score = 100 / (double) totalCompleteTimes / 2;
                smallscore = score / 2;
            }
        }


        //robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.BACKWARD);


        // startDetectFace();


        //999跟0的還沒解決!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!有十萬分之一的機率會錯誤
        answer = new DetectPersonXYZ[100000];
        //999跟0的還沒解決!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!有萬分之一的機率會錯誤

        getFirebase();
        videoPlayer();

        actionInitSpeaker();

        //跑handler
        TimeCounter();
        isGetFaceChecker();
        isCanStart();
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
    protected void onRestart() {
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
            mp = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("onRestartt", "stop");
        stopDetectFace();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onRestartt", "Destroy");
        handler.removeCallbacksAndMessages(null);

        if (mp != null && mp.isPlaying()) {
            mp.stop();
            mp.release();
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


    public WorkoutGamingDirection() {

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

            if (serial == iCurrentCommandSerial && state != RobotCmdState.ACTIVE) {

                Log.d("AABBCC", "succeedd");

                startDetectFace();

                preview.addView(mPreview);

                //isCompleteStartRobotMovement = true;
            }
        }

        @Override
        public void initComplete() {
            super.initComplete();

        }

        @Override
        public void onDetectFaceResult(List<DetectFaceResult> resultList) {
            super.onDetectFaceResult(resultList);

            if (times == 99999) {
                times = 0;
            }


            answer[times] = new DetectPersonXYZ(resultList.get(0).getTrackID(), resultList.get(0).getFaceLoc().x,
                    resultList.get(0).getFaceLoc().y, resultList.get(0).getFaceLoc().z);

            textface.setText(answer[times].getAllString());

            isgetFace = true;
            if (NeedCahngeGetFaceIcon) {
                //changeIMG just one time
                isGetPersonIcon.setImageResource(R.drawable.icondetect_get);
                NeedCahngeGetFaceIcon = false;
            }
            isGetCount = 0;

            times++;


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

        if (script.get(scriptNum) == 0) {
            actionComplete();
        } else {
            DirectionSetter(script.get(scriptNum));
            YoYo.with(Techniques.BounceIn)
                    .duration(700)
                    .repeat(0)
                    .playOn(textDirection);


            if (scriptNum >= 1) {
                actionStartRecorder();
            } else {
                if (actionStartChecker()) {
                    actionStartRecorder();
                } else {
                    isrestart = true;
                }
            }


        }
    }

    public boolean actionStartChecker() {
        d1 = times - 1;


        if (script.get(scriptNum) == 1 && answer[d1].getZ() > 1.3) {

            return true;
        } else if (script.get(scriptNum) == 2 && answer[d1].getZ() < 1.3) {

            return true;

        } else if (script.get(scriptNum) == 3 && answer[d1].getX() > 1.7) {

            return true;

        } else if (script.get(scriptNum) == 4 && answer[d1].getX() < 1.7) {

            return true;

        } else {
            actionErrorSpeaker();
            return false;
        }

    }

    public boolean actionRestartChecker() {
        d1 = times - 1;

        if (script.get(scriptNum) == 1 && answer[d1].getZ() > 1.3) {

            return true;
        } else if (script.get(scriptNum) == 2 && answer[d1].getZ() < 1.3) {

            return true;

        } else if (script.get(scriptNum) == 3 && answer[d1].getX() > 1.7) {

            return true;

        } else if (script.get(scriptNum) == 4 && answer[d1].getX() < 1.7) {

            return true;

        }

        return false;

    }


    public void actionStartRecorder() {
        //開啟偵測
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                //要Delay一下再抓位置，比較準確

                actionNameSpeaker();

                //使用者沒動一陣子的再提醒
                timeCountAction = 0;

                isrunning = true;

                d1 = times - 1;


            }
        }, 100);
    }


    public void actionChecker() {
        //檢查動作是否完成
        d2 = times - 1;
        actionDetecter = new ActionDetecter(answer[d1], answer[d2], script.get(scriptNum));


        if (actionDetecter.getActionResult()) {

            if (watchOrientation == CorrectOrientation) {
                effectSound(3);
                totalscore = totalscore + score;
                actionStarter();
            } else {
                effectSound(3);
                totalscore = totalscore + smallscore;
                robotAPI.robot.speak(getString(R.string.flat_hand));
                actionStarter();
            }


        }


    }

    //完成一組
    public void actionComplete() {

        Logtimes++;
        Log.d("LogTest", "actionComplete" + Logtimes);
        Log.d("LogTestGG", "" + scriptNum + "  " + script.size());
        pointInt++;


        if (scriptNum >= script.size() - 1) {
            actionEnd();
        } else {

            completeTimes++;
            //textTimes.setText(String.valueOf(completeTimes));
            rotationRatingBar.setRating(completeTimes);

            effectSound(1);
            actionStarter();

        }

    }

    //完成所有
    public void actionEnd() {

        isrunning = false;


        robotAPI.robot.speak(getString(R.string.finish_game));
        effectSound(2);


        Log.d("scoreget", totalscore + "");

        //傳分數

        changeActivityDelay();
    }

    public void actionNotEnd() {

        isrunning = false;


        robotAPI.robot.speak(getString(R.string.sorry_timeup));

        changeActivityDelay();
    }


    public void runTimer() {

        handler.post(new Runnable() {
            @Override
            public void run() {


                Log.d("lxhandler", "WorkoutGaming");


                if (isrunning) {

                    actionChecker();
                }
                if (isrestart) {
                    boolean isrestartOk = actionRestartChecker();
                    if (isrestartOk) {
                        isrestart = false;
                        actionStartRecorder();
                    }
                }
                handler.postDelayed(this, 10);
            }
        });
    }


    public void isGetFaceChecker() {

        handler.post(new Runnable() {
            @Override
            public void run() {


                isGetCount++;

                Log.d("lxhandler", "WorkoutGaming");

                if (isGetCount == 10) {

                    textface.setText("沒有人");

                    isgetFace = false;
                    isGetPersonIcon.setImageResource(R.drawable.icondetect_non);
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(0)
                            .playOn(isGetPersonIcon);
                    NeedCahngeGetFaceIcon = true;

                    // Toast toast = Toast.makeText(WorkoutGamingDirection.this,
                    //    "Zenbo看不到你！", Toast.LENGTH_SHORT);
                    //toast.show();

                }

                if (!isStarted && isGetCount == 20) {
                    robotAPI.robot.speak(getString(R.string.not_find));
                }


                handler.postDelayed(this, 100);
            }
        });
    }

    public void isCanStart() {

        handler.post(new Runnable() {
            @Override
            public void run() {

                Log.d("lxhandler", "WorkoutGaming");
                if (isgetFace && isStarted) {

                    Log.d("Yifantest", "success");

                    startInit();
                    isStarted = false;

                }

                handler.postDelayed(this, 100);
            }
        });

    }


    public void startInit() {
        scriptNum = -1;
        actionStarter();
        runTimer();


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
                    startActivity(new Intent(WorkoutGamingDirection.this, MainActivity.class));
                    System.exit(0);
                    handlerC.removeCallbacksAndMessages(null);
                }
                handlerC.postDelayed(this, 300);
            }
        });

        robotAPI.robot.speak("掰掰");

        doPostEnd();
        doInvokeIndex();

        //finish();


    }

    public void TimeCounter() {

        //計算運動總時間,以秒來記

        handler.post(new Runnable() {
            @Override
            public void run() {


                timeCount--;
                timeCountAction++;

                if (timeCountAction % 10 == 0 && !isStarted && isgetFace && !isrestart) {
                    actionAgainNameSpeaker();
                }


                if (timeCount == 950 && isStarted) {
                    robotAPI.robot.speak("我找不到你，如果是我的錯，請幫我重新開始運動，我需要重啟偵測相機");
                }


                if (timeCount == 0) {
                    actionNotEnd();

                }


                handler.postDelayed(this, 1000);
            }
        });
    }


    public void DirectionSetter(int i) {
        switch (i) {
            default:
                textDirection.setText(getString(R.string.wrong_dir));
                break;
            case 1:
                textDirection.setText(getString(R.string.squat_dw));
                break;
            case 2:
                textDirection.setText(getString(R.string.stand_up));
                break;
            case 3:
                textDirection.setText(getString(R.string.go_forward));
                break;
            case 4:
                textDirection.setText(getString(R.string.go_backwards));
                break;
            case 5:
                textDirection.setText(getString(R.string.to_right));
                break;
            case 6:
                textDirection.setText(getString(R.string.to_left));
                break;
        }
    }

    public void actionNameSpeaker() {


        switch (script.get(scriptNum)) {
            default:
                robotAPI.robot.speak(getString(R.string.plez_mv));
                break;
            case 1:
                robotAPI.robot.speak(getString(R.string.squat_dw));
                break;
            case 2:
                robotAPI.robot.speak(getString(R.string.stand_up));
                break;
            case 3:
                robotAPI.robot.speak(getString(R.string.go_forward));
                break;
            case 4:
                robotAPI.robot.speak(getString(R.string.go_backwards));
                break;
            case 5:
                robotAPI.robot.speak(getString(R.string.to_right));
                break;
            case 6:
                robotAPI.robot.speak(getString(R.string.to_left));
                break;
        }
    }

    public void actionAgainNameSpeaker() {


        switch (script.get(scriptNum)) {
            default:
                robotAPI.robot.speak(getString(R.string.plez_mv));
                break;
            case 1:
                robotAPI.robot.speak(getString(R.string.squat_dw_plz), new SpeakConfig().pitch(129));
                break;
            case 2:
                robotAPI.robot.speak(getString(R.string.stand_up_plz), new SpeakConfig().pitch(129));
                break;
            case 3:
                robotAPI.robot.speak(getString(R.string.forward_plz), new SpeakConfig().pitch(129));
                break;
            case 4:
                robotAPI.robot.speak(getString(R.string.backwards_plz), new SpeakConfig().pitch(129));
                break;
            case 5:
                robotAPI.robot.speak(getString(R.string.to_right), new SpeakConfig().pitch(129));
                break;
            case 6:
                robotAPI.robot.speak(getString(R.string.to_left), new SpeakConfig().pitch(129));
                break;
        }
    }

    public void actionErrorSpeaker() {

        switch (script.get(scriptNum)) {
            case 1:
                robotAPI.robot.speak(getString(R.string.too_low));
                break;
            case 2:
                robotAPI.robot.speak(getString(R.string.too_high));
                break;
            case 3:
                robotAPI.robot.speak(getString(R.string.too_close));
                break;
            case 4:
                robotAPI.robot.speak(getString(R.string.too_far));
                break;
            case 5:
                robotAPI.robot.speak(getString(R.string.action_start_err));
                break;
            case 6:
                robotAPI.robot.speak(getString(R.string.action_start_err));
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

    public void actionInitSpeaker() {

        robotAPI.robot.speak(getString(R.string.welcome_to) + WorkoutName);
        robotAPI.robot.speak(getString(R.string.plez_finish) + totalCompleteTimes + getString(R.string.times));
        robotAPI.robot.speak(getString(R.string.ready));

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
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/raw/" + VideoUri);
        //Uri uri = Uri.parse("android.resource://" + getPackageName() + "/raw/squat");
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


    private static void startDetectFace() {
        // start detect face
        VisionConfig.FaceDetectConfig config = new VisionConfig.FaceDetectConfig();
        config.enableDebugPreview = false;  // set to true if you need preview screen
        config.intervalInMS = 100;
        config.enableDetectHead = true;
        config.enableFacePosture = true;
        config.enableHeadGazeClassifier = true;
        robotAPI.vision.requestDetectFace(config);
    }


    private void stopDetectFace() {
        // stop detect person
        robotAPI.vision.cancelDetectFace();

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
                    isShake = (Boolean) FirebaseData.get("isShake");
                    watchOrientation = ((Number) FirebaseData.get("orientation")).intValue();
                    Log.d("DataCheck", Boolean.toString(isShake));

                } else {
                    Log.d("TAG", source + " data: null");
                }
            }
        });
        //firebase
    }

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


    public void getAws() {

        handler.post(new Runnable() {
            @Override
            public void run() {

                doCheckisShake();
                doCheckOrientation();
                Log.d("awsYifan", "fuck");
                Log.d("awsYifan", Boolean.toString(isShake) + "  " + Integer.toString(watchOrientation));
                textisShake.setText(Boolean.toString(isShake));
                textOrientation.setText(Integer.toString(watchOrientation));
                handler.postDelayed(this, 200);
            }
        });
    }

    private void doCheckisShake() {
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
        parameters.put("userId", "isShakjemcinoasncoiccx");

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
                        String tmp = responseData.substring(responseData.indexOf("{"), responseData.lastIndexOf("}") + 1);
                        JSONObject json_read2 = new JSONObject(tmp);
                        isShake = json_read2.getBoolean("isShaked");
                        Log.d(TAG, "Response for state : " + Boolean.toString(isShake));


                    }

                    Log.d(TAG, "status code = " + response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    private void doCheckOrientation() {
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
        parameters.put("userId", "watchOrientation");

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
                        String tmp = responseData.substring(responseData.indexOf("{"), responseData.lastIndexOf("}") + 1);
                        JSONObject json_read2 = new JSONObject(tmp);
                        watchOrientation = json_read2.getInt("orientation");
                        Log.d(TAG, "Response for watchOrientation : " + Integer.toString(watchOrientation));


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




