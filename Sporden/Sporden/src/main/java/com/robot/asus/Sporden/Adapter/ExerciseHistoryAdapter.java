package com.robot.asus.Sporden.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.robot.asus.Sporden.HistoryData;
import com.robot.asus.Sporden.R;

import java.util.ArrayList;

public class ExerciseHistoryAdapter extends ArrayAdapter<HistoryData> {
    private static final String TAG = "ExerciseHistoryAdapter";
    private Context mContext;
    int mResource;

    /**
     * Default constructor for the ExerciseHistoryAdapter
     * @param context
     * @param resource
     * @param objects
     */
    public ExerciseHistoryAdapter(@NonNull Context context, int resource, @NonNull ArrayList<HistoryData> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String time = getItem(position).getTime();
        String name = getItem(position).getSport_name();

        HistoryData historyData = new HistoryData(time, name);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView hTime = convertView.findViewById(R.id.history_time);
        TextView hName = convertView.findViewById(R.id.history_sport_name);

        hTime.setText(time);
        hName.setText(name);

        return convertView;
    }
}
