package com.program.healthhallym.entity;

public class Trail {

    public String code;             // 코드 (QR코드)
    public String name;             // 산책로 이름
    public String address1;         // 시작주소
    public String address2;         // 끝주소

    public Trail(String code, String name, String address1, String address2) {
        this.code = code;
        this.name = name;
        this.address1 = address1;
        this.address2 = address2;
    }
}
