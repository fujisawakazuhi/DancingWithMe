package com.robot.asus.Sporden.Model;

import com.robot.asus.Sporden.R;

//目前測試，到時候會依條件顯示清單
public class SportListModel {
    // 宣告每個項目資料需要的欄位變數
    private String name;
    private int Img;
    private String description;

    // 建構子
    public SportListModel(String name, int Img, String description) {
        this.Img =Img;
        this.name = name;
        this.description = description;
    }

    //測試資料
    public static final SportListModel[] sportListArray = {
            new SportListModel("腹部運動", R.drawable.woman_abdominal, "中階者 - 需要器材 - 強烈運動"),
            new SportListModel("甩腳", R.drawable.ad, "初學者 - 不需要器材 - 中等運動"),
            new SportListModel("有氧", R.drawable.o2, "初學者 - 不需要器材 - 中等運動"),
            new SportListModel("甩手", R.drawable.pit, "熟練者 - 不需要器材 - 強烈運動"),
    };

    public int getImg() {
        return Img;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
