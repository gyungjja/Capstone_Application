package com.program.healthhallym.layout;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import com.program.healthhallym.R;

public class EditLayout extends LinearLayout {
    private static String TAG = EditLayout.class.getSimpleName();

    private InputMethodManager imm;

    private EditText edit;
    private ImageView imgDelete;

    public EditLayout(Context context, int res, String hint) {
        super(context);

        this.imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout layout = (LinearLayout)inflater.inflate(res, null);

        addView(layout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        this.edit = layout.findViewById(R.id.edit);
        this.edit.setHint(hint);
        this.edit.addTextChangedListener(mTextWatcher);

        this.imgDelete = layout.findViewById(R.id.imgDelete);
        this.imgDelete.setOnClickListener(mClickListener);
    }

    /* 입력값 얻기 */
    public String getText() {
        return this.edit.getText().toString();
    }

    /* 입력값 설정 */
    public void setText(String text) {
        this.edit.setText(text);
    }

    /* input 타입 설정 */
    public void setInputType(int type) {
        this.edit.setInputType(type);
    }

    /* 키보드에 검색 버튼 보이게 하기(EditorInfo.IME_ACTION_SEARCH) */
    public void setImeOptions(int options) {
        this.edit.setImeOptions(options);
    }

    /* 텍스트 enable 설정 */
    public void setEnabled(boolean enabled) {
        this.edit.setEnabled(enabled);

        if (enabled) {
            this.edit.setTextColor(ContextCompat.getColor(getContext(), R.color.default_text_color));
        } else {
            this.edit.setTextColor(ContextCompat.getColor(getContext(), R.color.disable_text_color));
        }
    }

    /* 포커스 */
    public void focus() {
        this.edit.requestFocus();

        // 키보드 보이게 하기
        this.imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /* 커서 위치 설정 */
    public void setSelection(int index) {
        this.edit.setSelection(index);
    }

    /* 최대 입력 글자수 제한 */
    public void setMaxLength(int length) {
        this.edit.setFilters(new InputFilter[] { new InputFilter.LengthFilter(length) });
    }

    /* 키보드 보이기 */
    public void showKeyboard() {
        this.imm.showSoftInput(this.edit, 0);
    }

    /* 키보드 숨기기 */
    public void hideKeyboard() {
        this.imm.hideSoftInputFromWindow(this.edit.getWindowToken(), 0);
    }

    /* edit 객체 얻기 */
    public EditText getEditText() {
        return this.edit;
    }

    TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (TextUtils.isEmpty(s.toString())) {
                imgDelete.setVisibility(View.INVISIBLE);
            } else {
                if (imgDelete.getVisibility() != View.VISIBLE) {
                    imgDelete.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int before, int count) {}
    };

    private OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.imgDelete:
                    // 글자 제거
                    edit.setText("");
                    //imgDelete.setVisibility(View.INVISIBLE);

                    break;
            }
        }
    };
}
