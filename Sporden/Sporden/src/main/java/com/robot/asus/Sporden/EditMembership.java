package com.robot.asus.Sporden;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.amazonaws.amplify.generated.graphql.UpdateUserMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtilityOptions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.asus.robotframework.API.RobotFace;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.robot.asus.Sporden.Model.ClientFactory;
import com.robot.asus.Sporden.Model.MemberModel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Nonnull;

import type.UpdateUserInput;

import static com.robot.asus.robotactivity.RobotActivity.robotAPI;

public class EditMembership extends Fragment {

    private EditText editYear;
    private EditText editMonth;
    private EditText editDay;
    private int yearMinus;
    private int monthMinus;
    private int dayMinus;
    private String personEmail;
    private static String userID;
    private static String personName;
    private String filename;
    private Uri imageUri;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private String selectedImagePath; // 圖片檔案位置
    private ImageView ivImage;
    private TextView changePicBtn;
    //private static boolean isChecked;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 125;
    private String photoFileName;
    private static Boolean isSelectedPhoto = false;
    public static Boolean isUploadCompleted = false;
    private TextView gender_tv;
    private EditText get_height;
    private EditText get_weight;
    private RadioGroup gender_group;
    private RadioButton man;
    private RadioButton woman;
    private String gender;
    private final Handler handler = new Handler();
    private static boolean isValidate = false;
    private static boolean isageAlgo = false;
    private final Handler handler2 = new Handler();
    public final static String DOMAIN = "2532C682CCD447C7AEAE1830C7DC2219";
    private CheckBox Osteoporosis;
    private CheckBox heart_disease;
    private CheckBox hypertension;
    private CheckBox diabetes;
    private ArrayList<String> user_disease = new ArrayList<>();
    private Dialog dialog;

    EditMembership2 editMembership2 = new EditMembership2();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view =inflater.inflate(R.layout.fragment_edit_membership, container, false);

        //amplify s3 transferUtility
        getContext().startService(new Intent(getContext(), TransferService.class));

        // Initialize the AWSMobileClient if not initialized
        AWSMobileClient.getInstance().initialize(getContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                Log.i("AWSmoblie s3", "AWSMobileClient initialized. User State is " + userStateDetails.getUserState());
            }

            @Override
            public void onError(Exception e) {
                Log.e("AWSmobile s3", "Initialization error.", e);
            }
        });


        TextView cancel = view.findViewById(R.id.edit_cancel);
        TextView submit = view.findViewById(R.id.edit_submit);

        ivImage = view.findViewById(R.id.profile_pic);
        changePicBtn = view.findViewById(R.id.change_pic_btn);

        //生日
        editYear = view.findViewById(R.id.edit_birth_date_Y);
        editMonth = view.findViewById(R.id.edit_birth_date_M);
        editDay = view.findViewById(R.id.edit_birth_date_D);

        //radio gender
        gender_tv = view.findViewById(R.id.sex);

        gender_group = view.findViewById(R.id.sex_radio);
        man = view.findViewById(R.id.man);
        woman = view.findViewById(R.id.woman);
        //radio button to string
        gender_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (man.getId() == checkedId) {
                    gender = "男性";
                } else if (woman.getId() == checkedId) {
                    gender = "女性";
                }
                Log.d("mannn", gender);
            }
        });

        Osteoporosis = view.findViewById(R.id.Osteoporosis);
        heart_disease = view.findViewById(R.id.heart_disease);
        hypertension = view.findViewById(R.id.hypertension);
        diabetes = view.findViewById(R.id.diabetes);

        //height
        get_height = view.findViewById(R.id.edit2_show_height);

        //weight
        get_weight = view.findViewById(R.id.edit2_show_weight);

        /*TextView nextpage = view.findViewById(R.id.edit_nextpage);
        nextpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editMembership2 == null)
                    editMembership2 = new EditMembership2();
                getFragmentManager().beginTransaction().replace(R.id.membership_container, editMembership2).commitAllowingStateLoss();
            }
        });*/

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MembershipFragment membershipFragment1 = new MembershipFragment();
                getFragmentManager().beginTransaction()
                        .replace(R.id.membership_container, membershipFragment1)
                        .commitAllowingStateLoss();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //生日欄位辨認機制
                if (!validateForm()) {
                    return;
                }

                //如果生日超過目前的時間的話
                if (ageAlgo() == -1) {
                    Toast.makeText(getContext(), "無效的生日", Toast.LENGTH_SHORT).show();
                    return;
                }

                //上傳時的時間當作照片的檔名
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                Date date = new Date(System.currentTimeMillis());
                photoFileName = format.format(date);

                if(Osteoporosis.isChecked()) {
                    user_disease.add("膝蓋疼痛");
                }

                if (heart_disease.isChecked()) {
                    user_disease.add("手腕疼痛");
                }

                if (hypertension.isChecked()) {
                    user_disease.add("骨質疏鬆");
                }

                if (diabetes.isChecked()) {
                    user_disease.add("糖尿病");
                }

                //搜尋使用者id並寫入更新至該使用者DB
                queryUserIDandUpdate();

                /*//暫停1秒讓他上傳
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }*/

                //顯示Progress對話視窗
                //直接等五秒讓他轉，避免閃退發生
                dialog = ProgressDialog.show(getContext(), "更新中", "正在更新您的個人資訊....", true);

                /*new Thread(){
                    public void run(){
                        try{
                            sleep(5000);
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                        finally{
                            dialog.dismiss();
                        }
                    }
                }.start();*/
            }
        });

        //show email 不用填
        /***之後不要留格子，因為這個不給填**/
        TextView email = view.findViewById(R.id.edit_show_account);
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());

        if (acct != null) {
            //取得google帳戶資訊
            //String personName = acct.getDisplayName();
            personEmail = acct.getEmail();

            //顯示目前登入google信箱
            //name.setText("哈囉！" + personName);
            email.setText(personEmail);
        }


        changePicBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //要求讀取權限
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (shouldShowRequestPermissionRationale(
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // Explain to the user why we need to read the contacts
                    }

                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                    // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                    // app-defined int constant that should be quite unique
                    //selectImage();
                    //isChecked = true;
                }

                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
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
                }

                selectImage();

                //Log.d("fffffuck", Boolean.toString(isChecked));
                //if (isChecked) {
                //    setImage();
                    //代表已選擇了圖片
                //    isSelectedPhoto = true;
                //}

            }
        });

        MemberModel memberModel = new MemberModel();
        boolean test = memberModel.getCompleted();
        Log.d("testtttttkoko", Boolean.toString(test));
        memberModel.setCompleted(false);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // close faical
        //robotAPI.robot.setExpression(RobotFace.HIDEFACE);

        // jump dialog domain
        //robotAPI.robot.jumpToPlan(DOMAIN, "Membership");

        runTimer();
        runTimer2();
    }

    @Override
    public void onStop() {
        super.onStop();

        handler.removeCallbacksAndMessages(null);
        handler2.removeCallbacksAndMessages(null);

        robotAPI.robot.stopSpeakAndListen();
    }



    @Override
    public void onActivityCreated( @Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private boolean validateForm() {
        boolean result = true;
        String oddM [] = {"1","3","5","7","8","10","12"};
        String evenM [] = {"4","6","9","11"};
        if (TextUtils.isEmpty(editYear.getText().toString())) {
            editYear.setError("必填");
            result = false;
        } else {
            editYear.setError(null);
        }

        if (TextUtils.isEmpty(editMonth.getText().toString())) {
            editMonth.setError("必填");
            result = false;
        } else {
            editMonth.setError(null);
        }

        if (!editMonth.getText().toString().isEmpty()) {
            if (Integer.parseInt(editMonth.getText().toString()) > 12 || Integer.parseInt(editMonth.getText().toString()) < 1) {
                editMonth.setError("格式無效");
                result = false;
            } else {
                editMonth.setError(null);
            }
        }

        if (TextUtils.isEmpty(editDay.getText().toString())) {
            editDay.setError("必填");
            result = false;
        } else {
            editDay.setError(null);
        }

        //如果天不為空
        if (!editDay.getText().toString().isEmpty()) {
            //如果是奇數月
            for (int i = 0; i < oddM.length; i++) {
                if (editMonth.getText().toString().equals(oddM[i])) {
                    //如果1<day<31
                    if (Integer.parseInt(editDay.getText().toString()) > 31 || Integer.parseInt(editDay.getText().toString()) < 1) {
                        editDay.setError("格式無效");
                        result = false;
                    } else {
                        editDay.setError(null);
                    }
                    break;
                }
            }

            // if month is even month
            for (int j = 0; j < evenM.length; j++) {
                if (editMonth.getText().toString().equals(evenM[j])) {
                    //如果1<day<30
                    if (Integer.parseInt(editDay.getText().toString()) > 30 || Integer.parseInt(editDay.getText().toString()) < 1) {
                        editDay.setError("格式無效");
                        result = false;
                    } else {
                        editDay.setError(null);
                    }
                    break;
                }
            }

            //if month is fucking February
            if (editMonth.getText().toString().equals("2")) {
                //閏年
                if (Integer.parseInt(editYear.getText().toString()) % 4 == 0 && Integer.parseInt(editYear.getText().toString()) % 100 != 0 || Integer.parseInt(editYear.getText().toString()) % 400 == 0) {
                    //如果1<day<29
                    if (Integer.parseInt(editDay.getText().toString()) > 29 || Integer.parseInt(editDay.getText().toString()) < 1) {
                        editDay.setError("格式無效");
                        result = false;
                    } else {
                        editDay.setError(null);
                    }
                } else {
                    //平年
                    if (Integer.parseInt(editDay.getText().toString()) > 28 || Integer.parseInt(editDay.getText().toString()) < 1) {
                        editDay.setError("格式無效");
                        result = false;
                    } else {
                        editDay.setError(null);
                    }
                }
            }


        }

        if (gender_group.getCheckedRadioButtonId() <= 0) {//Grp is your radio group object
            man.setError("必填");//Set error to last Radio button
            woman.setError("必填");
            result = false;
        } else {
            man.setError(null);
            woman.setError(null);
        }

        if (TextUtils.isEmpty(get_height.getText().toString())) {
            get_height.setError("必填");
            result = false;
        } else {
            if  (0 < Integer.parseInt(get_height.getText().toString()) && Integer.parseInt(get_height.getText().toString()) <= 200) {
                get_height.setError(null);
            } else {
                get_height.setError("不符合規則");
                result = false;
            }
        }

        if (TextUtils.isEmpty(get_weight.getText().toString())) {
            get_weight.setError("必填");
            result = false;
        } else {
            get_weight.setError(null);
            if  (0 < Integer.parseInt(get_weight.getText().toString()) && Integer.parseInt(get_weight.getText().toString()) <= 200) {
                get_height.setError(null);
            } else {
                get_height.setError("不符合規則");
                result = false;
            }
        }

        return result;
    }

    private int ageAlgo() {
        int birthDateYear = Integer.parseInt(editYear.getText().toString());
        int birthDateMonth = Integer.parseInt(editMonth.getText().toString());
        int birthDateDay = Integer.parseInt(editDay.getText().toString());

        //age 演算法
        //得到目前時間
        Calendar cal = Calendar.getInstance();
        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH) + 1;
        int dayNow = cal.get(Calendar.DATE);

        //目前減生日
        yearMinus = yearNow - birthDateYear;
        monthMinus = monthNow - birthDateMonth;
        dayMinus = dayNow - birthDateDay;

        //先給值
        int age = yearMinus;
        //如果選了未來的年份
        if (yearMinus < 0) {
            age = -1;
        } else if (yearMinus == 0) { // 同年的，不是1就是選到未來的日期，就將他改為-1來識別為錯誤值
            // 選未來的月份
            if (monthMinus < 0) {
                age = -1;
            } else if (monthMinus == 0) { // 同月份的
                // 選未來的天
                if (dayMinus < 0) {
                    age = -1;
                } else if (dayMinus >= 0) {
                    age = 1;
                }
            } else if (monthMinus > 0) {
                age = 1;
            }
        } else if (yearMinus > 0) {
            // 現在月>生日月
            if (monthMinus < 0) {
            } else if (monthMinus == 0) { // 同月份的，再根據日期計算年龄
                if (dayMinus < 0) {
                } else if (dayMinus >= 0) {
                    //如果今年的日比較大，就多一歲
                    age = age + 1;
                }
            } else if (monthMinus > 0) {
                //如果今年的月比較大，就多一歲
                age = age + 1;
            }
        }
        Log.d("fuckyou", Integer.toString(age));
        return age;
    }

    //run update user db
    private void updateUserDB() {
        AWSAppSyncClient awsAppSyncClient = ClientFactory.getInstance(getContext());

        //id name email必填，並更新birthday and age欄位上DB
        //如果有選照片的話就更新圖片位址，沒有就不更新
        if (isSelectedPhoto) {
            UpdateUserInput updateUserInput = UpdateUserInput.builder().
                    id(userID).
                    email(personEmail).
                    name(personName).
                    birthdayY(editYear.getText().toString()).
                    birthdayM(editMonth.getText().toString()).
                    birthdayD(editDay.getText().toString()).
                    age(ageAlgo()).
                    gender(gender).
                    height(Double.valueOf(get_height.getText().toString())).
                    weight(Double.valueOf(get_weight.getText().toString())).
                    photoUri(photoFileName).
                    disease(user_disease.toString()).
                    build();

            awsAppSyncClient.mutate(UpdateUserMutation.builder().input(updateUserInput).build()).clone().enqueue(mutationUPCallback);
        } else {
            UpdateUserInput updateUserInput = UpdateUserInput.builder().
                    id(userID).
                    email(personEmail).
                    name(personName).
                    birthdayY(editYear.getText().toString()).
                    birthdayM(editMonth.getText().toString()).
                    birthdayD(editDay.getText().toString()).
                    age(ageAlgo()).
                    gender(gender).
                    height(Double.valueOf(get_height.getText().toString())).
                    weight(Double.valueOf(get_weight.getText().toString())).
                    disease(user_disease.toString()).
                    build();
            awsAppSyncClient.mutate(UpdateUserMutation.builder().input(updateUserInput).build()).clone().enqueue(mutationUPCallback);

            //暫停1秒讓他上傳
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            //沒選照片所以更新完後直接換頁
            toMembershipFragment();
        }
    }

    private GraphQLCall.Callback<UpdateUserMutation.Data> mutationUPCallback = new GraphQLCall.Callback<UpdateUserMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<UpdateUserMutation.Data> response) {
            Log.i("update database", "更新bithday and age");
            if (isSelectedPhoto) {
                uploadWithTransferUtility();
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error", "fuck" + e.toString());
        }
    };

    //查詢email的id
    private void queryUserIDandUpdate() {
        // Get the client instance
        AWSAppSyncClient awsAppSyncClient = ClientFactory.getInstance(getContext());

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
                    //拿登入這個人的db id and name
                    userID = response.data().listUsers().items().get(i).id();
                    personName = response.data().listUsers().items().get(i).name();

                    //upload
                    //把選取的相片傳上S3(如果有選擇新圖片的話)
                    Log.d("有沒有select photo", isSelectedPhoto.toString());

                    //update DB (先更新db再看要不要上傳，如果有選照片就更新DB後上傳完成再換頁，如果沒選就更新DB不上傳等一秒後換頁)
                    updateUserDB();

                    //如果更新圖片的話再上傳，沒有的話不上傳
                    /*if (isSelectedPhoto) {
                        uploadWithTransferUtility();
                    }*/
                    break;
                }
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
        }
    };

    //照片設定
    private void selectImage() {
        final String item1, item2, item3;
        if (GetLocale.getLocale().equals("zh")) {
            item1 = "拍一張照";
            item2 = "從圖庫選取";
            item3 = "取消";

            final CharSequence[] items = { item1, item2, item3 };

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            if (GetLocale.getLocale().equals("zh")) {
                builder.setTitle("新增照片視窗");
            } else if (GetLocale.getLocale().equals("en")) {
                builder.setTitle("Change new Picture");
            }
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                        case 0: // 拍一張照
                            /* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */
                            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                            Date date = new Date(System.currentTimeMillis());
                            filename = format.format(date);
                            //创建File对象用于存储拍照的图片 SD卡根目录
                            //File outputImage = new File(Environment.getExternalStorageDirectory(),"test.jpg");
                            //存储至DCIM文件夹
                            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                            File outputImage = new File(path,filename+".jpg");
                            Log.d("camerapath", "outputImage:" + outputImage.toString());
                            Log.d("camerapath", "path:" + path);
                            try {
                                if(outputImage.exists()) {
                                    outputImage.delete();
                                }
                                outputImage.createNewFile();
                            } catch(IOException e) {
                                e.printStackTrace();
                            }
                            //将File对象转换为Uri并启动照相程序
                            imageUri = Uri.fromFile(outputImage);
                            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE"); //照相
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); //指定图片输出地址
                            /* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */
                            //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                            StrictMode.setVmPolicy(builder.build());
                            startActivityForResult(intent, REQUEST_CAMERA);
                            //已選了照片
                            //isChecked = true;
                            //給上傳檔案的位置
                            selectedImagePath = outputImage.toString();
                            Log.d("camerapath", outputImage.toString());
                            break;
                        case 1: // 從圖庫選取
                            try {
                                Intent intent1 = new Intent(
                                        Intent.ACTION_PICK);
                                intent1.setType("image/*");
                        /*startActivityForResult(
                                Intent.createChooser(intent1, "選擇開啟圖庫"),
                                SELECT_FILE);*/
                                startActivityForResult(intent1, SELECT_FILE);

                                //已選了照片
                                //isChecked = true;
                            }catch (Exception exp) {
                                Log.i("Error",exp.toString());
                            }
                            //break;
                        default: // 取消
                            dialog.dismiss(); // 關閉對畫框
                            break;
                    }
                }
            });
            builder.show();
        } else if (GetLocale.getLocale().equals("en")) {
            item1 = "Take a picture";
            item2 = "Choose from album";
            item3 = "Cancel";

            final CharSequence[] items = { item1, item2, item3 };

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            if (GetLocale.getLocale().equals("zh")) {
                builder.setTitle("新增照片視窗");
            } else if (GetLocale.getLocale().equals("en")) {
                builder.setTitle("Change new Picture");
            }
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                        case 0: // 拍一張照
                            /* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */
                            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                            Date date = new Date(System.currentTimeMillis());
                            filename = format.format(date);
                            //创建File对象用于存储拍照的图片 SD卡根目录
                            //File outputImage = new File(Environment.getExternalStorageDirectory(),"test.jpg");
                            //存储至DCIM文件夹
                            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                            File outputImage = new File(path,filename+".jpg");
                            Log.d("camerapath", "outputImage:" + outputImage.toString());
                            Log.d("camerapath", "path:" + path);
                            try {
                                if(outputImage.exists()) {
                                    outputImage.delete();
                                }
                                outputImage.createNewFile();
                            } catch(IOException e) {
                                e.printStackTrace();
                            }
                            //将File对象转换为Uri并启动照相程序
                            imageUri = Uri.fromFile(outputImage);
                            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE"); //照相
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); //指定图片输出地址
                            /* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */
                            //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                            StrictMode.setVmPolicy(builder.build());
                            startActivityForResult(intent, REQUEST_CAMERA);
                            //已選了照片
                            //isChecked = true;
                            //給上傳檔案的位置
                            selectedImagePath = outputImage.toString();
                            Log.d("camerapath", outputImage.toString());
                            break;
                        case 1: // 從圖庫選取
                            try {
                                Intent intent1 = new Intent(
                                        Intent.ACTION_PICK);
                                intent1.setType("image/*");
                        /*startActivityForResult(
                                Intent.createChooser(intent1, "選擇開啟圖庫"),
                                SELECT_FILE);*/
                                startActivityForResult(intent1, SELECT_FILE);

                                //已選了照片
                                //isChecked = true;
                            }catch (Exception exp) {
                                Log.i("Error",exp.toString());
                            }
                            //break;
                        default: // 取消
                            dialog.dismiss(); // 關閉對畫框
                            break;
                    }
                }
            });
            builder.show();
        }
    }


    /* 啟動選擇方式 */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) { // 從圖庫開啟
                //onSelectFromGalleryResult(data);
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContext().getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    ivImage.setImageBitmap(selectedImage);
                    selectedImagePath = getPath(getContext(), imageUri);
                    Log.i("imagepath1", selectedImagePath);
                    Log.i("imagepath2", imageUri.toString());
                    //顯示img
                    setImage();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                }


            } else if (requestCode == REQUEST_CAMERA) { // 拍照
                //顯示img
                isSelectedPhoto = true;
                ivImage.setImageURI(imageUri);
            }
        }
    }

    /* 設定圖片 */
    private void setImage() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false; // 不顯示照片
        BitmapFactory.decodeFile(selectedImagePath, options);
        final int REQUIRED_SIZE = 200;
        int scale = 1;
        /* 圖片縮小2倍 */
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                && options.outHeight / scale / 2 >= REQUIRED_SIZE) {
            scale *= 2;
        }
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false; // 顯示照片
        Bitmap bm = BitmapFactory.decodeFile(selectedImagePath, options);
        Log.i("selectedImagePathSetI", selectedImagePath + "");
        Log.i("bmmmm",bm.toString());
        isSelectedPhoto = true;
        ivImage.setImageBitmap(bm);// 將圖片顯示
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    //s3 upload method
    public void uploadWithTransferUtility() {
        /**S3 設定**/
        TransferUtilityOptions options = new TransferUtilityOptions();
        options.setTransferThreadPoolSize(8);
        options.setTransferServiceCheckTimeInterval(500);
        /**S3 設定**/

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                        .transferUtilityOptions(options)
                        .build();

        //上傳檔名為key(一定要有public)，船file selectedImgPath這個位置的檔案
        TransferObserver uploadObserver =
                transferUtility.upload(
                        "public/profilePic/" + userID + "/" + photoFileName,
                        new File(compressImage()));
        Log.d("photofilename", photoFileName);
        Log.d("photopath", selectedImagePath);
        Log.d("compressPath",compressImage());
        Log.d("MembershipEdit", "upload = " + "public/profilePic/" + userID + "/" + photoFileName);

        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                    Log.d("membershipEdit", "complete upload S3");

                    //上傳完再換到membershipFragment
                    toMembershipFragment();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d("MembershipEdit", "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
                Log.d("MembershipEdit", "upload failed" + ex.toString());
            }

        });

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.
        if (TransferState.COMPLETED == uploadObserver.getState()) {
            // Handle a completed upload.
        }

        Log.d("MembershipEdit", "Bytes Transferred: " + uploadObserver.getBytesTransferred());
        Log.d("MembershipEdit", "Bytes Total: " + uploadObserver.getBytesTotal());
    }

    private void toMembershipFragment(){
        //上傳完再換到membershipFragment
        //Log.d("TAG", bundle.getString("name"));
        //先dismiss再換頁面才不會閃退
        isSelectedPhoto = false;
        dialog.dismiss();

        MembershipFragment membershipFragment = new MembershipFragment();
        Bundle bundle = new Bundle();
                /*bundle.putString("birthday_Y", editYear.getText().toString());
                bundle.putString("birthday_M", editMonth.getText().toString());
                bundle.putString("birthday_D", editDay.getText().toString());
                bundle.putString("age", Integer.toString(ageAlgo()));*/
        membershipFragment.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .replace(R.id.membership_container, membershipFragment)
                .commitAllowingStateLoss();
    }

    private String compressImage() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false; // 不顯示照片
        BitmapFactory.decodeFile(selectedImagePath, options);
        final int REQUIRED_SIZE = 200;
        int scale = 1;
        /* 圖片縮小2倍 */
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                && options.outHeight / scale / 2 >= REQUIRED_SIZE) {
            scale *= 2;
        }
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false; // 顯示照片
        Bitmap bm = BitmapFactory.decodeFile(selectedImagePath, options);
        Log.i("selectedImagePathSetI", selectedImagePath + "");
        Log.i("bmmmm",bm.toString());

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        String filename = format.format(date);
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File outputImage = new File(path,filename+".jpg");
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputImage));
            bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputImage.toString();
    }

    public void runTimer() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d("lxhandler","EditMembership running");

                MemberModel memberModel = new MemberModel();
                if (memberModel.getCompleted()) {
                    Log.d("lxhandlerxxx","into completed");
                    //編輯完成上傳的code
                    //生日欄位辨認機制
                    if (!validateForm()) {
                        Log.d("lxhandlerxxx","into completed2");
                        isValidate = true;
                        memberModel.setCompleted(false);
                        //robotAPI.robot.jumpToPlan(DOMAIN, "Membership");
                        return;
                    }
                    Log.d("lxhandlerxxx","into completed3");
                    //如果生日超過目前的時間的話
                    if (ageAlgo() == -1) {
                        Toast.makeText(getContext(), "無效的生日", Toast.LENGTH_SHORT).show();
                        isageAlgo = true;
                        memberModel.setCompleted(false);
                        return;
                    }

                    Log.d("lxhandlerxxx", "進來了");
                    memberModel.setCompleted(false);

                    //上傳時的時間當作照片的檔名
                    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                    Date date = new Date(System.currentTimeMillis());
                    photoFileName = format.format(date);

                    //搜尋使用者id並寫入更新至該使用者DB
                    queryUserIDandUpdate();
                    //memberModel.setCompleted(false);

                } else if (memberModel.getChanged()) {
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {

                        // Should we show an explanation?
                        if (shouldShowRequestPermissionRationale(
                                Manifest.permission.CAMERA)) {
                            // Explain to the user why we need to read the contacts
                        }

                        requestPermissions(new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);
                    }

                    //用講的直接拍照，不需要選
                    /* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */
                    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                    Date date = new Date(System.currentTimeMillis());
                    filename = format.format(date);
                    //创建File对象用于存储拍照的图片 SD卡根目录
                    //File outputImage = new File(Environment.getExternalStorageDirectory(),"test.jpg");
                    //存储至DCIM文件夹
                    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                    File outputImage = new File(path,filename+".jpg");
                    Log.d("camerapath", "outputImage:" + outputImage.toString());
                    Log.d("camerapath", "path:" + path);
                    try {
                        if(outputImage.exists()) {
                            outputImage.delete();
                        }
                        outputImage.createNewFile();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                    //将File对象转换为Uri并启动照相程序
                    imageUri = Uri.fromFile(outputImage);
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE"); //照相
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); //指定图片输出地址
                    /* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */
                    //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder.build());
                    startActivityForResult(intent, REQUEST_CAMERA);
                    //已選了照片
                    //isChecked = true;
                    //給上傳檔案的位置
                    selectedImagePath = outputImage.toString();
                    Log.d("camerapath", outputImage.toString());

                    memberModel.setChanged(false);
                }
                handler.postDelayed(this, 50);
            }
        });
    }

    private void runTimer2() {
        handler2.post(new Runnable() {
            @Override
            public void run() {
                Log.d("lxhandler", "EditMembership running2");
                if (isValidate || isageAlgo) {
                    isageAlgo = false;
                    isValidate = false;
                    runTimer();
                }
                handler2.postDelayed(this, 50);
            }
        });
    }
}
