package com.program.healthhallym.entity;

public class Point {
    // 위도
    public double latitude;
    // 경도
    public double longitude;

    public Point(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}