package com.example.leejaeyun.bikenavi2.Tmap;

/**
 * Created by Lee Jae Yun on 2017-06-07.
 */

public class DistanceValue {
     String endTime;
     String startTime;
     String distance;

    public DistanceValue(){

    }

    public DistanceValue(String endTime, String startTime, String distance){
        this.endTime = endTime;
        this.startTime = startTime;
        this.distance = distance;
    }

    public String getDistance()
    {
        return distance;
    }

    public String getstartTime()
    {
        return startTime;
    }

    public String getendTime()
    {
        return endTime;
    }

}
