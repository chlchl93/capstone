package com.example.leejaeyun.bikenavi2.Retrofit;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.leejaeyun.bikenavi2.Weather;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;


public class LivingThread extends Thread {
    final static String TAG = "LivingThread";
    private Context mContext;
    private Handler handler;
    private Living_Dust_Repo living_dust_repo;
    private Living_UV_Repo living_uv_repo;

    private int version = 1;
    private String lat;
    private String lon;
    private String uv;

    private boolean getDust = false;
    private boolean getUv = false;

    public LivingThread(Handler handler, Context mContext, double lat, double lon) {
        this.mContext = mContext;
        this.handler = handler;
        this.lat = String.valueOf(lat);
        this.lon = String.valueOf(lon);
    }

    @Override
    public void run() {
        super.run();
        //미세먼지
        Retrofit client = new Retrofit.Builder().baseUrl("http://apis.skplanetx.com/").addConverterFactory(GsonConverterFactory.create()).build();
        Living_Dust_Repo.LivingApiInterface dust_service = client.create(Living_Dust_Repo.LivingApiInterface.class);
        Call<Living_Dust_Repo> dust_call = dust_service.get_dust_retrofit(version, lat, lon);
        dust_call.enqueue(new Callback<Living_Dust_Repo>() {
            @Override
            public void onResponse(Call<Living_Dust_Repo> call, Response<Living_Dust_Repo> response) {
                Log.d(TAG, "dust / response.raw :" + response.raw());
                if (response.isSuccessful()) {
                    living_dust_repo = response.body();
                    Log.d(TAG, "미세먼지 : " + living_dust_repo.get_Dust_Weather().getDust().get(0).getPm10().getGrade());
                    String dust_grade = living_dust_repo.get_Dust_Weather().getDust().get(0).getPm10().getGrade();
                    String dust_value = living_dust_repo.get_Dust_Weather().getDust().get(0).getPm10().getValue();
                    Weather.getInstance().setDust_grade(dust_grade);
                    Weather.getInstance().setDust_value(dust_value);
                    getDust = true;
                    sendMsg("dust");
                }
            }

            @Override
            public void onFailure(Call<Living_Dust_Repo> call, Throwable t) {
                Log.e(TAG, "미세먼지 정보 실패 :" + t.getMessage());
            }
        });
        //자외선
        Living_UV_Repo.LivingApiInterface uv_service = client.create(Living_UV_Repo.LivingApiInterface.class);
        Call<Living_UV_Repo> uv_call = uv_service.get_uv_retrofit(version, lat, lon);
        uv_call.enqueue(new Callback<Living_UV_Repo>() {
            @Override
            public void onResponse(Call<Living_UV_Repo> call, Response<Living_UV_Repo> response) {
                Log.d(TAG, "uv / response.raw :" + response.raw());
                if (response.isSuccessful()) {
                    living_uv_repo = response.body();
                    Log.d(TAG, "자외선 : " + living_uv_repo.get_UV_Weather().getwIndex().getUvindex().get(0).getDay00().getIndex());
                    uv = living_uv_repo.get_UV_Weather().getwIndex().getUvindex().get(0).getDay00().getIndex();

                    Weather.getInstance().setUv(uv);
                    getUv = true;
                    sendMsg("uv");
                }
            }

            @Override
            public void onFailure(Call<Living_UV_Repo> call, Throwable t) {
                Log.e(TAG, "자외선 정보 실패 :" + t.getMessage());
            }
        });
    }

    private void sendMsg(String temp) {
        //완료됨을 알림
        Message msg = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString(temp, temp);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }
}
