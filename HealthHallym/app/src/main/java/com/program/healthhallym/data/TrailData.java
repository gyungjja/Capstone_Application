package com.program.healthhallym.data;

import com.program.healthhallym.entity.Trail;

import java.util.ArrayList;

public class TrailData {
    private volatile static TrailData _instance = null;

    private ArrayList<Trail> trails;

    /* 싱글톤 패턴 적용 */
    public static TrailData getInstance() {
        if (_instance == null) {
            synchronized (TrailData.class) {
                if (_instance == null) {
                    _instance = new TrailData();
                }
            }
        }

        return _instance;
    }

    private TrailData() {
        // 초기화 (데이터 생성)
        init();
    }

    /* 초기화 (데이터 생성) */
    private void init() {
        this.trails = new ArrayList<>();

        this.trails.add(new Trail("A001", "A 산책로", "부산 사상구 백양대로 887-26", "부산 사상구 모라로110번길 29"));
        this.trails.add(new Trail("A002", "B 산책로", "부산 사상구 모라로 22", "부산 사상구 사상로 515"));
        this.trails.add(new Trail("A003", "C 산책로", "부산 사상구 백양대로 943-25", "부산 사상구 백양대로 924"));
    }

    /* 산책로 데이터 얻기 */
    public ArrayList<Trail> getTrails() {
        return this.trails;
    }
}
