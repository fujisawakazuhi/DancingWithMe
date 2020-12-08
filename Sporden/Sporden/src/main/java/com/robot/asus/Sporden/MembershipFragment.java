package com.robot.asus.Sporden;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtilityOptions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.robot.asus.Sporden.Model.ClientFactory;

import java.io.File;

import javax.annotation.Nonnull;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

/**
 * A simple {@link Fragment} subclass.
 */
public class MembershipFragment extends Fragment {

    private MembershipFragment2 membershipFragment2;
    private String personEmail;
    private TextView birthday_Y;
    private TextView birthday_M;
    private TextView birthday_D;
    private TextView tv_age;
    private String get_BirthdayY;
    private String get_BirthdayM;
    private String get_BirthdayD;
    private int get_age;
    private static String get_id;
    private static String imgUri;
    private ImageView userPhoto;
    private Bitmap bm;
    //private static boolean isLoaded = false;
    //private final Handler handler = new Handler();
    public final static String DOMAIN = "2532C682CCD447C7AEAE1830C7DC2219";



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_membership, container, false);
        Button nextpage = view.findViewById(R.id.nextpage);
        nextpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果圖片載入好之後才能按下一頁，但僅限第一次
                /** 這邊只有第一次有用，很容易沒作用 **/
                //Log.d("fuckkk", Boolean.toString(isLoaded) + " and " + imgUri);
                //if (imgUri == null || isLoaded) {
                //    isLoaded = false;
                    if (membershipFragment2 == null)
                        membershipFragment2 = new MembershipFragment2();
                    getFragmentManager().beginTransaction().replace(R.id.membership_container, membershipFragment2).commitAllowingStateLoss();
                //}
            }
        });

        TextView edit = view.findViewById(R.id.edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (imgUri == null || isLoaded) {
                //    isLoaded = false;
                    EditMembership editMembership = new EditMembership();
                    getFragmentManager().beginTransaction()
                            .replace(R.id.membership_container, editMembership)
                            .commitAllowingStateLoss();
                //}
            }
        });

        //回到首頁
        TextView backmain = view.findViewById(R.id.backmain1);
        backmain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                //getActivity().finish();
                removeFragment();
            }
        });

        //名字and帳號顯示
        TextView name = view.findViewById(R.id.show_name);
        TextView email = view.findViewById(R.id.show_account);
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());
        if (acct != null) {
            //取得google帳戶資訊
            String personName = acct.getDisplayName();
            personEmail = acct.getEmail();

            //顯示目前登入google名字
            if (GetLocale.getLocale().equals("zh")) {
                name.setText("哈囉！" + personName);
            } else if (GetLocale.getLocale().equals("en")) {
                name.setText("Hello！" + personName);
            }
            email.setText(personEmail);
        }

        //大大大大大頭貼
        userPhoto = view.findViewById(R.id.photo);

        // Initialize the AWSMobileClient if not initialized
        /*AWSMobileClient.getInstance().initialize(getContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                Log.i("MembershipFragment", "AWSMobileClient initialized. User State is " + userStateDetails.getUserState());
            }

            @Override
            public void onError(Exception e) {
                Log.e("MembershipFragment", "Initialization error.", e);
            }
        });*/

        //birth init
        //year
        birthday_Y = view.findViewById(R.id.show_birth_date_Y);
        //month
        birthday_M= view.findViewById(R.id.show_birth_date_M);
        //day
        birthday_D = view.findViewById(R.id.show_birth_date_D);
        //age init
        tv_age = view.findViewById(R.id.show_age);

        // show db data to above
        //queryUserDB();

        return view;


    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
                                    /** 小心這邊出問題 (robotAPI)**/
        // close faical
        //robotAPI.robot.setExpression(RobotFace.HIDEFACE);

        // jump dialog domain
        //robotAPI.robot.jumpToPlan(DOMAIN, "Membership");

        //監聽activity傳來的DDE指令
        //runTimer();

        // show db data to above
        queryUserDB();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

        //結束runtimer(因為已換頁)
        //handler.removeCallbacksAndMessages(null);
    }

    private void queryUserDB() {
        // Get the client instance
        AWSAppSyncClient awsAppSyncClient = ClientFactory.getInstance(getContext());

        awsAppSyncClient.query(ListUsersQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK) /**小心這邊出問題**/
                .enqueue(usersCallback);
    }
    private GraphQLCall.Callback<ListUsersQuery.Data> usersCallback = new GraphQLCall.Callback<ListUsersQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListUsersQuery.Data> response) {
            //用迴圈顯示每一個item的email，並逐一比對是否跟user_email一樣，如果一樣就是使用者在資料庫內，後面也不會有它了直接break
            for (int i = 0; i < response.data().listUsers().items().size(); i++){
                if (personEmail.equals(response.data().listUsers().items().get(i).email())){
                    //拿登入這個人的db id and name
                    get_id = response.data().listUsers().items().get(i).id();

                    get_BirthdayY = response.data().listUsers().items().get(i).birthdayY();
                    get_BirthdayM = response.data().listUsers().items().get(i).birthdayM();
                    get_BirthdayD = response.data().listUsers().items().get(i).birthdayD();

                    //如果沒有生日資料的話就跳出去，呈現預設
                    if(get_BirthdayD == null || get_BirthdayM == null || get_BirthdayY == null){
                        break;
                    }

                    get_age = response.data().listUsers().items().get(i).age();

                    imgUri = response.data().listUsers().items().get(i).photoUri();

                    //給更新UI的執行續，才不會閃退
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            //把取到的資料射到前端
                            birthday_Y.setText(get_BirthdayY);
                            birthday_M.setText(get_BirthdayM);
                            birthday_D.setText(get_BirthdayD);
                            tv_age.setText(Integer.toString(get_age));
                       }
                    });

                    // download the profile photo from s3
                    downloadWithTransferUtility();

                    break;
                }
            }
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

        if (getContext() != null) {
            TransferUtility transferUtility =
                    TransferUtility.builder()
                            .context(getContext())
                            .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                            .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                            .transferUtilityOptions(options)
                            .defaultBucket("spordena15102b183ff4128b4ef6d0604708374-sporden")
                            .build();

            TransferObserver downloadObserver =
                    transferUtility.download(
                            "public/profilePic/" + get_id + "/" + imgUri,
                            //new File("/storage/emulated/0/Pictures/temp/" + imgUri + ".jpg"));
                            new File("/storage/emulated/0/Pictures/temp/" + imgUri + ".jpg"));

            Log.d("MembershipFragment", "download key is: public/profilePic/" + get_id + "/" + imgUri);

            // Attach a listener to the observer to get state update and progress notifications
            downloadObserver.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (TransferState.COMPLETED == state) {
                        // Handle a completed upload.
                        Log.d("MembershipFragment", "s3 download completed");
                        setImage();
                    }
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                    int percentDone = (int) percentDonef;

                    Log.d("MembershipFragment", "   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");
                }

                @Override
                public void onError(int id, Exception ex) {
                    // Handle errors
                    Log.d("MembershipFragment", "download failed" + ex.toString());
                }

            });

            // If you prefer to poll for the data, instead of attaching a
            // listener, check for the state and progress in the observer.
            if (TransferState.COMPLETED == downloadObserver.getState()) {
                // Handle a completed upload.
                Log.d("membershipFragment", "complete");
            }

            Log.d("Your Activity", "Bytes Transferred: " + downloadObserver.getBytesTransferred());
            Log.d("Your Activity", "Bytes Total: " + downloadObserver.getBytesTotal());
        }
    }


    /* 設定圖片 */
    private void setImage() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false; // 不顯示照片
        BitmapFactory.decodeFile("/storage/emulated/0/Pictures/temp/" + imgUri + ".jpg", options);
        final int REQUIRED_SIZE = 200;
        int scale = 1;
        /* 圖片縮小2倍 */
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                && options.outHeight / scale / 2 >= REQUIRED_SIZE) {
            scale *= 2;
        }
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false; // 顯示照片
        bm = BitmapFactory.decodeFile("/storage/emulated/0/Pictures/temp/" + imgUri + ".jpg", options);
        Log.i("selectedImagePathSetI", "/storage/emulated/0/Pictures/temp/" + imgUri + ".jpg" + "");

        runOnUiThread(new Runnable(){
            @Override
            public void run() {
                userPhoto.setImageBitmap(bm);// 將圖片顯示
                //isLoaded = true;
            }
        });


    }

    /*public void runTimer() {
        //final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d("testttt","running");
                //取container值測試
                Bundle bundle = getArguments();
                if (bundle != null) {
                    String test = bundle.getString("指令");
                    Log.d("testtttww", test);

                    if(test.equals("nextPage")){
                        //如果圖片載入好之後才能按下一頁，但僅限第一次
                        /** 這邊只有第一次有用，很容易沒作用 **/
                        /*Log.d("fuckkk", Boolean.toString(isLoaded));
                        if (isLoaded) {
                            isLoaded = false;
                            Log.d("testtttww", "okokokokookokooooooooooooo");
                            if (membershipFragment2 == null)
                                membershipFragment2 = new MembershipFragment2();
                            getFragmentManager().beginTransaction().replace(R.id.membership_container, membershipFragment2).commitAllowingStateLoss();
                        }
                    } else if (test.equals("edit")){
                        if (isLoaded) {
                            isLoaded = false;
                            Log.d("testtttww", "okokokokookokooooooooooooo");
                            EditMembership editMembership = new EditMembership();
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.membership_container, editMembership)
                                    .commitAllowingStateLoss();
                        }
                    }
                }

                handler.postDelayed(this, 50);
            }
        });
    }*/

    private void removeFragment(){
        // 將所有fragment移除
        FragmentManager fragmentManager=getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.membership_container);
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.remove(fragment);
        fragmentTransaction.commit();
    }
}