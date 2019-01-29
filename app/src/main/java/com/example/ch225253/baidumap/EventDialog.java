/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.example.ch225253.baidumap;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EventDialog extends Dialog {

    private Context mContext;
    private LinearLayout mRouteGuideLl;
    
    private TextView mRemainTimeTx;
    private TextView mRemainDistanceTx; // 剩余总距离
    private TextView mCurrentSpeedTx;
    
    private ImageView mTurnImage;
    private TextView mGoDistanceTx;
    private TextView mNextRoadTx;
    
    private TextView mAlongMeters;
    private TextView mCurrentRoadTx;
    
    private ImageView mEnlargeImg;
    
    private TextView mLocateTx;
    public EventDialog(Context context) {
        super(context);
        mContext = context;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater infalter = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = infalter.inflate(R.layout.normal_demo_navi_event_dialog, null);
        setContentView(layout);

        Window window = this.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = 700;
        lp.height = 700;
        window.setAttributes(lp);
        window.setGravity(Gravity.BOTTOM | Gravity.LEFT);

        mRouteGuideLl = layout.findViewById(R.id.route_guide_ll);

        mRemainTimeTx = layout.findViewById(R.id.remain_time_tx);
        mRemainDistanceTx = layout.findViewById(R.id.remain_distance_tx);
        mCurrentSpeedTx = layout.findViewById(R.id.current_speed_tx);

        mTurnImage = layout.findViewById(R.id.turn_img);
        mGoDistanceTx = layout.findViewById(R.id.remain_distance);
        mNextRoadTx = layout.findViewById(R.id.next_road_tx);

        mAlongMeters = layout.findViewById(R.id.along_meters_tx);
        mCurrentRoadTx = layout.findViewById(R.id.current_road_tx);

        mEnlargeImg = layout.findViewById(R.id.enlarge_view_img);

        mLocateTx = layout.findViewById(R.id.loacte_tx);
    }
//    public void initview (){
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View layout2 = inflater.inflate(R.layout.normal_demo_navi_event_textview, null);
//        this.addContentView(layout2,lp);
//        mRouteGuideLl = layout2.findViewById(R.id.route_guide_ll);
//
//        mRemainTimeTx = layout2.findViewById(R.id.remain_time_tx);
//        mRemainDistanceTx = layout2.findViewById(R.id.remain_distance_tx);
//        mCurrentSpeedTx = layout2.findViewById(R.id.current_speed_tx);
//
//        mTurnImage = layout2.findViewById(R.id.turn_img);
//        mGoDistanceTx = layout2.findViewById(R.id.remain_distance);
//        mNextRoadTx = layout2.findViewById(R.id.next_road_tx);
//
//        mAlongMeters = layout2.findViewById(R.id.along_meters_tx);
//        mCurrentRoadTx = layout2.findViewById(R.id.current_road_tx);
//
//        mEnlargeImg = layout2.findViewById(R.id.enlarge_view_img);
//
//        mLocateTx = layout2.findViewById(R.id.loacte_tx);
//    }

    public void updateLocateState(boolean hasLocate) {
        if (mLocateTx != null) {
            mLocateTx.setText(hasLocate ? "定位成功" : "定位中");
        }
    }
    
    public void onEnlageShow(int type, Bitmap arrowBmp, Bitmap bgBmp) {
        if (mEnlargeImg != null) {
            mEnlargeImg.setImageBitmap(arrowBmp);
            mEnlargeImg.setBackgroundDrawable(new BitmapDrawable(bgBmp));
            mEnlargeImg.setVisibility(View.VISIBLE);
        }
        if (mRouteGuideLl != null) {
            mRouteGuideLl.setVisibility(View.GONE);
        }
    }
    
    public void onEnlargeHide() {
        if (mEnlargeImg != null) {
            mEnlargeImg.setVisibility(View.GONE);
        }
        if (mRouteGuideLl != null) {
            mRouteGuideLl.setVisibility(View.VISIBLE);
        }
    }
    
    public void updateTurnIcon(Bitmap map) {
        if (mTurnImage != null) {
            mTurnImage.setImageBitmap(map);
        }
    }
    
    public void updateGoDistanceTx(String tx) {
        if (mGoDistanceTx != null) {
            mGoDistanceTx.setText(tx);
        }
    }
    
    public void updateNextRoad(String nextRoad) {
        if (mNextRoadTx != null) {
            mNextRoadTx.setText(nextRoad);
        }
    }
    
    public void updateAlongMeters(String alongMeters) {
        if (mAlongMeters != null) {
            mAlongMeters.setText(alongMeters);
        }
    }
    
    public void updateCurrentRoad(String currentRoad) {
        if (mCurrentRoadTx != null) {
            mCurrentRoadTx.setText(currentRoad);
        }
    }
    
    public void updateCurrentSpeed(String speed) {
        if (mCurrentSpeedTx != null) {
            mCurrentSpeedTx.setText(speed);
        }
    }
    
    public void updateRemainDistance(String distance) {
        if (mRemainDistanceTx != null) {
            mRemainDistanceTx.setText(distance);
        }
    }
    
    public void updateRemainTime(String time) {
        if (mRemainTimeTx != null) {
            mRemainTimeTx.setText(time);
        }
    }
}
