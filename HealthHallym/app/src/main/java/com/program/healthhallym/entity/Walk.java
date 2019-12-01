package com.program.healthhallym.entity;

public class Walk {

    // 산책로 코드
    private String trailCode;

    // 산책일 (예:20191101) => 검색 조건으로 필요함
    private String trailDate;

    // 산책시간 millisecond 로 표현
    private long time1;             // 시작일시
    private long time2;             // 완료일시

    // 산책거리 (m)
    private double distance;

    // 칼로리 소모
    private double calorie;

    public Walk() {}

    public Walk(String trailCode, String trailDate, long time1, long time2, double distance, double calorie) {
        this.trailCode = trailCode;
        this.trailDate = trailDate;
        this.time1 = time1;
        this.time2 = time2;
        this.distance = distance;
        this.calorie = calorie;
    }

    public String getTrailCode() {
        return this.trailCode;
    }

    public String getTrailDate() {
        return this.trailDate;
    }

    public long getTime1() {
        return this.time1;
    }

    public long getTime2() {
        return this.time2;
    }

    public double getDistance() {
        return this.distance;
    }

    public double getCalorie() {
        return this.calorie;
    }
}
