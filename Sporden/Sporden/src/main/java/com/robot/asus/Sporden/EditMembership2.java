package com.robot.asus.Sporden;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.amazonaws.amplify.generated.graphql.UpdateUserMutation;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.robot.asus.Sporden.Model.ClientFactory;

import javax.annotation.Nonnull;

import type.UpdateUserInput;
/** 我把這頁弄成一頁了(EditMembership.java)，看情況把這頁刪掉 **/
public class EditMembership2 extends Fragment {
    private String gender;
    private RadioButton man;
    private RadioButton woman;
    private TextView gender_tv;
    private EditText get_height;
    private EditText get_weight;
    private RadioGroup gender_group;
    private String personEmail;
    private String personName;
    private String userID;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_edit_membership2, container, false);

        TextView previous = view.findViewById(R.id.edit2_previous_page);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditMembership editMembership = new EditMembership();
                getFragmentManager().beginTransaction()
                        .replace(R.id.membership_container, editMembership)
                        .commitAllowingStateLoss();
            }
        });

        TextView cancel = view.findViewById(R.id.edit2_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MembershipFragment2 membershipFragment2 = new MembershipFragment2();
                getFragmentManager().beginTransaction()
                        .replace(R.id.membership_container, membershipFragment2)
                        .commitAllowingStateLoss();
            }
        });

        TextView submit = view.findViewById(R.id.edit2_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //生日欄位辨認機制
                if (!validateForm()) {
                    return;
                }

                //搜尋使用者id並寫入更新至該使用者DB
                queryUserIDandUpdate();


                //暫停1秒讓他上傳
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                //Log.d("TAG", bundle.getString("name"));
                MembershipFragment2 membershipFragment2 = new MembershipFragment2();
                Bundle bundle = new Bundle();
                /*bundle.putString("birthday_Y", editYear.getText().toString());
                bundle.putString("birthday_M", editMonth.getText().toString());
                bundle.putString("birthday_D", editDay.getText().toString());
                bundle.putString("age", Integer.toString(ageAlgo()));*/
                membershipFragment2.setArguments(bundle);
                getFragmentManager().beginTransaction()
                        .replace(R.id.membership_container, membershipFragment2)
                        .commitAllowingStateLoss();


            }
        });

        //show email 不用填
        /***之後不要留格子，因為這個不給填**/
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());
        if (acct != null) {
            //取得google帳戶資訊
            personEmail = acct.getEmail();
        }

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
                    gender = man.getText().toString();
                } else if (woman.getId() == checkedId) {
                    gender = woman.getText().toString();
                }
                Log.d("mannn", gender);
            }
        });

        //height
        get_height = view.findViewById(R.id.edit2_show_height);

        //weight
        get_weight = view.findViewById(R.id.edit2_show_weight);

        return view;
    }

    private boolean validateForm() {
        boolean result = true;
        if (gender_group.getCheckedRadioButtonId() <= 0) {//Grp is your radio group object
            man.setError("必填");//Set error to last Radio button
            woman.setError("必填");
        } else {
            man.setError(null);
            woman.setError(null);
        }

        if (TextUtils.isEmpty(get_height.getText().toString())) {
            get_height.setError("必填");
            result = false;
        } else {
            get_height.setError(null);
        }

        if (TextUtils.isEmpty(get_weight.getText().toString())) {
            get_weight.setError("必填");
            result = false;
        } else {
            get_weight.setError(null);
        }

        return result;
    }

    //查詢email的id
    private void queryUserIDandUpdate() {
        // Get the client instance
        AWSAppSyncClient awsAppSyncClient = ClientFactory.getInstance(getContext());

        awsAppSyncClient.query(ListUsersQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(usersCallback);
    }

    private GraphQLCall.Callback<ListUsersQuery.Data> usersCallback = new GraphQLCall.Callback<ListUsersQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListUsersQuery.Data> response) {
            //用迴圈顯示每一個item的email，並逐一比對是否跟user_email一樣，如果一樣就是使用者在資料庫內，後面也不會有它了直接break
            for (int i = 0; i < response.data().listUsers().items().size(); i++) {
                if (personEmail.equals(response.data().listUsers().items().get(i).email())) {
                    //拿登入這個人的db id and name
                    userID = response.data().listUsers().items().get(i).id();
                    personName = response.data().listUsers().items().get(i).name();
                    //update DB
                    updateUserDB();
                    break;
                }
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
        }
    };

    //run update user db
    private void updateUserDB() {
        AWSAppSyncClient awsAppSyncClient = ClientFactory.getInstance(getContext());

        //id name email必填，並更新birthday and age欄位上DB
        UpdateUserInput updateUserInput = UpdateUserInput.builder().
                id(userID).
                email(personEmail).
                name(personName).
                gender(gender).
                height(Double.valueOf(get_height.getText().toString())).
                weight(Double.valueOf(get_weight.getText().toString())).
                build();

        awsAppSyncClient.mutate(UpdateUserMutation.builder().input(updateUserInput).build()).clone().enqueue(mutationUPCallback);
    }

    private GraphQLCall.Callback<UpdateUserMutation.Data> mutationUPCallback = new GraphQLCall.Callback<UpdateUserMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<UpdateUserMutation.Data> response) {
            Log.i("update database", "更新bithday and age");
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error", "fuck" + e.toString());
        }
    };
}
