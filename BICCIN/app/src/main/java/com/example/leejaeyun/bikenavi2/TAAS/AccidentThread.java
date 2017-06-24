package com.example.leejaeyun.bikenavi2.Retrofit;

/**
 * Created by jslcs on 2017-04-15.
 */

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;


import com.example.leejaeyun.bikenavi2.Tmap.TmapPointArr;
import com.skp.Tmap.TMapPoint;



import java.util.ArrayList;
import java.util.List;


public class AccidentThread extends Thread {
    final static String TAG = "AccidentThread";
    Handler handler;
    AccidentRepo accidentRepo;
    TmapPointArr tmapPointArr;
    ArrayList<TMapPoint> arr_TmapPoint;

    Context mContext;

    String apiKey ;
    String temp_api ="kYOcotgjm0noDmDh574Ly5VRxFgOc6BkRk%2B8t4MvWaOYS0oZ8aLXJfgu9vUsnjxH";
    int searchYearCd = 2015046; //14년
    String sido = "11"; // 공백 시 전체 선택 //url집힐때
    String gugun = "";
    String death = "N";

    public AccidentThread(Handler handler, Context mContext){
        super();
        this.handler = handler;
        this.mContext = mContext;
    }

    @Override
    public void run() {
        super.run();

        try {

            apiKey = java.net.URLDecoder.decode(temp_api, "UTF-8");

        }catch (Exception e){Log.e(TAG,"인코딩 오류 :"+e.getMessage());}

       // apiKey = "kYOcotgjm0noDmDh574Ly5VRxFgOc6BkRk%2B8t4MvWaOYS0oZ8aLXJfgu9vUsnjxH";//여기가원래 TASSKEY 가져오는함수엿음

        Retrofit client = new Retrofit.Builder().baseUrl("http://taas.koroad.or.kr/data/").addConverterFactory(GsonConverterFactory.create()).build();
                //taas.koroad.or.kr/data에서 받아온 json데이터를 객체로 바꾸어주는 addconvert 퍄


        AccidentRepo.AccidentApiInterface service = client.create(AccidentRepo.AccidentApiInterface.class);
            //과정을 진행할 객체 생성, accidentapiinterface 기능(받은json이 객체로 변환)을 할 객체 service

        Call<AccidentRepo> call = service.get_Accident_retrofit(apiKey,searchYearCd,sido,gugun,death);
             //  저 service를 통해 받아온 json이 변환된 객체(Accident Repo)를 불러오는 'call'함수

        call.enqueue(new Callback<AccidentRepo>() { //비동기화과정
            @Override
            public void onResponse(Call<AccidentRepo> call, Response<AccidentRepo> response) {
                if(response.isSuccessful()){
                    arr_TmapPoint = new ArrayList<TMapPoint>();
                    //사고다발지역 결과를 받아 repo로 Gson 양식으로 받음
                    accidentRepo = response.body();
                    Log.d(TAG,"response.raw :"+response.raw());
                    List<AccidentRepo.searchResult.frequentzone> frequentzoneList = accidentRepo.getSearchResult().getFrequentzone();
                                //frequentzone(사고지점,해당xy좌표) 들을 받는 배열 frequentzoneList

                    //받은 결과를 TMapPoint ArrayList로 저장
                    for(int i=0; i < frequentzoneList.size(); i++ ){
                        try {
                            arr_TmapPoint.add(new TMapPoint(Double.parseDouble(frequentzoneList.get(i).getY_crd()),
                                    Double.parseDouble(frequentzoneList.get(i).getX_crd())));
                        }catch (NumberFormatException e){Log.d(TAG,"좌표변환 오류 :"+e.getMessage());}
                    }

                    tmapPointArr = new TmapPointArr(arr_TmapPoint);
                    Message msg = Message.obtain();   //handle =>메시지 를 조정하는 것을 도와줌
                    Bundle bundle = new Bundle();     //bundle ->데이터 저장하는 것을 도ㅘ줌
                    bundle.putSerializable("THREAD", tmapPointArr);
                    msg.setData(bundle); //읽은 사고지점 데이터를 보여주나봄
                    handler.sendMessage(msg); //메시지 보내나봄, sendmessage
                }
            }
            @Override
            public void onFailure(Call<AccidentRepo> call, Throwable t) {
                Log.e(TAG,"taas 통신 실패 :"+t.getMessage());
            }
        });
    }
}
