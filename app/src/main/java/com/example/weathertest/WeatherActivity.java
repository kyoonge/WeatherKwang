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



        readExcel(localName); //??????
        //xylocationText.setText("?????????( x y ) : "+ x + " "+y);
        //String rDateTime[] = getRealDateTime(); //??????



        Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            public void run() {

                CopyDatabaseAsyncTask task = new CopyDatabaseAsyncTask(WeatherActivity.this) ;
                task.execute(rDateTime[0],rDateTime[1],rDateTime[2],x,y,localName,rDateTime[4]) ;
            }
        }, 800);  // 2000??? 2?????? ???????????????.


        //Handler handler = new Handler();
        handler2.postDelayed(new Runnable() {
            public void run() {

            exitProgram();

        }
    }, 60000);  // 1?????? ??????




    }



    private class CopyDatabaseAsyncTask extends AsyncTask<String, Long, Boolean> {



        public CopyDatabaseAsyncTask(Context context) {

        }

        String weather = "", tmperature = "", sky = "", realTime = "", nowTime="", localName = "", resultPrint = "", response = "";
        private String nx = "55";	//??????
        private String ny = "127";	//??????
        private String numOfRows = "30";	//?????? ???
        private String pageNo = "1";	//??????
        private String baseDate = "20220111";	//?????????????????? ??????
        private String baseTime = "1400";	//?????????????????? ??????
        private String type = "json";	//???????????? ?????? type(json, xml ??? ??????)


        @Override
        protected void onPreExecute() {
            //??????????????? ???????????? ???????????? ???, ?????? ???????????? ?????? ???????????? ????????? ( ???????????? ???????????? )
            //?????? UI

        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected Boolean doInBackground(String... params) {
            //AssetManager am = mContext.getResources().getAssets() ;

            //		??????????????? ?????? url??????
            String apiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst";
//         ?????????????????? ?????? ???
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
                urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode(numOfRows, "UTF-8")); //?????? ???
                urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode(pageNo, "UTF-8")); //????????? ???
                urlBuilder.append("&" + URLEncoder.encode("dataType","UTF-8") + "=" + URLEncoder.encode(type, "UTF-8"));	/* ?????? */
                urlBuilder.append("&" + URLEncoder.encode("base_date","UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8")); /* ?????????????????? ??????*/
                urlBuilder.append("&" + URLEncoder.encode("base_time","UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8")); /* ?????????????????? ?????? AM 02????????? 3?????? ?????? */
                urlBuilder.append("&" + URLEncoder.encode("nx","UTF-8") + "=" + URLEncoder.encode(nx, "UTF-8")); //??????
                urlBuilder.append("&" + URLEncoder.encode("ny","UTF-8") + "=" + URLEncoder.encode(ny, "UTF-8")); //??????
                Log.i("TAG, url Msg",urlBuilder.toString());
                /*
                 * GET???????????? ???????????? ???????????? ????????????
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

                //=======??? ?????? ????????? json?????? ????????? ????????? ?????? ????????????=====//

                // response ?????? ????????? ???????????? ??????
                JSONObject jsonObj_1 = new JSONObject(result);
                response = jsonObj_1.getString("response");
                Log.i("TAG,RESPONSE",response);


                // response ??? ?????? body ??????
                JSONObject jsonObj_2 = new JSONObject(response);
                String body = jsonObj_2.getString("body");

                // body ??? ?????? items ??????
                JSONObject jsonObj_3 = new JSONObject(body);
                String items = jsonObj_3.getString("items");
                Log.i("TAG,ITEMS",items);

                // items??? ?????? itemlist ??? ??????
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
//                            sky = "?????? ?????? ";
//                        }else if(fcstValue.equals("2")) {
//                            sky = "?????? ?????? ?????? ";
//                        }else if(fcstValue.equals("3")) {
//                            sky = "?????? ?????? ";
//                        }else if(fcstValue.equals("4")) {
//                            sky = "???????????? ????????? ";
//                        }else if(fcstValue.equals("0")) {
//                            sky = "????????? ";
//                        }
//                    }//Log.i("TAG,sky",sky);
//
//                    if(category.equals("SKY")&&weather.equals("")){
//                        if(fcstValue.equals("1")) {
//                            weather = "?????? ";
//                        }else if(fcstValue.equals("3")) {
//                            weather = "????????? ?????? ";
//                        }else if(fcstValue.equals("4")) {
//                            weather = "????????? ";
//                        }
//                    }//Log.i("TAG,weather",weather);
//
//                    if(category.equals("T1H")&&tmperature.equals("")){
//                        tmperature = fcstValue+"??";
//                    }//Log.i("TAG,tmperature",tmperature);
//
////                    if(category.equals("POP")){
////                        rainpercent = " \n????????????: "+fcstValue+"%";
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
                resultPrint = "?????????!";
            }
            else if(weather.equals("2")){
                resultPrint = "?????? ??? ?????????!";
            }
            else if(weather.equals("3")){
                resultPrint = "?????????!!";
            }
            else if(weather.equals("4")){
                resultPrint = "???????????????..!";
            }
            else if(weather.equals("0")){
                if(sky.equals("1")){
                    if(Integer.parseInt(tmperature)>=20){
                        resultPrint = "?????????..!";
                    }
                    else if(Integer.parseInt(tmperature) <=-3){
                        resultPrint = "?????????..!";
                        resultView.setBackgroundResource(R.drawable.cub);
                        resultImg.setImageResource(R.drawable.cu);
                    }
                    else{
                        resultPrint = "????????????>3<";
                        resultView.setBackgroundResource(R.drawable.mub);
                        resultImg.setImageResource(R.drawable.mu);
                    }
                }
                else if(sky.equals("3")){
                    resultPrint = "????????? ?????????!";

                }
                else if(sky.equals("4")){
                    resultPrint = "?????????!";
                    resultView.setBackgroundResource(R.drawable.hrb);
                    resultImg.setImageResource(R.drawable.hr);
                }

            }
            if(resultPrint.isEmpty()){
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(WeatherActivity.this);// ?????????
                alert_confirm.setMessage("?????? ????????? ???????????? ????????????.\n"+"ERROR: " + response); // ?????? ?????? ?????????
                alert_confirm.setPositiveButton("??????", null);// ??????????????? ??????
                AlertDialog alert = alert_confirm.create();// ?????????
                alert.setIcon(R.drawable.kwang);// ??????????????? ?????????
                alert.setTitle("?????? ?????????");// ??????????????? ??????
                alert.show();
            }

            Log.i("TAG,????????????",resultPrint);

            Log.i("TAG,??????",",w: "+weather +" s: "+ sky +" t: "+ tmperature);


            resultView.setVisibility(View.VISIBLE);
            connectView.setVisibility(View.INVISIBLE);
            dateText.setText(realTime);
            locationText.setText(localName);
            tempText.setText(tmperature + "??");
            //resultText.setText( resultPrint ) ;
            timeText.setText(nowTime);

            //?????????
            //resultImg.setImageResource(R.drawable.cu);
            //resultView.setBackgroundResource(R.drawable.cub);

        }


    }




    public void readExcel(String localName) {

        try {
            InputStream is = getBaseContext().getResources().getAssets().open("local_name.xls");
            Workbook wb = Workbook.getWorkbook(is);

            if (wb != null) {
                Sheet sheet = wb.getSheet(0);   // ?????? ????????????
                if (sheet != null) {
                    int colTotal = sheet.getColumns();    // ?????? ??????
                    int rowIndexStart = 1;                  // row ????????? ??????
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
        // x, y = String??? ????????????
        Log.i("?????????", "x = " + x + "  y = " + y);
        if(x.isEmpty()||y.isEmpty()){
            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(WeatherActivity.this);// ?????????
            alert_confirm.setMessage("????????? ??????????????? ??????????????????."); // ?????? ?????? ?????????
            alert_confirm.setPositiveButton("??????", null);// ??????????????? ??????
            AlertDialog alert = alert_confirm.create();// ?????????
            alert.setIcon(R.drawable.kwang);// ??????????????? ?????????
            alert.setTitle("?????? ?????????");// ??????????????? ??????
            alert.show();
        }
    }



    private void exitProgram() {
        // ??????
        // ???????????? ?????????????????? ??????
        // moveTaskToBack(true);

        if (Build.VERSION.SDK_INT >= 21) {
            // ???????????? ?????? + ????????? ??????????????? ?????????
            finishAndRemoveTask();
        } else {
            // ???????????? ??????
            finish();
        }
        System.exit(0);
    }


}
