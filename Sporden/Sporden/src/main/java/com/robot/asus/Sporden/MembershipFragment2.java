package com.robot.asus.Sporden;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;

import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.util.StringUtils;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.asus.robotframework.API.RobotFace;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.robot.asus.Sporden.Model.ClientFactory;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

public class MembershipFragment2 extends Fragment {
    private TextView show_height;
    private TextView show_weight;
    private double get_height;
    private double get_weight;
    private String personEmail;
    private TextView show_gender;
    private String get_gender;
    private String get_disease;
    private TextView show_Ill;
    private String[] split_disease;
    private ArrayList<String> translated_disease = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_membership_fragment2, container, false);

        TextView show_ill = view.findViewById(R.id.show_ill);
        show_ill.setText("無");

        /*TextView edit = view.findViewById(R.id.edit2);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditMembership2 editMembership2 = new EditMembership2();
                getFragmentManager().beginTransaction()
                        .replace(R.id.membership_container, editMembership2)
                        .commitAllowingStateLoss();
            }
        });*/

        Button previouspage = view.findViewById(R.id.previous_page2);
        if (GetLocale.getLocale().equals("en")) {
            previouspage.setTextSize(33);
        }
        previouspage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MembershipFragment membershipFragment1 = new MembershipFragment();
                getFragmentManager().beginTransaction()
                        .replace(R.id.membership_container, membershipFragment1)
                        .commitAllowingStateLoss();
            }
        });

        //回到首頁
        TextView backmain = view.findViewById(R.id.backmain2);
        backmain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                //getActivity().finish();
                removeFragment();
            }
        });

        //取google email
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());
        if (acct != null) {
            //取得google帳戶資訊
            personEmail = acct.getEmail();
        }

        show_height = view.findViewById(R.id.show_height);
        show_weight = view.findViewById(R.id.show_weight);
        show_gender = view.findViewById(R.id.gender);
        show_Ill = view.findViewById(R.id.show_ill);

        queryUserDB();

        return view;
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
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();

        translated_disease.clear();
    }


    private void queryUserDB() {
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
                    //如果沒有資料的話就跳出去，呈現預設

                    if(response.data().listUsers().items().get(i).height() == null || response.data().listUsers().items().get(i).weight() == null || response.data().listUsers().items().get(i).gender() == null){
                        break;
                    }

                    //拿登入這個人的db id and name
                    get_height = response.data().listUsers().items().get(i).height();
                    get_weight = response.data().listUsers().items().get(i).weight();
                    get_gender = response.data().listUsers().items().get(i).gender();

                    //給更新UI的執行續，才不會閃退
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            //把取到的資料射到前端
                            show_height.setText(Double.toString(get_height));
                            show_weight.setText(Double.toString(get_weight));
                            if (GetLocale.getLocale().equals("zh")) {
                                show_gender.setText(get_gender);
                            } else if (GetLocale.getLocale().equals("en")) {
                                if (get_gender.equals("男性")) {
                                    show_gender.setText("Male");
                                } else if (get_gender.equals("女性")) {
                                    show_gender.setText("Female");
                                }
                            }
                        }
                    });

                    //如果疾病抓不到就預設
                    if (response.data().listUsers().items().get(i).disease() == null) {
                        break;
                    }

                    get_disease = response.data().listUsers().items().get(i).disease();

                    if (GetLocale.getLocale().equals("zh")) {
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                //把[]拿掉
                                show_Ill.setText(get_disease.substring(get_disease.indexOf("[") + 1, get_disease.lastIndexOf("]")));
                            }
                        });
                    } else if (GetLocale.getLocale().equals("en")) {
                        String sl = get_disease.substring(get_disease.indexOf("[") + 1, get_disease.lastIndexOf("]"));
                        split_disease = sl.split(", ");
                        for (int k = 0; k < split_disease.length; k++) {
                            if (split_disease[k].equals("膝蓋疼痛")) {
                                translated_disease.add("Knee Pain");
                            } else if (split_disease[k].equals("手腕疼痛")) {
                                translated_disease.add("Wrist Pain");
                            } else if (split_disease[k].equals("骨質疏鬆")) {
                                translated_disease.add("Osteoporosis");
                            } else if (split_disease[k].equals("糖尿病")) {
                                translated_disease.add("Diabetes");
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //把[]拿掉
                                show_Ill.setText(translated_disease.toString().substring(translated_disease.toString().indexOf("[") + 1, translated_disease.toString().lastIndexOf("]")));
                            }
                        });
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

    private void removeFragment(){
        // 將所有fragment移除
        FragmentManager fragmentManager=getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.membership_container);
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.remove(fragment);
        fragmentTransaction.commit();
    }

}