package com.program.healthhallym;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.program.healthhallym.entity.User;
import com.program.healthhallym.layout.EditLayout;
import com.program.healthhallym.util.Constants;
import com.program.healthhallym.util.GlobalVariable;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private LinearLayout layLoading;

    private EditLayout editNumber, editPassword;
    private TextView txtMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
        super.onCreate(savedInstanceState);

        // 툴바 안보이게 하기 위함
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        this.layLoading = findViewById(R.id.layLoading);
        ((ProgressBar) findViewById(R.id.progressBar)).setIndeterminateTintList(ColorStateList.valueOf(Color.WHITE));

        this.editNumber = new EditLayout(this, R.layout.layout_border_edit, "학번을 입력하세요.");
        this.editNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        ((LinearLayout) findViewById(R.id.layNumber)).addView(this.editNumber, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        this.editPassword = new EditLayout(this, R.layout.layout_border_edit, "비밀번호를 입력하세요.");
        this.editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        ((LinearLayout) findViewById(R.id.layPassword)).addView(this.editPassword, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        // 오류 표시
        this.txtMessage = findViewById(R.id.txtMessage);
        this.txtMessage.setText("");

        findViewById(R.id.btnLogin).setOnClickListener(mClickListener);
        findViewById(R.id.txtJoin).setOnClickListener(mClickListener);
        findViewById(R.id.layLoading).setOnClickListener(mClickListener);

        // 테스트를 편하게 위함
        this.editNumber.setText("123456789");
        this.editPassword.setText("123456");

        this.editNumber.requestFocus();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        finish();
        // 프로세스까지 강제 종료
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constants.RequestCode.JOIN:
                    // 회원가입 이후 로그인하기
                    this.editNumber.setText(data.getStringExtra("number"));
                    this.editPassword.setText(data.getStringExtra("password"));

                    this.txtMessage.setText("");

                    this.layLoading.setVisibility(View.VISIBLE);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 로그인
                            login();
                        }
                    }, Constants.LoadingDelay.SHORT);

                    break;
            }
        }
    }

    /* 입력 데이터 체크 */
    private boolean checkData() {
        // 학번 입력 체크
        String number = this.editNumber.getText();
        if (TextUtils.isEmpty(number)) {
            this.txtMessage.setText(getString(R.string.msg_number_check_empty));
            this.editNumber.requestFocus();
            return false;
        }

        // 비밀번호 입력 체크
        String password = this.editPassword.getText();
        if (TextUtils.isEmpty(password)) {
            this.txtMessage.setText(getString(R.string.msg_password_check_empty));
            this.editPassword.requestFocus();
            return false;
        }

        this.editPassword.hideKeyboard();
        this.txtMessage.setText("");

        return true;
    }

    /* 로그인 */
    private void login() {
        final String number = this.editNumber.getText();
        final String password = this.editPassword.getText();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference reference = db.collection(Constants.FirestoreCollectionName.USER);

        // 로그인
        Query query = reference.whereEqualTo("number", number)
                .whereEqualTo("password", password).limit(1);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                layLoading.setVisibility(View.GONE);

                if (task.isSuccessful()) {
                    if (task.getResult().size() == 0) {
                        // 로그인 실패
                        txtMessage.setText(getString(R.string.msg_login_failure));
                    } else {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());

                            // Document Id 저장
                            GlobalVariable.documentId = document.getId();

                            // 사용자 객체 생성
                            GlobalVariable.user = document.toObject(User.class);

                            // 메인 화면으로 이동
                            goMain();

                            break;
                        }
                    }
                } else {
                    // 오류
                    txtMessage.setText(getString(R.string.msg_error));
                }
            }
        });
    }

    /* 메인화면으로 이동 */
    private void goMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        finish();
    }

    /* 회원가입화면으로 이동 */
    private void goJoin() {
        Intent intent = new Intent(this, JoinActivity.class);
        startActivityForResult(intent, Constants.RequestCode.JOIN);
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnLogin:
                    // 로그인
                    if (checkData()) {
                        layLoading.setVisibility(View.VISIBLE);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 로그인
                                login();
                            }
                        }, Constants.LoadingDelay.SHORT);
                    }

                    break;
                case R.id.txtJoin:
                    // 회원가입 화면으로 이동
                    goJoin();

                    break;
                case R.id.layLoading:
                    break;
            }
        }
    };
}
