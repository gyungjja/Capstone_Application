package com.program.healthhallym;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.program.healthhallym.entity.User;
import com.program.healthhallym.layout.EditLayout;
import com.program.healthhallym.util.Constants;
import com.program.healthhallym.util.GlobalVariable;
import com.program.healthhallym.util.Utils;

import java.util.Calendar;

public class JoinActivity extends AppCompatActivity {
    private static final String TAG = JoinActivity.class.getSimpleName();

    private LinearLayout layLoading;

    private EditLayout editNumber, editName, editPhone, editHeight, editWeight, editPassword;
    private TextView txtMessage;

    // 성별
    private String gender;

    // 비밀번호 최소 자리수
    private final static int PASSWORD_MIN_SIZE = 6;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
        super.onCreate(savedInstanceState);

        // 툴바 안보이게 하기 위함
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_join);

        this.layLoading = findViewById(R.id.layLoading);
        ((ProgressBar) findViewById(R.id.progressBar)).setIndeterminateTintList(ColorStateList.valueOf(Color.WHITE));

        this.editNumber = new EditLayout(this, R.layout.layout_border_edit, "학번을 입력하세요.");
        this.editNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        ((LinearLayout) findViewById(R.id.layNumber)).addView(this.editNumber, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        this.editName = new EditLayout(this, R.layout.layout_border_edit, "이름을 입력하세요.");
        this.editName.setInputType(InputType.TYPE_CLASS_TEXT);
        ((LinearLayout) findViewById(R.id.layName)).addView(this.editName, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        this.editPhone = new EditLayout(this, R.layout.layout_border_edit, "휴대번호를 입력하세요.");
        this.editPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        ((LinearLayout) findViewById(R.id.layPhone)).addView(this.editPhone, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        this.editHeight = new EditLayout(this, R.layout.layout_border_edit, "키 입력");
        this.editHeight.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        ((LinearLayout) findViewById(R.id.layHeight)).addView(this.editHeight, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        this.editWeight = new EditLayout(this, R.layout.layout_border_edit, "몸무게 입력");
        this.editWeight.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        ((LinearLayout) findViewById(R.id.layWeight)).addView(this.editWeight, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        this.editPassword = new EditLayout(this, R.layout.layout_border_edit, "6글자 이상 입력하세요.");
        this.editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        ((LinearLayout) findViewById(R.id.layPassword)).addView(this.editPassword, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        // 남자를 기본값으로 설정
        this.gender = Constants.Gender.MALE;
        ((RadioGroup) findViewById(R.id.rdgGender)).setOnCheckedChangeListener(mCheckedChangeListener);
        ((RadioButton) findViewById(R.id.rdM)).setChecked(true);

        // 오류 표시
        this.txtMessage = findViewById(R.id.txtMessage);
        this.txtMessage.setText("");

        findViewById(R.id.btnJoin).setOnClickListener(mClickListener);
        findViewById(R.id.layLoading).setOnClickListener(mClickListener);

        this.editPhone.setText(Utils.getPhoneNumber(this));
        this.editNumber.requestFocus();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
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

        // 휴대번호 입력 체크
        String phone = this.editPhone.getText();
        if (TextUtils.isEmpty(phone)) {
            this.txtMessage.setText(getString(R.string.msg_phone_number_check_empty));
            this.editPhone.requestFocus();
            return false;
        }

        // 휴대번호 유효성 체크
        if (!Utils.isPhoneNumber(phone)) {
            this.txtMessage.setText(getString(R.string.msg_phone_number_check_wrong));
            this.editPhone.requestFocus();
            return false;
        }

        // 이름 입력 체크
        String name = this.editName.getText();
        if (TextUtils.isEmpty(name)) {
            this.txtMessage.setText(getString(R.string.msg_user_name_check_empty));
            this.editName.requestFocus();
            return false;
        }

        // 키 입력 체크
        String height = this.editHeight.getText();
        if (TextUtils.isEmpty(height)) {
            this.txtMessage.setText(getString(R.string.msg_user_height_check_empty));
            this.editHeight.requestFocus();
            return false;
        }

        if (!Utils.isNumeric(height)) {
            this.txtMessage.setText(getString(R.string.msg_user_height_check_wrong));
            this.editHeight.requestFocus();
            return false;
        }

        // 몸무게 입력 체크
        String weight = this.editWeight.getText();
        if (TextUtils.isEmpty(weight)) {
            this.txtMessage.setText(getString(R.string.msg_user_weight_check_empty));
            this.editWeight.requestFocus();
            return false;
        }

        if (!Utils.isNumeric(weight)) {
            this.txtMessage.setText(getString(R.string.msg_user_weight_check_wrong));
            this.editWeight.requestFocus();
            return false;
        }

        // 비밀번호 입력 체크
        String password = this.editPassword.getText();
        if (TextUtils.isEmpty(password)) {
            this.txtMessage.setText(getString(R.string.msg_password_check_empty));
            this.editPassword.requestFocus();
            return false;
        }

        // 비밀번호 자리수 체크
        if (password.length() < PASSWORD_MIN_SIZE) {
            this.txtMessage.setText(getString(R.string.msg_password_check_length));
            this.editPassword.requestFocus();
            return false;
        }

        this.editPassword.hideKeyboard();
        this.txtMessage.setText("");

        return true;
    }

    /* 회원가입 */
    private void join() {
        final String number = this.editNumber.getText();
        final String name = this.editName.getText();
        final String phone = this.editPhone.getText();
        final String height = this.editHeight.getText();
        final String weight = this.editWeight.getText();
        final String password = this.editPassword.getText();

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference reference = db.collection(Constants.FirestoreCollectionName.USER);

        // 학번 중복 체크
        Query query = reference.whereEqualTo("number", number);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().size() == 0) {
                        // 회원가입

                        // 자동 문서 ID 값 생성 (컬렉션에 add 하면 document 가 자동 생성됨)
                        final User user = new User(number, name, phone, gender, Float.parseFloat(height), Float.parseFloat(weight),
                                password, Calendar.getInstance().getTimeInMillis(), 0);

                        db.collection(Constants.FirestoreCollectionName.USER)
                                .add(user)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        // 성공

                                        // Document Id 저장
                                        GlobalVariable.documentId = documentReference.getId();

                                        // 사용자 객체 생성
                                        GlobalVariable.user = user;

                                        layLoading.setVisibility(View.GONE);

                                        // 로그인 Activity 에 전달
                                        Intent intent = new Intent();
                                        intent.putExtra("number", user.getNumber());
                                        intent.putExtra("password", user.getPassword());
                                        setResult(Activity.RESULT_OK, intent);

                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // 실패
                                        layLoading.setVisibility(View.GONE);
                                        txtMessage.setText(getString(R.string.msg_error));
                                    }
                                });
                    } else {
                        // 중복
                        layLoading.setVisibility(View.GONE);
                        txtMessage.setText(getString(R.string.msg_number_check_overlap));
                    }
                } else {
                    // 오류
                    layLoading.setVisibility(View.GONE);
                    txtMessage.setText(getString(R.string.msg_error));
                }
            }
        });
    }

    private RadioGroup.OnCheckedChangeListener mCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
            switch (checkedId) {
                case R.id.rdM:
                    // 남자
                    gender = Constants.Gender.MALE;
                    break;
                case R.id.rdF:
                    // 여자
                    gender = Constants.Gender.FEMALE;
                    break;
            }
        }
    };

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnJoin:
                    // 회원가입
                    if (checkData()) {
                        layLoading.setVisibility(View.VISIBLE);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 학번 중복체크 후 가입
                                join();
                            }
                        }, Constants.LoadingDelay.SHORT);
                    }

                    break;
                case R.id.layLoading:
                    break;
            }
        }
    };
}
