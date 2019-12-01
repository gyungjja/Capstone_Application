package com.program.healthhallym;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.program.healthhallym.entity.Point;
import com.program.healthhallym.entity.Trail;
import com.program.healthhallym.entity.Walk;
import com.program.healthhallym.util.Constants;
import com.program.healthhallym.util.GlobalVariable;
import com.program.healthhallym.util.SharedPreferencesUtils;
import com.program.healthhallym.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity  extends AppCompatActivity implements OnMapReadyCallback {
    private static String TAG = MapActivity.class.getSimpleName();

    private GoogleMap googleMap;
    private Marker currentMarker;
    private Location currentLocation;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;

    private Timer timer;
    private TimerTask timerTask;

    private LinearLayout layLoading;

    private TextView txtDistance, txtTime, txtCalorie;
    private Button btnToggle;

    // 첫 현재위치 설정 여부
    private boolean first = false;

    // 디폴트 위도/경도
    private double latitude, longitude;

    // 산책로 코드
    private String trailCode;

    // start 여부
    private boolean started = false;

    private double distance;            // 거리
    private long time1;                 // 시작일시
    private long time2;                 // 완료일시
    private double calorie;             // 칼로리소모

    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // 화면 켜진 상태 유지하기
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 현재 위치 (위도/경도) / 산책로 코드
        Intent intent = getIntent();
        this.latitude = intent.getDoubleExtra("latitude", 0);
        this.longitude = intent.getDoubleExtra("longitude", 0);
        this.trailCode = intent.getStringExtra("trail_code");

        String trailName = "산책로";
        for (Trail trail : GlobalVariable.trails) {
            if (trail.code.equals(trailCode)) {
                trailName = trail.name;
                break;
            }
        }

        // 제목 표시
        setTitle(trailName);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.layLoading = findViewById(R.id.layLoading);
        ((ProgressBar) findViewById(R.id.progressBar)).setIndeterminateTintList(ColorStateList.valueOf(Color.WHITE));

        this.txtDistance = findViewById(R.id.txtDistance);
        this.txtTime = findViewById(R.id.txtTime);
        this.txtCalorie = findViewById(R.id.txtCalorie);

        this.btnToggle = findViewById(R.id.btnToggle);
        this.btnToggle.setOnClickListener(mClickListener);
        findViewById(R.id.layLoading).setOnClickListener(mClickListener);

        this.txtDistance.setText("");
        this.txtTime.setText("");
        this.txtCalorie.setText("");

        this.locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(this.locationRequest);

        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // TimerTask stop
        stopTimerTask();
    }

    @Override
    protected void onStart() {
        super.onStart();

        this.fusedLocationClient.requestLocationUpdates(this.locationRequest, mLocationCallback, null);

        if (this.googleMap!=null) {
            this.googleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (this.location != null) {
            // 현재 위치정보 설정
            SharedPreferencesUtils preferencesUtils = SharedPreferencesUtils.getInstance(this, Constants.CACHE_NAME);

            preferencesUtils.put("latitude", this.location.getLatitude());
            preferencesUtils.put("longitude", this.location.getLongitude());
        }

        if (this.fusedLocationClient != null) {
            this.fusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constants.RequestCode.GPS_ENABLE:
                    // 사용자가 GPS 활성 시켰는지 검사
                    if (checkLocationServicesStatus()) {
                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음");
                        btnToggle.setEnabled(true);
                        return;
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;

        // GPS 활성 요청 대화상자 보이기전에
        // 최초 위치가 없으면 지도의 초기위치를 서울로 이동
        setDefaultLocation(this.latitude, this.longitude);

        // 산책로 위치 표시
        setTailLocation(this.trailCode);

       startLocationUpdates();

        this.googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        this.googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                Log.d( TAG, "onMapClick :");
            }
        });
    }

    private void startLocationUpdates() {
        // GPS 사용 여부 상태 체크
        if (!checkLocationServicesStatus()) {
            btnToggle.setEnabled(false);

            // GPS 활성화 하기
            showDialogForLocationServiceSetting();
        }else {
            this.fusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());

            this.googleMap.setMyLocationEnabled(true);
        }
    }

    /* GPS 사용여부 체크 */
    private boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /* 현재 위치 설정 */
    public void setCurrentLocation(Location location, String markerTitle) {
        if (this. currentMarker != null) {
            this.currentMarker.remove();
        }

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        // 마커 생성
        this.currentMarker = createMarker(currentLatLng, markerTitle, "", BitmapDescriptorFactory.HUE_CYAN);
        // 마크 타이틀 항상 표시
        this.currentMarker.showInfoWindow();

        // 첫번째 현재 위치 표시 또는 start 된 상태이면 카메라 이동
        if (!first || started) {
            this.first = true;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
            this.googleMap.moveCamera(cameraUpdate);
        }
    }

    /* 디폴트 위치 */
    private void setDefaultLocation(double latitude, double longitude) {
        LatLng defaultLocation;

        // 디폴트
        if (latitude == 0 && longitude == 0) {
            // 서울
            defaultLocation = new LatLng(37.56, 126.97);
        } else {
            defaultLocation = new LatLng(latitude, longitude);
        }
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";

        if (this.currentMarker != null) {
            this.currentMarker.remove();
        }

        // 마커 생성
        this.currentMarker = createMarker(defaultLocation, markerTitle, markerSnippet, BitmapDescriptorFactory.HUE_CYAN);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(defaultLocation, 15);
        this.googleMap.moveCamera(cameraUpdate);
    }

    /* 산책로 위치 */
    private void setTailLocation(String tailCode) {
        Trail trail = null;
        for (Trail t : GlobalVariable.trails) {
            if (t.code.equals(tailCode)) {
                trail = t;
                break;
            }
        }

        if (trail == null) {
            return;
        }

        // 위도 경도 얻기
        Point point1 = Utils.getGpsFromAddress(this, trail.address1);
        LatLng tailLocation1 = new LatLng(point1.latitude, point1.longitude);

        // 시작점 마커 생성
        createMarker(tailLocation1, trail.name, "시작점", BitmapDescriptorFactory.HUE_RED);

        // 위도 경도 얻기
        Point point2 = Utils.getGpsFromAddress(this, trail.address2);
        LatLng tailLocation2 = new LatLng(point2.latitude, point2.longitude);

        // 끝점 마커 생성
        createMarker(tailLocation2, trail.name, "끝점", BitmapDescriptorFactory.HUE_RED);
    }

    /* 마커 생성 */
    private Marker createMarker(LatLng latLng, String markerTitle, String markerSnippet, float icon) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(markerTitle);
        if (!TextUtils.isEmpty(markerSnippet)) {
            markerOptions.snippet(markerSnippet);
        }
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(icon));

        return this.googleMap.addMarker(markerOptions);
    }

    /* GPS 활성화 */
    private void showDialogForLocationServiceSetting() {
        new AlertDialog.Builder(this)
                .setTitle("위치 서비스")
                .setMessage("앱을 사용하기 위해 위치 서비스가 필요합니다.\n위치 설정을 하시겠습니까?")
                .setCancelable(true)
                .setPositiveButton("설정", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(callGPSSettingIntent, Constants.RequestCode.GPS_ENABLE);
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    /* START 하기 */
    private void start() {
        if (location == null) {
            Toast.makeText(this, getString(R.string.msg_gps_disable), Toast.LENGTH_SHORT).show();
            return;
        }

        this.txtDistance.setText("0m");
        this.txtTime.setText("0초");
        this.txtCalorie.setText("0Kcal");

        this.distance = 0;
        this.time1 = Calendar.getInstance().getTimeInMillis();
        this.time2 = 0;
        this.calorie = 0;

        this.currentLocation = null;

        this.started = true;
        this.btnToggle.setText("STOP");

        // Timer 시작
        startTimer();
    }

    /* STOP 하기 */
    private void stop() {
        this.started = false;
        this.btnToggle.setText("START");

        // Timer 종료
        stopTimerTask();

        this.time2 = Calendar.getInstance().getTimeInMillis();

        this.layLoading.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 걷기 정보 저장
                saveWalk();
            }
        }, Constants.LoadingDelay.SHORT);
    }

    /* 걷기 정보 저장 */
    private void saveWalk() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 산책일
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(this.time1));

        // 걷기정보
        final Walk walk = new Walk(this.trailCode, dateFormat.format(calendar.getTime()),
                this.time1, this.time2, this.distance, this.calorie);

        db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId)
                .collection(Constants.FirestoreCollectionName.WALK)
                .add(walk)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // 성공
                        layLoading.setVisibility(View.GONE);

                        // 메인 Activity 에 전달
                        setResult(Activity.RESULT_OK);

                        Toast.makeText(MapActivity.this, getString(R.string.msg_walk_save_success), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 실패
                        layLoading.setVisibility(View.GONE);
                        Toast.makeText(MapActivity.this, getString(R.string.msg_error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /* Timer 시작 */
    private void startTimer() {
        this.timer = new Timer();

        // TimerTask 초기화
        initTimerTask();

        // 1초마다 체크
        this.timer.schedule(timerTask, 1000, 1000);
    }

    /* TimerTask 초기화 */
    private void initTimerTask() {
        this.timerTask = new TimerTask() {
            @Override
            public void run() {
                // TextView 를 컨트롤 하기 위해 Main Thread 가 필요함
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        long diff = Calendar.getInstance().getTimeInMillis() - time1;
                        // 시간 표시
                        txtTime.setText(Utils.getDisplayTime(diff));
                    }
                });
            }
        };
    }

    /* TimerTask stop */
    private void stopTimerTask() {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);

                if (started && currentLocation != null) {
                    // 거리 계산
                    double d = currentLocation.distanceTo(location);
                    Log.d(TAG, "distance:" + d);

                    // 1m 미만이면 제외
                    if (d < 1) {
                        return;
                    }

                    distance += d;
                    txtDistance.setText(Utils.getDistanceStr(distance));

                    // 칼로리 소모 (몸무게 * 거리 = 칼로리소모)
                    calorie = GlobalVariable.user.getWeight() * distance;
                    txtCalorie.setText(Utils.getCalorieStr(calorie));
                }

                //LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                // 현재 주소 얻기
                //String markerTitle = Utils.getAddressFromGps(MapActivity.this, currentPosition.latitude, currentPosition.longitude);
                //String markerSnippet = "위도:" + location.getLatitude() + " 경도:" + location.getLongitude();

                //Log.d(TAG, "onLocationResult : " + markerSnippet);

                // 현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, "나");
                currentLocation = location;
            }
        }
    };

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnToggle:
                    if (started) {
                        // STOP 하기
                        stop();
                    } else {
                        // START 하기
                        start();
                    }
                    break;
                case R.id.layLoading:
                    break;
            }
        }
    };
}
