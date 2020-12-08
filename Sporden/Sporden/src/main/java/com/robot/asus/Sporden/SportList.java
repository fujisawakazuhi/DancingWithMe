package com.robot.asus.Sporden;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;

import com.robot.asus.Sporden.Adapter.SportListAdapter;
import com.robot.asus.Sporden.Model.SportListModel;

public class SportList extends AppCompatActivity {

    @Override //喵
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport_list);
        //保持畫面不讓zenbo臉打斷
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        RecyclerView listRecycler = (RecyclerView)findViewById(R.id.sport_list_recycler);

        /*******************************************************************************************************************
         * 要注意這邊，這邊則為activity對recyclerview用法
         ***********************************************************************************************************************/

        // 宣告一個新陣列，去找每一個sportListArray個陣列的name，並把它存在新陣列
        String[] sportNames = new String[SportListModel.sportListArray.length];
        for (int i = 0; i < sportNames.length; i++) {
            sportNames[i] = SportListModel.sportListArray[i].getName();
        }

        // 宣告一個新陣列，去找每一個sportListArray這個陣列的img，並把它存在新陣列
        int[] sportImgs = new int[SportListModel.sportListArray.length];
        for (int i = 0; i < sportImgs.length; i++) {
            sportImgs[i] = SportListModel.sportListArray[i].getImg();
        }

        // 宣告一個新陣列，去找每一個sportListArray這個陣列的img，並把它存在新陣列
        String[] sportDescription = new String[SportListModel.sportListArray.length];
        for (int i = 0; i < sportDescription.length; i++) {
            sportDescription[i] = SportListModel.sportListArray[i].getDescription();
        }

        //將三陣列配給adapter
        SportListAdapter adapter = new SportListAdapter(sportNames, sportDescription, sportImgs);
        listRecycler.setAdapter(adapter);
        //設定為直向排列模式
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        listRecycler.setLayoutManager(layoutManager);

        adapter.setListener(new SportListAdapter.Listener() {
            public void onClick(int position) {
                Intent intent = new Intent(getApplicationContext(), SportTeachIntro.class);
                //這邊到時候可以參照書來寫該類別所產生的清單(傳id過去來過濾清單)
                //intent.putExtra(SportList.Extra_ID, i);
                startActivity(intent);
            }
        });
    }
}