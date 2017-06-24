package com.example.leejaeyun.bikenavi2.Tmap;

/**
 * Created by Lee Jae Yun on 2017-04-14.
 */
public class POI_Data {
    //이름
    public String poiname;
    //주소
    public String address;
    //위치
    public String point;
    //거리
    public String distanceStr;
    //거리 더블
    public Double distance;

    public String getPoiname() {
        return poiname;
    }

    public String getAddress() {
        return address;
    }

    public String getPoint() {
        return point;
    }

    public String getDistanceStr() {
        return distanceStr;
    }

    public Double getDistance() {
        return distance;
    }
}