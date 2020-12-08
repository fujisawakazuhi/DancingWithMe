package com.robot.asus.Sporden;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.CreateExerciseMutation;
import com.amazonaws.amplify.generated.graphql.ListExercisesQuery;
import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.amazonaws.amplify.generated.graphql.UpdateExerciseMutation;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.mobileconnectors.apigateway.ApiRequest;
import com.amazonaws.mobileconnectors.apigateway.ApiResponse;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.regions.Regions;
import com.amazonaws.util.IOUtils;
import com.amazonaws.util.StringUtils;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.asus.robotframework.API.RobotAPI;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotCommand;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.RobotUtil;
import com.asus.robotframework.API.SpeakConfig;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.robot.asus.Sporden.Model.ClientFactory;
import com.robot.asus.Sporden.WorkoutCategory.WorkoutCategory;
import com.robot.asus.Sporden.spodenIndex.SpodenIndexClient;
import com.robot.asus.Sporden.spordenREST.SpordenRESTClient;
import com.robot.asus.robotactivity.RobotActivity;
import com.shashank.sony.fancytoastlib.FancyToast;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import type.CreateExerciseInput;
import type.UpdateExerciseInput;

import static com.robot.asus.Sporden.EditMembership.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE;

public class MainActivity extends RobotActivity {

    private GoogleSignInClient mGoogleSignInClient;

    //private static boolean startact = false;
    private static boolean startact_teach = false;
    private static boolean startact_info = false;
    private static boolean startact_data = false;
    private static boolean startact_logout = false;
    private final Handler handler = new Handler();
    private static boolean noData = false;
    private String personEmail;
    private static boolean noIndexData = false;
    private static boolean startact_garden = false;
    private String personName;


    public final static String TAG = "SpordenDialogue";
    public final static String DOMAIN = "2532C682CCD447C7AEAE1830C7DC2219";

    // TODO Replace this with your client class name
    private SpordenRESTClient apiClient;
    private SpodenIndexClient apiClient2;

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
            Log.d(TAG, jsonObject.toString());
            String text;
            text = "onResult: " + jsonObject.toString();
            Log.d(TAG, text);

            String sIntentionID = RobotUtil.queryListenResultJson(jsonObject, "IntentionId");
            Log.d(TAG, "Intention Id = " + sIntentionID);

            //如果是這個plan
            if(sIntentionID.equals("EnterButton")) {
                //取這個plan的那個要得變數
                String sSluResultButton = RobotUtil.queryListenResultJson(jsonObject, "按鈕名稱", null);
                Log.d(TAG, "Result Button = " + sSluResultButton);

                if(sSluResultButton != null) {
                    //要和concept的instance一樣(這是把變數拿來比對的條件式)
                    if (sSluResultButton.equals("運動教學")) {
                        startact_teach = true;
                    } else if (sSluResultButton.equals("我的資訊")) {
                        startact_info = true;
                    } else if (sSluResultButton.equals("健康儀錶板")) {
                        startact_data = true;
                    } else if (sSluResultButton.equals("登出")) {
                        startact_logout = true;
                    } else if (sSluResultButton.equals("花園")) {
                        startact_garden = true;
                    }
                }
            }
        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };

    public MainActivity () {
        super(robotCallback, robotListenCallback);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //保持畫面不讓zenbo臉打斷
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("463158886631-op8tuv929cd5jcfivm9kr7fc24qgpfh5.apps.googleusercontent.com")
                .requestEmail()
                .requestProfile()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // [END build_client]

        //aws auth init
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

        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:04ceb567-f209-4104-80fd-b17f27110ab0", // Identity pool ID
                Regions.US_EAST_1 // Region
        );
        //給index用的權限，必須用credentialProvider，不然沒資料的人第一次進主頁會不跑
        apiClient2 = new ApiClientFactory()
                .credentialsProvider(credentialsProvider)
                .region("us-east-1")
                .build(SpodenIndexClient.class);

        //在首頁若無取得授權先取得，才可以進行相機和寫入的動作
        getPermission();


        /*//aws auth login drop-in 介面會倒過來
        AWSMobileClient.getInstance().showSignIn(
                this,
                SignInUIOptions.builder()
                        .nextActivity(MainActivity.class)
                        .logo(R.drawable.sport_logo)
                        .backgroundColor(R.color.colorPrimaryDark)
                        .canCancel(false)
                        .build(),
                new Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails result) {
                        Log.d("drop-in", "onResult: " + result.getUserState());
                        switch (result.getUserState()){
                            case SIGNED_IN:
                                Log.i("INIT", "logged in!");
                                break;
                            case SIGNED_OUT:
                                Log.i("drop-in", "onResult: User did not choose to sign-in");
                                break;
                            default:
                                AWSMobileClient.getInstance().signOut();
                                break;
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("drop-in", "onError: ", e);
                    }
                }
        );*/

        TextView garden = findViewById(R.id.garden);
        garden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gardenIntent = new Intent();
                gardenIntent.setAction("im.myGarden");
                startActivity(gardenIntent);
            }
        });

        TextView membership = findViewById(R.id.withmeapp);
        membership.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent membership = new Intent();
                membership.setClass(MainActivity.this, MembershipContainer.class);
                startActivity(membership);
            }
        });

        TextView button = findViewById(R.id.ex_teaching);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noData){
                    if (GetLocale.getLocale().equals("zh")) {
                        robotAPI.robot.speak("請填寫完整個人資料才能夠使用唷");
                        FancyToast.makeText(getApplicationContext(), "請填寫完整個人資料才能夠使用唷", FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                    } else if (GetLocale.getLocale().equals("en")) {
                        robotAPI.robot.speak("Please fill in complete profile to be able to use exercise teaching!");
                        FancyToast.makeText(getApplicationContext(), "Please fill in complete profile to be able to use exercise teaching!", FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                    }
                } else {
                    Intent intent = new Intent(v.getContext(), WorkoutCategory.class);
                    startActivity(intent);
                }
            }
        });

        /*TextView Gamebutton = findViewById(R.id.ex_game);
        Gamebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), WorkoutCategory.class);
                startActivity(intent);
            }
        });*/

        TextView my_board = findViewById(R.id.ex_dashboard);
        my_board.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (noIndexData) {
                    if (GetLocale.getLocale().equals("zh")) {
                        robotAPI.robot.speak("你還沒有運動過的紀錄唷");
                        FancyToast.makeText(getApplicationContext(), "你還沒有運動過的紀錄唷", FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                    } else if (GetLocale.getLocale().equals("en")) {
                        robotAPI.robot.speak("You haven't had a record of exercises yet.");
                        FancyToast.makeText(getApplicationContext(), "You haven't had a record of exercises yet.", FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                    }
                } else {
                    Intent my_board = new Intent();
                    my_board.setClass(MainActivity.this, Myboard.class);
                    startActivity(my_board);
                }
            }
        });

        //先寫invoke在連接裝置
        /*TextView connect = findViewById(R.id.connect);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //暫時放這邊測試api而已
                //doInvokeAPI2();
                //runExerciseMutation();
                //runExerciseUpdate();
            }
        });*/

        //runExerciseUpdate();

        //登出按紐
        //ImageButton logout_btn = findViewById(R.id.logout);
        TextView logout_btn = findViewById(R.id.logout);
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

        //account textview
        TextView acc_text = findViewById(R.id.account);
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (acct != null) {
            //取得google帳戶資訊
            personName = acct.getDisplayName();
            Uri personPhoto = acct.getPhotoUrl();

            //顯示目前登入google名字
            acc_text.setText(personName);
        }

        //account email
        TextView email_text = findViewById(R.id.email);
        if (acct != null) {
            personEmail = acct.getEmail();
            email_text.setText(personEmail);
        }

        //mutation Exercise test
        //runExerciseMutation();

        // Create the client (測試REST API)
        apiClient = new ApiClientFactory()
                .credentialsProvider(AWSMobileClient.getInstance())
                .region("us-east-1")
                .build(SpordenRESTClient.class);

        //runExerciseMutation();

        postAccToUnity();
    }

    /*public MainActivity() {
        super(robotCallback, robotListenCallback);
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        //robotAPI.robot.speak("歡迎來到sporden，跟著我一起運動吧");

        // close faical
        robotAPI.robot.setExpression(RobotFace.HIDEFACE);

        // jump dialog domain
        robotAPI.robot.jumpToPlan(DOMAIN, "EnterButton");

        //在頁面開啟時註冊Listen callback
        //robotAPI.robot.registerListenCallback(robotListenCallback);

        // listen user utterance
        //robotAPI.robot.speakAndListen("嗨!" + personName + "，請告訴我您要使用什麼功能呢?", new SpeakConfig().timeout(20));
        if(GetLocale.getLocale().equals("zh")) {
            robotAPI.robot.speak("lee後!" + personName + "，請告訴我您要使用什麼功能呢?");
        } else if (GetLocale.getLocale().equals("en")) {
            robotAPI.robot.speakAndListen("Hello!" + personName + ", please tell me which function would you like to use?", new SpeakConfig().timeout(20));
        }

        runTimer();

        //查詢此使用者個人資料是否齊全
        runQueryUserforEnsureHaveData();

        //查詢此使用者使否有做過運動
        ensureHaveIndex();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //stop listen user utterance
        robotAPI.robot.stopSpeakAndListen();

        //在停止頁面時停止註冊
        //robotAPI.robot.unregisterListenCallback();

        handler.removeCallbacksAndMessages(null);
    }


    @Override
    protected void onStop() {
        super.onStop();

        robotAPI.cancelCommand(RobotCommand.CANCEL);
    }

    //會員sign out回登入介面
    public void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(MainActivity.this, Login.class));
                    }
                });
    }

    //更新該id的資料
    public void runExerciseUpdate(){
        AWSAppSyncClient awsAppSyncClient = ClientFactory.getInstance(this.getApplicationContext());

        UpdateExerciseInput updateExerciseInput = UpdateExerciseInput.builder().
                id("dda7102e-f896-4e24-a1eb-c287bcad6a15").
                enName("knee press").
                enTextInfo("Pressing the knee is a slightly more difficult action. The right foot is straight and the right hand press your right knee, and the left hand is extended to right. Repeatedly on the left and right.").
                enVoiceInfo("Pressing the knee is a slightly more difficult action. The right foot is straight and the right hand press your right knee, and the left hand is extended to right. Repeatedly on the left and right.").
                build();
        Log.d("fuck",updateExerciseInput.id());

        awsAppSyncClient.mutate(UpdateExerciseMutation.builder().input(updateExerciseInput).build()).clone().enqueue(mutationUPCallback);
    }

    private GraphQLCall.Callback<UpdateExerciseMutation.Data> mutationUPCallback = new GraphQLCall.Callback<UpdateExerciseMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<UpdateExerciseMutation.Data> response) {
            Log.i("fuck", "fuckyou");
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error", "fuck" + e.toString());
        }
    };


    //AWS mutation method test
    public void runExerciseMutation(){
        // Get the client instance
        AWSAppSyncClient awsAppSyncClient = ClientFactory.getInstance(this.getApplicationContext());
        // Create the mutation request
        CreateExerciseInput createExerciseInput = CreateExerciseInput.builder().
                name("單腳平衡").
                textInfo("以單腳站立的方式，試著平衡自己的身體達到調節身體的平衡，鍛鍊腿部和雙腳的肌肉").
                videoUri("onefootbalance").
                imgUri("public/exercisePic/單腳平衡.png").
                category("肌力").
                voiceInfo("以單腳站立的方式，試著平衡自己的身體達到調節身體的平衡，鍛鍊腿部和雙腳的肌肉").
                expTime(3).
                script("[0,0]").
                method("WorkoutGamingOnlyWatch").
                enName("One foot balance").
                enTextInfo("Balance on one foot and try to keep balance. It will make you regulate body balance and strengthen your muscles of hips  and thighs.").
                enVoiceInfo(":Balance on one foot and try to keep balance. It will make you regulate body balance and strengthen your muscles of hips  and thighs.").
                build();

        awsAppSyncClient.mutate(CreateExerciseMutation.builder().input(createExerciseInput).build()).refetchQueries(ListExercisesQuery.builder().build())
                .enqueue(mutationCallback);

        addExerciseOffline(createExerciseInput);
    }

    private GraphQLCall.Callback<CreateExerciseMutation.Data> mutationCallback = new GraphQLCall.Callback<CreateExerciseMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateExerciseMutation.Data> response) {
            Log.i("Exercise_Results", "Added Exerise");
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error", e.toString());
        }
    };

    // 當離線時儲存資料到本地端
    private void addExerciseOffline(CreateExerciseInput input) {
        final CreateExerciseMutation.CreateExercise expected = new CreateExerciseMutation.CreateExercise("Exercise", UUID.randomUUID().toString(), input.name(), input.voiceInfo(), input.videoUri(), input.textInfo(), input.imgUri(), input.category(), input.expTime(), input.script(), input.watchDirection(), input.disease(), input.method(), input.enName(), input.enTextInfo(), input.enVoiceInfo());

        final AWSAppSyncClient awsAppSyncClient = ClientFactory.getInstance(this.getApplicationContext());
        final ListExercisesQuery listExercisesQuery = ListExercisesQuery.builder().build();

        awsAppSyncClient.query(listExercisesQuery)
                .responseFetcher(AppSyncResponseFetchers.CACHE_ONLY)
                .enqueue(new GraphQLCall.Callback<ListExercisesQuery.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<ListExercisesQuery.Data> response) {
                        List<ListExercisesQuery.Item> items = new ArrayList<>();
                        if (response.data() != null){
                            items.addAll(response.data().listExercises().items());
                        }

                        items.add(new ListExercisesQuery.Item(expected.__typename(),
                                expected.id(),
                                expected.name(),
                                expected.voiceInfo(),
                                expected.videoUri(),
                                expected.textInfo(),
                                expected.imgUri(),
                                expected.category(),
                                expected.expTime(),
                                expected.script(),
                                expected.watchDirection(),
                                expected.disease(),
                                expected.method(),
                                expected.enName(),
                                expected.enTextInfo(),
                                expected.enVoiceInfo()));

                        ListExercisesQuery.Data data = new ListExercisesQuery.Data(new ListExercisesQuery.ListExercises("ModelExerciseConnection", items, null));
                        awsAppSyncClient.getStore().write(listExercisesQuery, data).enqueue(null);
                        Log.d("Exercise offline", "Successfully wrote item to local store while being offline.");

                        //finishIfOffline();
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.e("Exercise offline", "Failed to update event query list.", e);
                    }
                });
    }

    // 如果是離線狀態的話則在存完資料到本地端後呼叫此方法結束activity回到主頁(回到哪可以自己改)
    private void finishIfOffline(){
        // Close the add activity when offline otherwise allow callback to close
        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            Log.d("Exercise offline", "App is offline. Returning to MainActivity .");
            finish();
        }
    }

    public static void gotoSportTeach(View view) {
        Intent start = new Intent(view.getContext(),SportSearchAndSort.class);
        view.getContext().startActivity(start);
    }

    public void runTimer() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (startact_teach) {
                    startActivity(new Intent(MainActivity.this, WorkoutCategory.class));
                    startact_teach = false;
                } else if (startact_info) {
                    startActivity(new Intent(MainActivity.this, MembershipContainer.class));
                    startact_info = false;
                } else if (startact_data) {
                    startActivity(new Intent(MainActivity.this, Myboard.class));
                    startact_data = false;
                } else if (startact_logout) {
                    signOut();
                    startact_logout = false;
                } else if (startact_garden) {
                    Intent gardenIntent = new Intent();
                    gardenIntent.setAction("im.myGarden");
                    startActivity(gardenIntent);
                    startact_garden = false;
                }
                Log.d("lxhandler", "MA");
                handler.postDelayed(this, 10);
            }
        });
    }

    //rest api invoke test
    private void doInvokeAPI() {
        // Create components of api request
        final String method = "POST";
        final String path = "/Health";

        //body放要post的資料(userId一定要有)
        final String body = "{\"userId\":\"true\", \"email\":\"SSSS@A.C.C\"}";
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

    public void getPermission() {
        //要求讀取和相機權限
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to read the contacts
            }

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.CAMERA)) {
                // Explain to the user why we need to read the contacts
            }

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

            // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
            // app-defined int constant that should be quite unique
            //selectImage();
            //isChecked = true;
            }

        //詢問開啟相機全縣
        /** 這邊有可能不會跑 要注意 **/
        /*if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.CAMERA)) {
                // Explain to the user why we need to read the contacts
            }

            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);

            // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
            // app-defined int constant that should be quite unique
            //selectImage();
            //isChecked = true;
        }*/
    }

    //rest api invoke test
    private void ensureHaveIndex() {
        noIndexData = false;
        // Create components of api request
        final String method = "GET";
        final String path = "/index/object/:user_email";

        //body放要post的資料(userId一定要有)
        //final String body = "{\"user_email\":\"test2@test.com\", \"done_id\":\"121\"}";
        final String body = "";
        final byte[] content = body.getBytes(StringUtils.UTF8);

        final Map parameters = new HashMap<>();
        //parameters.put("lang", "en_US");
        //這邊放get要搜尋的哪個PK欄位
        parameters.put("user_email", personEmail);

        final Map headers = new HashMap<>();

        // Use components to create the api request
        ApiRequest localRequest =
                new ApiRequest(apiClient2.getClass().getSimpleName())
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

                    final ApiResponse response = apiClient2.execute(request);

                    final InputStream responseContentStream = response.getContent();

                    if (responseContentStream != null) {
                        final String responseData = IOUtils.toString(responseContentStream);
                        Log.d(TAG, "Response : " + responseData);
                        //如果傳回空的
                        if (responseData.equals("{}")){
                            //代表這個使用者index沒資料
                            noIndexData = true;
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

    public void runQueryUserforEnsureHaveData(){
        noData = false;
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
                if (personEmail.equals(response.data().listUsers().items().get(i).email())){
                    if (response.data().listUsers().items().get(i).age() == null || response.data().listUsers().items().get(i).height() == null || response.data().listUsers().items().get(i).weight() == null) {
                        noData = true;
                    }
                    break;
                }
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
        }
    };

    //rest api invoke test
    private void postAccToUnity() {
        // Create components of api request
        final String method = "POST";
        final String path = "/index";

        //body放要post的資料(userId一定要有)
        //final String body = "{\"user_email\":\"test2@test.com\", \"done_id\":\"121\"}";
        final String body = "{\"user_email\":\"accountForUnity0000006Spoden\", \"done_list\":\"" + personEmail + "\"}";
        final byte[] content = body.getBytes(StringUtils.UTF8);

        final Map parameters = new HashMap<>();
        //parameters.put("lang", "en_US");
        //這邊放get要搜尋的哪個PK欄位
        //parameters.put("user_email", personEmail);

        final Map headers = new HashMap<>();

        // Use components to create the api request
        ApiRequest localRequest =
                new ApiRequest(apiClient2.getClass().getSimpleName())
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

                    final ApiResponse response = apiClient2.execute(request);

                    final InputStream responseContentStream = response.getContent();

                    if (responseContentStream != null) {
                        final String responseData = IOUtils.toString(responseContentStream);
                        Log.d(TAG, "unity post Response : " + responseData);
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
