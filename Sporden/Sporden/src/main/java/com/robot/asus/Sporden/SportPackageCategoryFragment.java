package com.robot.asus.Sporden;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.robot.asus.Sporden.Adapter.SportPackageCategoryAdapter;
import com.robot.asus.Sporden.Model.SportPackageCategory;

/**
 * A simple {@link Fragment} subclass.
 */
public class SportPackageCategoryFragment extends Fragment {


    public SportPackageCategoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_sport_package_category, container, false);
        RecyclerView recycler = (RecyclerView) v.findViewById(R.id.sport_package_category_recycler);

        // 宣告一個新陣列，去找每一個SportPackageCategory這個陣列的name，並把它存在新陣列
        String[] packageNames = new String[SportPackageCategory.sportpackages.length];
        for (int i = 0; i < packageNames.length; i++) {
            packageNames[i] = SportPackageCategory.sportpackages[i].getName();
        }

        // 宣告一個新陣列，去找每一個SportPackageCategory這個陣列的img，並把它存在新陣列
        int[] packageImgs = new int[SportPackageCategory.sportpackages.length];
        for (int i = 0; i < packageImgs.length; i++) {
            packageImgs[i] = SportPackageCategory.sportpackages[i].getImg();
        }

        // 宣告一個新陣列，去找每一個SportPackageCategory這個陣列的img，並把它存在新陣列
        String[] packageIntros = new String[SportPackageCategory.sportpackages.length];
        for (int i = 0; i < packageIntros.length; i++) {
            packageIntros[i] = SportPackageCategory.sportpackages[i].getIntro();
        }

        //將3陣列配給adapter
        SportPackageCategoryAdapter adapter = new SportPackageCategoryAdapter(packageNames,packageImgs,packageIntros);
        recycler.setAdapter(adapter);
        //設定為排列模式，並將cardview顯示
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(layoutManager);

        adapter.setListener(new SportPackageCategoryAdapter.Listener() {
            public void onClick(int position) {
                Intent intent = new Intent(getActivity(), SportTeachIntro.class);
                //這邊到時候可以參照書來寫該類別所產生的清單(傳id過去來過濾清單)
                // intent.putExtra(SportList.Extra_ID, i);
                getActivity().startActivity(intent);
            }
        });

        return recycler;
    }

}