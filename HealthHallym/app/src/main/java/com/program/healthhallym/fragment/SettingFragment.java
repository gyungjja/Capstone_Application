package com.program.healthhallym.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.program.healthhallym.MyEditActivity;
import com.program.healthhallym.R;
import com.program.healthhallym.SettingActivity;
import com.program.healthhallym.layout.EditLayout;
import com.program.healthhallym.util.Constants;
import com.program.healthhallym.util.GlobalVariable;
import com.program.healthhallym.util.Utils;

public class SettingFragment extends Fragment {
    private static String TAG = SettingFragment.class.getSimpleName();

    private View viewMain;

    private LinearLayout layLoading;

    private EditLayout editGoal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.viewMain = inflater.inflate(R.layout.fragment_setting, container, false);

        this.layLoading = this.viewMain.findViewById(R.id.layLoading);
        ((ProgressBar) this.viewMain.findViewById(R.id.progressBar)).setIndeterminateTintList(ColorStateList.valueOf(Color.WHITE));

        this.editGoal = new EditLayout(getContext(), R.layout.layout_border_edit, "목표치를 입력하세요.");
        this.editGoal.setInputType(InputType.TYPE_CLASS_NUMBER);
        ((LinearLayout) this.viewMain.findViewById(R.id.layGoal)).addView(this.editGoal, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        this.viewMain.findViewById(R.id.btnGoalApply).setOnClickListener(mClickListener);
        this.viewMain.findViewById(R.id.imgMyEdit).setOnClickListener(mClickListener);
        this.viewMain.findViewById(R.id.layLoading).setOnClickListener(mClickListener);

        // 초기화
        init();

        return this.viewMain;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constants.RequestCode.EDIT:
                    // 수정

                    // 나의 정보
                    displayMyInfo();

                    // 메인 Activity 에 전달
                    getActivity().setResult(Activity.RESULT_OK);
                    break;
            }
        }
    }

    /* 초기화 */
    private void init() {
        // 목표치
        this.editGoal.setText(String.valueOf(GlobalVariable.user.getGoal()));

        // 나의 정보
        displayMyInfo();
    }

    /* 나의 정보 */
    private void displayMyInfo() {
        ((TextView) this.viewMain.findViewById(R.id.txtNumber)).setText(GlobalVariable.user.getNumber());
        ((TextView) this.viewMain.findViewById(R.id.txtName)).setText(GlobalVariable.user.getName());
        ((TextView) this.viewMain.findViewById(R.id.txtPhone)).setText(GlobalVariable.user.getPhone());
        ((TextView) this.viewMain.findViewById(R.id.txtHeight)).setText(GlobalVariable.user.getHeight() +"cm");
        ((TextView) this.viewMain.findViewById(R.id.txtWeight)).setText(GlobalVariable.user.getWeight() + "kg");

        if (GlobalVariable.user.getGender().equals(Constants.Gender.MALE)) {
            ((TextView) this.viewMain.findViewById(R.id.txtGender)).setText("남");
        } else {
            ((TextView) this.viewMain.findViewById(R.id.txtGender)).setText("여");
        }
    }

    /* 입력 데이터 체크 */
    private boolean checkData() {
        //  목표치 입력 체크
        String goal = this.editGoal.getText();
        if (TextUtils.isEmpty(goal)) {
            Toast.makeText(getContext(), getString(R.string.msg_goal_check_empty), Toast.LENGTH_SHORT).show();
            this.editGoal.requestFocus();
            return false;
        }

        if (!Utils.isNumeric(goal)) {
            Toast.makeText(getContext(), getString(R.string.msg_goal_check_wrong), Toast.LENGTH_SHORT).show();
            this.editGoal.requestFocus();
            return false;
        }

        this.editGoal.hideKeyboard();

        return true;
    }

    /* 목표치 적용 */
    private void applyGoal() {
        final int goal = Integer.parseInt(this.editGoal.getText());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference reference = db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId);
        reference.update("goal", goal)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 성공
                        layLoading.setVisibility(View.GONE);

                        GlobalVariable.user.setGoal(goal);

                        // 메인 Activity 에 전달
                        getActivity().setResult(Activity.RESULT_OK);
                        getActivity().finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 실패
                        layLoading.setVisibility(View.GONE);
                        Toast.makeText(getContext(), getString(R.string.msg_error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnGoalApply:
                    // 목표치 적용
                    if (checkData()) {
                        layLoading.setVisibility(View.VISIBLE);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 적용
                                applyGoal();
                            }
                        }, Constants.LoadingDelay.SHORT);
                    }
                    break;
                case R.id.imgMyEdit:
                    // 나의 정보 수정 화면으로
                    Intent intent = new Intent(getActivity(), MyEditActivity.class);
                    startActivityForResult(intent, Constants.RequestCode.EDIT);
                    break;
                case R.id.layLoading:
                    break;
            }
        }
    };
}
