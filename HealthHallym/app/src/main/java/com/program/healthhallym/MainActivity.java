package com.program.healthhallym;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.program.healthhallym.fragment.MainFragment;
import com.program.healthhallym.fragment.abstracts.IFragment;
import com.program.healthhallym.util.Constants;
import com.program.healthhallym.util.SharedPreferencesUtils;

public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getSimpleName();

    private BackPressHandler backPressHandler;

    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // 액션바에 홈버튼 표시 여부
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 액션바에 제목 표시
        getSupportActionBar().setTitle(getString(R.string.activity_title_main));

        // 종료 핸들러
        this.backPressHandler = new BackPressHandler(this);

        this.fragment = new MainFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.layContent, this.fragment).commit();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }

    @Override
    public void onBackPressed() {
        this.backPressHandler.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if(result != null) {
                if(result.getContents() == null) {
                    Toast.makeText(this, getString(R.string.msg_qr_code_scan_failure), Toast.LENGTH_LONG).show();
                } else {
                    // QR 코드 인식 성공
                    String code = result.getContents();

                    // 산책로 화면으로 이동
                    goTrail(code);
                }
                return;
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constants.RequestCode.SETTING:
                    // 설정
                    ((IFragment) fragment).task(Constants.FragmentTaskKind.SETTING, null);
                    break;
                case Constants.RequestCode.MAP:
                    // 산책로 지도에서 저장
                    ((IFragment) fragment).task(Constants.FragmentTaskKind.WALK_SAVE, null);
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case R.id.menu_setting:
                // 설정
                intent = new Intent(this, SettingActivity.class);
                startActivityForResult(intent, Constants.RequestCode.SETTING);
                return true;
            case R.id.menu_history:
                // 내역
                intent = new Intent(this, WalkHistoryActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /* 산책로 화면으로 이동 */
    private void goTrail(String trailCode) {
        SharedPreferencesUtils preferencesUtils = SharedPreferencesUtils.getInstance(this, Constants.CACHE_NAME);

        // 최근 설정된 나의 위치정보
        double latitude = preferencesUtils.get(Constants.SharedPreferencesName.LATITUDE, 0.0);
        double longitude = preferencesUtils.get(Constants.SharedPreferencesName.LONGITUDE, 0.0);

        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("trail_code", trailCode);
        startActivityForResult(intent, Constants.RequestCode.MAP);
    }

    /* 종료 */
    public void end() {
        moveTaskToBack(true);
        finish();
        // 프로세스까지 강제 종료
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /* Back Press Class */
    public class BackPressHandler {
        private Context context;
        private Toast toast;

        private final long FINISH_INTERVAL_TIME = 2000;
        private long backPressedTime = 0;

        public BackPressHandler(Context context) {
            this.context = context;
        }

        public void onBackPressed() {
            if (System.currentTimeMillis() > this.backPressedTime + FINISH_INTERVAL_TIME) {
                this.backPressedTime = System.currentTimeMillis();

                this.toast = Toast.makeText(this.context, R.string.msg_back_press_end, Toast.LENGTH_SHORT);
                this.toast.show();

                return;
            }

            if (System.currentTimeMillis() <= this.backPressedTime + FINISH_INTERVAL_TIME) {
                // 종료
                end();
                this.toast.cancel();
            }
        }
    }
}
