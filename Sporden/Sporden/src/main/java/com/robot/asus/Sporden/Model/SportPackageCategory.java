package com.robot.asus.Sporden.Model;

import com.robot.asus.Sporden.R;

public class SportPackageCategory {
    private String name;
    private int Img;
    private String intro;

    // 建構子
    public SportPackageCategory(String name, int Img, String intro) {
        this.Img =Img;
        this.name = name;
        this.intro = intro;
    }

    //測試資料
    public static final SportPackageCategory[] sportpackages = {
            new SportPackageCategory("核心計畫", R.drawable.woman_abdominal, "簡介簡介簡介簡介簡介簡介簡介簡介簡介簡介簡介"),
            new SportPackageCategory("甩腳套餐", R.drawable.ad, "簡介簡介簡介簡介簡介簡介簡介簡介簡介簡介簡介"),
            new SportPackageCategory("有氧套餐", R.drawable.o2, "簡介簡介簡介簡介簡介簡介簡介簡介簡介簡介簡介"),
            new SportPackageCategory("甩手套餐", R.drawable.pit, "簡介簡介簡介簡介簡介簡介簡介簡介簡介簡介簡介"),
    };

    public int getImg() {
        return Img;
    }

    public String getName() {
        return name;
    }

    public String getIntro() {
        return intro;
    }


}
