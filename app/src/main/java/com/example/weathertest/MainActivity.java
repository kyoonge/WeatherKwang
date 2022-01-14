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
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

    private Button btn;
    private TextView resultText, locationText, xylocationText;
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

        btn = findViewById(R.id.btn);
        resultText = (TextView) findViewById(R.id.resultText);
        locationText = (TextView) findViewById(R.id.location);
        xylocationText = (TextView) findViewById(R.id.xylocation);


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {
                    // fetch data
                    //Toast.makeText(this,"네트워크 연결중입니다.", Toast.LENGTH_SHORT).show();
                    gpsTracker = new GpsTracker(MainActivity.this);

                    double latitude = gpsTracker.getLatitude();
                    double longitude = gpsTracker.getLongitude();

                    String address = getCurrentAddress(latitude, longitude);
                    locationText.setText(address);

                    String[] local = address.split(" ");
                    String localName = local[2];

                    readExcel(localName);

                    xylocationText.setText("격자값( x y ) : "+ x + " "+y);
                    String rDateTime[] = getRealDateTime();
                    CopyDatabaseAsyncTask task = new CopyDatabaseAsyncTask(MainActivity.this) ;
                    task.execute(rDateTime[0],rDateTime[1],rDateTime[2],x,y) ;

                } else {
                    // display error
                    AlertDialog.Builder alert_confirm = new AlertDialog.Builder(MainActivity.this);
                    // 메세지
                    alert_confirm.setMessage("인터넷 연결이 필요해요\n( Wi-Fi 또는 데이터를 켜주세요! )");
                    // 확인 버튼 리스너
                    alert_confirm.setPositiveButton("확인", null);
                    // 다이얼로그 생성
                    AlertDialog alert = alert_confirm.create();

                    // 아이콘
                    alert.setIcon(R.drawable.kwang);
                    // 다이얼로그 타이틀
                    alert.setTitle("잠깐만요!");
                    // 다이얼로그 보기
                    alert.show();
                }




            }
        });

    }



    private class CopyDatabaseAsyncTask extends AsyncTask<String, Long, Boolean> {



        public CopyDatabaseAsyncTask(Context context) {

        }

        String weather = "", tmperature = "", rainpercent = "",  realTime = "";
        private String nx = "55";	//위도
        private String ny = "127";	//경도
        private String numOfRows = "10";	//정보 수
        private String pageNo = "1";	//경도
        private String baseDate = "20220111";	//조회하고싶은 날짜
        private String baseTime = "1400";	//조회하고싶은 시간
        private String type = "json";	//조회하고 싶은 type(json, xml 중 고름)


        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected Boolean doInBackground(String... params) {
            //AssetManager am = mContext.getResources().getAssets() ;

            //		참고문서에 있는 url주소
            String apiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
//         홈페이지에서 받은 키
            String serviceKey = "Pc3mj0ODAsPJ1UTZ1BGByalWMKn%2BtZs9ye8MJ2mCTrZLTfOyZf1te7QKxcATs%2Bm5qGLpX8dLbwL8dhRLfRaxFw%3D%3D";

            try {
                baseDate = params[0];
                realTime = params[1];
                baseTime = params[2];
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
                    //Log.i("TAG1",fcstValue);
                    String category = jsonObj_4.getString("category");
                    //Log.i("TAG2",fcstValue);

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

                    if(category.equals("TMP") || category.equals("T1H")){
                        tmperature = " 기온은 "+fcstValue+"℃ 입니다.";
                    }


                    Log.i("TAG",weather + tmperature);
                }

            } catch (Exception e) {
                e.printStackTrace() ;
            }

            return true;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            resultText.setText( "현재시간은 "+ realTime + " " + weather + " " + tmperature ) ;
        }


    }



    public String[] getRealDateTime(){

        String[] RealDateTime = new String[3]; // {  날짜, 실시간, 가공시간 }
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd", Locale.KOREAN);
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH00", Locale.KOREAN);
        SimpleDateFormat sdf3 = new SimpleDateFormat("HH:mm", Locale.KOREAN);
        RealDateTime[0] = sdf1.format(System.currentTimeMillis());
        RealDateTime[1] = sdf3.format(System.currentTimeMillis());
        RealDateTime[2] = sdf2.format(System.currentTimeMillis());
        Log.i("TAG.hh00",RealDateTime[1]);

        switch(RealDateTime[2]) {

            case "0200":
            case "0300":
            case "0400":
                RealDateTime[2] = "0200";
                break;
            case "0500":
            case "0600":
            case "0700":
                RealDateTime[2] = "0500";
                break;
            case "0800":
            case "0900":
            case "1000":
                RealDateTime[2] = "0800";
                break;
            case "1100":
            case "1200":
            case "1300":
                RealDateTime[2] = "1100";
                break;
            case "1400":
            case "1500":
            case "1600":
                RealDateTime[2] = "1400";
                break;
            case "1700":
            case "1800":
            case "1900":
                RealDateTime[2] = "1700";
                break;
            case "2000":
            case "2100":
            case "2200":
                RealDateTime[2] = "2000";
                break;
            default:
                RealDateTime[2] = "2300";

        }
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
    }




}
