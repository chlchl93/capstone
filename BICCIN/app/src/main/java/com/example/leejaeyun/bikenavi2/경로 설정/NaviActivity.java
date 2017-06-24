package com.example.leejaeyun.bikenavi2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Looper;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.leejaeyun.bikenavi2.Arduino.FallbackException;
import com.example.leejaeyun.bikenavi2.Arduino.MyDialogFragment;
import com.example.leejaeyun.bikenavi2.Retrofit.AccidentThread;
import com.example.leejaeyun.bikenavi2.Retrofit.LivingThread;
import com.example.leejaeyun.bikenavi2.Retrofit.WeatherThread;
import com.example.leejaeyun.bikenavi2.Tmap.AccidentValue;
import com.example.leejaeyun.bikenavi2.Tmap.GpsInfo;
import com.example.leejaeyun.bikenavi2.Tmap.RoughRoadValue;
import com.example.leejaeyun.bikenavi2.Tmap.TmapPointArr;
import com.example.leejaeyun.bikenavi2.findroad.CalDistance;
import com.example.leejaeyun.bikenavi2.findroad.FindRoad;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skp.Tmap.TMapCircle;
import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapGpsManager;
import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapView;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Lee Jae Yun on 2017-05-09.
 */

public class NaviActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {

    String TAG = getClass().getSimpleName();
    public static Context mContext;
    private TMapGpsManager gps = null;
    private GpsInfo gpsInfo;
    private TMapPoint startPoint;
    private static TMapPoint arrivePoint;
    private Vibrator vibe;
    MyApplication myApp;
    CalDistance calculUtil = new CalDistance();

    private final static int DEVICES_DIALOG = 1;
    private final static int ERROR_DIALOG = 2;
    private final static int QUIT_DIALOG = 3;
    private final int REQUEST_BLUETOOTH_ENABLE = 100;
    public static AppCompatActivity activity;
    static BluetoothAdapter mBluetoothAdapter;
    BluetoothSocketWrapper mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread = null;
    byte[] readBuffer;
    int readBufferPosition;
    boolean isConnected = false;
    volatile boolean stopWorker;
    public static boolean isConnectionError = false;

    private static int mMarkerID;
    private ArrayList<String> mArrayMarkerID = new ArrayList<String>();

    @Bind(R.id.tracking_on)
    FloatingActionButton fab_traking_on;
    @Bind(R.id.tracking_off)
    FloatingActionButton fab_traking_off;
    @Bind(R.id.bt_findfac_on)
    FloatingActionButton bt_findfac_on;
    @Bind(R.id.bt_findfac_off)
    FloatingActionButton bt_findfac_off;
    @Bind(R.id.main_weather_dust_grade)
    TextView dust_grade;
    @Bind(R.id.main_weather_dust_power)
    TextView dust_power;
    @Bind(R.id.main_weather_icon)
    ImageView weather_icon;
    @Bind(R.id.main_weather_temp)
    TextView temp;
    @Bind(R.id.main_weather_uv_grade)
    TextView uv_grade;
    @Bind(R.id.main_weather_uv_power)
    TextView uv_power;
    @Bind(R.id.main_weather_wind_spd)
    TextView wind_speed;
    @Bind(R.id.main_weather_wind_dir)
    ImageView wind_dir;
    @Bind(R.id.main_speed)
    TextView tv_speed;
    @Bind(R.id.main_totalDistance)
    TextView totalDist;

    ArrayList<TMapPoint> arr_tMapPoint;
    ArrayList<TMapCircle> arr_tMapCircle;
    private TmapPointArr tmapPointArr;
    private FrameLayout mapLayout;
    private TMapView mapView = null;


    private double latitude = 0; //위도
    private double longitude = 0; // 경도
    private String arrive; // 도착지 좌표

    private boolean m_bTrackingMode = false;
    View v;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    ;
    DatabaseReference myRef = database.getReference("Accident Spot");
    DatabaseReference myRef2 = database.getReference("RoughRoad Spot");
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    List<AccidentValue> mAccidentValue;
    List<RoughRoadValue> mRoughRoadValue;
    private TimerTask autoUpdate;
    private final Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home);
        init();
        tMapGps();
        tMapInit();
        weatherInit();
        updatespot(); //네비게이션 실행시, firebase로부터 최초 사고지역 정보들을 받아서 지도에 띄운다
        TimerSchedule(); //네비게이션 실행동안, 60초마다 자동으로 firebase에 추가된 사고지역 정보들을 받아서 지도에 갱신
        mContext = this;
        activity = this;
        myApp = (MyApplication) getApplicationContext();

        if (isConnected == false) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                ErrorDialog("This device is not implement Bluetooth.");
                return;
            }

            if (!mBluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE);
                return;
            } else {
                //2. 페어링 되어 있는 블루투스 장치들의 목록을 보여줍니다.
                //3. 목록에서 블루투스 장치를 선택하면 선택한 디바이스를 인자로 하여 doConnect 함수가 호출됩니다.
                DeviceDialog();
                isConnected = true;
            }

        }

        Handler handler = new AccidentReceiveHandler();
        Thread accidentThread = new AccidentThread(handler, NaviActivity.this);
        accidentThread.start();
        Button backbutton = (Button) findViewById(R.id.backButton);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        FloatingActionButton fab_nowLocation = (FloatingActionButton) findViewById(R.id.fab_nowLocation);
        fab_nowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    gpsInfo = new GpsInfo(NaviActivity.this);
                    if (gpsInfo.isGetLocation()) {
                        latitude = gpsInfo.getLatitude();
                        longitude = gpsInfo.getLongitude();
                        mapView.setLocationPoint(longitude, latitude);
                        mapView.setCenterPoint(longitude, latitude);
                    } else {
                        Toast.makeText(mContext, "GPS 연동 실패", Toast.LENGTH_SHORT).show();
                    }
                } catch (NullPointerException e) {
                    Log.e(TAG, "now_btnClick 오류");
                }
            }
        });

        FloatingActionButton fab_findroad = (FloatingActionButton) findViewById(R.id.fab_findroad);
        fab_findroad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NaviActivity.this, FindRoad.class);
                intent.putExtra("startPoint", startPoint.toString());
                startActivity(intent);
            }
        });

        FloatingActionButton fab_deletePath = (FloatingActionButton) findViewById(R.id.fab_deletePath);
        fab_deletePath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapView.removeTMapPath();
                arrivePoint = null;
                Toast.makeText(NaviActivity.this, "경로 취소", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void init() {
        mContext = getApplicationContext();
        mapLayout = (FrameLayout) findViewById(R.id.mapLayout);
        arr_tMapPoint = new ArrayList<TMapPoint>();
        arr_tMapCircle = new ArrayList<TMapCircle>();
        mAccidentValue = new ArrayList<>();
        mRoughRoadValue = new ArrayList<>();
        ButterKnife.bind(this);
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void tMapGps() {
        gps = new TMapGpsManager(NaviActivity.this);
        gps.setMinTime(500);
        gps.setMinDistance(3);
        gps.setProvider(gps.NETWORK_PROVIDER);
        gps.setProvider(gps.GPS_PROVIDER);
        gps.OpenGps();
    }

    private void tMapInit() {
        startPoint = new TMapPoint(latitude, longitude);
        gpsInfo = new GpsInfo(NaviActivity.this);
        if (gpsInfo.isGetLocation()) { // 현재 위치 받아오기
            latitude = gpsInfo.getLatitude();
            longitude = gpsInfo.getLongitude();
            startPoint = new TMapPoint(latitude, longitude);
        }
        mapView = new TMapView(mContext); // 지도 위도, 경도, 줌레벨
        mapView.setSKPMapApiKey(getString(R.string.skplanet_key));
        mapView.setTrackingMode(m_bTrackingMode); // 트래킹 모드 사용
        mapView.setZoomLevel(16);
        mapView.setBicycleInfo(true);//자전거 도로 표시
        mapView.setBicycleFacilityInfo(true);//자전거 시설물 표시
        mapView.setIconVisibility(true); // 현재 위치 표시하는지 여부
        mapView.setLocationPoint(longitude, latitude); // 지도 현재 좌표 설정
        mapView.setCenterPoint(longitude, latitude); // 지도 현재 위치로
        mapView.setTMapLogoPosition(TMapView.TMapLogoPositon.POSITION_BOTTOMLEFT);

        mapLayout.addView(mapView);
    }

    private void weatherInit() {
        Handler weatherHandler = new WeatherReceiveHandler();
        //날씨 정보 가져오기
        Thread weatherThread = new WeatherThread(weatherHandler, NaviActivity.this, latitude, longitude);
        weatherThread.start();
        //미세먼지, 자외선 가져오기
        Thread livingThread = new LivingThread(weatherHandler, NaviActivity.this, latitude, longitude);
        livingThread.start();
    }

    @Override
    public void onLocationChange(Location location) {
        double distance;
        if (location != null) {
            tv_speed.setText(Double.parseDouble(String.format("%.3f", location.getSpeed())) + "km");
        }

        if (m_bTrackingMode) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            mapView.setLocationPoint(longitude, latitude);
            mapView.setIconVisibility(true);
            mapView.setCenterPoint(longitude, latitude);

            distance = calculUtil.calDistance(latitude, longitude);
            double distance2 = (Double.parseDouble(String.format("%.2f", distance / 1000f)));
            myApp.settotalDistance(myApp.gettotalDistance() + distance2);

            totalDist.setText(Double.parseDouble(String.format("%.2f", myApp.gettotalDistance())) + "km");

            /*빨간 원 접근시 알림*/
            if (tmapPointArr != null) {
                for (int i = 0; i < tmapPointArr.gettMapPointArr().size(); i++) {
                    TMapPoint tMapPoint = tmapPointArr.gettMapPointArr().get(i);
                    if ((tMapPoint.getLatitude() - 0.0005000 < latitude && tMapPoint.getLatitude() + 0.0005000 > latitude)
                            && (tMapPoint.getLongitude() - 0.0005000 < longitude && tMapPoint.getLongitude() + 0.0005000 > longitude)) {
                        Log.e(TAG, "호출");
                        vibe.vibrate(new long[]{0, 500, 200, 500}, -1);
                    }
                }
            }
            /*파란 원 접근시 알림*/
            if (mAccidentValue != null) {
                for (int i = 0; i < mAccidentValue.size(); i++) {
                    AccidentValue accidentpoint = mAccidentValue.get(i);
                    if ((accidentpoint.getLatitude() - 0.0005000 < latitude && accidentpoint.getLatitude() + 0.0005000 > latitude)
                            && (accidentpoint.getLongitude() - 0.0005000 < longitude && accidentpoint.getLongitude() + 0.0005000 > longitude)) {
                        Log.e(TAG, "호출");
                        vibe.vibrate(new long[]{0, 500, 200, 500}, -1);
                    }
                }
            }
        }
    }

    //트래킹 모드 토글
    @OnClick(R.id.tracking_on)
    void tracking_on_Click(View view) {
        m_bTrackingMode = false;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String finishDate = df.format(c.getTime());
        myApp.setfinishDate(finishDate);
        mapView.setTrackingMode(m_bTrackingMode); // 트래킹 모드 미사용
        Toast.makeText(NaviActivity.this, "트래킹 꺼짐", Toast.LENGTH_SHORT).show();
        fab_traking_on.setVisibility(View.INVISIBLE);
        fab_traking_off.setVisibility(View.VISIBLE);
        DatabaseReference myRef = database.getReference("users").child(user.getUid()).child("distance").child(finishDate);
        Hashtable<String, String> distance = new Hashtable<String, String>();
        distance.put("startTime", myApp.getStartDate());
        distance.put("endTime", myApp.getfinishDate());
        distance.put("distance", Double.toString(myApp.gettotalDistance()));
        myRef.setValue(distance);
        myApp.settotalDistance(0.0);
    }

    @OnClick(R.id.tracking_off)
    void tracking_off_Click(View view) {
        m_bTrackingMode = true;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startDate = df.format(c.getTime());
        myApp.setStartDate(startDate);
        mapView.setTrackingMode(m_bTrackingMode); // 트래킹 모드 사용
        Toast.makeText(NaviActivity.this, "트래킹 켜짐", Toast.LENGTH_SHORT).show();
        fab_traking_on.setVisibility(View.VISIBLE);
        fab_traking_off.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.bt_findfac_off)
    void fab_nearby_Click(View view) {
        Toast.makeText(NaviActivity.this, "주변탐색 켜짐", Toast.LENGTH_SHORT).show();
        bt_findfac_on.setVisibility(View.VISIBLE);
        bt_findfac_off.setVisibility(View.INVISIBLE);
        mapView.setZoomLevel(18);
        getAroundBizpoi();
    }

    @OnClick(R.id.bt_findfac_on)
    void fab_nearby_Click_off(View view) {
        Toast.makeText(NaviActivity.this, "주변탐색 꺼짐", Toast.LENGTH_SHORT).show();
        bt_findfac_on.setVisibility(View.INVISIBLE);
        bt_findfac_off.setVisibility(View.VISIBLE);
        mapView.removeAllMarkerItem();
        mapView.setZoomLevel(16);
    }


    public void TimerSchedule() {
        autoUpdate = new TimerTask() {
            @Override
            public void run() {
                Log.i("Test", "Timer start");
                Update();
            }
        };
        Timer timer = new Timer();
        timer.schedule(autoUpdate, 0, 60000);
    }

    protected void Update() {
        Runnable updater = new Runnable() {
            public void run() {
                AutoUpdate();
            }
        };
        handler.post(updater);
    }

    public void updatespot() {
        //네비게이션 실행시에 사고지점을 지도에 표시
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                Log.d(TAG, "Value is: " + value);
                int i = 0;
                for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                    i++;
                    String value2 = dataSnapshot2.getValue().toString();
                    Log.d(TAG, "Value is: " + value2);
                    AccidentValue accident = dataSnapshot2.getValue(AccidentValue.class);
                    mAccidentValue.add(accident);
                    TMapPoint Accidentpoint = new TMapPoint(accident.getLatitude(), accident.getLongitude());
                    try {
                        mapView.addTMapCircle("Accidentcircle" + i, setAcciCircle(Accidentpoint)); //지도에 파란원을 표시함
                    } catch (NullPointerException e) {
                        Log.e(TAG, "TmapCircle error :" + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                Log.d(TAG, "Value is: " + value);
                int i = 0;
                for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                    i++;
                    String value2 = dataSnapshot2.getValue().toString();
                    Log.d(TAG, "Value is: " + value2);
                    RoughRoadValue roughroad = dataSnapshot2.getValue(RoughRoadValue.class);
                    mRoughRoadValue.add(roughroad);
                    TMapPoint RoughRoadpoint = new TMapPoint(roughroad.getLatitude(), roughroad.getLongitude());
                    try {
                        mapView.addTMapCircle("RoughRoadcircle" + i, setRoughCircle(RoughRoadpoint)); //지도에 파란원을 표시함
                    } catch (NullPointerException e) {
                        Log.e(TAG, "TmapCircle error :" + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void AutoUpdate() {
        //60초마다 사고지점을 자동으로 표시
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                Log.d(TAG, "Value is: " + value);
                int i = 0;
                int dataValue = 0;
                for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                    dataValue++;
                    if (mAccidentValue.size() < dataValue) {
                        i++;
                        String value2 = dataSnapshot2.getValue().toString();
                        Log.d(TAG, "Value is: " + value2);
                        AccidentValue accident = dataSnapshot2.getValue(AccidentValue.class);
                        TMapPoint Accidentpoint = new TMapPoint(accident.getLatitude(), accident.getLongitude());
                        try {
                            mapView.addTMapCircle("Accidentcircle" + i, setAcciCircle(Accidentpoint)); //지도에 파란원을 표시함
                        } catch (NullPointerException e) {
                            Log.e(TAG, "TmapCircle error :" + e.getMessage());
                        }
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                Log.d(TAG, "Value is: " + value);
                int i = 0;
                int dataValue = 0;
                for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                    dataValue++;
                    if (mRoughRoadValue.size() < dataValue) {
                        i++;
                        String value2 = dataSnapshot2.getValue().toString();
                        Log.d(TAG, "Value is: " + value2);
                        RoughRoadValue roughroad = dataSnapshot2.getValue(RoughRoadValue.class);
                        TMapPoint RoughRoadpoint = new TMapPoint(roughroad.getLatitude(), roughroad.getLongitude());
                        try {
                            mapView.addTMapCircle("RoughRoadcircle" + i, setRoughCircle(RoughRoadpoint)); //지도에 파란원을 표시함
                        } catch (NullPointerException e) {
                            Log.e(TAG, "TmapCircle error :" + e.getMessage());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


    }


    public void AccidentUpdate() {
        //사고 발생시 firebase에 사고지점 업데이트
        if (user != null) {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = df.format(c.getTime());
            DatabaseReference myRef = database.getReference("Accident Spot").child(formattedDate);
            latitude = gpsInfo.getLatitude();
            longitude = gpsInfo.getLongitude();
            Hashtable<String, Double> gps = new Hashtable<String, Double>();
            gps.put("latitude", latitude);
            gps.put("longitude", longitude);
            myRef.setValue(gps);
            SmsSender(latitude, longitude);
        }
        AutoUpdate();
    }

    public void RoughRoadUpdate() {
        //거친도로의 위치를 firebase에 업데이트
        if (user != null) {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = df.format(c.getTime());
            DatabaseReference myRef = database.getReference("RoughRoad Spot").child(formattedDate);
            latitude = gpsInfo.getLatitude();
            longitude = gpsInfo.getLongitude();
            Hashtable<String, Double> gps = new Hashtable<String, Double>();
            gps.put("latitude", latitude);
            gps.put("longitude", longitude);
            myRef.setValue(gps);
        }
        AutoUpdate();
    }

    private TMapCircle setAcciCircle(TMapPoint Accidentpoint) {
        //사고지점 청색원표시 설정
        TMapCircle tempcircle2 = new TMapCircle();
        tempcircle2.setCenterPoint(Accidentpoint);
        tempcircle2.setRadius(25);
        tempcircle2.setAreaColor(Color.BLUE);
        tempcircle2.setAreaAlpha(35);
        return tempcircle2;
    }

    private TMapCircle setRoughCircle(TMapPoint RoughRoadpoint) {
        //사고지점 녹색원표시 설정
        TMapCircle tempcircle2 = new TMapCircle();
        tempcircle2.setCenterPoint(RoughRoadpoint);
        tempcircle2.setRadius(25);
        tempcircle2.setAreaColor(Color.GREEN);
        tempcircle2.setAreaAlpha(35);
        return tempcircle2;
    }

    public void SmsSender(double latitude, double longitude) {
        final String txtMessage = " http://maps.google.com/?q=" + latitude + "," + longitude;
        DatabaseReference myRef3 = database.getReference("users").child(user.getUid()).child("number");
        myRef3.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String PhoneNumber = dataSnapshot.getValue().toString();
                String message = txtMessage;
                if (PhoneNumber.length() > 0 && message.length() > 0)
                    sendSMS(PhoneNumber, message);
                else
                    Toast.makeText(getBaseContext(),
                            "Please enter both phone number and message.",
                            Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void sendSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }

    public void getAroundBizpoi() {
        TMapData tmapdata = new TMapData();
        TMapPoint point = mapView.getCenterPoint();
        tmapdata.findAroundNamePOI(point, "편의점;병원", 1, 99,
                new TMapData.FindAroundNamePOIListenerCallback() {
                    @Override
                    public void onFindAroundNamePOI(ArrayList<TMapPOIItem> poiItem) {
                        for (int i = 0; i < poiItem.size(); i++) {
                            TMapPOIItem item = poiItem.get(i);

                            String temp[] = item.getPOIPoint().toString().split(" ");
                            double[] latitude;
                            latitude = new double[poiItem.size()];
                            latitude[i] = Double.parseDouble(temp[1]);
                            double[] longitude;
                            longitude = new double[poiItem.size()];
                            longitude[i] = Double.parseDouble(temp[3]);

                            Log.d("편의시설 ", "POI Name: " + item.getPOIName() + "," + "Address: " + item.getPOIAddress().replace("null", "") + "Point: " + item.getPOIPoint().toString() + " " + latitude[i] + " " + longitude[i]);

                            showMarkerPoint(item.getPOIName(), latitude[i], longitude[i]);

                        }
                    }
                });
    }

    public void showMarkerPoint(String name, double a, double b) {

        TMapPoint point = new TMapPoint(a, b);
        TMapMarkerItem item1 = new TMapMarkerItem();
        Bitmap bitmap = null;

        bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.map_pin_red);

        item1.setTMapPoint(point);
        item1.setName(name);
        item1.setVisible(item1.VISIBLE);
        item1.setIcon(bitmap);

        item1.setCalloutTitle(name);
        item1.setCanShowCallout(true);
        item1.setAutoCalloutVisible(true);

        String strID = String.format("pmarker%d", mMarkerID++);

        mapView.addMarkerItem(strID, item1);
        mArrayMarkerID.add(strID);

    }

    static public Set<BluetoothDevice> getPairedDevices() {
        return mBluetoothAdapter.getBondedDevices();
    }

    @Override
    public void onBackPressed() {
        doClose();
        super.onBackPressed();
    }

    //백버튼이 눌러지거나, ConnectTask에서 예외발생시
    //데이터 수신을 위한 스레드를 종료시키고 CloseTask를 실행하여 입출력 스트림을 닫고,
    //소켓을 닫아 통신을 종료한다.
    public void doClose() {
        if (workerThread != null) workerThread.interrupt();
        new CloseTask().execute();
        Log.e("BT", "close socket");
    }

    public void doConnect(BluetoothDevice device) {
        mmDevice = device;

        //Standard SerialPortService ID
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        try {
            // 4. 지정한 블루투스 장치에 대한 특정 UUID 서비스를 하기 위한 소켓을 생성
            // 여기선 시리얼 통신을 위한 UUID를 지정
            BluetoothSocket tmp;
            tmp = mmDevice.createRfcommSocketToServiceRecord(uuid);

            mmSocket = new NativeBluetoothSocket(tmp);

            // 블루투스 장치 검색을 중단합니다.
            mBluetoothAdapter.cancelDiscovery();

            // ConnectTask를 시작합니다.
            new ConnectTask().execute();

        } catch (IOException e) {
            Log.e("BT", e.toString(), e);
            ErrorDialog("Connect? " + e.toString());
        }
    }


    public void DeviceDialog() {
        if (activity.isFinishing()) return;

        FragmentManager fm = NaviActivity.this.getSupportFragmentManager();
        MyDialogFragment alertDialog = MyDialogFragment.newInstance(DEVICES_DIALOG, "");
        alertDialog.show(fm, "");
    }


    public void ErrorDialog(String text) {
        if (activity.isFinishing()) return;

        FragmentManager fm = NaviActivity.this.getSupportFragmentManager();
        MyDialogFragment alertDialog = MyDialogFragment.newInstance(ERROR_DIALOG, text);
        alertDialog.show(fm, "");
    }

    public void QuitDialog(String text) {
        if (activity.isFinishing()) return;

        FragmentManager fm = NaviActivity.this.getSupportFragmentManager();
        MyDialogFragment alertDialog = MyDialogFragment.newInstance(QUIT_DIALOG, text);
        alertDialog.show(fm, "");
    }

    public void beginListenForData() {
        final Handler handler = new Handler(Looper.getMainLooper());

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == '\n') {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, 0, encodedBytes.length);
                                    Log.d(TAG, "getArriveInfo:" + data + ":");

                                    readBufferPosition = 0;
                                    handler.post(new Runnable() {
                                        public void run() {
                                            if (data.length() > 7) {
                                                //사고발생시 정보 수신
                                                Toast.makeText(NaviActivity.this, "사고발생", Toast.LENGTH_SHORT).show();
                                                AccidentUpdate();
                                            } else
                                                //노면상태 정보 수신
                                                RoughRoadUpdate();
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }

                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });
        workerThread.start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_BLUETOOTH_ENABLE) {
            if (resultCode == RESULT_OK) {
                DeviceDialog();
            }
            if (resultCode == RESULT_CANCELED) {
                QuitDialog("You need to enable bluetooth");
            }
        }
    }


    public void getArriveInfo() {
        try {
            Intent intent = getIntent();
            try {
                arrive = intent.getStringExtra("arrivePoint");
                String temp[] = arrive.split(" ");
                double latitude = Double.parseDouble(temp[1]);
                double longitude = Double.parseDouble(temp[3]);
                arrivePoint = new TMapPoint(latitude, longitude); // 도착지 포인트
            } catch (Exception e) {
                Log.e(TAG, "get arrivePoint error");
            }
        } catch (Exception e) {
            Log.d(TAG, "getArriveInfo error");
        }
    }


    private List<TMapCircle> setCircle(ArrayList<TMapPoint> tMapPointArr) {
        for (int i = 0; i < tMapPointArr.size(); i++) {
            TMapPoint tempPoint = tMapPointArr.get(i);
            TMapCircle tempcircle = new TMapCircle();
            tempcircle.setCenterPoint(tempPoint);
            tempcircle.setRadius(70);
            tempcircle.setAreaColor(Color.rgb(255, 0, 0));
            tempcircle.setAreaAlpha(60);
            arr_tMapCircle.add(tempcircle);
        }
        return arr_tMapCircle;
    }


    @Override
    public void onStart() {
        super.onStart();
        getArriveInfo();
        if (arrivePoint != null) {
            setPath();
        }
    }

    /**
     * setPath
     * 도착지 정보가 있다면 맵에 경로를 그려준다
     */
    private void setPath() {
        try {
            TMapData tmapdata = new TMapData();

            if (arrivePoint != null) { //출발지와 도착지 결과를 받아 경로를 그려줌
                tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, startPoint, arrivePoint, new TMapData.FindPathDataListenerCallback() {
                    @Override
                    public void onFindPathData(TMapPolyLine tMapPolyLine) {
                        mapView.addTMapPath(tMapPolyLine);
                    }
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "setPath error");
        }
    }

    private class AccidentReceiveHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tmapPointArr = (TmapPointArr) msg.getData().getSerializable("THREAD"); // 스레드에서 tMapPointArr를 받음
            try {
                setCircle(tmapPointArr.gettMapPointArr()); //setcircle 함수
                for (int i = 0; i < arr_tMapCircle.size(); i++) {
                    mapView.addTMapCircle("circle" + i, arr_tMapCircle.get(i)); //지도에 빨간원들을 표시함
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "TmapCircle error :" + e.getMessage());
            }
        }
    }

    /**
     * 날씨 데이터를 받아와 set함
     */
    private class WeatherReceiveHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.getData().getString("dust") != null) {
                int dust = Integer.parseInt(Weather.getInstance().getDust_value());
                dust_power.setText(Weather.getInstance().getDust_value());
                dust_grade.setText(Weather.getInstance().getDust_grade());
                if (dust < 30) {
                    dust_grade.setTextColor(mContext.getResources().getColor(R.color.blue));
                } else if (dust >= 30 && dust <= 60) {
                    dust_grade.setTextColor(mContext.getResources().getColor(R.color.green));
                } else if (dust > 60) {
                    dust_grade.setTextColor(mContext.getResources().getColor(R.color.orange));
                }
            } else if (msg.getData().getString("weather") != null) {
                temp.setText(Weather.getInstance().getTemperature() + " \u00b0");
                wind_speed.setText(Weather.getInstance().getWind_speed() + "m/s"); // 풍속
                int icon_id = mContext.getResources().getIdentifier(Weather.getInstance().getWeatherIcon(), "drawable", "com.example.leejaeyun.bikenavi2");
                weather_icon.setBackground(mContext.getResources().getDrawable(icon_id)); // 날씨 아이콘

            } else if (msg.getData().getString("uv") != null) {
                int uv = 0;
                try {
                    uv = Integer.parseInt(Weather.getInstance().getUv());
                } catch (Exception e) {
                }
                uv_power.setText(Weather.getInstance().getUv());
                if (uv < 30) {
                    uv_grade.setText("좋음");
                    uv_grade.setTextColor(mContext.getResources().getColor(R.color.blue));
                } else if (uv >= 30 && uv <= 60) {
                    uv_grade.setText("보통");
                    uv_grade.setTextColor(mContext.getResources().getColor(R.color.green));
                } else if (uv > 60) {
                    uv_grade.setText("나쁨");
                    uv_grade.setTextColor(mContext.getResources().getColor(R.color.orange));
                }
            }
        }
    }


    public class ConnectTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                //블루투스 장치로 연결을 시도합니다.
                mmSocket.connect();

                //소켓에 대한 입출력 스트림을 가져옵니다.
                mmOutputStream = mmSocket.getOutputStream();
                mmInputStream = mmSocket.getInputStream();

                //데이터 수신을 대기하기 위한 스레드를 생성하여 입력스트림로부터의 데이터를 대기하다가
                //들어오기 시작하면 버퍼에 저장합니다.
                //'\n' 문자가 들어오면 지금까지 버퍼에 저장한 데이터를 UI에 출력하기 위해 핸들러를 사용합니다.
                beginListenForData();

            } catch (Throwable t) {
                Log.e("BT", "connect? " + t.getMessage());
                try {
                    mmSocket = new FallbackBluetoothSocket(mmSocket.getUnderlyingSocket());
                    Thread.sleep(500);

                    //재접속을 시도합니다.
                    mmSocket.connect();

                    //소켓에 대한 입출력 스트림을 가져옵니다.
                    mmOutputStream = mmSocket.getOutputStream();
                    mmInputStream = mmSocket.getInputStream();

                    //데이터 수신을 대기하기 위한 스레드를 생성하여 입력스트림로부터의 데이터를 대기하다가
                    //들어오기 시작하면 버퍼에 저장합니다.
                    //'\n' 문자가 들어오면 지금까지 버퍼에 저장한 데이터를 UI에 출력하기 위해 핸들러를 사용합니다.
                    beginListenForData();

                    return null;
                } catch (FallbackException e1) {
                    Log.e("BT", "Could not initialize FallbackBluetoothSocket classes.", e1);
                    return false;
                } catch (InterruptedException e1) {
                    Log.e("BT", e1.getMessage(), e1);
                    return false;
                } catch (IOException e1) {
                    //재접속 실패한 경우...
                    Log.e("BT", "Fallback failed. Cancelling.", e1);
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSucess) {
            //블루투스 통신이 연결되었음을 화면에 출력합니다.
            if (isSucess == true) {
                Toast.makeText(NaviActivity.this, "블루투스 연결 성공", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("BT", "Failed to connect to device");
                isConnectionError = true;
                ErrorDialog("Failed to connect to device");
            }
        }
    }


    public class CloseTask extends AsyncTask<Void, Void, Object> {
        @Override
        protected Object doInBackground(Void... params) {
            try {
                try {
                    mmOutputStream.close();
                } catch (Throwable t) {/*ignore*/}
                try {
                    mmInputStream.close();
                } catch (Throwable t) {/*ignore*/}
                mmSocket.close();
            } catch (Throwable t) {
                return t;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result instanceof Throwable) {
                Log.e("BT", result.toString(), (Throwable) result);
                ErrorDialog(result.toString());
            }
        }
    }

    public interface BluetoothSocketWrapper {
        InputStream getInputStream() throws IOException;

        OutputStream getOutputStream() throws IOException;

        String getRemoteDeviceName();

        void connect() throws IOException;

        String getRemoteDeviceAddress();

        void close() throws IOException;

        BluetoothSocket getUnderlyingSocket();
    }

    public class NativeBluetoothSocket implements BluetoothSocketWrapper {

        private BluetoothSocket socket;

        public NativeBluetoothSocket(BluetoothSocket tmp) {
            this.socket = tmp;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return socket.getInputStream();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return socket.getOutputStream();
        }

        @Override
        public String getRemoteDeviceName() {
            return socket.getRemoteDevice().getName();
        }

        @Override
        public void connect() throws IOException {
            socket.connect();
        }

        @Override
        public String getRemoteDeviceAddress() {
            return socket.getRemoteDevice().getAddress();
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }

        @Override
        public BluetoothSocket getUnderlyingSocket() {
            return socket;
        }

    }

    public class FallbackBluetoothSocket extends NativeBluetoothSocket {

        private BluetoothSocket fallbackSocket;

        public FallbackBluetoothSocket(BluetoothSocket tmp) throws FallbackException {
            super(tmp);
            try {
                Class<?> clazz = tmp.getRemoteDevice().getClass();
                Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[]{Integer.valueOf(1)};
                fallbackSocket = (BluetoothSocket) m.invoke(tmp.getRemoteDevice(), params);
            } catch (Exception e) {
                throw new FallbackException(e);
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return fallbackSocket.getInputStream();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return fallbackSocket.getOutputStream();
        }

        @Override
        public void connect() throws IOException {
            fallbackSocket.connect();
        }

        @Override
        public void close() throws IOException {
            fallbackSocket.close();
        }
    }


}
