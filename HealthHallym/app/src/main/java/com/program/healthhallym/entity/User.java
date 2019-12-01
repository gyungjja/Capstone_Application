package com.program.healthhallym.entity;

public class User {

    // 학번 (user id 값)
    private String number;

    private String name;
    private String phone;
    private String password;

    // 성별 (M:남, F:여)
    private String gender;

    private float height;           // 키
    private float weight;           // 몸무게

    // 목표치
    private int goal;

    // 가입일시를 millisecond 로 표현
    private long joinTimeMillis;

    public User() {}

    public User(String number, String name, String phone, String gender,
                float height, float weight, String password, long joinTimeMillis, int goal) {
        this.number = number;
        this.name = name;
        this.phone = phone;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.password = password;
        this.joinTimeMillis = joinTimeMillis;

        this.goal = goal;
    }

    public String getNumber() {
        return this.number;
    }

    public String getName() {
        return this.name;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getGender() {
        return this.gender;
    }

    public float getHeight() {
        return this.height;
    }

    public float getWeight() {
        return this.weight;
    }

    public String getPassword() {
        return this.password;
    }

    public long getJoinTimeMillis() {
        return this.joinTimeMillis;
    }

    public int getGoal() {
        return this.goal;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }
}
