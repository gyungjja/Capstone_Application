package com.program.healthhallym.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.program.healthhallym.MapActivity;
import com.program.healthhallym.R;
import com.program.healthhallym.entity.Walk;
import com.program.healthhallym.fragment.abstracts.IFragment;
import com.program.healthhallym.util.Constants;
import com.program.healthhallym.util.GlobalVariable;
import com.program.healthhallym.util.SharedPreferencesUtils;
import com.program.healthhallym.util.Utils;

import java.util.ArrayList;
import java.util.Collections;

public class MainFragment extends Fragment implements IFragment {
    private static final String TAG = MainFragment.class.getSimpleName();

    private LinearLayout layLoading;

    private ImageView imgIcon;
    private TextView txtName, txtNumber, txtBMI, txtGoal, txtRate;
    private TextView txtDistanceM, txtTimeM, txtCalorieM, txtDistanceT, txtTimeT, txtCalorieT;
    // 달성율 그래프 표시
    private ProgressBar progressBarRate;

    private double distanceM, distanceT;
    private long timeM, timeT;
    private double calorieM, calorieT;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        this.layLoading = view.findViewById(R.id.layLoading);
        ((ProgressBar) view.findViewById(R.id.progressBar)).setIndeterminateTintList(ColorStateList.valueOf(Color.WHITE));

        this.imgIcon = view.findViewById(R.id.imgIcon);

        this.txtName = view.findViewById(R.id.txtName);
        this.txtNumber = view.findViewById(R.id.txtNumber);
        this.txtBMI = view.findViewById(R.id.txtBMI);
        this.txtGoal = view.findViewById(R.id.txtGoal);
        this.txtRate = view.findViewById(R.id.txtRate);

        this.txtDistanceM = view.findViewById(R.id.txtDistanceM);
        this.txtTimeM = view.findViewById(R.id.txtTimeM);
        this.txtCalorieM = view.findViewById(R.id.txtCalorieM);
        this.txtDistanceT = view.findViewById(R.id.txtDistanceT);
        this.txtTimeT = view.findViewById(R.id.txtTimeT);
        this.txtCalorieT = view.findViewById(R.id.txtCalorieT);

        this.progressBarRate =  view.findViewById(R.id.progressBarRate);

        view.findViewById(R.id.layTrailSelect).setOnClickListener(mClickListener);
        view.findViewById(R.id.layLoading).setOnClickListener(mClickListener);

        view.post(new Runnable() {
            @Override
            public void run() {
                // 초기화
                init();
            }
        });

        return view;
    }

    @Override
    public void task(int kind, Bundle bundle) {
        switch (kind) {
            case Constants.FragmentTaskKind.SETTING:
                // 정보 표시
                displayInfo();

                // 달성률
                displayRate();
                break;
            case Constants.FragmentTaskKind.WALK_SAVE:
                // 걷기 저장
                this.layLoading.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 거리 시간 칼로리 구하기 (현재 월/일)
                        infoWalk();
                    }
                }, Constants.LoadingDelay.SHORT);
                break;
        }
    }

    /* 초기화 */
    private void init() {
        // 정보 표시
        displayInfo();

        this.txtDistanceM.setText("");
        this.txtTimeM.setText("");
        this.txtCalorieM.setText("");
        this.txtDistanceT.setText("");
        this.txtTimeT.setText("");
        this.txtCalorieT.setText("");

        this.layLoading.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 거리 시간 칼로리 구하기 (현재 월/일)
                infoWalk();
            }
        }, Constants.LoadingDelay.SHORT);
    }

    /* 정보 표시 */
    private void displayInfo() {
        if (GlobalVariable.user.getGender().equals(Constants.Gender.MALE)) {
            this.imgIcon.setImageResource(R.drawable.ic_account_circle_48_blue);
        } else {
            this.imgIcon.setImageResource(R.drawable.ic_account_circle_48_pink);
        }

        this.txtName.setText(GlobalVariable.user.getName());
        this.txtNumber.setText(GlobalVariable.user.getNumber());

        // BMI
        double bmi = GlobalVariable.user.getWeight() / Math.pow(GlobalVariable.user.getHeight() / 100.0, 2.0);
        this.txtBMI.setText(String.valueOf(Math.round(bmi * 10) / 10.0));

        // 목표치
        this.txtGoal.setText(GlobalVariable.user.getGoal() + "km");
        this.progressBarRate.setMax(GlobalVariable.user.getGoal() * 1000);
        this.progressBarRate.setProgress(0);

        if (GlobalVariable.user.getGoal() > 0) {
            this.progressBarRate.setVisibility(View.VISIBLE);
        } else {
            this.progressBarRate.setVisibility(View.INVISIBLE);
            this.txtRate.setText("");
        }
    }

    /* 달성률 */
    private void displayRate() {
        if (GlobalVariable.user.getGoal() == 0) {
            return;
        }

        double percent = (distanceM / (GlobalVariable.user.getGoal() * 1000)) * 100;
        String mark = (Math.round(percent * 10) / 10.0) + "%";
        this.txtRate.setText(mark);

        this.progressBarRate.setProgress((int) this.distanceM);
    }

    /* 거리 시간 칼로리 구하기 (현재 월/일) */
    private void infoWalk() {
        this.distanceM = 0;
        this.distanceT = 0;
        this.timeM = 0;
        this.timeT = 0;
        this.calorieM = 0;
        this.calorieT = 0;

        // 현재일
        final String date = Utils.getCurrentDate("yyyyMMdd");
        String date1 = date.substring(0, 6) + "01";
        String date2 = date.substring(0, 6) + "31";

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference reference = db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId)
                .collection(Constants.FirestoreCollectionName.WALK);

        Query query = reference.whereGreaterThanOrEqualTo("trailDate", date1)
                .whereLessThanOrEqualTo("trailDate", date2)
                .orderBy("trailDate");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    layLoading.setVisibility(View.GONE);

                    if (task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Walk walk = document.toObject(Walk.class);

                            distanceM += walk.getDistance();
                            timeM += walk.getTime2() - walk.getTime1();
                            calorieM += walk.getCalorie();

                            // 현재일
                            if (date.equals(walk.getTrailDate())) {
                                distanceT += walk.getDistance();
                                timeT += walk.getTime2() - walk.getTime1();
                                calorieT += walk.getCalorie();
                            }
                        }

                        // 이달
                        txtDistanceM.setText(Utils.getDistanceStr(distanceM));
                        txtTimeM.setText(Utils.getDisplayTime(timeM));
                        txtCalorieM.setText(Utils.getCalorieStr(calorieM));

                        // 오늘
                        txtDistanceT.setText(Utils.getDistanceStr(distanceT));
                        txtTimeT.setText(Utils.getDisplayTime(timeT));
                        txtCalorieT.setText(Utils.getCalorieStr(calorieT));

                        // 달성률
                        displayRate();
                    }
                } else {
                    // 오류
                    layLoading.setVisibility(View.GONE);
                }
            }
        });
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.layTrailSelect:
                    // 산책로 선택 (QR 코드)
                    IntentIntegrator intentIntegrator = new IntentIntegrator(getActivity());
                    // 바코드 인식시 소리
                    intentIntegrator.setBeepEnabled(true);
                    intentIntegrator.initiateScan();
                    break;
                case R.id.layLoading:
                    break;
            }
        }
    };
}
