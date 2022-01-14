package com.example.weathertest;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class weatherData {

    private String nx = "60";	//위도
    private String ny = "125";	//경도
    private String baseDate = "20210531";	//조회하고싶은 날짜
    private String baseTime = "0500";	//조회하고싶은 시간
    private String type = "json";	//조회하고 싶은 type(json, xml 중 고름)

    public void lookUpWeather() throws IOException, JSONException {

//		참고문서에 있는 url주소
        String apiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
//         홈페이지에서 받은 키
        String serviceKey = "Pc3mj0ODAsPJ1UTZ1BGByalWMKn%2BtZs9ye8MJ2mCTrZLTfOyZf1te7QKxcATs%2Bm5qGLpX8dLbwL8dhRLfRaxFw%3D%3D";
        String weather = null, tmperature = null;

        StringBuilder urlBuilder = new StringBuilder(apiUrl);
        urlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8") + "="+serviceKey);
        urlBuilder.append("&" + URLEncoder.encode("nx","UTF-8") + "=" + URLEncoder.encode(nx, "UTF-8")); //경도
        urlBuilder.append("&" + URLEncoder.encode("ny","UTF-8") + "=" + URLEncoder.encode(ny, "UTF-8")); //위도
        urlBuilder.append("&" + URLEncoder.encode("base_date","UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8")); /* 조회하고싶은 날짜*/
        urlBuilder.append("&" + URLEncoder.encode("base_time","UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8")); /* 조회하고싶은 시간 AM 02시부터 3시간 단위 */
        urlBuilder.append("&" + URLEncoder.encode("dataType","UTF-8") + "=" + URLEncoder.encode(type, "UTF-8"));	/* 타입 */

        /*
         * GET방식으로 전송해서 파라미터 받아오기
         */
        URL url = new URL(urlBuilder.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
//        System.out.println("Response code: " + conn.getResponseCode());

        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }

        rd.close();
        conn.disconnect();
        String result= sb.toString();

        //=======이 밑에 부터는 json에서 데이터 파싱해 오는 부분이다=====//

        // response 키를 가지고 데이터를 파싱
        JSONObject jsonObj_1 = new JSONObject(result);
        String response = jsonObj_1.getString("response");

        // response 로 부터 body 찾기
        JSONObject jsonObj_2 = new JSONObject(response);
        String body = jsonObj_2.getString("body");

        // body 로 부터 items 찾기
        JSONObject jsonObj_3 = new JSONObject(body);
        String items = jsonObj_3.getString("items");
        Log.i("ITEMS",items);

        // items로 부터 itemlist 를 받기 
        JSONObject jsonObj_4 = new JSONObject(items);
        JSONArray jsonArray = jsonObj_4.getJSONArray("item");

        for(int i=0;i<jsonArray.length();i++){
            jsonObj_4 = jsonArray.getJSONObject(i);
            String fcstValue = jsonObj_4.getString("fcstValue");
            String category = jsonObj_4.getString("category");

            if(category.equals("SKY")){
                weather = "현재 날씨는 ";
                if(fcstValue.equals("1")) {
                    weather += "맑은 상태로";
                }else if(fcstValue.equals("2")) {
                    weather += "비가 오는 상태로 ";
                }else if(fcstValue.equals("3")) {
                    weather += "구름이 많은 상태로 ";
                }else if(fcstValue.equals("4")) {
                    weather += "흐린 상태로 ";
                }
            }

            if(category.equals("T3H") || category.equals("T1H")){
                tmperature = "기온은 "+fcstValue+"℃ 입니다.";
            }

            Log.i("WEATER_TAG",weather + tmperature);
        }

    }

}