package com.example.leejaeyun.bikenavi2.Retrofit;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;


public class WeatherRepo implements Serializable {

    @SerializedName("result")
    Result result;
    @SerializedName("weather")
    weather weather;


    public class Result {
        @SerializedName("message")
        String message;

        public String getMessage() {
            return message;
        }
    }

    public class weather {

        public List<hourly> hourly = new ArrayList<>();

        public List<hourly> getHourly() {
            return hourly;
        }

        public class hourly {
            @SerializedName("sky")
            Sky sky;
            @SerializedName("precipitation")
            precipitation precipitation;
            @SerializedName("temperature")
            temperature temperature;
            @SerializedName("wind")
            wind wind;

            public class Sky {
                @SerializedName("name")
                String name;
                @SerializedName("code")
                String code;

                public String getName() {
                    return name;
                }

                public String getCode() {
                    return code;
                }
            }

            public class precipitation { // 강수 정보
                @SerializedName("sinceOntime")
                String sinceOntime; // 강우
                @SerializedName("type")
                String type; //0 :없음 1:비 2: 비/눈 3: 눈

                public String getSinceOntime() {
                    return sinceOntime;
                }

                public String getType() {
                    return type;
                }
            }

            public class temperature {
                @SerializedName("tc")
                String tc; // 현재 기온

                public String getTc() {
                    return tc;
                }
            }

            public class wind { // 바람
                @SerializedName("wdir")
                String wdir;
                @SerializedName("wspd")
                String wspd;

                public String getWdir() {
                    return wdir;
                }

                public String getWspd() {
                    return wspd;
                }
            }

            public Sky getSky() {
                return sky;
            }

            public hourly.precipitation getPrecipitation() {
                return precipitation;
            }

            public hourly.temperature getTemperature() {
                return temperature;
            }

            public hourly.wind getWind() {
                return wind;
            }
        }


    }


    public Result getResult() {
        return result;
    }

    public weather getWeather() {
        return weather;
    }

    //한국 날씨 weather/hourly/hourly
    //세계 날씨 weather/hourly
    public interface AccidentApiInterface {
        @Headers({"Accept: application/json", "access_token: 705c1ee2-9ce5-4a61-97ad-fc3e55d0b4dc", "appKey: 6a2f52a6-1cde-388f-b68d-184bb7cc7c4a"})
        @GET("weather/current/hourly")
        Call<WeatherRepo> get_Weather_retrofit(@Query("version") int version, @Query("lat") String lat, @Query("lon") String lon);
    }
}
