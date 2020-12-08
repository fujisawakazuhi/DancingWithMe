package com.robot.asus.Sporden;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.CreateUserMutation;
import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.IdentityProvider;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignInResult;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotUtil;
import com.asus.robotframework.API.SpeakConfig;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.robot.asus.Sporden.Model.ClientFactory;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import javax.annotation.Nonnull;

import type.CreateUserInput;

import static com.robot.asus.Sporden.EditMembership.MY_PERMISSIONS_REQUEST_CAMERA;
import static com.robot.asus.Sporden.EditMembership.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE;

public class Login extends RobotActivity {
    private TextView sign_up;
    private Button sign_in;
    private EditText email;
    private EditText passwd;
    private Button google_login;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 0;
    private ImageView img;
    private String user_email;
    private String user_name;
    private static boolean userInDatabase = false;
    private String user_email_existing;

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
        }

        @Override
        public void onResult(JSONObject jsonObject) {

        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };

    public Login() {
        super(robotCallback, robotListenCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //保持畫面不讓zenbo臉打斷
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        //robotAPI.utility.followUser();

        //logo init
        //img=(ImageView)findViewById(R.id.logo);
        Drawable myDrawable = getResources().getDrawable(R.drawable.sport_logo, null);
        //img.setImageDrawable(myDrawable);

        /*
        //註冊
        sign_up =(TextView)findViewById(R.id.sign_up);
        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goSignUp();
            }
        });

        //email and passwd
        email = (EditText)findViewById(R.id.email_input);
        passwd = (EditText)findViewById(R.id.passwd_input);

        //login
        sign_in = (Button)findViewById(R.id.login);
        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goSignIn();
            }
        });
        */

        // google login
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                //google auto generate web appllication client id
                .requestIdToken("463158886631-op8tuv929cd5jcfivm9kr7fc24qgpfh5.apps.googleusercontent.com")
                .requestEmail()
                .requestProfile()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);



        //onCreate就直接把登入介面打開
        googleLogin();

        //如果有人按上一頁可以換按按鈕登入
        google_login = (Button)findViewById(R.id.google_login);
        google_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleLogin();
            }
        });
    }

    //語音測試
    @Override
    public void onResume(){
        super.onResume();
        robotAPI.utility.followUser();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        //如果上次沒登出，還抓地到帳戶的話
        /*GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            //抓他的帳號
            user_email_existing = account.getEmail();
            //查詢會員是否有欄位沒填，如果沒填就跳到編輯，有填就跳到主頁
            runQueryUserforExisting(); 放這句會有跑進去兩次的可能
        }*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
            //Log.d("ttttestAc", task.getResult().toString());
        }
    }

    private void googleLogin() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        Log.d("google sign-in", "intent =" + signInIntent.toString());

        //在oncreate判斷是否有登入，如果有登入過不講話，沒登入過(按過登出)就講話
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null) {
            //以前有登入過
            Log.d("hi","有登入過");
        } else {
            Log.d("hi","沒登入過");
            //zenbo 語音引導
            if (GetLocale.getLocale().equals("zh")) {
                robotAPI.robot.speak("請點選或新增您的google帳戶", new SpeakConfig().speed(90));
            } else if (GetLocale.getLocale().equals("en")) {
                robotAPI.robot.speak("Please choose or add your google account.", new SpeakConfig().speed(90));
            }
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d("google sign-in", "account =" + account.toString());
            // Signed in successfully, show authenticated UI.

            // 取得此登入帳號之資訊
            //GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplication());

            //取得google帳戶資訊
            user_email= account.getEmail();
            user_name = account.getDisplayName();
            // 如果有登入帳號
            if (account != null) {
                //尋找資料庫內的email有沒有跟user_email相同的
                Log.i("whyyy before", Boolean.toString(userInDatabase));
                Log.i("whyyy email", user_email);
                Log.i("whyyy name", user_name);

                runQueryUser();

            }

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Google sign in", "signInResult:failed code=" + e.getStatusCode());
            switch (e.getStatusCode()) {
                case GoogleSignInStatusCodes.CANCELED:
                    Log.w("Google sign in", "CANCELED");
                    break;
                case GoogleSignInStatusCodes.NETWORK_ERROR:
                    Log.w("Google sign in", "NETWORK_ERROR");
                    break;
                case GoogleSignInStatusCodes.SIGN_IN_CANCELLED:
                    Log.w("Google sign in", "SIGN_IN_CANCELLED");
                    break;
                case GoogleSignInStatusCodes.ERROR:
                    Log.w("Google sign in", "ERROR");
                    break;
            }
        }
    }

    //aws federated login test
    private void goawslogin() {
        AWSMobileClient.getInstance().initialize(this, new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                Log.i("INIT", userStateDetails.getUserState().toString());
                AWSMobileClient.getInstance().showSignIn(
                        Login.this,
                        SignInUIOptions.builder()
                                .nextActivity(MainActivity.class)
                                .build(),
                        new Callback<UserStateDetails>() {
                            @Override
                            public void onResult(UserStateDetails result) {
                                Log.d("google_login", "onResult: " + result.getUserState());
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("google_login", "onError: ", e);
                            }
                        }
                );
            }

            @Override
            public void onError(Exception e) {
                Log.e("INIT", "Error during initialization", e);
            }
        });

        AWSMobileClient.getInstance().federatedSignIn(IdentityProvider.GOOGLE.toString(), "GOOGLE_TOKEN_HERE", new Callback<UserStateDetails>() {
            @Override
            public void onResult(final UserStateDetails userStateDetails) {
                Log.d("Google-login, ", userStateDetails.toString());
            }


            @Override
            public void onError(Exception e) {
                Log.e("Google-login", "sign-in error", e);
            }
        });
    }



    private void goSignUp() {
        //redirect to sign up
    }

    private void goSignIn() {
        String username = email.getText().toString();
        String password = passwd.getText().toString();
        Log.d("Login", username);

        if(username == null || password == null) {
            Toast.makeText(Login.this, "請輸入完整的帳號密碼", Toast.LENGTH_LONG).show();
            Log.d("login", "輸入");
        } else {
            AWSMobileClient.getInstance().signIn(username, password, null, new Callback<SignInResult>() {
                @Override
                public void onResult(final SignInResult signInResult) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("login", "Sign-in callback state: " + signInResult.getSignInState());
                            switch (signInResult.getSignInState()) {
                                case DONE:
                                    Toast.makeText(Login.this, "已登入", Toast.LENGTH_SHORT).show();
                                    break;
                                case SMS_MFA:
                                    Toast.makeText(Login.this, "請用簡訊登入", Toast.LENGTH_SHORT).show();
                                    break;
                                case NEW_PASSWORD_REQUIRED:
                                    Toast.makeText(Login.this, "請使用新密碼登入", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(Login.this, "無法登入" + signInResult.getSignInState(), Toast.LENGTH_SHORT).show();
                                    Log.d("login" ,  signInResult.getSignInState().toString());
                                    break;
                            }
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    Log.e("login", "Sign-in error", e);
                }
            });
        }


    }

    //寫入會員登入資料
    //AWS mutation method test
    public void runUserMutation(){
        // Get the client instance
        AWSAppSyncClient awsAppSyncClient = ClientFactory.getInstance(this.getApplicationContext());
        // Create the mutation request
        CreateUserInput createUserInput = CreateUserInput.builder().
                email(user_email).
                name(user_name).
                build();

        awsAppSyncClient.mutate(CreateUserMutation.builder().input(createUserInput).build())/*.refetchQueries(ListUsersQuery.builder().build())*/
                .enqueue(mutationCallback);

        //暫時先不寫離線儲存
        //addExerciseOffline(createExerciseInput);
    }

    private GraphQLCall.Callback<CreateUserMutation.Data> mutationCallback = new GraphQLCall.Callback<CreateUserMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateUserMutation.Data> response) {
            Log.i("User_Results", "Added User");
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error", e.toString());
        }
    };

    //查詢會員資料庫裡面的帳號
    public void runQueryUser(){
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
                Log.i("whyyy e", response.data().listUsers().items().get(i).email());
                Log.i("whyyy user", user_email);
                if (user_email.equals(response.data().listUsers().items().get(i).email())){
                    //使用者在資料庫內
                    userInDatabase = true;
                    if (response.data().listUsers().items().get(i).age() == null || response.data().listUsers().items().get(i).height() == null || response.data().listUsers().items().get(i).weight() == null) {
                        startActivity(new Intent(Login.this, MembershipContainer.class));
                        if (GetLocale.getLocale().equals("zh")) {
                            robotAPI.robot.speak(user_name + "你好ㄚ，請先讓zenbo來認識你吧，請告訴我完整的資料!", new SpeakConfig().speed(150));
                        } else if (GetLocale.getLocale().equals("en")) {
                            robotAPI.robot.speak(user_name + "Hello, let me acquaint you first, please tell me complete information about you!", new SpeakConfig().speed(150));
                        }
                    } else {
                        startActivity(new Intent(Login.this, MainActivity.class));
                    }
                    Log.i("whyyy in query", Boolean.toString(userInDatabase));
                    break;
                }
            }

            // 如果使用者不在資料庫
            if (!userInDatabase){
                Log.i("whyyy after", Boolean.toString(userInDatabase));
                //寫入資料庫
                runUserMutation();
                //因為第一次用，先給他新增使用者資料
                startActivity(new Intent(Login.this, MembershipContainer.class));
                /**這個之後連zenbo測要看會不會出事**/
                if (GetLocale.getLocale().equals("zh")) {
                    robotAPI.robot.speak("歡迎" + user_name + "來到sporden，請先讓zenbo來認識你吧!", new SpeakConfig().speed(90));
                } else if (GetLocale.getLocale().equals("en")) {
                    robotAPI.robot.speak("Welcome " + user_name + "to sporden, let zenbo acquaint you first!", new SpeakConfig().speed(90));
                }
            }
            //回歸false
            userInDatabase = false;
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
        }
    };

    //查詢會員是否有欄位沒填，如果沒填就跳到編輯，有填就跳到主頁
    public void runQueryUserforExisting(){
        Log.i("whyyy", "有進來嗎");
        // Get the client instance
        AWSAppSyncClient awsAppSyncClient = ClientFactory.getInstance(this.getApplicationContext());

        //network_first才不會跑兩次，cache_and_network會有幾個就跑幾次(可能因為會先讀快取)
        awsAppSyncClient.query(ListUsersQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                .enqueue(usersCallback2);
    }

    private GraphQLCall.Callback<ListUsersQuery.Data> usersCallback2 = new GraphQLCall.Callback<ListUsersQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListUsersQuery.Data> response) {
            //用迴圈顯示每一個item的email，並逐一比對是否跟user_email一樣，如果一樣就是使用者在資料庫內，後面也不會有它了直接break
            for (int i = 0; i < response.data().listUsers().items().size(); i++){
                if (user_email_existing.equals(response.data().listUsers().items().get(i).email())){
                    if (response.data().listUsers().items().get(i).age() == null || response.data().listUsers().items().get(i).height() == null || response.data().listUsers().items().get(i).weight() == null) {
                        startActivity(new Intent(Login.this, MembershipContainer.class));
                        if (GetLocale.getLocale().equals("zh")) {
                            robotAPI.robot.speak(user_name + "你好ㄚ，請先讓zenbo來認識你吧，請告訴我完整的資料!", new SpeakConfig().speed(150));
                        } else if (GetLocale.getLocale().equals("en")) {
                            robotAPI.robot.speak(user_name + "Hello, let me acquaint you first, please tell me complete information about you!", new SpeakConfig().speed(150));
                        }
                    } else {
                        startActivity(new Intent(Login.this, MainActivity.class));
                        robotAPI.robot.stopSpeak();
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
}
