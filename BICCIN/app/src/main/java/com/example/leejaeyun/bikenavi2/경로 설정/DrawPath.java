package com.example.leejaeyun.bikenavi2.findroad;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.leejaeyun.bikenavi2.NaviActivity;
import com.example.leejaeyun.bikenavi2.Tmap.GpsInfo;
import com.example.leejaeyun.bikenavi2.R;
import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapView;

import butterknife.Bind;

/**
 * Created by Lee Jae Yun on 2017-04-12.
 */

public class DrawPath extends AppCompatActivity {
    final static String TAG = "DrawPath";
    TMapView mapView;
    FrameLayout mapLayout;
    @Bind(R.id.pathInfo)
    TextView tv_pathInfo;
    @Bind(R.id.start)
    Button startBtn;
    GpsInfo gpsInfo;

    FindRoad findRoad;

    TMapPoint startPoint;
    TMapPoint arrivePoint;
    double latitude;
    double longitude;
    double distance;
    String distanceStr;
    double pathTime;
    String pathTimeStr;
    String arrive; // 도착지 좌표

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path);
        tv_pathInfo = (TextView) findViewById(R.id.pathInfo);
        mapLayout = (FrameLayout) findViewById(R.id.pathMapLayout);
        startBtn = (Button) findViewById(R.id.start);
        startBtn.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "arriveTemp :" + arrive);
                Intent intent = new Intent(DrawPath.this, NaviActivity.class);
                intent.putExtra("arrivePoint", arrive);
                startActivity(intent);
                finish();
            }
        }));

        getGpsInfo();
        getArriveInfo();
        tMapInit();
    }

    /**
     * getGpsInfo
     * 현재 위치를 시작점으로 선택한다.
     */
    private void getGpsInfo() {
        gpsInfo = new GpsInfo(DrawPath.this);
        if (gpsInfo.isGetLocation()) {
            latitude = gpsInfo.getLatitude();
            longitude = gpsInfo.getLongitude();
            startPoint = new TMapPoint(latitude, longitude);
        } else {
            Toast.makeText(DrawPath.this, "GPS 연동 실패", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * getArriveInfo
     * FindRoad_Activity 에서 받은 위치를 받아 도착지로 선택한다.
     */
    private void getArriveInfo() {
        try {
            Intent intent = getIntent();
            arrive = intent.getStringExtra("arrivePoint");
            String temp[] = arrive.split(" ");

            double latitude = Double.parseDouble(temp[1]);
            double longitude = Double.parseDouble(temp[3]);
            arrivePoint = new TMapPoint(latitude, longitude);
        } catch (Exception e) {
            Log.e(TAG, "getArriveInfo 에러");
        }
    }

    /**
     * tMapInit
     * 출발지와 도착지를 받아 지도로 표시해준다
     * 거리와 시간 계산을 해준다
     */
    private void tMapInit() {
        try {
            TMapData tmapdata = new TMapData();
            //출발지와 도착지를 받아 자전거 경로를 그려줌
            tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, startPoint, arrivePoint, new TMapData.FindPathDataListenerCallback() {
                @Override
                public void onFindPathData(TMapPolyLine tMapPolyLine) {
                    mapView.addTMapPath(tMapPolyLine);
                    distance = tMapPolyLine.getDistance();
                    pathTime = distance / 200;
                    pathTimeStr = "" + pathTime;
                    if (distance > 1000) { // 1km 이상
                        if (distance > 12000) { //12km 이상 시간 단위
                            pathTimeStr = "" + distance / 12000;
                            int min = Integer.parseInt(pathTimeStr.substring(pathTimeStr.indexOf(".") + 1, pathTimeStr.indexOf(".") + 2)) * 6;
                            pathTimeStr = pathTimeStr.substring(0, pathTimeStr.indexOf(".")) + "시간 " + min + "분";
                        } else {
                            pathTimeStr = pathTimeStr.substring(0, pathTimeStr.indexOf(".")) + "분";
                        }
                        distanceStr = "" + distance / 1000;
                        distanceStr = distanceStr.substring(0, distanceStr.indexOf(".") + 2) + "km";
                    } else {
                        distanceStr = "" + distance;
                        distanceStr = distanceStr.substring(0, distanceStr.indexOf(".")) + 'm';
                        pathTimeStr = pathTimeStr.substring(0, pathTimeStr.indexOf(".")) + "분";
                    }
                    new Thread(new Runnable() {
                        public void run() {
                            Message msg = handler.obtainMessage();
                            handler.sendMessage(msg);
                        }
                    }).start();
                }
            });
            mapView = new TMapView(this);
            mapView.setSKPMapApiKey("6a2f52a6-1cde-388f-b68d-184bb7cc7c4a");
            mapView.setLocationPoint(longitude, latitude);
            mapView.setCenterPoint(longitude, latitude); // 현재 위치로 지도화면 젼환
            mapView.zoomToTMapPoint(startPoint, arrivePoint);
            mapLayout.addView(mapView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            tv_pathInfo.setText("거리 " + distanceStr + "  시간 :" + pathTimeStr);
        }
    };

}