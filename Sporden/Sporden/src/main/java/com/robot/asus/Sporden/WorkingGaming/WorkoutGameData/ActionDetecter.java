package com.robot.asus.Sporden.WorkingGaming.WorkoutGameData;


public class ActionDetecter {

    private DetectPersonXYZ f;
    private DetectPersonXYZ s;
    private int actionNum;
    private float x;
    private float y;
    private float z;


    public ActionDetecter(DetectPersonXYZ f, DetectPersonXYZ s, int actionNum) {
        this.actionNum = actionNum;
        this.f = f;
        this.s = s;
    }

    public boolean getActionResult() {

        x = f.getX() - s.getX();
        y = f.getY() - s.getY();
        z = f.getZ() - s.getZ();


        if (z > 0.35 && actionNum == 1) {
            //往下
            return true;
        }

        if (z < -0.35 && actionNum == 2) {
            //往上
            return true;
        }

        if (x > 0.55 && actionNum == 3) {
            //往前
            return true;
        }
        if (x < -0.55 && actionNum == 4) {
            //往後
            return true;
        }

        if (y < -0.2 && actionNum == 5) {
            //往右
            return true;
        }
        if (y > 0.2 && actionNum == 6) {
            //往左
            return true;
        }

        return false;
    }


}
