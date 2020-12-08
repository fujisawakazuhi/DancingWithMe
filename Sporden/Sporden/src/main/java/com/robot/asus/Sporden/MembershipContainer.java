package com.robot.asus.Sporden;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.asus.robotframework.API.MotionControl;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotCommand;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.RobotUtil;
import com.asus.robotframework.API.SpeakConfig;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.robot.asus.Sporden.Model.MemberModel;
import com.robot.asus.Sporden.WorkingGaming.WorkoutGamingResult;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import static com.robot.asus.Sporden.WorkingGaming.WorkoutGaming.TYPE_CAPACITY_TOUCH;

public class MembershipContainer extends RobotActivity {
    private GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "MembershipDialogue";
    private static MembershipFragment membershipFragment = new MembershipFragment();
    private static MembershipFragment2 membershipFragment2 = new MembershipFragment2();
    private static EditMembership editMembership = new EditMembership();
    public final static String DOMAIN = "2532C682CCD447C7AEAE1830C7DC2219";
    private static boolean isNextPg = false;
    private static boolean isEdit = false;
    private static boolean isPreviousPg = false;
    private static boolean isCancel = false;
    private static boolean isChanged = false;
    private static boolean isCompleted = false;
    private static boolean isBacked = false;
    private final Handler handler = new Handler();
    private static String get_email;
    private static boolean isPress = false;
    private static int iCurrentSpeakSerial;

    private SensorManager mSensorManager;
    private Sensor mSensorCapacityTouch;

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
               robotAPI.utility.followUser();
            }
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
            if(sIntentionID.equals("Membership")) {
                //取這個plan的那個要得變數
                String sSluResultButton = RobotUtil.queryListenResultJson(jsonObject, "個資動作", null);
                Log.d(TAG, "Result Button = " + sSluResultButton);

                //要和concept的instance一樣(這是把變數拿來比對的條件式)
                if (sSluResultButton.equals("下一頁")) {
                    /*Bundle bundle = new Bundle();
                    bundle.putString("指令","nextPage");
                    membershipFragment.setArguments(bundle);*/
                    isNextPg = true;
                } else if (sSluResultButton.equals("編輯")) {
                    /*Bundle bundle = new Bundle();
                    bundle.putString("指令","edit");
                    membershipFragment.setArguments(bundle);*/
                    //membershipFragment.getArguments().putString("指令","edit");
                    isEdit = true;
                } else if (sSluResultButton.equals("上一頁")) {
                    isPreviousPg = true;
                } else if (sSluResultButton.equals("取消")) {
                    isCancel = true;
                } else if (sSluResultButton.equals("更換照片")) {
                    isChanged = true;
                } else if (sSluResultButton.equals("完成")) {
                    isCompleted = true;
                }
            } else if (sIntentionID.equals("BackToMain")) {
                //此為從main直接接過來的，也就是onCreate什麼都不講就直接返回
                //取這個plan的那個要得變數
                String sSluResultButton = RobotUtil.queryListenResultJson(jsonObject, "返回", null);
                Log.d(TAG, "Result Button = " + sSluResultButton);

                //要和concept的instance一樣(這是把變數拿來比對的條件式)
                if(sSluResultButton.equals("返回")) {
                    isBacked = true;
                }
            }
        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };

    public MembershipContainer() {
        super(robotCallback, robotListenCallback);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_membership);
        //保持畫面不讓zenbo臉打斷
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //打頭部
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorCapacityTouch = mSensorManager.getDefaultSensor(TYPE_CAPACITY_TOUCH);

        /*GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("463158886631-op8tuv929cd5jcfivm9kr7fc24qgpfh5.apps.googleusercontent.com")
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        TextView sign_out = findViewById(R.id.signout);
        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
                finish();
            }
        });*/

        getFragmentManager().beginTransaction().add(R.id.membership_container, membershipFragment).commitAllowingStateLoss();

        //回到首頁
        /*TextView backmain = (TextView)findViewById(R.id.backmain);
        backmain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MembershipContainer.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });*/




        /*MembershipFragment2 membershipFragment2 = new MembershipFragment2();
        getFragmentManager().beginTransaction().add(R.id.membership_container, membershipFragment2).commitAllowingStateLoss();*/

        /*if (test == -1) {
            Bundle bundle = new Bundle();
            bundle.putString("指令","nextPage");
            membershipFragment.setArguments(bundle);
        } else if (test == 1){
            Bundle bundle = new Bundle();
            bundle.putString("指令","edit");
            membershipFragment.setArguments(bundle);
            //membershipFragment.getArguments().putAll(bundle)/*.putString("指令","edit")*/;
        /*} else if (test == 0) {
            Bundle bundle = new Bundle();
            bundle.putString("指令", "previousPage");
            membershipFragment2.setArguments(bundle);
        }*/
        /*GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if(account != null) {
            get_email = account.getEmail();
            Log.d("myemaiill", get_email);
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        // close faical
        robotAPI.robot.setExpression(RobotFace.HIDEFACE);


        mSensorManager.registerListener(listenerCapacityTouch, mSensorCapacityTouch, SensorManager.SENSOR_DELAY_UI);

        // jump dialog domain
        // robotAPI.robot.jumpToPlan(DOMAIN, "Membership");

        /*int test = 1;
        if (test == 1) {
            MemberModel memberModel = new MemberModel();
            Log.d("testtttttkoko", Boolean.toString(memberModel.getCompleted()));
            memberModel.setCompleted(true);
        }*/
        runTimer();

        // listen user utterance
        //robotAPI.robot.speakAndListen("讓我來認識你吧!", new SpeakConfig().timeout(20));

        //runQueryUserforEnsureHaveData();
    }

    @Override
    protected void onPause() {
        super.onPause();

        handler.removeCallbacksAndMessages(null);
        robotAPI.robot.stopSpeakAndListen();

        mSensorManager.unregisterListener(listenerCapacityTouch);

    }

    @Override
    protected void onStop() {
        super.onStop();


        robotAPI.cancelCommand(RobotCommand.CANCEL);

        //runQueryUserforEnsureHaveData();
    }

    public void runTimer() {
        //final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d("testttt0","running");
                if (isNextPg) {
                    isNextPg = false;
                    getFragmentManager().beginTransaction().replace(R.id.membership_container, membershipFragment2).commitAllowingStateLoss();
                    // jump dialog domain
                    //robotAPI.robot.jumpToPlan(DOMAIN, "Membership");
                } else if (isEdit) {
                    isEdit = false;
                    getFragmentManager().beginTransaction().replace(R.id.membership_container, editMembership).commitAllowingStateLoss();
                    // jump dialog domain
                    //robotAPI.robot.jumpToPlan(DOMAIN, "Membership");
                } else if (isPreviousPg) {
                    isPreviousPg = false;
                    getFragmentManager().beginTransaction().replace(R.id.membership_container, membershipFragment).commitAllowingStateLoss();
                    // jump dialog domain
                    //robotAPI.robot.jumpToPlan(DOMAIN, "Membership");
                } else if (isCancel) {
                    isCancel = false;
                    getFragmentManager().beginTransaction().replace(R.id.membership_container, membershipFragment).commitAllowingStateLoss();
                    // jump dialog domain
                    //robotAPI.robot.jumpToPlan(DOMAIN, "Membership");
                } else if (isChanged) {
                    MemberModel memberModel = new MemberModel();
                    memberModel.setChanged(true);
                    isChanged = false;
                    // jump dialog domain
                    //robotAPI.robot.jumpToPlan(DOMAIN, "Membership");
                } else if (isCompleted) {
                    MemberModel memberModel = new MemberModel();
                    memberModel.setCompleted(true);
                    isCompleted = false;
                    // jump dialog domain
                    //robotAPI.robot.jumpToPlan(DOMAIN, "Membership");
                } else if (isBacked) {
                    Intent intent = new Intent(MembershipContainer.this, MainActivity.class);
                    startActivity(intent);
                    removeFragment();
                    isBacked = false;
                }

                handler.postDelayed(this, 50);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub

        if (keyCode == KeyEvent.KEYCODE_BACK) { // 攔截返回鍵
            Intent intent = new Intent(MembershipContainer.this, MainActivity.class);
            startActivity(intent);
            //this.finish();
            //startActivity(new Intent(MembershipContainer.this, MainActivity.class));
            //暫停1秒再關閉
            /*try {
                Thread.sleep(2000);
                finish();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }*/
            // 將所有fragment移除
            removeFragment();
            //System.exit(0);
            //}
        }
        return true;
    }

    private void removeFragment() {
        // 將所有fragment移除
        FragmentManager fragmentManager=getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.membership_container);
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.remove(fragment);
        fragmentTransaction.commit();
    }

    /*public void runQueryUserforEnsureHaveData(){
        Log.i("whyyy", "有進來嗎");
        // Get the client instance
        AWSAppSyncClient awsAppSyncClient = ClientFactory.getInstance(this.getApplicationContext());

        //network_first才不會跑兩次，cache_and_network會有幾個就跑幾次(可能因為會先讀快取)
        awsAppSyncClient.query(ListUsersQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                .enqueue(usersCallback);
    }

    private GraphQLCall.Callback<ListUsersQuery.Data> usersCallback = new GraphQLCall.Callback<ListUsersQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListUsersQuery.Data> response) {
            //用迴圈顯示每一個item的email，並逐一比對是否跟user_email一樣，如果一樣就是使用者在資料庫內，後面也不會有它了直接break
            for (int i = 0; i < response.data().listUsers().items().size(); i++){
                if (get_email.equals(response.data().listUsers().items().get(i).email())){
                    if (response.data().listUsers().items().get(i).age() == null || response.data().listUsers().items().get(i).height() == null || response.data().listUsers().items().get(i).weight() == null) {
                        //signOut();
                        isPress = true;
                    }
                    break;
                }
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
        }
    };*/

    //會員sign out回登入介面
    public void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(MembershipContainer.this, Login.class));
                    }
                });
    }


    //拍頭
    SensorEventListener listenerCapacityTouch = new SensorEventListener() {
        //Sensor

        @Override
        public void onSensorChanged(SensorEvent event) {

            Log.d("asdfg", event.values[0] + "");
            if (event.values[0] == 1) {
                robotAPI.utility.playEmotionalAction(RobotFace.HAPPY,2);
              iCurrentSpeakSerial = robotAPI.robot.speak("謝謝大家來看我！我超開心！掰掰！",new SpeakConfig().pitch(120));

            }


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

}
