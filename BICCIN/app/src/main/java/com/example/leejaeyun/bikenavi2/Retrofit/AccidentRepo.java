package com.example.leejaeyun.bikenavi2.Retrofit;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by hp on 2016-06-28.
 */
public class AccidentRepo implements Serializable {

    @SerializedName("searchResult")
    private searchResult searchResult;

    @SerializedName("resultCode")
    String resultCode;

    public String getResultCode() {return resultCode;}
    public searchResult getSearchResult() {return searchResult;}
      //searchresult함수 진행하고 결과 불러오기



    public class searchResult {

        private List<frequentzone> frequentzone = new ArrayList<>();
           //frequentzone라는 class 형식들을 담을 배열(arraylist) 생성

        public List<frequentzone> getFrequentzone() {return frequentzone;}

        public class frequentzone {
            @SerializedName("spotname")
            String spotname;
            @SerializedName("x_crd")
            String x_crd;
            @SerializedName("y_crd")
            String y_crd;

            public String getSpotname() {return spotname;}

            public String getX_crd() {return x_crd;}

            public String getY_crd() {return y_crd;}
        } //사고지점 정보들 받아온 것 , spotname: ex)서울특별시 강북구 한성빌딩 1주근, xcrd, ycrd : 해당지점좌표
    }

    public interface AccidentApiInterface {
        @GET("rest/frequentzone/bicycle")
             //base주소인 http://taas.koroad.or.kr/data/에서 http://taas.koroad.or.kr/rest/frequentzone/bicycle을 의미하게됨
            //http://taas.koroad.or.kr/data/rest/frequentzone/bicycle?authKey=[인증키]&searchYearCd=2015046&sido=11&gugun=305&DEATH=N <-서울특별시 강북구 data 요청 url 예시


        Call<AccidentRepo> get_Accident_retrofit(@Query("authKey") String authKey, @Query("searchYearCd") int searchYearCd, @Query("sido") String sido, @Query("gugun") String gugun, @Query("DEATH") String DEATH);
    }      ///call <주고받을객체> 지저할 함수명 = (@query(파라미터명),자료형 변수이름)
           //<AccidentRepo>이것은 이제 실제로 우리가 HTTP 통신을 통해 수신한 JSON 등이 변환될 객체이다.

}