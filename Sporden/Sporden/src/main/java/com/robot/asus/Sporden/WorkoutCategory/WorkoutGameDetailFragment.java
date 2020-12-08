package com.robot.asus.Sporden.WorkoutCategory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
//import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.amazonaws.amplify.generated.graphql.ListExercisesQuery;
import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.mobileconnectors.apigateway.ApiRequest;
import com.amazonaws.mobileconnectors.apigateway.ApiResponse;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.util.IOUtils;
import com.amazonaws.util.StringUtils;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.robot.asus.Sporden.Model.ClientFactory;
import com.robot.asus.Sporden.R;
import com.robot.asus.Sporden.WorkingGaming.WorkoutGaming;
import com.robot.asus.Sporden.spordenREST.SpordenRESTClient;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;
import static com.robot.asus.robotactivity.RobotActivity.robotAPI;


/**
 * A simple {@link Fragment} subclass.
 */
public class WorkoutGameDetailFragment extends Fragment {
    private long workoutId;
    private VideoView videoView;
    private TextView textback;
    private String intent_name;
    private String intent_id;
    private String get_intro;
    private TextView description;;
    private String TAG = "WorkoutGameDetailFragment";
    private SpordenRESTClient apiClient;
    private String email;
    private Boolean stateChecked;
    private double user_height;
    private double user_weight;
    private int user_age;
    private String user_gender;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            workoutId = savedInstanceState.getLong("workoutId");
        }

        Intent intent = getActivity().getIntent();
        intent_name = intent.getStringExtra("sport_name");
        intent_id = intent.getStringExtra("sport_id");
        Log.d("intennnntfra", intent_name + " and " + intent_id );

        // Create the client (測試REST API)
        apiClient = new ApiClientFactory()
                .credentialsProvider(AWSMobileClient.getInstance())
                .region("us-east-1")
                .build(SpordenRESTClient.class);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());
        if (acct != null) {
            //取得google帳戶資訊
            email = acct.getEmail();
        }

        //尋找這個運動的資訊
        queryExercise();
        //尋找這個使用者的資訊
        queryUserImfor();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_workout_game_detail, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        if (view != null) {

            videoPlayer();

            backSetup();

            TextView title = (TextView) view.findViewById(R.id.textTitle);
            //Workout workout = Workout.workouts[0];
            title.setText(intent_name);
            description = (TextView) view.findViewById(R.id.textDescription);
            //Log.d("intennnntfra2", get_intro);


            TextView button = (TextView) view.findViewById(R.id.startworkoutgame);

            //開始運動按鈕
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //get是否開啟手表APP，並做判斷接著要做什麼
                    /**  記得打開!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! **/
                    doCheckAppState();
                    //doPostStartAndEmail();
                    //Intent intent = new Intent(getActivity(), WorkoutGaming.class);
                    //startActivity(intent);
                    /** 記得把上面三行刪掉!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! **/
                }
            });


        }
    }

    public void onStop() {
        super.onStop();

        videoView.stopPlayback();

    }

   /*
    }*/

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong("workoutId", workoutId);
    }

    public void setWorkout(long id) {
        this.workoutId = id;
    }

    public void videoPlayer() {

        View view = getView();
        videoView = (VideoView) view.findViewById(R.id.videoView);
        Uri uri = Uri.parse("android.resource://" + "com.robot.asus.Sporden" + "/raw/shakehand");
        videoView.setVideoURI(uri);
        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
    }

    public void backSetup() {

        View view = getView();

        textback = (TextView) view.findViewById(R.id.textback);
        textback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
    }

    private void queryExercise() {
        // Get the client instance
        AWSAppSyncClient awsAppSyncClient = ClientFactory.getInstance(getContext());

        awsAppSyncClient.query(ListExercisesQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK) /**小心這邊出問題**/
                .enqueue(exerciseSearchCallback);
    }
    private GraphQLCall.Callback<ListExercisesQuery.Data> exerciseSearchCallback = new GraphQLCall.Callback<ListExercisesQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListExercisesQuery.Data> response) {
            //用迴圈顯示每一個item，並逐一比對找這個運動是哪一個，找到就結束迴圈
            for (int i = 0; i < response.data().listExercises().items().size(); i++){
                if (response.data().listExercises().items().get(i).name().equals(intent_name)){
                    get_intro = response.data().listExercises().items().get(i).textInfo();
                    break;
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    description.setText(get_intro);
                }
            });

        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
        }
    };

    //取得目前登入user的資料
    private void queryUserImfor() {
        // Get the client instance
        AWSAppSyncClient awsAppSyncClient = ClientFactory.getInstance(getContext());

        awsAppSyncClient.query(ListUsersQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK) /**小心這邊出問題**/
                .enqueue(userSearchCallback);
    }
    private GraphQLCall.Callback<ListUsersQuery.Data> userSearchCallback = new GraphQLCall.Callback<ListUsersQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListUsersQuery.Data> response) {
            //用迴圈顯示每一個item，並逐一比對找這個運動是哪一個，找到就結束迴圈
            for (int i = 0; i < response.data().listUsers().items().size(); i++){
                //如果搜尋到的email跟現在登入的email一樣
                if (response.data().listUsers().items().get(i).email().equals(email)){
                    user_height = response.data().listUsers().items().get(i).height();
                    user_weight = response.data().listUsers().items().get(i).weight();
                    user_age = response.data().listUsers().items().get(i).age();
                    user_gender = response.data().listUsers().items().get(i).gender();
                    break;
                }
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
        }
    };



    //rest api invoking (把開始的布林值和目前使用者的email post上去)
    private void doPostStartAndEmail() {
        // Create components of api request
        final String method = "POST";
        final String path = "/Health";

        //body放要post的資料(userId一定要有)
        final String body = "{\"userId\":\"dansmocheckforstartsend\", \"email\":\""+ email + "\", \"height\":\""+ user_height + "\", \"weight\":\""+ user_weight + "\", \"age\":\"" + user_age + "\", \"gender\":\"" + user_gender + "\", \"sport_id\":\"" + intent_id + "\", \"isChecked\":\"true\"}";
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

    private void doCheckAppState() {
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
        parameters.put("userId","isStartstartednajsistar");

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
                        //一開始從網路接收通常為String型態,tmp為接收到的String,為避免串流內有其他資料只需抓取{}間的內容
                        String tmp2 = responseData.substring(responseData.indexOf("{"),responseData.lastIndexOf("}") + 1);
                        JSONObject json_read2 = new JSONObject(tmp2);
                        stateChecked = json_read2.getBoolean("isStarted");
                        Log.d(TAG, "Response for state : " + Boolean.toString(stateChecked));


                        //拿到手錶是否開啟後，利用判斷式先判斷有沒有取得相機權限，接著確認是不是有開啟手錶APP
                        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED) {
                            Log.d("changepage", "success");
                            //如果有開啟
                            if (stateChecked) {
                                doPostStartAndEmail();
                                Intent intent = new Intent(getActivity(), WorkoutGaming.class);
                                startActivity(intent);
                            } else {
                                //Toast.makeText(getContext(), "請開啟手錶APP", Toast.LENGTH_LONG).show();
                                //zenbo speak
                                robotAPI.robot.speak("請打開手錶APP才能開始運動唷!");
                            }
                        }
                        else {
                            Toast toast = Toast.makeText(getContext(),
                                    "請開啟相機權限", Toast.LENGTH_LONG);
                            toast.show();


                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.CAMERA},
                                    0);
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
}