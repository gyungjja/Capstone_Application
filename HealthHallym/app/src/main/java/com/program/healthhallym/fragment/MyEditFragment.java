package com.program.healthhallym.fragment;

import android.app.Activity;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.program.healthhallym.R;
import com.program.healthhallym.fragment.abstracts.IFragment;
import com.program.healthhallym.layout.EditLayout;
import com.program.healthhallym.util.Constants;
import com.program.healthhallym.util.GlobalVariable;
import com.program.healthhallym.util.Utils;

public class MyEditFragment extends Fragment implements IFragment {
    private static final String TAG = MyEditFragment.class.getSimpleName();

    private LinearLayout layLoading;

    private EditLayout editName, editPhone, editHeight, editWeight;
    private TextView txtNumber, txtMessage;

    // 성별
    private String gender;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_edit, container, false);

        this.layLoading = view.findViewById(R.id.layLoading);
        ((ProgressBar) view.findViewById(R.id.progressBar)).setIndeterminateTintList(ColorStateList.valueOf(Color.WHITE));

        this.editName = new EditLayout(getContext(), R.layout.layout_border_edit, "이름을 입력하세요.");
        this.editName.setInputType(InputType.TYPE_CLASS_TEXT);
        ((LinearLayout) view.findViewById(R.id.layName)).addView(this.editName, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        this.editPhone = new EditLayout(getContext(), R.layout.layout_border_edit, "휴대번호를 입력하세요.");
        this.editPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        ((LinearLayout) view.findViewById(R.id.layPhone)).addView(this.editPhone, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        this.editHeight = new EditLayout(getContext(), R.layout.layout_border_edit, "키 입력");
        this.editHeight.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        ((LinearLayout) view.findViewById(R.id.layHeight)).addView(this.editHeight, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        this.editWeight = new EditLayout(getContext(), R.layout.layout_border_edit, "몸무게 입력");
        this.editWeight.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        ((LinearLayout) view.findViewById(R.id.layWeight)).addView(this.editWeight, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        ((RadioGroup) view.findViewById(R.id.rdgGender)).setOnCheckedChangeListener(mCheckedChangeListener);

        this.txtNumber = view.findViewById(R.id.txtNumber);

        // 오류 표시
        this.txtMessage = view.findViewById(R.id.txtMessage);
        this.txtMessage.setText("");

        // 초기화
        init(view);

        return view;
    }

    @Override
    public void task(int kind, Bundle bundle) {
        if (kind == Constants.FragmentTaskKind.OK) {
            // 확인
            if (checkData()) {
                this.layLoading.setVisibility(View.VISIBLE);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 수정
                        modifyInfo();
                    }
                }, Constants.LoadingDelay.SHORT);
            }
        }
    }

    /* 초기화 */
    private void init(View view) {
        this.txtNumber.setText(GlobalVariable.user.getNumber());
        this.editName.setText(GlobalVariable.user.getName());
        this.editPhone.setText(GlobalVariable.user.getPhone());
        this.editHeight.setText(String.valueOf(GlobalVariable.user.getHeight()));
        this.editWeight.setText(String.valueOf(GlobalVariable.user.getWeight()));

        // 성별 표시
        this.gender = GlobalVariable.user.getGender();
        if (GlobalVariable.user.getGender().equals(Constants.Gender.MALE)) {
            ((RadioButton) view.findViewById(R.id.rdM)).setChecked(true);
        } else {
            ((RadioButton) view.findViewById(R.id.rdF)).setChecked(true);
        }
    }

    /* 입력 데이터 체크 */
    private boolean checkData() {
        // 휴대번호 입력 체크
        String phone = this.editPhone.getText();
        if (TextUtils.isEmpty(phone)) {
            this.txtMessage.setVisibility(View.VISIBLE);
            this.txtMessage.setText(getString(R.string.msg_phone_number_check_empty));
            this.editPhone.requestFocus();
            return false;
        }

        // 휴대번호 유효성 체크
        if (!Utils.isPhoneNumber(phone)) {
            this.txtMessage.setVisibility(View.VISIBLE);
            this.txtMessage.setText(getString(R.string.msg_phone_number_check_wrong));
            this.editPhone.requestFocus();
            return false;
        }

        // 이름 입력 체크
        String name = this.editName.getText();
        if (TextUtils.isEmpty(name)) {
            this.txtMessage.setVisibility(View.VISIBLE);
            this.txtMessage.setText(getString(R.string.msg_user_name_check_empty));
            this.editName.requestFocus();
            return false;
        }

        // 키 입력 체크
        String height = this.editHeight.getText();
        if (TextUtils.isEmpty(height)) {
            this.txtMessage.setVisibility(View.VISIBLE);
            this.txtMessage.setText(getString(R.string.msg_user_height_check_empty));
            this.editHeight.requestFocus();
            return false;
        }

        if (!Utils.isNumeric(height)) {
            this.txtMessage.setVisibility(View.VISIBLE);
            this.txtMessage.setText(getString(R.string.msg_user_height_check_wrong));
            this.editHeight.requestFocus();
            return false;
        }

        // 몸무게 입력 체크
        String weight = this.editWeight.getText();
        if (TextUtils.isEmpty(weight)) {
            this.txtMessage.setVisibility(View.VISIBLE);
            this.txtMessage.setText(getString(R.string.msg_user_weight_check_empty));
            this.editWeight.requestFocus();
            return false;
        }

        if (!Utils.isNumeric(weight)) {
            this.txtMessage.setVisibility(View.VISIBLE);
            this.txtMessage.setText(getString(R.string.msg_user_weight_check_wrong));
            this.editWeight.requestFocus();
            return false;
        }

        this.editWeight.hideKeyboard();
        this.txtMessage.setVisibility(View.GONE);
        this.txtMessage.setText("");

        return true;
    }

    /* 정보 수정 */
    private void modifyInfo() {
        final String name = this.editName.getText();
        final String phone = this.editPhone.getText();
        final String height = this.editHeight.getText();
        final String weight = this.editWeight.getText();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference reference = db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId);
        reference.update("name", name, "phone", phone, "gender", this.gender,
                "height", Float.parseFloat(height), "weight", Float.parseFloat(weight))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 성공
                        layLoading.setVisibility(View.GONE);

                        GlobalVariable.user.setName(name);
                        GlobalVariable.user.setPhone(phone);
                        GlobalVariable.user.setGender(gender);
                        GlobalVariable.user.setHeight(Float.parseFloat(height));
                        GlobalVariable.user.setWeight(Float.parseFloat(weight));

                        // Activity 에 전달
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
}
