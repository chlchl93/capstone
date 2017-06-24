package com.example.leejaeyun.bikenavi2.findroad;

import android.location.Location;

/**
 * Created by Lee Jae Yun on 2017-04-12.
 */

public class CalDistance {


    double beforeLat = 0;
    double beforeLon = 0;

    /**
     * 거리를 계산하여 표시한다.
     */
    public static String convertDistanceStr(Double distance){
        String distanceStr = "";
        if(distance > 1000) { // 1km 이상
            distanceStr = ""+distance / 1000;
            distanceStr = distanceStr.substring(0,distanceStr.indexOf(".")+2)+"km";
            return distanceStr;
        }
        else{
            distanceStr = ""+distance;
            distanceStr = distanceStr.substring(0,distanceStr.indexOf("."))+'m';
            return distanceStr;
        }
    }

    /**
     * 위도, 경도를 받아 거리를 계산하고 누적 거리를 저장한다.
     */
    public double calDistance(double lat, double lon){
        if(beforeLat == 0){
            beforeLat = lat;
            beforeLon = lon;
        }
        Location before = new Location("before");
        before.setLatitude(beforeLat);
        before.setLongitude(beforeLon);
        Location now = new Location("now");
        now.setLatitude(lat);
        now.setLongitude(lon);
        double distance = before.distanceTo(now);

        beforeLat = lat;
        beforeLon = lon;

        return distance;
    }
}

