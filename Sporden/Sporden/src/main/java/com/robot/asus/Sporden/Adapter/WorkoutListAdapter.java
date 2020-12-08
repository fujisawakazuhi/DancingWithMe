package com.robot.asus.Sporden.Adapter;

import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtilityOptions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.robot.asus.Sporden.GetLocale;
import com.robot.asus.Sporden.R;

import java.io.File;
import java.util.ArrayList;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

public class WorkoutListAdapter extends RecyclerView.Adapter<WorkoutListAdapter.ViewHolder> {
    private String[] names;
    private String[] imgs;
    private WorkoutListAdapter.Listener listener;

    // 加入介面，讓使用者按下卡片視區時能呼叫onClick
    public interface Listener {
        void onClick(int position);
    }

    //activities and fragment會使用此方法來註冊listener
    public void setListener(WorkoutListAdapter.Listener listener){
        this.listener = listener;
    }

    //建立view holder
    public class ViewHolder extends RecyclerView.ViewHolder{
        // 定義各個資料項目使用的視區
        //recyclerview需顯示cardview，所以宣告viewHolder裡有cardview。若要在recycler視區內顯示別的類型的資料須在此處定義
        private CardView cardView;

        public ViewHolder(CardView itemView) {
            super(itemView);
            cardView = itemView;
        }
    }

    //用adapter建構是把資料傳給兩個變數
    public WorkoutListAdapter(String[] names, String[] imgs) {
        this.names = names;
        this.imgs = imgs;
    }

    //視區建立新view holder時呼叫這邊(使用cardview版面)
    @NonNull
    @Override
    public WorkoutListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // 建立項目使用的畫面配置元件，指定viewholder需使用哪個版面(card)
        // 用layoutinflater將版面轉為cardview
        CardView cv = (CardView) LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.cardview_workout_category_detial, viewGroup, false);
        // 建立與回傳包裝好的畫面配置元件
        return new WorkoutListAdapter.ViewHolder(cv);
    }

    //當recycler視區想要使用或重複使用view holder來顯示新資料時會呼叫此方法(且將資料放進去view相對應位置裡面)
    @Override
    public void onBindViewHolder(@NonNull WorkoutListAdapter.ViewHolder viewHolder, final int i) {
        CardView cardView = viewHolder.cardView;
        //設定imgview
        ImageView imageView = (ImageView)cardView.findViewById(R.id.workout_img);
        //設定為陣列中(drawable內)第幾個照片
        //Drawable drawable = ContextCompat.getDrawable(cardView.getContext(), imgs.get(i));
        //將那張照片設定在imgview
        //imageView.setImage(drawable);
        //imageView.setContentDescription(names[i]);

        //去找對應陣列的照片file位置(imgs[]是運動id)，並轉為uri來呈現
        File file = new File("/storage/emulated/0/Pictures/temp/" + imgs[i] + ".jpg");
        Uri uri = Uri.fromFile(file);
        imageView.setImageURI(uri);

        //設定textview，並將第幾個name設定上去
        TextView textViewName = (TextView)cardView.findViewById(R.id.workout_name);
        textViewName.setText(names[i]);
        if (GetLocale.getLocale().equals("en")) {
            textViewName.setTextSize(70);
        }

        //讓cardview可以被按下並啟動別的(將listener加入cardview)
        cardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //當cardview被按下時呼叫listener.onClick()
                if (listener != null){
                    listener.onClick(i);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        //names陣列長度等於recycler視區的資料數目
        return names.length;
    }

}
