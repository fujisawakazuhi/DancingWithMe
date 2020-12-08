package com.robot.asus.Sporden;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.robot.asus.Sporden.Adapter.SportCategoryAdapter;
import com.robot.asus.Sporden.Model.SportCategory;

public class SportSearchAndCategoryFragment extends Fragment {


    public SportSearchAndCategoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_sport_search_and_category, container, false);

        //這邊可寫搜尋的DB搜尋方法
        //設置全部的資料
        String[] vocabulary = {
                "apple", "application", "appal", "appalachia", "apposite","pineapple"
        };

        AutoCompleteTextView input_search_actv = v.findViewById(R.id.input_search_sport);

        // https://xnfood.com.tw/android-autocompletetextview/ (說明)
        //設置adapter給actv
        ArrayAdapter<String> adapterforactv = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line,
                vocabulary);
        input_search_actv.setThreshold(1); //輸入幾個字時開始搜尋
        input_search_actv.setAdapter(adapterforactv); //設定 Adapter 給 input_search_actv
        input_search_actv.setCompletionHint("相關運動"); //設定提示訊息


        //button of 搜尋
        Button search_button = (Button)v.findViewById(R.id.btn_sport_search_name);

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*FragmentTransaction fr = getFragmentManager().beginTransaction();
                // 將剛剛的搜尋結果導到下面的activity
                fr.replace(R.id.sport_list_recycler, new SportList());
                fr.commit();*/
                Intent intent = new Intent(v.getContext(),SportList.class);
                startActivity(intent);
            }
        });


        // recyclerView
        //Inflate the recyclerview for this fragment(用到的那個fragment)
        //RecyclerView categoryRecycler = (RecyclerView)inflater.inflate(R.layout.fragment_sport_search_and_category, container,false);
        RecyclerView categoryRecycler = (RecyclerView) v.findViewById(R.id.sport_category_recycler);

        /*******************************************************************************************************************
         * 要注意這邊，這邊是因為上面有一個view了，因為只能回傳一個值，所以需要用這個方法，否則用上面那個註解即可
         * android.widget.FrameLayout cannot be cast to android.support.v7.widget.RecyclerView 像是出現這個錯誤訊息時
         ***********************************************************************************************************************/

        // 宣告一個新陣列，去找每一個sportcategorys這個陣列的name，並把它存在新陣列
        String[] sportNames = new String[SportCategory.sportcategorys.length];
        for (int i = 0; i < sportNames.length; i++) {
            sportNames[i] = SportCategory.sportcategorys[i].getName();
        }

        // 宣告一個新陣列，去找每一個sportcategorys這個陣列的img，並把它存在新陣列
        int[] sportImgs = new int[SportCategory.sportcategorys.length];
        for (int i = 0; i < sportImgs.length; i++) {
            sportImgs[i] = SportCategory.sportcategorys[i].getImg();
        }

        //將兩陣列配給adapter
        SportCategoryAdapter adapter = new SportCategoryAdapter(sportNames,sportImgs);
        categoryRecycler.setAdapter(adapter);
        //設定為網格排列模式，並將cardview顯示為幾行
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        categoryRecycler.setLayoutManager(layoutManager);
        //return categoryRecycler;

        adapter.setListener(new SportCategoryAdapter.Listener() {
            public void onClick(int position) {
                Intent intent = new Intent(getActivity(), SportList.class);
                //這邊到時候可以參照書來寫該類別所產生的清單(傳id過去來過濾清單)
                // intent.putExtra(SportList.Extra_ID, i);
                getActivity().startActivity(intent);
            }
        });

        return v;
    }

}