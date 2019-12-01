package com.program.healthhallym.fragment;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.program.healthhallym.R;
import com.program.healthhallym.adapter.WalkAdapter;
import com.program.healthhallym.entity.Walk;
import com.program.healthhallym.entity.WalkItem;
import com.program.healthhallym.util.Constants;
import com.program.healthhallym.util.GlobalVariable;
import com.program.healthhallym.util.Utils;

import java.util.ArrayList;
import java.util.Collections;

public class WalkHistoryFragment extends Fragment {
    private static final String TAG = WalkHistoryFragment.class.getSimpleName();

    private LinearLayout layLoading;

    private RecyclerView recyclerView;
    private WalkAdapter adapter;

    private ArrayList<WalkItem> items;

    // 데이터 없을때 표시할 레이아웃
    private LinearLayout layNoData;

    private TextView txtCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_walk_history, container, false);

        this.layLoading = view.findViewById(R.id.layLoading);
        ((ProgressBar) view.findViewById(R.id.progressBar)).setIndeterminateTintList(ColorStateList.valueOf(Color.WHITE));

        this.recyclerView = view.findViewById(R.id.recyclerView);
        this.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        this.layNoData = view.findViewById(R.id.layNoData);

        this.txtCount = view.findViewById(R.id.txtCount);
        this.txtCount.setText("");

        view.post(new Runnable() {
            @Override
            public void run() {
                layLoading.setVisibility(View.VISIBLE);

                // (딜레이 가동)
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 내역
                        listHistory();
                    }
                }, Constants.LoadingDelay.SHORT);
            }
        });

        return view;
    }

    /* 방 목록 구성하기 */
    private void listHistory() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference reference = db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId)
                .collection(Constants.FirestoreCollectionName.WALK);

        Query query = reference.orderBy("time1", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    layLoading.setVisibility(View.GONE);

                    if (task.getResult() != null) {
                        items = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Walk walk = document.toObject(Walk.class);
                            items.add(new WalkItem(document.getId(), walk));
                        }

                        if (items.size() == 0) {
                            layNoData.setVisibility(View.VISIBLE);
                        } else {
                            layNoData.setVisibility(View.GONE);
                        }

                        adapter = new WalkAdapter(items);
                        recyclerView.setAdapter(adapter);

                        txtCount.setText("내역 " + Utils.formatComma(items.size()));
                    }
                } else {
                    // 오류
                    layLoading.setVisibility(View.GONE);
                }
            }
        });
    }
}
