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
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.SpeakConfig;
import com.asus.robotframework.API.Utility;
import com.asus.robotframework.API.VisionConfig;
import com.asus.robotframework.API.WheelLights;
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
import com.robot.asus.Sporden.GetLocale;
import com.robot.asus.Sporden.MainActivity;
import com.robot.asus.Sporden.R;
import com.robot.asus.Sporden.WorkingGaming.WorkoutGameData.ActionDetecter;
import com.robot.asus.Sporden.WorkingGaming.WorkoutGameData.DetectPersonXYZ;
import com.robot.asus.Sporden.spodenIndex.SpodenIndexClient;
import com.robot.asus.Sporden.spordenREST.SpordenRESTClient;
import com.robot.asus.robotactivity.RobotActivity;
import com.willy.ratingbar.RotationRatingBar;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WorkoutGaming extends RobotActivity {

    private Context context;

    public static final int TYPE_CAPACITY_TOUCH = Utility.SensorType.CAPACITY_TOUCH;

    private static TextView textface, textTimes, textTimeCount, textDirection;

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


    private static VideoView videoView, videoView2;


    private Button endButton;

    private String WorkoutName, VideoUri;
    private int totalCompleteTimes;

    private static int d1, d2, completeTimes, timeCount, timeCountAction, watchOrientation, iCurrentCommandSerial;
    private static int pointInt, isGetCount;

    private static int Logtimes;

    //相機相關
    private static FrameLayout preview;
    private static CameraPreview mPreview;


    private static int times, backTime;
    private double score, totalscore;
    private static int stageNum;
    private static int scriptNum, iCurrentSpeakSerial;

    private static int[] WorkoutActionList;

    private static boolean isrunning, isgetFace, isStarted, isrestart, NeedCahngeGetFaceIcon, isShake = false;
    private static ImageView cancleGame, isGetPersonIcon;


    private static ArrayList<Integer> script;


    private static DetectPersonXYZ[] answer;
    private static ActionDetecter actionDetecter;

    private SoundPool soundPool;
    private int correctsound, completesound, checksound;

    final Handler handler = new Handler();

    private RotationRatingBar rotationRatingBar;
    private final String TAG = "WorkoutGaming";
    private SpordenRESTClient apiClient;
    private SpodenIndexClient indexClient;
    private String acc_email;
    private static String get_list;
    private static boolean isPostEnd = false;
    private static boolean isPostFalse = false;

    private MediaPlayer mp = new MediaPlayer();

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

        iCurrentCommandSerial = robotAPI.motion.moveBody(-0.7f, 0, 0, MotionControl.SpeedLevel.Body.L1);


        robotAPI.wheelLights.turnOff(WheelLights.Lights.SYNC_BOTH, 0xff);
        robotAPI.wheelLights.setColor(WheelLights.Lights.SYNC_BOTH, 0xff, 0x0000ff00);
        robotAPI.wheelLights.setBrightness(WheelLights.Lights.SYNC_BOTH, 0xff, 10);
        robotAPI.wheelLights.startBlinking(WheelLights.Lights.SYNC_BOTH, 0xff, 50, 50, 5);

        soundinitializer();


        context = getApplicationContext();

        //music 開始播


        Logtimes = 1;


        RotationRatingBar ratingBar = new RotationRatingBar(this);
        ratingBar.setClickable(false);
        ratingBar.setStepSize(1);

        rotationRatingBar = findViewById(R.id.rotationratingbar_main);
        rotationRatingBar.setClickable(false);
        rotationRatingBar.setNumStars(5);
        rotationRatingBar.setRating(0);


        textface = findViewById(R.id.textface);
        textTimes = findViewById(R.id.textTimes);
        textDirection = findViewById(R.id.textDirection);
        endButton = findViewById(R.id.end);
        textisShake = findViewById(R.id.textisShake);
        textOrientation = findViewById(R.id.textOrientation);
        cancleGame = findViewById(R.id.cancleGame);
        isGetPersonIcon = findViewById(R.id.isGetPersonIcon);
        preview = findViewById(R.id.camera_preview);
        mPreview = new CameraPreview(this);

        WorkoutName = "簡易深蹲";
        backTime = 3000;
        score = 0;
        stageNum = 0;
        scriptNum = 0;
        times = 0;
        isGetCount = 0;
        pointInt = 0;
        completeTimes = 0;
        timeCount = 1000;


        //抓取運動腳本以及名稱
        Intent intent = getIntent();
        WorkoutName = intent.getStringExtra("WorkoutName");
        WorkoutActionList = intent.getIntArrayExtra("WorkoutActionListInt");
        VideoUri = intent.getStringExtra("VideoUri");


        script = new ArrayList<>();

        for (int i : WorkoutActionList) {
            script.add(i);
            if (i == 0) {
                totalCompleteTimes++;
                score = 100 / (double) totalCompleteTimes / 2;
            }
        }
        Log.d("yifanTest", WorkoutName + script.toString());

        isrunning = false;
        isgetFace = false;
        isStarted = true;
        NeedCahngeGetFaceIcon = true;


        // Create the client (測試REST API)
        apiClient = new ApiClientFactory()
                .credentialsProvider(AWSMobileClient.getInstance())
                .region("us-east-1")
                .build(SpordenRESTClient.class);


        //robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.BACKWARD);


        //999跟0的還沒解決!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!有十萬分之一的機率會錯誤
        answer = new DetectPersonXYZ[1000000];
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

        //為了再次跳進來時script不會因為startinit()而變成-1
        isgetFace = false;


        mSensorManager.registerListener(listenerCapacityTouch, mSensorCapacityTouch, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (GetLocale.getLocale().equals("zh")) {
            robotAPI.robot.speak("您已跳出遊戲，按叉叉，再請重新開始");
        } else if (GetLocale.getLocale().equals("en")) {
            robotAPI.robot.speak("You have jumped out of the game, press the cross, then start again");
        }
    }

    @Override
    protected void onPause() {

        super.onPause();
        robotAPI.robot.stopSpeak();
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
        robotAPI.wheelLights.turnOff(WheelLights.Lights.SYNC_BOTH, 0xff);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onRestartt", "Destroy");
        handler.removeCallbacksAndMessages(null);


        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
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


    public WorkoutGaming() {

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
        Log.d("timesTes", times + "");
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

        } else if (script.get(scriptNum) == 3 && answer[d1].getX() > 1.8) {

            return true;

        } else if (script.get(scriptNum) == 4 && answer[d1].getX() < 1.8) {

            return true;

        }

        return false;

    }


    public void actionStartRecorder() {
        //開啟偵測
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                actionNameSpeaker();

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

            totalscore = totalscore + score;
            effectSound(3);
            actionStarter();
        }


    }

    //完成一組
    public void actionComplete() {

        pointInt++;


        if (scriptNum >= script.size() - 1) {
            actionEnd();
        } else {

            completeTimes++;
            textTimes.setText(String.valueOf(completeTimes));
            rotationRatingBar.setRating(completeTimes);

            effectSound(1);
            actionStarter();

        }

    }

    //完成所有
    public void actionEnd() {

        isrunning = false;

        if (GetLocale.getLocale().equals("zh")) {
            robotAPI.robot.speak("恭喜你，完成運動！");
        } else if (GetLocale.getLocale().equals("en")) {
            robotAPI.robot.speak("Congratulations, you have completed the exercise!");
        }
        effectSound(2);

        Log.d("scoreGet", totalscore + "");


        changeActivityDelay();
    }

    //沒有在時間內完成所有
    public void actionNotEnd() {

        isrunning = false;

        if (GetLocale.getLocale().equals("zh")) {
            robotAPI.robot.speak("很抱歉，時間到了");
        } else if (GetLocale.getLocale().equals("en")) {
            robotAPI.robot.speak("It's a pity that time is up.");
        }

        changeActivityDelay();
    }


    public void ActionrunTimer() {

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

                    if (GetLocale.getLocale().equals("zh")) {
                        Toast toast = Toast.makeText(WorkoutGaming.this,
                                "Zenbo看不到你！", Toast.LENGTH_SHORT);
                        toast.show();
                    } else if (GetLocale.getLocale().equals("en")) {
                        Toast toast = Toast.makeText(WorkoutGaming.this,
                                "Zenbo cannot see you！", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                }

                if (!isStarted && isGetCount == 20) {
                    if (GetLocale.getLocale().equals("zh")) {
                        robotAPI.robot.speak("我找不到你，不要離開我");
                    } else if (GetLocale.getLocale().equals("en")) {
                        robotAPI.robot.speak("I cannot find you, don't leave me alone!");
                    }
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
                Log.d("testisget", Boolean.toString(isgetFace));
                if (isgetFace && isStarted) {

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
        ActionrunTimer();

    }


    public void changeActivityDelay() {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(WorkoutGaming.this, WorkoutGamingResult.class);
                intent.putExtra("score", totalscore);
                startActivity(intent);
                //System.exit(0);


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
                    startActivity(new Intent(WorkoutGaming.this, MainActivity.class));
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

        //計算運動總時間

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

    //開頭解說動作
    public void actionInitSpeaker() {

        if (GetLocale.getLocale().equals("zh")) {
            robotAPI.robot.speak("歡迎來到，" + WorkoutName);
            iCurrentSpeakSerial = robotAPI.robot.speak("請完成" + totalCompleteTimes + "下");
            robotAPI.robot.speak("準備");
        } else if (GetLocale.getLocale().equals("en")) {
            robotAPI.robot.speak("Welcome to，" + WorkoutName);
            iCurrentSpeakSerial = robotAPI.robot.speak("Please complete " + totalCompleteTimes + " times");
            robotAPI.robot.speak("Ready");
        }

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

        //Uri littleAnimation = Uri.parse("android.resource://" + getPackageName() + "/raw/walkingcp");
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/raw/" + VideoUri);
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

                } else {
                    Log.d("TAG", source + " data: null");
                }
            }
        });
        //firebase

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




