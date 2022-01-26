package com.example.weathertest;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class WeatherActivity extends AppCompatActivity {


    //private Button btn;
    private TextView resultText, locationText, dateText, tempText, timeText;
    private LinearLayout connectView;
    private FrameLayout resultView;
    private ImageView resultImg;
    private GpsTracker gpsTracker;
    private String x = "", y = "", address = "";

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        Intent intent = getIntent();
        String rDateTime[] = intent.getStringArrayExtra("rDateTime");
        String localName = intent.getStringExtra("localName");



        //btn = findViewById(R.id.btn);
        dateText = (TextView) findViewById(R.id.dateText);
        timeText = (TextView) findViewById(R.id.timeText);
        tempText = (TextView) findViewById(R.id.tempText);
        //resultText = (TextView) findViewById(R.id.resultText);
        locationText = (TextView) findViewById(R.id.locationText);
        connectView = (LinearLayout) findViewById(R.id.connectView);
        resultView = (FrameLayout) findViewById(R.id.resultView);
        resultImg = (ImageView) findViewById(R.id.resultImg);



        readExcel(localName); //위치
        //xylocationText.setText("격자값( x y ) : "+ x + " "+y);
        //String rDateTime[] = getRealDateTime(); //시간



        Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            public void run() {

                CopyDatabaseAsyncTask task = new CopyDatabaseAsyncTask(WeatherActivity.this) ;
                task.execute(rDateTime[0],rDateTime[1],rDateTime[2],x,y,localName,rDateTime[4]) ;
            }
        }, 800);  // 2000은 2초를 의미합니다.


        //Handler handler = new Handler();
        handler2.postDelayed(new Runnable() {
            public void run() {

            exitProgram();

        }
    }, 60000);  // 1분뒤 종료




    }



    private class CopyDatabaseAsyncTask extends AsyncTask<String, Long, Boolean> {



        public CopyDatabaseAsyncTask(Context context) {

        }

        String weather = "", tmperature = "", sky = "", realTime = "", nowTime="", localName = "", resultPrint = "", response = "";
        private String nx = "55";	//위도
        private String ny = "127";	//경도
        private String numOfRows = "30";	//정보 수
        private String pageNo = "1";	//경도
        private String baseDate = "20220111";	//조회하고싶은 날짜
        private String baseTime = "1400";	//조회하고싶은 시간
        private String type = "json";	//조회하고 싶은 type(json, xml 중 고름)


        @Override
        protected void onPreExecute() {
            //백그라운드 스레드가 실행되기 전, 메인 스레드에 의해 호출되는 메서드 ( 로딩화면 불러오기 )
            //로딩 UI

        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected Boolean doInBackground(String... params) {
            //AssetManager am = mContext.getResources().getAssets() ;

            //		참고문서에 있는 url주소
            String apiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst";
//         홈페이지에서 받은 키
            String serviceKey = "" + "Pc3mj0ODAsPJ1UTZ1BGByalWMKn%2BtZs9ye8MJ2mCTrZLTfOyZf1te7QKxcATs%2Bm5qGLpX8dLbwL8dhRLfRaxFw%3D%3D";

            try {
                baseDate = params[0];
                realTime = params[1];
                baseTime = params[2];
                localName = params[5];
                nowTime = params[6];
                nx = params[3];
                ny = params[4];
                Log.i("baseTime TAG",baseTime);

                StringBuilder urlBuilder = new StringBuilder(apiUrl);
                urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "="+ serviceKey);
                urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode(numOfRows, "UTF-8")); //정보 수
                urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode(pageNo, "UTF-8")); //페이지 수
                urlBuilder.append("&" + URLEncoder.encode("dataType","UTF-8") + "=" + URLEncoder.encode(type, "UTF-8"));	/* 타입 */
                urlBuilder.append("&" + URLEncoder.encode("base_date","UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8")); /* 조회하고싶은 날짜*/
                urlBuilder.append("&" + URLEncoder.encode("base_time","UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8")); /* 조회하고싶은 시간 AM 02시부터 3시간 단위 */
                urlBuilder.append("&" + URLEncoder.encode("nx","UTF-8") + "=" + URLEncoder.encode(nx, "UTF-8")); //경도
                urlBuilder.append("&" + URLEncoder.encode("ny","UTF-8") + "=" + URLEncoder.encode(ny, "UTF-8")); //위도
                Log.i("TAG, url Msg",urlBuilder.toString());
                /*
                 * GET방식으로 전송해서 파라미터 받아오기
                 */
                URL url = new URL(urlBuilder.toString());
                Log.i("TAG,URL",baseDate+baseTime+nx+ny);

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
                Log.i("RESULT",result);

                //=======이 밑에 부터는 json에서 데이터 파싱해 오는 부분이다=====//

                // response 키를 가지고 데이터를 파싱
                JSONObject jsonObj_1 = new JSONObject(result);
                response = jsonObj_1.getString("response");
                Log.i("TAG,RESPONSE",response);


                // response 로 부터 body 찾기
                JSONObject jsonObj_2 = new JSONObject(response);
                String body = jsonObj_2.getString("body");

                // body 로 부터 items 찾기
                JSONObject jsonObj_3 = new JSONObject(body);
                String items = jsonObj_3.getString("items");
                Log.i("TAG,ITEMS",items);

                // items로 부터 itemlist 를 받기
                JSONObject jsonObj_4 = new JSONObject(items);
                JSONArray jsonArray = jsonObj_4.getJSONArray("item");

                for(int i=0;i<jsonArray.length();i++){
                    jsonObj_4 = jsonArray.getJSONObject(i);
                    String fcstValue = jsonObj_4.getString("fcstValue");
                    //Log.i("TAG1",fcstValue);
                    String category = jsonObj_4.getString("category");
                    //Log.i("TAG2",fcstValue);

                    if(category.equals("PTY")&&weather.equals("")){
                        weather = fcstValue;
                    }
                    if(category.equals("SKY")&&sky.equals("")){
                        sky = fcstValue;
                    }
                    if(category.equals("T1H")&&tmperature.equals("")){
                        tmperature = fcstValue;
                    }
                    Log.i("TAG,tmperature",tmperature);


//
//                    if(category.equals("PTY")&&sky.equals("")){
//                        if(fcstValue.equals("1")) {
//                            sky = "비가 와요 ";
//                        }else if(fcstValue.equals("2")) {
//                            sky = "비랑 눈이 와요 ";
//                        }else if(fcstValue.equals("3")) {
//                            sky = "눈이 와요 ";
//                        }else if(fcstValue.equals("4")) {
//                            sky = "소나기가 내려요 ";
//                        }else if(fcstValue.equals("0")) {
//                            sky = "맑아요 ";
//                        }
//                    }//Log.i("TAG,sky",sky);
//
//                    if(category.equals("SKY")&&weather.equals("")){
//                        if(fcstValue.equals("1")) {
//                            weather = "맑고 ";
//                        }else if(fcstValue.equals("3")) {
//                            weather = "구름이 많고 ";
//                        }else if(fcstValue.equals("4")) {
//                            weather = "흐리고 ";
//                        }
//                    }//Log.i("TAG,weather",weather);
//
//                    if(category.equals("T1H")&&tmperature.equals("")){
//                        tmperature = fcstValue+"˚";
//                    }//Log.i("TAG,tmperature",tmperature);
//
////                    if(category.equals("POP")){
////                        rainpercent = " \n강수확률: "+fcstValue+"%";
////                    }


                }

            } catch (Exception e) {
                e.printStackTrace() ;
            }

            return true;
        }


        @Override
        protected void onPostExecute(Boolean result) {


            if(weather.equals("1")){
                resultPrint = "비온다!";
            }
            else if(weather.equals("2")){
                resultPrint = "비랑 눈 내려요!";
            }
            else if(weather.equals("3")){
                resultPrint = "눈온당!!";
            }
            else if(weather.equals("4")){
                resultPrint = "소나기와요..!";
            }
            else if(weather.equals("0")){
                if(sky.equals("1")){
                    if(Integer.parseInt(tmperature)>=20){
                        resultPrint = "더워요..!";
                    }
                    else if(Integer.parseInt(tmperature) <=-3){
                        resultPrint = "추워요..!";
                        resultView.setBackgroundResource(R.drawable.cub);
                        resultImg.setImageResource(R.drawable.cu);
                    }
                    else{
                        resultPrint = "맑아요옹>3<";
                        resultView.setBackgroundResource(R.drawable.mub);
                        resultImg.setImageResource(R.drawable.mu);
                    }
                }
                else if(sky.equals("3")){
                    resultPrint = "구름이 많아요!";

                }
                else if(sky.equals("4")){
                    resultPrint = "흐려요!";
                    resultView.setBackgroundResource(R.drawable.hrb);
                    resultImg.setImageResource(R.drawable.hr);
                }

            }
            if(resultPrint.isEmpty()){
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(WeatherActivity.this);// 메세지
                alert_confirm.setMessage("날씨 정보를 불러오지 못합니다.\n"+"ERROR: " + response); // 확인 버튼 리스너
                alert_confirm.setPositiveButton("확인", null);// 다이얼로그 생성
                AlertDialog alert = alert_confirm.create();// 아이콘
                alert.setIcon(R.drawable.kwang);// 다이얼로그 타이틀
                alert.setTitle("오류 메세지");// 다이얼로그 보기
                alert.show();
            }

            Log.i("TAG,출력결과",resultPrint);

            Log.i("TAG,결과",",w: "+weather +" s: "+ sky +" t: "+ tmperature);


            resultView.setVisibility(View.VISIBLE);
            connectView.setVisibility(View.INVISIBLE);
            dateText.setText(realTime);
            locationText.setText(localName);
            tempText.setText(tmperature + "˚");
            //resultText.setText( resultPrint ) ;
            timeText.setText(nowTime);

            //테스트
            //resultImg.setImageResource(R.drawable.cu);
            //resultView.setBackgroundResource(R.drawable.cub);

        }


    }




    public void readExcel(String localName) {

        try {
            InputStream is = getBaseContext().getResources().getAssets().open("local_name.xls");
            Workbook wb = Workbook.getWorkbook(is);

            if (wb != null) {
                Sheet sheet = wb.getSheet(0);   // 시트 불러오기
                if (sheet != null) {
                    int colTotal = sheet.getColumns();    // 전체 컬럼
                    int rowIndexStart = 1;                  // row 인덱스 시작
                    int rowTotal = sheet.getColumn(colTotal - 1).length;

                    for (int row = rowIndexStart; row < rowTotal; row++) {
                        String contents = sheet.getCell(0, row).getContents();
                        if (contents.contains(localName)) {
                            x = sheet.getCell(1, row).getContents();
                            y = sheet.getCell(2, row).getContents();
                            row = rowTotal;
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.i("READ_EXCEL1", e.getMessage());
            e.printStackTrace();
        } catch (BiffException e) {
            Log.i("READ_EXCEL1", e.getMessage());
            e.printStackTrace();
        }
        // x, y = String형 전역변수
        Log.i("격자값", "x = " + x + "  y = " + y);
        if(x.isEmpty()||y.isEmpty()){
            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(WeatherActivity.this);// 메세지
            alert_confirm.setMessage("위치를 불러오는데 실패했습니다."); // 확인 버튼 리스너
            alert_confirm.setPositiveButton("확인", null);// 다이얼로그 생성
            AlertDialog alert = alert_confirm.create();// 아이콘
            alert.setIcon(R.drawable.kwang);// 다이얼로그 타이틀
            alert.setTitle("오류 메세지");// 다이얼로그 보기
            alert.show();
        }
    }



    private void exitProgram() {
        // 종료
        // 태스크를 백그라운드로 이동
        // moveTaskToBack(true);

        if (Build.VERSION.SDK_INT >= 21) {
            // 액티비티 종료 + 태스크 리스트에서 지우기
            finishAndRemoveTask();
        } else {
            // 액티비티 종료
            finish();
        }
        System.exit(0);
    }


}
