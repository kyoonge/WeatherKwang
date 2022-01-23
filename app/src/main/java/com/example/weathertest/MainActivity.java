package com.example.weathertest;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;


public class MainActivity extends AppCompatActivity {

    //private Button btn;
    private ImageButton startBtn;
    private TextView resultText, locationText, xylocationText;
    private LinearLayout startView, connectView, resultView;
    private GpsTracker gpsTracker;
    private String x = "", y = "", address = "";

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        }else {
            checkRunTimePermission();
        }

//        //btn = findViewById(R.id.btn);
        startBtn = (ImageButton) findViewById(R.id.startBtn);
//        resultText = (TextView) findViewById(R.id.resultText);
//        locationText = (TextView) findViewById(R.id.location);
//        xylocationText = (TextView) findViewById(R.id.xylocation);
//        startView = (LinearLayout) findViewById(R.id.startView);
//        connectView = (LinearLayout) findViewById(R.id.connectView);
//        resultView = (LinearLayout) findViewById(R.id.resultView);

        String rDateTime[] = getRealDateTime(); //시간
        int intTime = Integer.parseInt(rDateTime[1].substring(0,2));
        if(7<=intTime&&19>intTime){
            startView.setBackgroundResource(R.drawable.morning);
        }
        //xylocationText.setText("시간 : "+ rDateTime[1] +" " +rDateTime[1].substring(0,2)+" "+ Integer.toString(intTime));


        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {
                    // fetch data
                    //Toast.makeText(this,"네트워크 연결중입니다.", Toast.LENGTH_SHORT).show();
                    startBtn.setImageResource(R.drawable.wake);

                    gpsTracker = new GpsTracker(MainActivity.this);
                    double latitude = gpsTracker.getLatitude();
                    double longitude = gpsTracker.getLongitude();
                    String address = getCurrentAddress(latitude, longitude);
                    String[] local = address.split(" ");
                    String localName = local[2];
                    //여기 잠깐 주소 마포구로 고정
                    //String localName = "마포구"; // 나중엔 삭제

                    Handler handler1 = new Handler();
                    handler1.postDelayed(new Runnable() {
                        public void run() {

                            Intent intent = new Intent(MainActivity.this,WeatherActivity.class);
                            intent.putExtra("rDateTime", rDateTime);
                            intent.putExtra("localName", localName);
                            startActivity(intent);

                        }
                    }, 300);  // 2000은 2초를 의미합니다.

                } else
                    {
                    // display error
                    AlertDialog.Builder alert_confirm = new AlertDialog.Builder(MainActivity.this);// 메세지
                    alert_confirm.setMessage("인터넷 연결이 필요해요\n( Wi-Fi 또는 데이터를 켜주세요! )"); // 확인 버튼 리스너
                    alert_confirm.setPositiveButton("확인", null);// 다이얼로그 생성
                    AlertDialog alert = alert_confirm.create();// 아이콘
                    alert.setIcon(R.drawable.kwang);// 다이얼로그 타이틀
                    alert.setTitle("잠깐만요!");// 다이얼로그 보기
                    alert.show();
                }



            }
        });

    }



    public String[] getRealDateTime(){

        String[] RealDateTime = new String[5]; // {  날짜, 출력할날짜, 가공시간, 가공분, 출력할시간 }
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd", Locale.KOREAN);
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH", Locale.KOREAN); //시간
        SimpleDateFormat sdf3 = new SimpleDateFormat("MM월 dd일", Locale.KOREAN);
        SimpleDateFormat sdf5 = new SimpleDateFormat("HH:mm", Locale.KOREAN);
        SimpleDateFormat sdf4 = new SimpleDateFormat("mm", Locale.KOREAN); //분

        RealDateTime[0] = sdf1.format(System.currentTimeMillis());
        RealDateTime[1] = sdf3.format(System.currentTimeMillis());
        RealDateTime[2] = sdf2.format(System.currentTimeMillis());
        RealDateTime[3] = sdf4.format(System.currentTimeMillis());
        RealDateTime[4] = sdf5.format(System.currentTimeMillis());
        if(Integer.parseInt(RealDateTime[3])>30){
            RealDateTime[3]="30";
        }else{
            RealDateTime[3]="30";

            if(RealDateTime[2].equals("0")){
                RealDateTime[2] = "23";
            }
            RealDateTime[2]=Integer.toString(Integer.parseInt(RealDateTime[2])-1);

        }
        RealDateTime[2] += RealDateTime[3];


        Log.i("TAG.hh00",RealDateTime[2]);
        Log.i("TAG.hh00 after",RealDateTime[2]);
        return RealDateTime;
    }


    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {

        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {

                //위치 값을 가져올 수 있음
                ;
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                } else {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }


    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음



        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }


    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }



}
