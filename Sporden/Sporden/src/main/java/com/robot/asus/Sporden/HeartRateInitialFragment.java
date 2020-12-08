package com.robot.asus.Sporden;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HeartRateInitialFragment extends Fragment {
    View v;
    String[] test;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_heartrate_initial, container, false);


        /*FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        HeartRateSwingHandFragment heartRateTestFragment = new HeartRateSwingHandFragment();
        fragmentTransaction.replace(R.id.heartRateContainer, heartRateTestFragment, "Work");
        fragmentTransaction.commit();*/

        return v;
    }

    public HeartRateInitialFragment(){
    }
}
