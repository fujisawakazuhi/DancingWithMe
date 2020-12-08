package com.robot.asus.Sporden;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

public class SportSearchAndSort extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport_search_and_sort);
        //保持畫面不讓zenbo臉打斷
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //將SportCategorySelectionAdapter指派給ViewPager
        SportCategorySelectionAdapter pagerAdapter = new SportCategorySelectionAdapter(getSupportFragmentManager());
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);

        //將viewPager指派給TabLayout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(pager);
    }



    private class SportCategorySelectionAdapter extends FragmentPagerAdapter {

        //加入建構式來讓他接收FragmentManager參數
        public SportCategorySelectionAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            //要在每一頁顯示的fragment(指定每一頁要顯示哪個fragment)
            switch (i) {
                case 0:
                    return new SportSearchAndCategoryFragment();
                case 1:
                    return new SportPackageCategoryFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            //ViewPager裡的頁數
            return 2;
        }

        @Override
        public CharSequence getPageTitle (int position){
            switch (position) {
                case 0:
                    return getResources().getText(R.string.sport_category_search);
                case 1:
                    return getResources().getText(R.string.sport_package);
            }
            return  null;
        }
    }

}