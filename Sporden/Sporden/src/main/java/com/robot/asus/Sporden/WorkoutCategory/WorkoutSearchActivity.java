package com.robot.asus.Sporden.WorkoutCategory;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.ListExercisesQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtilityOptions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.RobotUtil;
import com.asus.robotframework.API.SpeakConfig;
import com.robot.asus.Sporden.Adapter.WorkoutListAdapter;
import com.robot.asus.Sporden.GetLocale;
import com.robot.asus.Sporden.Model.ClientFactory;
import com.robot.asus.Sporden.Model.WorkoutListModel;
import com.robot.asus.Sporden.R;
import com.robot.asus.Sporden.SportList;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import javax.annotation.Nonnull;

import ru.github.igla.ferriswheel.FerrisWheelView;

public class WorkoutSearchActivity extends RobotActivity {
    private Button buttonForward, buttonBackward, buttonBack;
    private int page;
    private static int currentItem = 0;
    private RecyclerView listRecycler;
    //預先定義handler
    private final Handler handler = new Handler();
    //設定為橫向排列模式
    final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL);
    private ArrayList<String> totalNameList = new ArrayList<String>();
    private AutoCompleteTextView input_search_actv;
    private String search_name;
    private static ArrayList<String> nameList = new ArrayList<String>();
    private ArrayList<String> imgList = new ArrayList<String>();
    private int count = 0;
    private static String get_search_img;
    private static String get_search_id;
    private String TAG = "WorkoutSearchActivity";
    private static boolean isSecond = false;
    public final static String TAGd = "WorkoutSearchDialogue";
    public final static String DOMAIN = "2532C682CCD447C7AEAE1830C7DC2219";
    private static boolean startact_search = false;
    private static boolean startact_nextpg = false;
    private static boolean startact_previouspg = false;
    private final Handler handler1 = new Handler();
    private static int DDEIndex;
    private static boolean isDDE = false;
    private static boolean startact_back = false;
    private FerrisWheelView ferrisWheelView;
    private LinearLayout SLL_Recycler;
    private LinearLayout SLL_Wheel;
    private String get_search_voice;
    private ArrayList<String> voiceList = new ArrayList<String>();
    private static ArrayList<String> zhNameList = new ArrayList<String>();


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
            Log.d(TAGd, text);
        }

        @Override
        public void onResult(JSONObject jsonObject) {
            String text;
            text = "onResult: " + jsonObject.toString();
            Log.d(TAGd, text);

            String sIntentionID = RobotUtil.queryListenResultJson(jsonObject, "IntentionId");
            Log.d(TAGd, "Intention Id = " + sIntentionID);

            //如果是這個plan
            if(sIntentionID.equals("WorkoutName")) {
                //取這個plan的那個要得變數
                String sSluResultButton = RobotUtil.queryListenResultJson(jsonObject, "運動名稱", null);
                Log.d(TAGd, "Result Button = " + sSluResultButton);

                if (GetLocale.getLocale().equals("zh")) {
                    //要和concept的instance一樣(這是把變數拿來比對的條件式)
                    if (nameList != null || nameList.size() != 0) {
                        for (int j = 0; j < nameList.size(); j++) {
                            if (sSluResultButton.equals(nameList.get(j))) {
                                DDEIndex = j;
                                isDDE = true;
                                break;
                            }
                        }
                    }
                } else if (GetLocale.getLocale().equals("en")) {
                    //要和concept的instance一樣(這是把變數拿來比對的條件式)
                    if (zhNameList != null || zhNameList.size() != 0) {
                        for (int j = 0; j < zhNameList.size(); j++) {
                            if (sSluResultButton.equals(zhNameList.get(j))) {
                                DDEIndex = j;
                                isDDE = true;
                                break;
                            }
                        }
                    }
                }
            } else if (sIntentionID.equals("SearchAndDetial")) {
                //取這個plan的那個要得變數
                String sSluResultButton = RobotUtil.queryListenResultJson(jsonObject, "搜尋動作", null);
                Log.d(TAGd, "Result Button = " + sSluResultButton);
                //要和concept的instance一樣(這是把變數拿來比對的條件式)
                if(sSluResultButton.equals("搜尋")) {
                    startact_search = true;
                } else if (sSluResultButton.equals("上一頁")) {
                    startact_previouspg = true;
                } else if (sSluResultButton.equals("下一頁")) {
                    startact_nextpg = true;
                }
            } else if (sIntentionID.equals("BackToCategory")) {
                //取這個plan的那個要得變數
                String sSluResultButton = RobotUtil.queryListenResultJson(jsonObject, "返回", null);
                Log.d(TAGd, "Result Button = " + sSluResultButton);
                if (sSluResultButton.equals("返回")) {
                    startact_back = true;
                }
            }
        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };

    public WorkoutSearchActivity() {
        super(robotCallback, robotListenCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_search);
        //保持畫面不讓zenbo臉打斷
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //recyclerview
        listRecycler = (RecyclerView) findViewById(R.id.workout_search_list_recycler);

        //Layout setting
        SLL_Recycler = (LinearLayout)findViewById(R.id.SLLforRecycler);
        SLL_Wheel = (LinearLayout)findViewById(R.id.SLLforWheel);

        //ferrisWheelView = findViewById(R.id.ferrisWheelView);
        //ferrisWheelView.startAnimation();

        //最一開始，保留白色的recyclerview就好
        SLL_Wheel.setVisibility(View.GONE);
        SLL_Recycler.setVisibility(View.VISIBLE);

        //這邊可寫搜尋的DB搜尋方法
        //設置全部的資料
        queryExerciseTotal();

        //暫停0.5秒給查詢資料庫的時間
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Log.d("error", e.toString());
        }

        input_search_actv = findViewById(R.id.input_search_sport);


        // https://xnfood.com.tw/android-autocompletetextview/ (說明)
        //設置adapter給actv
        ArrayAdapter<String> adapterforactv = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_dropdown_item_1line,
                totalNameList);
        input_search_actv.setDropDownBackgroundResource(R.color.black);
        input_search_actv.setThreshold(1); //輸入幾個字時開始搜尋
        input_search_actv.setAdapter(adapterforactv); //設定 Adapter 給 input_search_actv
        input_search_actv.setCompletionHint(getString(R.string.related_ex)); //設定提示訊息
        input_search_actv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                search_name = charSequence.toString();
                Log.d("wtf name", search_name);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        //button of 搜尋
        final Button search_button = (Button) findViewById(R.id.btn_sport_search_name);

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*FragmentTransaction fr = getFragmentManager().beginTransaction();
                // 將剛剛的搜尋結果導到下面的activity
                fr.replace(R.id.sport_list_recycler, new SportList());
                fr.commit();*/
                /*Intent intent = new Intent(v.getContext(), SportList.class);
                startActivity(intent);*/
                nameList.clear();
                imgList.clear();
                voiceList.clear();
                /**按搜尋時把搜尋的東西傳到reclerview顯示(抓裡面的字去搜尋，再把搜尋結果傳給reclerview的data)**/
                //判斷是否有輸入關鍵字
                if (search_name != null) {
                    queryExerciseSearch();
                    //按下搜尋鈕時顯示摩天輪，把recyclerview隱藏
                    SLL_Recycler.setVisibility(View.GONE);
                    SLL_Wheel.setVisibility(View.VISIBLE);
                } else {
                    robotAPI.robot.speak(getString(R.string.plx_ent_wan_ex), new SpeakConfig().speed(60));
                }

            }
        });

        /*******************************************************************************************************************
         * 要注意這邊，這邊則為activity對recyclerview用法
         ***********************************************************************************************************************/

        //螢幕HOME鍵隱藏
        controlBarSetting();

        buttonBackward = (Button) findViewById(R.id.backward);
        buttonForward = (Button) findViewById(R.id.forward);
        buttonBack = (Button) findViewById(R.id.buttonback);

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WorkoutSearchActivity.this, WorkoutCategory.class);
                startActivity(intent);

            }
        });

        //上一頁
        buttonBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //page當index，從0開始算，上一頁不受奇偶數影響，因為Page只確認有沒有東西
                page = nameList.size() - 1;
                Log.d("pageP", page + "");
                //如果長度正的並且目前的item>=4(大於第一頁)，則向前4個item
                if (page >= 0 && currentItem > 4) {
                    //因為格式問題，所以要-6才是往前4個item之全新一頁
                    MoveToPosition(layoutManager,currentItem-=6);
                } else if (page >= 0 && currentItem == 4) {
                    MoveToPosition(layoutManager, currentItem-=4);
                } else if (page >= 0 && currentItem == 2) {
                    MoveToPosition(layoutManager, currentItem-=2);
                    Toast.makeText(getApplicationContext(), getString(R.string.already_not_forward), Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("pageP", "不能上一頁");
                    Toast.makeText(getApplicationContext(), getString(R.string.already_not_forward), Toast.LENGTH_SHORT).show();
                }


            }
        });

        //下一頁
        buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //page當index，不懂的話畫圖就懂了
                if(!nameList.isEmpty() && (nameList.size() % 2 == 0)) {
                    //偶數的話之current指標，因為從0開始算，所以要扣兩個
                    page = nameList.size() - 2;
                } else if (!nameList.isEmpty() && (nameList.size() % 2 == 1)) {
                    //奇數的話，current的指標
                    page = nameList.size() - 1;
                }
                Log.d("pageP333",Integer.toString(page));
                //如果page正的且現在item+4不會大於page
                if (page >= 0 && page >= (currentItem + 4) ) {
                    //跳到的那個位置在最左邊，所以加二
                    MoveToPosition(layoutManager,currentItem+=2);
                } else if (page >= 0 && page == (currentItem+2)) {
                    MoveToPosition(layoutManager,currentItem+=2);
                } else if (page >= 0 && page == currentItem) {
                    MoveToPosition(layoutManager,currentItem);
                    Toast.makeText(getApplicationContext(), getString(R.string.already_not_backward), Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("pageP", "底");
                    Toast.makeText(getApplicationContext(), getString(R.string.already_not_backward), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //此為給一執行緒來讓背景執行目前是到哪個item
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d("lxhandler","WorkoutSearchActivityForTracingItems");
                /**目前是找後面的那個item是哪一個，可考慮這邊的問題**/
                currentItem = layoutManager.findLastVisibleItemPositions(null)[0];
                Log.d("pageP2", Integer.toString(currentItem));
                handler.postDelayed(this, 500);
            }
        });

        //去抓語音的結果
        runTimer();
    }

    //移到哪個item
    public static void MoveToPosition(StaggeredGridLayoutManager manager, int n) {
        manager.scrollToPositionWithOffset(n, 0);
    }

    //重開啟此activity時恢復追蹤Item
    @Override
    protected void onRestart(){
        super.onRestart();

        handler.post(new Runnable() {
            @Override
            public void run() {
                currentItem = layoutManager.findLastVisibleItemPositions(null)[0];
                Log.d("pageP2", Integer.toString(currentItem));
                handler.postDelayed(this, 500);
            }
        });

        runTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // close faical
        robotAPI.robot.setExpression(RobotFace.HIDEFACE);

        // jump dialog domain
        //robotAPI.robot.jumpToPlan(DOMAIN, "WorkoutSearchActivityBtn");

        // listen user utterance
        //robotAPI.robot.speakAndListen("想要做哪一種運動呢?", new SpeakConfig().timeout(20));
    }

    //停止activity時停止追蹤
    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
        handler1.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onPause() {
        super.onPause();

        robotAPI.robot.stopSpeakAndListen();
    }

    public void controlBarSetting() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    private void queryExerciseTotal() {
        // Get the client instance
        AWSAppSyncClient awsAppSyncClient = ClientFactory.getInstance(getApplicationContext());

        awsAppSyncClient.query(ListExercisesQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST) /**小心這邊出問題**/
                .enqueue(exerciseCallback);
    }
    private GraphQLCall.Callback<ListExercisesQuery.Data> exerciseCallback = new GraphQLCall.Callback<ListExercisesQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListExercisesQuery.Data> response) {
            //用迴圈顯示每一個item，並逐一比對是否類別一樣，把這個類別的全抓出來放進去陣列
            if (GetLocale.getLocale().equals("zh")) {
                for (int i = 0; i < response.data().listExercises().items().size(); i++) {
                    //拿運動名稱、圖片位址和id
                    String get_name = response.data().listExercises().items().get(i).name();
                    totalNameList.add(get_name);
                }
                Log.d("wtf", totalNameList.toString());
            } else if (GetLocale.getLocale().equals("en")) {
                for (int i = 0; i < response.data().listExercises().items().size(); i++) {
                    //拿運動名稱、圖片位址和id
                    String get_name = response.data().listExercises().items().get(i).enName();
                    totalNameList.add(get_name);
                }
                Log.d("wtf", totalNameList.toString());
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
        }
    };

    private void queryExerciseSearch() {
        // Get the client instance
        AWSAppSyncClient awsAppSyncClient = ClientFactory.getInstance(getApplicationContext());

        awsAppSyncClient.query(ListExercisesQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST) /**小心這邊出問題**/
                .enqueue(exerciseSearchCallback);
    }
    private GraphQLCall.Callback<ListExercisesQuery.Data> exerciseSearchCallback = new GraphQLCall.Callback<ListExercisesQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListExercisesQuery.Data> response) {
            //用迴圈顯示每一個item，並逐一比對是否類別一樣，把這個類別的全抓出來放進去陣列
            if(GetLocale.getLocale().equals("zh")) {
                for (int i = 0; i < response.data().listExercises().items().size(); i++) {
                    if (response.data().listExercises().items().get(i).name().contains(search_name)) {
                        String get_search_name = response.data().listExercises().items().get(i).name();
                        get_search_img = response.data().listExercises().items().get(i).imgUri();
                        get_search_id = response.data().listExercises().items().get(i).id();
                        get_search_voice = response.data().listExercises().items().get(i).voiceInfo();

                        nameList.add(get_search_name);
                        imgList.add(get_search_id);
                        voiceList.add(get_search_voice);

                        downloadWithTransferUtility();
                    }
                }
            } else if (GetLocale.getLocale().equals("en")) {
                for (int i = 0; i < response.data().listExercises().items().size(); i++) {
                    if (response.data().listExercises().items().get(i).enName().contains(search_name)) {
                        String get_search_name = response.data().listExercises().items().get(i).enName();
                        get_search_img = response.data().listExercises().items().get(i).imgUri();
                        get_search_id = response.data().listExercises().items().get(i).id();
                        get_search_voice = response.data().listExercises().items().get(i).enVoiceInfo();
                        String get_search_zhName = response.data().listExercises().items().get(i).name();

                        nameList.add(get_search_name);
                        imgList.add(get_search_id);
                        voiceList.add(get_search_voice);
                        zhNameList.add(get_search_zhName);

                        downloadWithTransferUtility();
                    }
                }
            }
            Log.d("get_listttt", nameList.toString());
            Log.d("get_listttt", imgList.toString());

            // 宣告一個新陣列，把namelist這個listarray的name存在新陣列
            final String[] workoutNames = new String[nameList.size()];
            for (int i = 0; i < workoutNames.length; i++) {
                workoutNames[i] = nameList.get(i);
                Log.d("get_listtttarray", workoutNames[i]);
            }

            // 宣告一個新陣列，把imglist這個listarray的img id存在新陣列
            final String[] workoutImgs = new String[imgList.size()];
            for (int i = 0; i < workoutImgs.length; i++) {
                workoutImgs[i] = imgList.get(i);
            }

            // 宣告一個新陣列，把voicelist這個listarray的voice id存在新陣列
            final String[] workoutVoice = new String[voiceList.size()];
            for (int i = 0; i < workoutVoice.length; i++) {
                workoutVoice[i] = voiceList.get(i);
            }

            Log.d(TAG, "count: " + count);
            Log.d(TAG, "size" + nameList.size());

            //此為給一執行緒來讓背景執行，並給main thread來執行，跟上面的handler分開，分配在不同thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //再創一個handler，用來抓是否圖片都下載完了
                    final Handler handler = new Handler();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            handler.postDelayed(this, 100);
                            Log.d("lxhandler", "WorkoutSearchActivityForTracingS3Download");
                            //如果計數器已與陣列一樣大，代表全部都下載完了
                            Log.d(TAG, Boolean.toString(isSecond));
                            final WorkoutListAdapter adapter = new WorkoutListAdapter(workoutNames, workoutImgs);
                            if(count == nameList.size() && !isSecond) {
                                Log.d(TAG, "count: " + count);
                                //計數器歸零
                                count = 0;

                                //搜尋完成顯示結果，把摩天輪隱藏
                                SLL_Wheel.setVisibility(View.GONE);
                                SLL_Recycler.setVisibility(View.VISIBLE);

                                //將2陣列配給adapter
                                listRecycler.setAdapter(adapter);
                                //設定為橫向排列模式
                                listRecycler.setLayoutManager(layoutManager);

                                adapter.setListener(new WorkoutListAdapter.Listener() {
                                    public void onClick(int position) {
                                        Intent intent = new Intent(getApplicationContext(), WorkoutGameDetail.class);
                                        //按第幾個運動就傳那個位置的運動名稱到運動介紹
                                        intent.putExtra("sport_name", workoutNames[position]);
                                        intent.putExtra("sport_id", workoutImgs[position]);
                                        intent.putExtra("sport_voice", workoutVoice[position]);
                                        startActivity(intent);
                                    }
                                });
                                //確定全部都下載完後就把這個handler關掉
                                handler.removeCallbacksAndMessages(null);
                                isSecond = true;
                                if (GetLocale.getLocale().equals("zh")) {
                                    robotAPI.robot.speakAndListen("想要做哪一個運動呢?", new SpeakConfig().speed(150));
                                } else if (GetLocale.getLocale().equals("en")) {
                                    robotAPI.robot.speakAndListen("Which exercise do you want to do?", new SpeakConfig().speed(150));
                                }
                            } else if (count == nameList.size() && isSecond){
                                Log.d(TAG, "count: " + count);
                                Log.d(TAG, "issecond: " + Boolean.toString(isSecond));
                                //計數器歸零
                                count = 0;

                                //搜尋完成顯示結果，把摩天輪隱藏
                                SLL_Wheel.setVisibility(View.GONE);
                                SLL_Recycler.setVisibility(View.VISIBLE);

                                //將2陣列配給adapter
                                //final WorkoutListAdapter adapter = new WorkoutListAdapter(workoutNames, workoutImgs);
                                listRecycler.setAdapter(null);
                                listRecycler.setLayoutManager(null);
                                listRecycler.setAdapter(adapter);
                                //設定為橫向排列模式
                                listRecycler.setLayoutManager(layoutManager);
                                adapter.notifyDataSetChanged();

                                adapter.setListener(new WorkoutListAdapter.Listener() {
                                    public void onClick(int position) {
                                        Intent intent = new Intent(getApplicationContext(), WorkoutGameDetail.class);
                                        //按第幾個運動就傳那個位置的運動名稱到運動介紹
                                        intent.putExtra("sport_name", workoutNames[position]);
                                        intent.putExtra("sport_id", workoutImgs[position]);
                                        intent.putExtra("sport_voice", workoutVoice[position]);
                                        startActivity(intent);
                                    }
                                });
                                //確定全部都下載完後就把這個handler關掉
                                handler.removeCallbacksAndMessages(null);
                                isSecond = true;
                                if (GetLocale.getLocale().equals("zh")) {
                                    robotAPI.robot.speakAndListen("想要做哪一個運動呢?", new SpeakConfig().speed(150));
                                } else if (GetLocale.getLocale().equals("en")) {
                                    robotAPI.robot.speakAndListen("Which exercise do you want to do?", new SpeakConfig().speed(150));
                                }
                            }
                        }
                    });
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
        }
    };

    private void downloadWithTransferUtility() {
        /**S3 設定**/
        TransferUtilityOptions options = new TransferUtilityOptions();
        options.setTransferThreadPoolSize(8);
        options.setTransferServiceCheckTimeInterval(500);
        /**S3 設定**/

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                        .transferUtilityOptions(options)
                        .defaultBucket("spordena15102b183ff4128b4ef6d0604708374-sporden")
                        .build();

        TransferObserver downloadObserver =
                transferUtility.download(
                        get_search_img,
                        new File("/storage/emulated/0/Pictures/temp/" + get_search_id + ".jpg"));

        Log.d(TAG, "download key is: " + get_search_img);

        // Attach a listener to the observer to get state update and progress notifications
        downloadObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                    Log.d(TAG, "s3 download completed");
                    count+=1;
                    //每下載好一個就加入計數器
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float)bytesCurrent/(float)bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d(TAG, "   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
                Log.d(TAG, "download failed" + ex.toString());
            }

        });

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.
        if (TransferState.COMPLETED == downloadObserver.getState()) {
            // Handle a completed upload.
            Log.d(TAG, "complete");
        }

        Log.d("Your Activity", "Bytes Transferred: " + downloadObserver.getBytesTransferred());
        Log.d("Your Activity", "Bytes Total: " + downloadObserver.getBytesTotal());
    }

    public void runTimer() {
        //final Handler handler1 = new Handler();
        handler1.post(new Runnable() {
            @Override
            public void run() {
                Log.d("lxhandler","WorkoutSearchActivityDDE");
                if (isDDE) {
                    Intent intent = new Intent(getApplicationContext(), WorkoutGameDetail.class);
                    intent.putExtra("sport_name", nameList.get(DDEIndex));
                    intent.putExtra("sport_id", imgList.get(DDEIndex));
                    intent.putExtra("sport_voice", voiceList.get(DDEIndex));
                    startActivity(intent);
                    isDDE = false;
                } else if (startact_search) {
                    nameList.clear();
                    imgList.clear();
                    voiceList.clear();
                    /**按搜尋時把搜尋的東西傳到reclerview顯示(抓裡面的字去搜尋，再把搜尋結果傳給reclerview的data)**/
                    //判斷是否有輸入關鍵字
                    if (search_name != null) {
                        queryExerciseSearch();
                        //按下搜尋鈕時顯示摩天輪，把recyclerview隱藏
                        SLL_Recycler.setVisibility(View.GONE);
                        SLL_Wheel.setVisibility(View.VISIBLE);
                    } else {
                        robotAPI.robot.speak("請輸入您想要搜尋的運動", new SpeakConfig().speed(60));
                    }
                    startact_search = false;
                } else if (startact_previouspg) {
                    //page當index，從0開始算，上一頁不受奇偶數影響，因為Page只確認有沒有東西
                    page = nameList.size() - 1;
                    Log.d("pageP", page + "");
                    //如果長度正的並且目前的item>=4(大於第一頁)，則向前4個item
                    if (page >= 0 && currentItem > 4) {
                        //因為格式問題，所以要-6才是往前4個item之全新一頁
                        MoveToPosition(layoutManager,currentItem-=6);
                    } else if (page >= 0 && currentItem == 4) {
                        MoveToPosition(layoutManager, currentItem-=4);
                    } else if (page >= 0 && currentItem == 2) {
                        MoveToPosition(layoutManager, currentItem-=2);
                        Toast.makeText(getApplicationContext(), getString(R.string.already_not_forward), Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("pageP", "不能上一頁");
                        Toast.makeText(getApplicationContext(), getString(R.string.already_not_forward), Toast.LENGTH_SHORT).show();
                    }
                    // listen user utterance
                    robotAPI.robot.speakAndListen(getString(R.string.which_u_want), new SpeakConfig().timeout(20));
                    startact_previouspg = false;
                    //清除此acitivity的DDE Plan(這樣連續兩次才不會出問題)
                    //robotAPI.robot.clearAppContext(DOMAIN);
                    // jump dialog domain
                    //robotAPI.robot.jumpToPlan(DOMAIN, "WorkoutSearchActivityBtn");
                } else if (startact_nextpg) {
                    //page當index，不懂的話畫圖就懂了
                    if(!nameList.isEmpty() && (nameList.size() % 2 == 0)) {
                        //偶數的話之current指標，因為從0開始算，所以要扣兩個
                        page = nameList.size() - 2;
                    } else if (!nameList.isEmpty() && (nameList.size() % 2 == 1)) {
                        //奇數的話，current的指標
                        page = nameList.size() - 1;
                    }
                    Log.d("pageP333",Integer.toString(page));
                    //如果page正的且現在item+4不會大於page
                    if (page >= 0 && page >= (currentItem + 4) ) {
                        //跳到的那個位置在最左邊，所以加二
                        MoveToPosition(layoutManager,currentItem+=2);
                    } else if (page >= 0 && page == (currentItem+2)) {
                        MoveToPosition(layoutManager,currentItem+=2);
                    } else if (page >= 0 && page == currentItem) {
                        MoveToPosition(layoutManager,currentItem);
                        Toast.makeText(getApplicationContext(), getString(R.string.already_not_backward), Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("pageP", "底");
                        Toast.makeText(getApplicationContext(),  getString(R.string.already_not_backward), Toast.LENGTH_SHORT).show();
                    }
                    // listen user utterance
                    robotAPI.robot.speakAndListen(getString(R.string.which_u_want), new SpeakConfig().timeout(20));
                    startact_nextpg = false;
                    //清除此acitivity的DDE Plan(這樣連續兩次才不會出問題)
                    //robotAPI.robot.clearAppContext(DOMAIN);
                    // jump dialog domain
                    //robotAPI.robot.jumpToPlan(DOMAIN, "WorkoutSearchActivityBtn");
                }  else if (startact_back) {
                    Intent intent = new Intent(getApplicationContext(), WorkoutCategory.class);
                    startActivity(intent);
                    startact_back = false;
                }
                handler1.postDelayed(this, 10);
            }
        });
    }
}
