package com.program.healthhallym;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.program.healthhallym.fragment.MyEditFragment;
import com.program.healthhallym.fragment.abstracts.IFragment;
import com.program.healthhallym.util.Constants;

public class MyEditActivity extends AppCompatActivity {
    private static String TAG = MyEditActivity.class.getSimpleName();

    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);

        setTitle(getString(R.string.activity_title_my_edit));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.fragment = new MyEditFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.layContent, this.fragment).commit();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_ok:
                // 확인 (수정)
                ((IFragment) this.fragment).task(Constants.FragmentTaskKind.OK, null);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
