package com.fjuim.watch;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.mobileconnectors.apigateway.ApiRequest;
import com.amazonaws.mobileconnectors.apigateway.ApiResponse;
import com.amazonaws.regions.Regions;
import com.amazonaws.util.IOUtils;
import com.amazonaws.util.StringUtils;
import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.ShakeDetector;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends WearableActivity implements SensorEventListener {

    private static final String TAG = "Watch MainActivity";

    //手錶辨識用
    private static TextView textShake, textOrientation;
    private int shakeTimes = 0, direction = 0, lastDirection = 0;
    private float SmallDirectionCheckThreshold = 3f, DirectionCheckThreshold = 6f;
    private Sensor mAccelerometer;
    private static boolean isShake, isUpdateShake;
    private static float[] accDataList = new float[3];
    //手錶辨識用

    //Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference docRef = db.collection("Watch").document("watchState");
    private Map<String, Object> watchState = new HashMap<>();
    //Firebase


    private TextView mTextView;
    private Button show_button;
    private Boolean isChecked;
    private Boolean endChecked;
    private String acc_email;
    private String acc_gender;
    private String exercise_id;
    private int acc_age;
    private double acc_height;
    private double acc_weight;
    private final Handler handler = new Handler();
    private final Handler handlerWatch = new Handler();
    private String userID;
    private ArrayList<String> arrayList = new ArrayList<>();
    private double calories;
    private Boolean isUploadNew = true;

    ArrayList<String> data_heart_rate = new ArrayList<>();
    ArrayList<String> data_time_that = new ArrayList<>();

    ArrayList<ArrayList<String>> watch_data = new ArrayList<>();

    private watchREST.WatchRESTClient apiClient;
    private WatchIndex.WatchIndexClient indexClient;

    String account = "";

    SensorManager mSensorManager;
    Sensor mHeartRateSensor;

    long time_start, time_stop;
    long time_now;
    double total_second = 0;

    private static final int MY_PERMISSIONS_REQUEST_BODY_SENSORS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //要求權限
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.BODY_SENSORS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.BODY_SENSORS},
                        MY_PERMISSIONS_REQUEST_BODY_SENSORS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

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


        // Create the client (測試REST API)
        apiClient = new ApiClientFactory()
                .credentialsProvider(credentialsProvider)
                .build(watchREST.WatchRESTClient.class);

        // Create the index client
        indexClient = new ApiClientFactory()
                .credentialsProvider(credentialsProvider)
                .build(WatchIndex.WatchIndexClient.class);

        //開啟APP通知
        doPostStartApp();

        mTextView = (TextView) findViewById(R.id.heart_rate_bpm);

        //在背景0.5秒抓一次是不是true
        handler.post(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 500);
                Log.d(TAG, "postDelayed");
                getCheckAndEmail();
            }
        });

        //heart_rate = findViewById(R.id.heart_rate_bpm);
        show_button = findViewById(R.id.show_button);


        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mHeartRateSensor = Objects.requireNonNull(mSensorManager).getDefaultSensor(Sensor.TYPE_HEART_RATE);

        /*GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (acct != null) {
            //取得google帳戶資訊
            account = acct.getEmail();
            Log.d(TAG, "++"+account);
        }*/

        //doCheckIndexHasUser();

        // Enables Always-on

        //手錶運動辨識-------------------------------------

        Sensey.getInstance().init(this);
        textOrientation = findViewById(R.id.textOrientation);
        textShake = findViewById(R.id.textShakeHand);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensey.getInstance().startShakeDetection(shakeListener);
        watchState.put("isShake", false);
        watchState.put("orientation", -1);
        docRef.set(watchState);
        postFirebase();
        //postAws();


        //手錶運動辨識-------------------------------------


        setAmbientEnabled();
    }

    private void startMeasure() {
        data_heart_rate.clear();
        data_time_that.clear();
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        time_start = System.currentTimeMillis();
        //Log.d("Sensor Status:", " Sensor registered: " + (mSe ? "yes" : "no"));
        Log.d(TAG, "start measure");
        show_button.setText("量測中...");
        //開始之後恢復在背景0.5秒抓一次是不是運動結束了(true)
        doPostEnd();
        handler.post(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 500);
                Log.d(TAG, "postDelayed");
                doCheckEnd();
            }
        });
    }


    private void stopMeasure() {
        time_stop = System.currentTimeMillis();
        total_second = (double) (time_stop - time_start) / 1000.0;
        mSensorManager.unregisterListener(this);
        //把boolean改成false
        doPostEnd();
        show_button.setText("量測完畢。");
        Log.e("時間:", Arrays.toString(data_time_that.toArray()));
        Log.e("心跳:", Arrays.toString(data_heart_rate.toArray()));
        //watch_data.add(data_heart_rate);
        //watch_data.add(data_time_that);
        //Collections.sort(data_time_that);
        if (data_heart_rate.size() != 0 || data_time_that.size() != 0) {
            //計算卡路里
            calculateCalories();
            //上傳心跳那些資料上去資料庫
            doInvokeAPI();
        }

        //停止之後恢復在背景0.5秒抓一次是不是開始運動(true)
        handler.post(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 500);
                Log.d(TAG, "postDelayed");
                getCheckAndEmail();
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        time_now = System.currentTimeMillis();
        double record_time = (double) (time_now - time_start) / 1000.0;
        double heartrate = event.values[0];
        DecimalFormat df = new DecimalFormat("##.00");
        heartrate = Double.parseDouble(df.format(heartrate));
        String mHeartRate = String.format(Locale.getDefault(), "%3.2f", heartrate);

        mTextView.setText(mHeartRate);

        record_time = Double.parseDouble(df.format(record_time));
        String mrecord_time = String.format(Locale.getDefault(), "%3.2f", record_time);
        data_heart_rate.add(mHeartRate);
        data_time_that.add(mrecord_time);
        Log.e(TAG, "HR: " + mHeartRate + "TIME:" + record_time);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d(TAG, "Accuracy: " + i);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(accelerometer);
        Sensey.getInstance().stopShakeDetection(shakeListener);
        Sensey.getInstance().stop();
    }


    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(accelerometer, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        Sensey.getInstance().init(this);
        Sensey.getInstance().startShakeDetection(shakeListener);
    }


    //按出去離開APP時停止偵測handler
    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
        //通知APP關閉
        doPostEndApp();
        Sensey.getInstance().stopShakeDetection(shakeListener);
    }

    //按出去後回來繼續handler
    @Override
    protected void onRestart() {
        super.onRestart();
        //通知已開啟APP
        doPostStartApp();
        //在背景0.5秒抓一次是不是true
        handler.post(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 500);
                Log.d(TAG, "postDelayed");
                getCheckAndEmail();
            }
        });
    }

    //銷毀時停止handler
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        handlerWatch.removeCallbacksAndMessages(null);
        //通知APP銷毀
        //doPostEndApp();
    }

    private void doInvokeAPI() {
        // Create components of api request
        //Post的path為/watch，GET的為/watch/object/:userId
        double total_HR = 0;
        for (int j = 0; j < data_heart_rate.size(); j++) {
            total_HR += Double.parseDouble(data_heart_rate.get(j));
        }
        double average_HR = total_HR / data_heart_rate.size();
        DecimalFormat df_for_HR = new DecimalFormat("##.00000");
        average_HR = Double.parseDouble(df_for_HR.format(average_HR));

        final String method = "POST";
        final String path = "/watch";
        String data = Arrays.toString(data_heart_rate.toArray());
        String stop_time = String.valueOf(time_stop);
        String time = stop_time.substring(0, 10);
        userID = time + "000";
        Log.d(TAG, "時間" + userID);


        //body放要post的資料(userId一定要有)，get時則為空字串
        final String body = "{\"userId\": \"" + userID + "\", \"user_email\":\"" + acc_email + "\", \"heartRate\":\"" + data + "\", \"time_moment\":\""
                + data_time_that + "\" ,\"total_time\":\"" + total_second + "\", \"sport_id\":\"" + exercise_id + "\", \"calories\":\"" + calories + "\"" +
                ", \"averageHR\":\"" + average_HR + "\"}";
        //."account":"Spordan2018@gmail.com"
        //final String body = "";
        final byte[] content = body.getBytes(StringUtils.UTF8);

        final Map parameters = new HashMap<>();
        //post時留這邊就好
        parameters.put("lang", "en_US");
        //GET時這邊放要搜尋的哪個PK欄位
        //parameters.put("userId","testList");

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
                        Log.d(TAG, "ResponseHR : " + responseData);
                        /*** 這邊之後可能可以加入錯誤判斷機制 **/
                        //上傳健康數據到health之後，傳index，先檢查index裡有沒有這個人
                        isUploadNew = true;
                        doCheckIndexHasUser();
                    }

                    Log.d(TAG, "status code = " + response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    private void getCheckAndEmail() {
        // Create components of api request
        //Post的path為/watch，GET的為/watch/object/:userId
        final String method = "GET";
        final String path = "/watch/object/:userId";
        //String data = Arrays.toString(data_heart_rate.toArray());
        //String ee = data_heart_rate.toString();
        //String userID = String.valueOf(time_now);
        //Log.d(TAG, "現在時間:"+time_now);

        //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        //simpleDateFormat.format(time_stop);
        //String a = simpleDateFormat.format(time_stop);
        //Log.d(TAG, "這是:"+a);

        //body放要post的資料(userId一定要有)，get時則為空字串
        //final String body = "{\"userId\": \"" +userID + "\", \"heartRate\":\"sin"+data+"tiao\", \"time_moment\":\"shi"
        //        + data_time_that + "qian\" ,\"total_time\":\"zung"+ total_second +"xi"+a+"\"}";
        final String body = "";
        //."account":"Spordan2018@gmail.com"
        //final String body = "";
        final byte[] content = body.getBytes(StringUtils.UTF8);

        final Map parameters = new HashMap<>();
        //post時留這邊就好
        //parameters.put("lang", "en_US");
        //GET時這邊放要搜尋的哪個PK欄位
        parameters.put("userId", "dansmocheckforstartsend");

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
                        String tmp = responseData.substring(responseData.indexOf("{"), responseData.lastIndexOf("}") + 1);
                        JSONObject json_read = new JSONObject(tmp);
                        isChecked = json_read.getBoolean("isChecked");
                        acc_email = json_read.getString("email");
                        acc_gender = json_read.getString("gender");
                        acc_age = json_read.getInt("age");
                        acc_height = json_read.getDouble("height");
                        acc_weight = json_read.getDouble("weight");
                        exercise_id = json_read.getString("sport_id");
                        Log.d(TAG, "Response2 : " + Boolean.toString(isChecked));
                        Log.d(TAG, "Response2 : " + acc_email + acc_gender + acc_age + acc_height + acc_weight + exercise_id);

                        if (isChecked) {
                            handler.removeCallbacksAndMessages(null);
                            //把boolean改false
                            startMeasure();
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

    //rest api invoking (把布林值改false和目前使用者的email清除)
    private void doPostEnd() {
        // Create components of api request
        final String method = "POST";
        final String path = "/watch";

        //body放要post的資料(userId一定要有)
        final String body = "{\"userId\":\"dansmocheckforstartsend\", \"isChecked\":\"false\"}";
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
                        Log.d(TAG, "Response end: " + responseData);
                    }

                    Log.d(TAG, "status code end = " + response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    //檢查sporden那邊是否結束
    private void doCheckEnd() {
        // Create components of api request
        //Post的path為/watch，GET的為/watch/object/:userId
        final String method = "GET";
        final String path = "/watch/object/:userId";
        //String data = Arrays.toString(data_heart_rate.toArray());
        //String ee = data_heart_rate.toString();
        //String userID = String.valueOf(time_now);
        //Log.d(TAG, "現在時間:"+time_now);

        //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        //simpleDateFormat.format(time_stop);
        //String a = simpleDateFormat.format(time_stop);
        //Log.d(TAG, "這是:"+a);

        //body放要post的資料(userId一定要有)，get時則為空字串
        //final String body = "{\"userId\": \"" +userID + "\", \"heartRate\":\"sin"+data+"tiao\", \"time_moment\":\"shi"
        //        + data_time_that + "qian\" ,\"total_time\":\"zung"+ total_second +"xi"+a+"\"}";
        final String body = "";
        //."account":"Spordan2018@gmail.com"
        //final String body = "";
        final byte[] content = body.getBytes(StringUtils.UTF8);

        final Map parameters = new HashMap<>();
        //post時留這邊就好
        //parameters.put("lang", "en_US");
        //GET時這邊放要搜尋的哪個PK欄位
        parameters.put("userId", "dansmocheckforstartsend");

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
                        String tmp2 = responseData.substring(responseData.indexOf("{"), responseData.lastIndexOf("}") + 1);
                        JSONObject json_read2 = new JSONObject(tmp2);
                        endChecked = json_read2.getBoolean("endChecked");
                        //acc_email = json_read.getString("email");
                        Log.d(TAG, "Response2 : " + Boolean.toString(endChecked));
                        //Log.d(TAG, "Response2 : " + acc_email);

                        if (endChecked) {
                            handler.removeCallbacksAndMessages(null);
                            stopMeasure();
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

    //rest api invoking (如果開啟手錶APP就讓它變true)
    private void doPostStartApp() {
        // Create components of api request
        final String method = "POST";
        final String path = "/watch";

        //body放要post的資料(userId一定要有)
        final String body = "{\"userId\":\"isStartstartednajsistar\", \"isStarted\":\"true\"}";
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
                        Log.d(TAG, "Response start app: " + responseData);
                    }

                    Log.d(TAG, "status code end = " + response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    //關掉APP時Post false
    private void doPostEndApp() {
        // Create components of api request
        final String method = "POST";
        final String path = "/watch";

        //body放要post的資料(userId一定要有)
        final String body = "{\"userId\":\"isStartstartednajsistar\", \"isStarted\":\"false\"}";
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
                        Log.d(TAG, "Response start app: " + responseData);
                    }

                    Log.d(TAG, "status code end = " + response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    //檢查index裡面有沒有現在登入的user，並依條件post
    private void doCheckIndexHasUser() {
        arrayList.clear();
        // Create components of api request
        //Post的path為/watch，GET的為/watch/object/:userId
        final String method = "GET";
        final String path = "/windex/object/:user_email";

        //body放要post的資料(userId一定要有)，get時則為空字串
        final String body = "";
        final byte[] content = body.getBytes(StringUtils.UTF8);

        final Map parameters = new HashMap<>();
        //post時留這邊就好
        //parameters.put("lang", "en_US");
        //GET時這邊放要搜尋的哪個PK欄位
        Log.d("yyyyyemail", acc_email);
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
                        final String responseData = IOUtils.toString(responseContentStream);
                        Log.i("yyyyy1", responseData);
                        //取json
                        String tmp = responseData.substring(responseData.indexOf("{"), responseData.lastIndexOf("}") + 1);
                        JSONObject json_read = new JSONObject(tmp);

                        // 如果GET不到個人的話(傳回空值{})
                        if (responseData.equals("{}")) {
                            //post第一次上傳
                            Log.d("yyyyyenter", "近來空的");
                            doFirstPostIndex();
                        } else {
                            String rep = json_read.getString("user_email");
                            Log.d("yyyyyrep", rep);
                            //確認取到的是本人
                            if (rep.equals(acc_email)) {
                                Log.d("yyyyy", "近來有人");
                                /*JSONArray array = json_read.getJSONArray("done_list");
                                ArrayList<String> listdata = new ArrayList<>();
                                if (array != null) {
                                    for (int i = 0; i < array.length(); i++){
                                        listdata.add(array.getString(i));
                                    }
                                }
                                listdata.add(userID);*/
                                //Log.d("yyyyylist",listdata.get(0));
                                //得到資料庫上done_list資料
                                String get_list = json_read.getString("done_list");
                                //取[]裡的資料
                                String done_list = get_list.substring(get_list.indexOf("[") + 1, get_list.indexOf("]"));
                                //用,分開，並存成陣列
                                String[] split_list = done_list.split(", ");
                                arrayList = new ArrayList<>();
                                //將陣列之值全部存進arraylist(已做個的id)
                                for (int i = 0; i < split_list.length; i++) {
                                    arrayList.add(split_list[i]);
                                }
                                //加入剛剛做的id
                                arrayList.add(userID);
                                Log.d("yyyyylist", arrayList.toString());

                                //post新index陣列
                                doPostIndex();
                            }
                        }

                        /* 不知道為什麼在OnCreate時會出現could not load item，但在start不會，要用上面的方法
                        //去抓有沒有錯誤訊息(如果沒有的話就會跳過這行)
                        String rep = json_read.getString("error");
                        Log.i("yyyyy2", rep);
                        if (rep != null && rep.equals("Could not load items: One or more parameter values were invalid: An AttributeValue may not contain an empty string")){
                            Log.i("yyyyy3", "這邊是因為找不到這個帳戶");
                            //因為找不到帳戶，所以這是他的第一筆，幫他上傳
                            //doFirstPostIndex();
                        }*/
                    }

                    Log.d(TAG, "status code = " + response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    //此帳戶第一次post Index
    private void doFirstPostIndex() {
        // Create components of api request
        final String method = "POST";
        final String path = "/windex";

        //body放要post的資料(user_email一定要有)
        final String body = "{\"user_email\":\"" + acc_email + "\", \"done_list\":\"[" + userID + "]\", \"newData\":\"" + isUploadNew + "\"}";
        final byte[] content = body.getBytes(StringUtils.UTF8);

        final Map parameters = new HashMap<>();
        //parameters.put("lang", "en_US");
        //這邊放get要搜尋的哪個PK欄位
        //parameters.put("userId","AJSI");

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
                        final String responseData = IOUtils.toString(responseContentStream);
                        Log.d(TAG, "Response post first index: " + responseData);
                    }

                    Log.d(TAG, "status code post first index = " + response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    private void doPostIndex() {
        // Create components of api request
        final String method = "POST";
        final String path = "/windex";

        //body放要post的資料(user_email一定要有)
        final String body = "{\"user_email\":\"" + acc_email + "\", \"done_list\":\"" + arrayList + "\", \"newData\":\"" + isUploadNew + "\"}";
        final byte[] content = body.getBytes(StringUtils.UTF8);

        final Map parameters = new HashMap<>();
        //parameters.put("lang", "en_US");
        //這邊放get要搜尋的哪個PK欄位
        //parameters.put("userId","AJSI");

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
                        final String responseData = IOUtils.toString(responseContentStream);
                        Log.d(TAG, "Response post index: " + responseData);
                    }

                    Log.d(TAG, "status code post index = " + response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    private void calculateCalories() {
        /**根據公式：http://www.braydenwm.com/cal_vs_hr_ref_paper.pdf**/
        double total_HR = 0;
        if (acc_gender.equals("男性")) {
            for (int j = 0; j < data_heart_rate.size(); j++) {
                total_HR += Double.parseDouble(data_heart_rate.get(j));
            }
            double average_HR = total_HR / data_heart_rate.size();
            DecimalFormat df_for_HR = new DecimalFormat("##.00000");
            average_HR = Double.parseDouble(df_for_HR.format(average_HR));
            double total_minutes1 = total_second / 60.0;
            DecimalFormat df_for_min = new DecimalFormat("##.00000");
            total_minutes1 = Double.parseDouble(df_for_min.format(total_minutes1));
            Log.d("caloriesspp", "av_hr:" + average_HR + ", total_min:" + total_minutes1);
            //Male: ((-55.0969 + (0.6309 x HR) + (0.1988 x W) + (0.2017 x A))/4.184) x 60 x T
            //HR：平均心跳
            //W：體重(公斤)
            //A：年齡
            //T：運動時間(小時)
            calories = ((-55.0969 + (0.6309 * average_HR) + (0.1988 * acc_weight) + (0.2017 * acc_age)) / 4.184) * total_minutes1;
            DecimalFormat df_for_cal = new DecimalFormat("##.00000");
            calories = Double.parseDouble(df_for_cal.format(calories));
            Log.d("caloriessssfor", "((-55.0969+(0.6309*" + average_HR + ")+(0.1988*" + acc_weight + ")+(0.2017*" + acc_age + "))/4.184)*" + total_minutes1);
            Log.d("caloriessss", Double.toString(calories));
        } else if (acc_gender.equals("女性")) {
            for (int j = 0; j < data_heart_rate.size(); j++) {
                total_HR += Double.parseDouble(data_heart_rate.get(j));
            }
            double average_HR = total_HR / data_heart_rate.size();
            DecimalFormat df_for_HR = new DecimalFormat("##.00000");
            average_HR = Double.parseDouble(df_for_HR.format(average_HR));
            double total_minutes = total_second / 60.0;
            DecimalFormat df_for_min = new DecimalFormat("##.00000");
            total_minutes = Double.parseDouble(df_for_min.format(total_minutes));
            Log.d("caloriesspp", "av_hr:" + average_HR + ", total_min:" + total_minutes);
            //Female: ((-20.4022 + (0.4472 x HR) - (0.1263 x W) + (0.074 x A))/4.184) x 60 x T
            calories = ((-20.4022 + (0.4472 * average_HR) - (0.1263 * acc_weight) + (0.074 * acc_age)) / 4.184) * total_minutes;
            DecimalFormat df_for_cal = new DecimalFormat("##.00000");
            calories = Double.parseDouble(df_for_cal.format(calories));
            Log.d("caloriessssfor", "((-20.4022+(0.4472*" + average_HR + ")-(0.1263*" + acc_weight + ")+(0.074*" + acc_age + "))/4.184)*" + total_minutes);
            Log.d("caloriessss", Double.toString(calories));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_BODY_SENSORS: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    finish();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    //以下為手錶辨識用---------------------------------------------------------------------------------------------------------------


    ShakeDetector.ShakeListener shakeListener = new ShakeDetector.ShakeListener() {
        @Override
        public void onShakeDetected() {
            // Shake detected, do something

            shakeTimes++;

            if (shakeTimes == 2) {

                textShake.setText("搖！");

                //isUpdateShake = true;
                isShake = true;
                // doPostShakeBoolean();
                watchState.put("isShake", true);
                Log.d("iisShake?", watchState.toString());
                docRef.set(watchState);

                Log.d("iisShake?", isShake + "");


            }


        }

        @Override
        public void onShakeStopped() {
            // Shake stopped, do something

            shakeTimes = 0;

            textShake.setText("沒搖！");


            isShake = false;
            //doPostShakeBoolean();
            watchState.put("isShake", false);
            docRef.set(watchState);
            isUpdateShake = true;


            Log.d("iisShake?", isShake + "");
        }
    };


    SensorEventListener accelerometer = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {


            accDataList[0] = event.values[0];
            accDataList[1] = event.values[1];
            accDataList[2] = event.values[2];
            SmallCheckDirection();
            CheckDirection();


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    public void CheckDirection() {


        if (accDataList[0] > DirectionCheckThreshold) {
            textOrientation.setText("左傾");
            direction = 1;
        } else if (accDataList[0] < -DirectionCheckThreshold) {
            textOrientation.setText("右傾");
            direction = 3;
        } else if (accDataList[1] < -DirectionCheckThreshold) {
            textOrientation.setText("前傾");
            direction = 2;
        } else if (accDataList[1] > DirectionCheckThreshold) {
            textOrientation.setText("後傾");
            direction = 4;

        }


        if (accDataList[1] > DirectionCheckThreshold && accDataList[0] > DirectionCheckThreshold) {
            textOrientation.setText("左後傾");
            direction = 8;

        } else if (accDataList[1] > DirectionCheckThreshold && accDataList[0] < -DirectionCheckThreshold) {
            textOrientation.setText("右後傾");
            direction = 7;

        } else if (accDataList[1] < -DirectionCheckThreshold && accDataList[0] > DirectionCheckThreshold) {
            textOrientation.setText("左前傾");
            direction = 5;

        } else if (accDataList[1] < -DirectionCheckThreshold && accDataList[0] < -DirectionCheckThreshold) {
            textOrientation.setText("右前傾");
            direction = 6;

        }
    }

    public void SmallCheckDirection() {

        if (accDataList[0] > SmallDirectionCheckThreshold) {
            textOrientation.setText("小左傾");
            direction = 0;
        } else if (accDataList[0] < -SmallDirectionCheckThreshold) {
            textOrientation.setText("小右傾");
            direction = 0;
        } else if (accDataList[1] < -SmallDirectionCheckThreshold) {
            textOrientation.setText("小前傾");
            direction = 0;
        } else if (accDataList[1] > SmallDirectionCheckThreshold) {
            textOrientation.setText("小後傾");
            direction = 0;

        } else {
            textOrientation.setText("平");
            direction = 0;
        }


        if (accDataList[1] > SmallDirectionCheckThreshold && accDataList[0] > SmallDirectionCheckThreshold) {
            textOrientation.setText("小左後傾");
            direction = 0;

        } else if (accDataList[1] > SmallDirectionCheckThreshold && accDataList[0] < -SmallDirectionCheckThreshold) {
            textOrientation.setText("小右後傾");
            direction = 0;

        } else if (accDataList[1] < -SmallDirectionCheckThreshold && accDataList[0] > SmallDirectionCheckThreshold) {
            textOrientation.setText("小左前傾");
            direction = 0;

        } else if (accDataList[1] < -SmallDirectionCheckThreshold && accDataList[0] < -SmallDirectionCheckThreshold) {
            textOrientation.setText("小右前傾");
            direction = 0;

        }
    }

    //rest api invoking (如果開啟手錶APP就讓它變true)

    private void doPostShakeBoolean() {
        Log.d("fuck1", Boolean.toString(isShake));
        // Create components of api request
        final String method = "POST";
        final String path = "/watch";

        //body放要post的資料(userId一定要有)
        final String body = "{\"userId\":\"isShakjemcinoasncoiccx\", \"isShaked\":\"" + isShake + "\"}";
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
                        Log.d(TAG, "Response start app: " + responseData);
                    }

                    Log.d(TAG, "status code end = " + response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    private void doPostOrientation() {
        Log.d("orientation", direction + "");
        // Create components of api request
        final String method = "POST";
        final String path = "/watch";

        //body放要post的資料(userId一定要有)
        final String body = "{\"userId\":\"watchOrientation\", \"orientation\":\"" + direction + "\"}";
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
                        Log.d(TAG, "Response start app: " + responseData);
                    }

                    Log.d(TAG, "status code end = " + response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    public void postAws() {

        handlerWatch.post(new Runnable() {
            @Override
            public void run() {

                if (direction == lastDirection) {
                    //與之前的方向一樣就不做任何事
                    Log.d("sameN", direction + lastDirection + "");
                } else {

                    lastDirection = direction;
                    doPostOrientation();
                    Log.d("number1", direction + "");

                }

                handlerWatch.postDelayed(this, 200);
            }
        });
    }

    public void postFirebase() {

        handlerWatch.post(new Runnable() {
            @Override
            public void run() {

                if (direction == lastDirection) {
                    //與之前的方向一樣就不做任何事
                    Log.d("sameN", direction + lastDirection + "");
                } else {

                    lastDirection = direction;
                    watchState.put("orientation", direction);
                    docRef.set(watchState);

                    Log.d("number1", direction + "");

                }

                handlerWatch.postDelayed(this, 500);
            }
        });
    }


}
