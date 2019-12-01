package com.program.healthhallym.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.program.healthhallym.R;
import com.program.healthhallym.entity.Trail;
import com.program.healthhallym.entity.WalkItem;
import com.program.healthhallym.util.GlobalVariable;
import com.program.healthhallym.util.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WalkAdapter extends RecyclerView.Adapter<WalkAdapter.ViewHolder> {

    private ArrayList<WalkItem> items;

    public WalkAdapter(ArrayList<WalkItem> items) {
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_walk, null);

        // Item 사이즈 조절
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        v.setLayoutParams(lp);

        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 산책로 이름
        String trailName = "산책로";
        for (Trail trail : GlobalVariable.trails) {
            if (trail.code.equals(this.items.get(position).walk.getTrailCode())) {
                trailName = trail.name;
                break;
            }
        }
        holder.txtTrail.setText(trailName);

        // 일시 표시 형식
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        // 시작일시
        calendar.setTime(new Date(this.items.get(position).walk.getTime1()));
        String term = dateFormat.format(calendar.getTime());
        // 완료일시
        calendar.setTime(new Date(this.items.get(position).walk.getTime2()));
        term += " ~ " + dateFormat.format(calendar.getTime());

        holder.txtTerm.setText(term);

        holder.txtDistance.setText(Utils.getDistanceStr(this.items.get(position).walk.getDistance()));
        // 걸은 시간
        long diff = this.items.get(position).walk.getTime2() - this.items.get(position).walk.getTime1();
        holder.txtTime.setText(Utils.getDisplayTime(diff));
        holder.txtCalorie.setText(Utils.getCalorieStr(this.items.get(position).walk.getCalorie()));
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtTrail, txtTerm, txtDistance, txtTime, txtCalorie;

        public ViewHolder(View view) {
            super(view);

            this.txtTrail = view.findViewById(R.id.txtTrail);
            this.txtTerm = view.findViewById(R.id.txtTerm);
            this.txtDistance = view.findViewById(R.id.txtDistance);
            this.txtTime = view.findViewById(R.id.txtTime);
            this.txtCalorie = view.findViewById(R.id.txtCalorie);
        }
    }
}
