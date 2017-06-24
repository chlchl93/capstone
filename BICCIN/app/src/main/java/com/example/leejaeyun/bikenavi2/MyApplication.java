package com.example.leejaeyun.bikenavi2;

import android.app.Application;

/**
 * Created by Lee Jae Yun on 2017-06-10.
 */

public class MyApplication extends Application {

    private double totalDistance;
    String finishDate;
    String startDate;

    @Override
    public void onCreate() {
        //전역 변수 초기화
        super.onCreate();
    }

    public void setfinishDate(String finishDate){
        this.finishDate = finishDate;
    }

    public String getfinishDate(){
        return finishDate;
    }

    public void setStartDate(String startDate){
        this.startDate = startDate;
    }

    public String getStartDate(){
        return startDate;
    }

    public void settotalDistance(double totalDistance){
        this.totalDistance = totalDistance;
    }

    public double gettotalDistance(){
        return totalDistance;
    }

}
