package com.program.healthhallym;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.program.healthhallym.data.TrailData;
import com.program.healthhallym.util.GlobalVariable;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class IntroActivity extends AppCompatActivity {
    private static String TAG = IntroActivity.class.getSimpleName();

    private static final int INTRO_DELAY = 2000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
        super.onCreate(savedInstanceState);

        // 툴바 안보이게 하기 위함
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_intro);

        ((TextView) findViewById(R.id.txtMessage)).setText("초기화중...");

        // 인트로 화면을 2초동안 보여주고 메인을 호출하기 위함
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 초기화
                IntroActivityPermissionsDispatcher.initWithPermissionCheck(IntroActivity.this);
            }
        }, INTRO_DELAY);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }

    @Override
    public void onBackPressed() {
        // 백키 눌려도 종료 안되게 하기 위함
        //super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        IntroActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    /* 초기화 */
    @NeedsPermission({Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION})
    void init() {
        // 산책로 데이터
        GlobalVariable.trails = TrailData.getInstance().getTrails();

        // 로그인화면으로 이동
        Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
        startActivity(intent);

        finish();
    }

    @OnShowRationale({Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION})
    void showRationale(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton("수락", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton("거절", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(getString(R.string.permission_rationale_app_use))
                .show();
    }

    @OnPermissionDenied({Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION})
    void showDenied() {
        Toast.makeText(this, getString(R.string.permission_rationale_app_use), Toast.LENGTH_LONG).show();
    }

    @OnNeverAskAgain({Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION})
    void showNeverAsk() {
        Toast.makeText(this, getString(R.string.permission_rationale_app_use), Toast.LENGTH_LONG).show();
    }
}
