package com.example.leejaeyun.bikenavi2.findroad;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.leejaeyun.bikenavi2.Tmap.POI_Data;
import com.example.leejaeyun.bikenavi2.R;
import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by Lee Jae Yun on 2017-04-12.
 */

public class FindRoad extends AppCompatActivity {

    TMapData tmapdata = new TMapData();
    TMapPoint startPoint; //출발지좌표
    POI_Data poi_data = new POI_Data();
    ArrayList<POI_Data> mListData = new ArrayList<POI_Data>();
    CalDistance calculUtil = new CalDistance();
    POI_DataCustomAdapter poi_dataCustomAdapter;
    private InputMethodManager imm;

    Button search;
    EditText goal;
    ListView listview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findroad);
        getStartpoint(); //출발지좌표값 받아오기
        goal = (EditText) findViewById(R.id.goal);
        listview = (ListView) findViewById(R.id.listview);
        poi_dataCustomAdapter = new POI_DataCustomAdapter(FindRoad.this);
        listview.setAdapter(poi_dataCustomAdapter);
        listview.setOnItemClickListener(onClickListItem);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); //키보드 보임

        /*
         검색어를 검색하여 item 으로 받는다.
         */
        search = (Button) findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    final String strData = goal.getText().toString();
                    Log.e(TAG, strData);
                    tmapdata.findAllPOI(strData, new TMapData.FindAllPOIListenerCallback() {
                        @Override
                        public void onFindAllPOI(ArrayList<TMapPOIItem> poiItem) {
                            ArrayList<POI_Data> set_itemlist = new ArrayList<POI_Data>();
                            for (int i = 0; i < poiItem.size(); i++) {
                                TMapPOIItem item = poiItem.get(i);
                                POI_Data poi_data = new POI_Data();
                                poi_data.poiname = item.getPOIName().toString();
                                poi_data.address = item.getPOIAddress().replace("null", "");
                                poi_data.point = item.getPOIPoint().toString();
                                poi_data.distance = item.getDistance(startPoint);
                                poi_data.distanceStr = calculUtil.convertDistanceStr(item.getDistance(startPoint));
                                set_itemlist.add(poi_data);
                                Log.d("주소로찾기", "POI Name: " + item.getPOIName().toString() + ", " +
                                        "Address: " + item.getPOIAddress().replace("null", "") + ", " +
                                        "Point: " + item.getPOIPoint().toString());
                            }
                            poi_dataCustomAdapter.setdata(set_itemlist);
                        }
                    });
                    imm.hideSoftInputFromWindow(goal.getWindowToken(), 0); // 키보드 숨김
                } catch (Exception e) {
                    Log.e(TAG, "검색 오류");
                }
            }
        });
    }

    // 아이템 터치 이벤트
    private OnItemClickListener onClickListItem = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

        }
    };

    /**
     * getArriveInfo
     * 길 찾기 에서 출발지를 받아온다
     */
    private void getStartpoint() {
        try {
            Intent intent = getIntent();
            String arrive = intent.getStringExtra("startPoint");
            String temp[] = arrive.split(" ");
            double latitude = Double.parseDouble(temp[1]);
            double longitude = Double.parseDouble(temp[3]);
            startPoint = new TMapPoint(latitude, longitude); // 출발지 포인트
        } catch (Exception e) {
            Log.d(TAG, "getStartpoint null");
        }
    }

}