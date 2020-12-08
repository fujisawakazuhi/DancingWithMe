package com.robot.asus.Sporden.WorkoutCategory;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.ListExercisesQuery;
import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
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
import com.robot.asus.Sporden.Adapter.SportListAdapter;
import com.robot.asus.Sporden.Adapter.WorkoutListAdapter;
import com.robot.asus.Sporden.GetLocale;
import com.robot.asus.Sporden.MainActivity;
import com.robot.asus.Sporden.Model.ClientFactory;
import com.robot.asus.Sporden.Model.SportListModel;
import com.robot.asus.Sporden.Model.WorkoutListModel;
import com.robot.asus.Sporden.Myboard;
import com.robot.asus.Sporden.R;
import com.robot.asus.Sporden.SportList;
import com.robot.asus.Sporden.SportSearchAndSort;
import com.robot.asus.Sporden.SportTeachIntro;
import com.robot.asus.Sporden.WorkoutGame;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import ru.github.igla.ferriswheel.FerrisWheelView;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

public class WorkoutCategoryDetial extends RobotActivity {
    private Button buttonForward, buttonBackward, buttonBack;
    private int page;
    //public static List<Fragment> fdList;
    private static int currentItem = 0;
    private RecyclerView listRecycler;
    //預先定義handler
    private final Handler handler2 = new Handler();
    //設定為橫向排列模式
    final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL);
    private String intent_category;
    private static ArrayList<String> nameList = new ArrayList<String>();
    //用來與dde id配合，蒐集中文運動名稱清單
    private static ArrayList<String> zhnameList = new ArrayList<String>();
    private ArrayList<String> imgList = new ArrayList<String>();
    private ArrayList<String> voiceList = new ArrayList<String>();
    private static String get_img;
    private static String get_id;
    private static String get_voiceIntro;
    private int count = 0;
    private static final String TAG = "CategoryDetialDialogue";
    private static int DDEIndex;
    private static boolean isDDE = false;
    private static boolean startact_bgpg = false;
    private static boolean startact_nxpg = false;
    private final Handler handler3 = new Handler();
    public final static String DOMAIN = "2532C682CCD447C7AEAE1830C7DC2219";
    private static boolean startact_back = false;
    private FerrisWheelView ferrisWheelView;
    private LinearLayout LL_Recycler;
    private LinearLayout LL_Wheel;
    private String get_name;

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
            if(sIntentionID.equals("WorkoutName")) {
                //取這個plan的那個要得變數
                String sSluResultButton = RobotUtil.queryListenResultJson(jsonObject, "運動名稱", null);
                Log.d(TAG, "Result Button = " + sSluResultButton);

                //要和concept的instance一樣(這是把變數拿來比對的條件式)
                if (GetLocale.getLocale().equals("zh")) {
                    if (nameList != null || nameList.size() != 0) {
                        for (int j = 0; j < nameList.size(); j++) {
                            if (sSluResultButton.equals(nameList.get(j))) {
                                DDEIndex = j;
                                isDDE = true;
                                break;
                            }
                        }
                    }
                } else if (GetLocale.getLocale().equals("en")) { //若是英文，要找出中文名稱才可以跟DDE ID比對
                    if (zhnameList != null || zhnameList.size() != 0) {
                        for (int j = 0; j < zhnameList.size(); j++) {
                            if (sSluResultButton.equals(zhnameList.get(j))) {
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
                Log.d(TAG, "Result Button = " + sSluResultButton);
                if (sSluResultButton.equals("上一頁")) {
                    startact_bgpg = true;
                } else if (sSluResultButton.equals("下一頁")){
                    startact_nxpg = true;
                }
            } else if (sIntentionID.equals("BackToCategory")) {
                //取這個plan的那個要得變數
                String sSluResultButton = RobotUtil.queryListenResultJson(jsonObject, "返回", null);
                Log.d(TAG, "Result Button = " + sSluResultButton);

                //要和concept的instance一樣(這是把變數拿來比對的條件式)
                if(sSluResultButton.equals("返回") || sSluResultButton.equals("選單")) {
                    startact_back = true;
                }
            }
        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };

    public WorkoutCategoryDetial() {
        super(robotCallback, robotListenCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_category_detial);
        //保持畫面不讓zenbo臉打斷
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        listRecycler = (RecyclerView) findViewById(R.id.workout_list_recycler);

        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                Log.i("aws", "AWSMobileClient initialized. User State is " + userStateDetails.getUserState());
            }

            @Override
            public void onError(Exception e) {
                Log.e("aws", "Initialization error.", e);
            }
        });

        //取分類那邊的分類資料
        Intent intent = getIntent();
        intent_category = intent.getStringExtra("category");
        Log.d("intennnnt", intent_category);

        //清空namelist(init)
        nameList.clear();
        imgList.clear();
        voiceList.clear();

        //搜尋運動資料、下載照片
        queryExerciseDB();

        //螢幕HOME鍵隱藏
        controlBarSetting();

        buttonBackward = (Button) findViewById(R.id.button);
        buttonForward = (Button) findViewById(R.id.button2);
        buttonBack = (Button) findViewById(R.id.buttonback);

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
                    if (GetLocale.getLocale().equals("zh")) {
                        Toast.makeText(getApplicationContext(), "已無法往前囉!", Toast.LENGTH_SHORT).show();
                    } else if (GetLocale.getLocale().equals("en")) {
                        Toast.makeText(getApplicationContext(), "Cannot go forward!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("pageP", "不能上一頁");
                    if (GetLocale.getLocale().equals("zh")) {
                        Toast.makeText(getApplicationContext(), "已無法往前囉!", Toast.LENGTH_SHORT).show();
                    } else if (GetLocale.getLocale().equals("en")) {
                        Toast.makeText(getApplicationContext(), "Cannot go forward!", Toast.LENGTH_SHORT).show();
                    }
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
                    if (GetLocale.getLocale().equals("zh")) {
                        Toast.makeText(getApplicationContext(), "已無法往後囉!", Toast.LENGTH_SHORT).show();
                    } else if (GetLocale.getLocale().equals("en")) {
                        Toast.makeText(getApplicationContext(), "Cannot go backwards!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("pageP", "底");
                    if (GetLocale.getLocale().equals("zh")) {
                        Toast.makeText(getApplicationContext(), "已無法往後囉!", Toast.LENGTH_SHORT).show();
                    } else if (GetLocale.getLocale().equals("en")) {
                        Toast.makeText(getApplicationContext(), "Cannot go backwards!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //Layout setting
        LL_Recycler = (LinearLayout)findViewById(R.id.LLforRecyler);
        LL_Wheel = (LinearLayout)findViewById(R.id.LLforWheel);
        //一開始顯示摩天輪，把recyclerview隱藏
        LL_Recycler.setVisibility(View.GONE);
        LL_Wheel.setVisibility(View.VISIBLE);

        ferrisWheelView = findViewById(R.id.ferrisWheelView);
        ferrisWheelView.startAnimation();

        //back btn
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(getApplicationContext(), WorkoutCategory.class);
                //這邊到時候可以參照書來寫該類別所產生的清單(傳id過去來過濾清單)
                //intent.putExtra(SportList.Extra_ID, i);
                startActivity(intent);*/
                //直接用finish來結束這個activity
                finish();
            }
        });

        //此為給一執行緒來讓背景執行目前是到哪個item
        handler2.post(new Runnable() {
            @Override
            public void run() {
                Log.d("lxhandler", "WorkoutCategoryDetialForTracingItems");
                /**目前是找後面的那個item是哪一個，可考慮這邊的問題**/
                currentItem = layoutManager.findLastVisibleItemPositions(null)[0];
                Log.d("pageP2", Integer.toString(currentItem));
                handler2.postDelayed(this, 500);
            }
        });
    }

    //移到哪個item
    public static void MoveToPosition(StaggeredGridLayoutManager manager, int n) {
        manager.scrollToPositionWithOffset(n, 0);
        //manager.scrollToPosition(2);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // close faical
        robotAPI.robot.setExpression(RobotFace.HIDEFACE);

        // jump dialog domain
        //robotAPI.robot.jumpToPlan(DOMAIN, "WorkoutCaregoryDetial");
    }

    @Override
    protected void onPause() {
        super.onPause();

        //stop listen user utterance
        robotAPI.robot.stopSpeakAndListen();
        //robotAPI.robot.clearAppContext(DOMAIN);
    }

    //重開啟此activity時恢復追蹤Item
    @Override
    protected void onRestart(){
        super.onRestart();

        handler2.post(new Runnable() {
            @Override
            public void run() {
                currentItem = layoutManager.findLastVisibleItemPositions(null)[0];
                Log.d("pageP2", Integer.toString(currentItem));
                handler2.postDelayed(this, 500);
            }
        });

        // jump dialog domain
        //robotAPI.robot.jumpToPlan(DOMAIN, "WorkoutCaregoryDetial");

        runTimerforDDE();
    }

    //停止activity時停止追蹤
    @Override
    protected void onStop() {
        super.onStop();
        handler2.removeCallbacksAndMessages(null);
        //DDE handler
        handler3.removeCallbacksAndMessages(null);
        //robotAPI.robot.clearAppContext(DOMAIN);
    }

    protected void onDestroy() {
        super.onDestroy();
        //在activity結束後將adapter清空，來將圖片所占用的記憶體清空，避免out of memory
        listRecycler.setAdapter(null);
        ferrisWheelView = null;

        Log.d(TAG, "Destroy");
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

    private void queryExerciseDB() {
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
            for (int i = 0; i < response.data().listExercises().items().size(); i++){
                if (intent_category.equals(response.data().listExercises().items().get(i).category())){
                    //拿運動名稱、圖片位址和id
                    if (GetLocale.getLocale().equals("zh")) {
                        get_name = response.data().listExercises().items().get(i).name();
                        get_img = response.data().listExercises().items().get(i).imgUri();
                        get_id = response.data().listExercises().items().get(i).id();
                        get_voiceIntro = response.data().listExercises().items().get(i).voiceInfo();
                        Log.d("get_nameanana", get_name);

                        //將名稱和id加入兩陣列
                        nameList.add(get_name);
                        imgList.add(get_id);
                        voiceList.add(get_voiceIntro);

                        //將s3圖片下載下來
                        downloadWithTransferUtility();
                    } else if (GetLocale.getLocale().equals("en")) {
                        get_name = response.data().listExercises().items().get(i).enName();
                        String get_zhName = response.data().listExercises().items().get(i).name();
                        get_img = response.data().listExercises().items().get(i).imgUri();
                        get_id = response.data().listExercises().items().get(i).id();
                        get_voiceIntro = response.data().listExercises().items().get(i).enVoiceInfo();
                        Log.d("get_nameanana", get_name);

                        //將名稱和id加入兩陣列
                        nameList.add(get_name);
                        //在英文模式把中文名稱放進去list，來與dde配對
                        zhnameList.add(get_zhName);
                        imgList.add(get_id);
                        voiceList.add(get_voiceIntro);

                        //將s3圖片下載下來
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

            Log.d("WorkoutCategoryDetial", "count: " + count);
            Log.d("WorkoutCategoryDetial", "size" + nameList.size());

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
                            Log.d("lxhandler", "WorkoutCategoryDetialForTracingS3DownloadCount");
                            //如果計數器已與陣列一樣大，代表全部都下載完了
                            if(count == nameList.size()) {
                                Log.d("WorkoutCategoryDetial", "count: " + count);
                                //計數器歸零
                                count = 0;

                                //這邊下載好之後將摩天輪隱藏並顯示結果
                                LL_Wheel.setVisibility(View.GONE);
                                LL_Recycler.setVisibility(View.VISIBLE);

                                //將2陣列配給adapter
                                final WorkoutListAdapter adapter = new WorkoutListAdapter(workoutNames, workoutImgs);
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
                                    intent.putExtra("sport_category", intent_category);
                                    startActivity(intent);
                                    }
                                });
                                //確定全部都下載完後就把這個handler關掉
                                handler.removeCallbacksAndMessages(null);

                                // listen user utterance
                                //robotAPI.robot.speakAndListen("想要做哪一個運動呢?", new SpeakConfig().timeout(20));
                                if (GetLocale.getLocale().equals("zh")) {
                                    robotAPI.robot.speak("想要做哪一個運動呢?");
                                } else if (GetLocale.getLocale().equals("en")) {
                                    robotAPI.robot.speakAndListen("Which exercise do you want to do?", new SpeakConfig().timeout(20));
                                }
                                //確認已加入nameList，開始監聽是否有說到這個指令
                                runTimerforDDE();
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
                        get_img,
                        new File("/storage/emulated/0/Pictures/temp/" + get_id + ".jpg"));

        Log.d("WorkoutCategoryDetial", "download key is: " + get_img);

        // Attach a listener to the observer to get state update and progress notifications
        downloadObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                    Log.d("WorkoutCategoryDetial", "s3 download completed");
                    count+=1;
                    //每下載好一個就加入計數器
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float)bytesCurrent/(float)bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d("WorkoutCategoryDetial", "   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
                Log.d("WorkoutCategoryDetial", "download failed" + ex.toString());
            }

        });

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.
        if (TransferState.COMPLETED == downloadObserver.getState()) {
            // Handle a completed upload.
            Log.d("WorkoutCategoryDetial", "complete");
        }

        Log.d("Your Activity", "Bytes Transferred: " + downloadObserver.getBytesTransferred());
        Log.d("Your Activity", "Bytes Total: " + downloadObserver.getBytesTotal());
    }

    private void runTimerforDDE() {
        handler3.post(new Runnable() {
            @Override
            public void run() {
                Log.d("lxhandler", "WorkoutCategoryDetialDDE");
                if (isDDE) {
                    Intent intent = new Intent(getApplicationContext(), WorkoutGameDetail.class);
                    intent.putExtra("sport_name", nameList.get(DDEIndex));
                    intent.putExtra("sport_id", imgList.get(DDEIndex));
                    intent.putExtra("sport_voice", voiceList.get(DDEIndex));
                    intent.putExtra("sport_category", intent_category);
                    startActivity(intent);
                    isDDE = false;
                } else if (startact_bgpg) {
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
                        if (GetLocale.getLocale().equals("zh")) {
                            Toast.makeText(getApplicationContext(), "已無法往前囉!", Toast.LENGTH_SHORT).show();
                        } else if (GetLocale.getLocale().equals("en")) {
                            Toast.makeText(getApplicationContext(), "Cannot go forward!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("pageP", "不能上一頁");
                        if (GetLocale.getLocale().equals("zh")) {
                            Toast.makeText(getApplicationContext(), "已無法往前囉!", Toast.LENGTH_SHORT).show();
                        } else if (GetLocale.getLocale().equals("en")) {
                            Toast.makeText(getApplicationContext(), "Cannot go forward!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    // listen user utterance
                    if (GetLocale.getLocale().equals("zh")) {
                        robotAPI.robot.speakAndListen("想要做哪一個運動呢?", new SpeakConfig().timeout(20));
                    } else if (GetLocale.getLocale().equals("en")) {
                        robotAPI.robot.speakAndListen("Which exercise do you want to do?", new SpeakConfig().timeout(20));
                    }
                    startact_bgpg = false;
                    //清除此acitivity的DDE Plan(這樣連續兩次才不會出問題)
                    //robotAPI.robot.clearAppContext(DOMAIN);
                    // jump dialog domain
                    //robotAPI.robot.jumpToPlan(DOMAIN, "WorkoutCaregoryDetial");
                } else if (startact_nxpg) {
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
                        if (GetLocale.getLocale().equals("zh")) {
                            Toast.makeText(getApplicationContext(), "已無法往後囉!", Toast.LENGTH_SHORT).show();
                        } else if (GetLocale.getLocale().equals("en")) {
                            Toast.makeText(getApplicationContext(), "Cannot go backwards!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("pageP", "底");
                        if (GetLocale.getLocale().equals("zh")) {
                            Toast.makeText(getApplicationContext(), "已無法往後囉!", Toast.LENGTH_SHORT).show();
                        } else if (GetLocale.getLocale().equals("en")) {
                            Toast.makeText(getApplicationContext(), "Cannot go backwards!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    // listen user utterance
                    if (GetLocale.getLocale().equals("zh")) {
                        robotAPI.robot.speakAndListen("想要做哪一個運動呢?", new SpeakConfig().timeout(20));
                    } else if (GetLocale.getLocale().equals("en")) {
                        robotAPI.robot.speakAndListen("Which exercise do you want to do?", new SpeakConfig().timeout(20));
                    }
                    startact_nxpg = false;
                    //清除此acitivity的DDE Plan(這樣連續兩次才不會出問題)
                    //robotAPI.robot.clearAppContext(DOMAIN);
                    // jump dialog domain
                    //robotAPI.robot.jumpToPlan(DOMAIN, "WorkoutCaregoryDetial");
                } else if (startact_back) {
                    Intent intent = new Intent(WorkoutCategoryDetial.this, WorkoutCategory.class);
                    startActivity(intent);
                    startact_back = false;
                }
                handler3.postDelayed(this, 50);
            }
        });
    }
}
