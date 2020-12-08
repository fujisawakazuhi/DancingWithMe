package com.robot.asus.Sporden.Model;


import com.robot.asus.Sporden.R;

public class SportCategory {
    // 宣告每個項目資料需要的欄位變數
    private String name;
    private int Img;

    // 建構子
    public SportCategory(String name, int Img) {
        this.Img =Img;
        this.name = name;
    }

    //測試資料
    public static final SportCategory[] sportcategorys = {
        new SportCategory("腹部運動", R.drawable.woman_abdominal),
            new SportCategory("甩腳", R.drawable.ad),
            new SportCategory("有氧", R.drawable.o2),
            new SportCategory("甩手", R.drawable.pit),
    };

    public void setImg(int Img) {
        this.Img = Img;
    }

    public int getImg() {
        return Img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
