package com.robot.asus.Sporden.WorkoutCategory;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageButton;

import com.asus.robotframework.API.RobotAPI;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.RobotUtil;
import com.asus.robotframework.API.SpeakConfig;
import com.asus.robotframework.API.results.DetectFaceResult;
import com.robot.asus.Sporden.GetLocale;
import com.robot.asus.Sporden.MainActivity;
import com.robot.asus.Sporden.MembershipContainer;
import com.robot.asus.Sporden.Model.Workout;
//import com.robot.asus.Sporden.Model.WorkoutGameData.DetectPersonXYZ;
import com.robot.asus.Sporden.Myboard;
import com.robot.asus.Sporden.R;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.util.List;

public class WorkoutCategory extends RobotActivity {
    private TextView textback;
    private TextView search;

    private CardView cardcourse, cardwarmup, cardaerobic, cardcooldown, cardmuscle;
    public final static String TAG = "WorkoutCategoryDialogue";
    public final static String DOMAIN = "2532C682CCD447C7AEAE1830C7DC2219";
    private final String TAG2 = "lxhandler";

    private static boolean startact_warmup = false;
    private static boolean startact_ox = false;
    private static boolean startact_muscle = false;
    private static boolean startact_calmdown = false;
    private static boolean startact_package = false;
    private static boolean startact_back = false;
    private static boolean startact_search = false;
    private final Handler handler = new Handler();



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
            if(sIntentionID.equals("EnterExerciseCategory")) {
                //取這個plan的那個要得變數
                String sSluResultButton = RobotUtil.queryListenResultJson(jsonObject, "運動分類", null);
                Log.d(TAG, "Result Button = " + sSluResultButton);

                //要和concept的instance一樣(這是把變數拿來比對的條件式)
                if(sSluResultButton.equals("暖身")) {
                    startact_warmup = true;
                } else if (sSluResultButton.equals("有氧")) {
                    startact_ox = true;
                } else if (sSluResultButton.equals("肌力")) {
                    startact_muscle = true;
                } else if (sSluResultButton.equals("收操")) {
                    startact_calmdown = true;
                } else if (sSluResultButton.equals("組合運動")) {
                    startact_package = true;
                } else if (sSluResultButton.equals("搜尋")) {
                    startact_search = true;
                }
            } else if (sIntentionID.equals("BackToMain")) {
                //取這個plan的那個要得變數
                String sSluResultButton = RobotUtil.queryListenResultJson(jsonObject, "返回", null);
                Log.d(TAG, "Result Button = " + sSluResultButton);

                //要和concept的instance一樣(這是把變數拿來比對的條件式)
                if(sSluResultButton.equals("返回")) {
                    startact_back = true;
                }
            }
        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };

    public WorkoutCategory() {
        super(robotCallback, robotListenCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_category);
        //保持畫面不讓zenbo臉打斷
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initView();
        //返回鍵
        backSetup();
        //搜尋建
        changePageSearch();
        //RobotActivity.robotAPI.robot.speakAndListen("想要做哪一種運動呢?", new SpeakConfig().timeout(20));
        //runTimer();
    }

    @Override
    protected void onResume(){
        super.onResume();
        // clear dde
        //robotAPI.robot.clearAppContext(DOMAIN);

        // close faical
        robotAPI.robot.setExpression(RobotFace.HIDEFACE);

        //在頁面開啟時註冊Listen callback
        //robotAPI.robot.registerListenCallback(robotListenCallback);

        // jump dialog domain
        //robotAPI.robot.jumpToPlan(DOMAIN, "EnterExerciseCategory");

        // listen user utterance
        //robotAPI.robot.speakAndListen("想要做哪一種運動呢?", new SpeakConfig().timeout(20));
        if(GetLocale.getLocale().equals("zh")) {
            robotAPI.robot.speak("想要做哪一種運動呢?");
        } else if (GetLocale.getLocale().equals("en")) {
            robotAPI.robot.speakAndListen("Which types of exercise do you want to do?", new SpeakConfig().timeout(20));
        }

        runTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //stop listen user utterance
        robotAPI.robot.stopSpeakAndListen();

        handler.removeCallbacksAndMessages(null);

        //在停止頁面時停止註冊
        //robotAPI.robot.unregisterListenCallback();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //handler.removeCallbacksAndMessages(null);
        //robotAPI.robot.clearAppContext(DOMAIN);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // jump dialog domain
        //robotAPI.robot.jumpToPlan(DOMAIN, "EnterExerciseCategory");
        //runTimer();
    }

    /*@Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }*/


    public void initView() {
        textback = findViewById(R.id.textback);
        cardaerobic = findViewById(R.id.cardaerobic);
        cardwarmup = findViewById(R.id.cardwarmup);
        cardcooldown = findViewById(R.id.cardcooldown);
        cardmuscle = findViewById(R.id.cardmuscle);
        //cardcourse = findViewById(R.id.cardcourse);
        search = findViewById(R.id.buttonSearch);
    }

    public void changePageWarmup(View view) {
        Intent intent = new Intent(WorkoutCategory.this, WorkoutCategoryDetial.class);
        intent.putExtra("category", "暖身");
        startActivity(intent);

    }

    public void changePageAerobic(View view) {

        Intent intent = new Intent(WorkoutCategory.this, WorkoutCategoryDetial.class);
        intent.putExtra("category", "有氧");
        startActivity(intent);

    }
    public void changePageMuscle(View view) {

        Intent intent = new Intent(WorkoutCategory.this, WorkoutCategoryDetial.class);
        intent.putExtra("category", "肌力");
        startActivity(intent);

    }
    public void changePageCooldown(View view) {

        Intent intent = new Intent(WorkoutCategory.this, WorkoutCategoryDetial.class);
        intent.putExtra("category", "收操");
        startActivity(intent);

    }
    public void changePageCourse(View view) {

        Intent intent = new Intent(WorkoutCategory.this, WorkoutCategoryDetial.class);
        intent.putExtra("category", "套餐");
        startActivity(intent);

    }

    public void backSetup() {

        textback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });


        //textback.setOnClickListener(new View.OnClickListener() {
            //@Override
            //public void onClick(View view) {
                //finish();
            //}
        //});
    }

    public void changePageSearch() {
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WorkoutCategory.this, WorkoutSearchActivity.class);
                startActivity(intent);
            }
        });
    }

    public void runTimer() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d("lxhandler", "workoutCategory running");
                    if (startact_warmup) {
                        Intent intent = new Intent(WorkoutCategory.this, WorkoutCategoryDetial.class);
                        intent.putExtra("category", "暖身");
                        startActivity(intent);
                        startact_warmup = false;
                    } else if (startact_ox) {
                        Intent intent = new Intent(WorkoutCategory.this, WorkoutCategoryDetial.class);
                        intent.putExtra("category", "有氧");
                        startActivity(intent);
                        startact_ox = false;
                    } else if (startact_muscle) {
                        Intent intent = new Intent(WorkoutCategory.this, WorkoutCategoryDetial.class);
                        intent.putExtra("category", "肌力");
                        startActivity(intent);
                        startact_muscle = false;
                    } else if (startact_calmdown) {
                        Intent intent = new Intent(WorkoutCategory.this, WorkoutCategoryDetial.class);
                        intent.putExtra("category", "收操");
                        startActivity(intent);
                        startact_calmdown = false;
                    } else if (startact_package) {
                        Intent intent = new Intent(WorkoutCategory.this, WorkoutCategoryDetial.class);
                        intent.putExtra("category", "套餐");
                        startActivity(intent);
                        startact_package = false;
                    } else if (startact_back) {
                        Intent intent = new Intent(WorkoutCategory.this, MainActivity.class);
                        startActivity(intent);
                        startact_back = false;
                    } else if (startact_search) {
                        Intent intent = new Intent(WorkoutCategory.this, WorkoutSearchActivity.class);
                        startActivity(intent);
                        startact_search = false;
                    }
                handler.postDelayed(this, 50);
            }
        });
    }


}

