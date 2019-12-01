package com.program.healthhallym.util;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.program.healthhallym.entity.Point;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class Utils {

    /* 숫자 콤마 표시 */
    public static String formatComma(long value) {
        DecimalFormat format = new DecimalFormat("#,###");

        return format.format(value);
    }

    /* 숫자 체크 */
    public static boolean isNumeric(String str) {
        boolean chk = false;

        try{
            Double.parseDouble(str) ;
            chk = true ;
        } catch (Exception e) {}

        return chk;
    }

    /* 휴대번호 체크 */
    public static boolean isPhoneNumber(String number) {
        String regEx = "^(010)-?(\\d{4})-?(\\d{4})$";
        if (number.indexOf("010") != 0) {
            regEx = "^(01(?:1|[6-9]))-?(\\d{3})-?(\\d{4})$";
        }

        return Pattern.matches(regEx, number);
    }

    /* 휴대번호 얻기 */
    public static String getPhoneNumber(Activity activity) {
        String number = "";

        try {
            TelephonyManager tel = (TelephonyManager) activity.getSystemService(Activity.TELEPHONY_SERVICE);
            number = tel.getLine1Number();

            if (!TextUtils.isEmpty(number)) {
                // "-", "+" 제거
                number = number.replace("-", "").replace("+", "");

                if (number.indexOf("82") == 0) {
                    number = "0" + number.substring(2);
                }
            }
        } catch (SecurityException e) {
        } catch (Exception e) {
        }

        return number;
    }

    /* 현재날자 구하기 */
    public static String getCurrentDate(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());

        return dateFormat.format(date);
    }

    /* 시간 표현 값 얻기(시,분) */
    public static String getDisplayTime(long millis) {
        StringBuilder str = new StringBuilder();

        long second = (millis / 1000) % 60;
        long minute = (millis / (1000 * 60)) % 60;
        long hour = (millis / (1000 * 60 * 60)) % 24;

        if(hour > 0)
        {
            str.append(hour + "시간");
        }
        if(minute > 0)
        {
            str.append(minute + "분");
        }
        if(second > 0)
        {
            str.append(second + "초");
        }

        if (TextUtils.isEmpty(str.toString())) {
            str.append("0초");
        }

        return str.toString();
    }

    /* 거리 */
    public static String getDistanceStr(double distance) {
        String distanceStr;

        // 1km 이상이면
        if (distance > 1000) {
            distance = distance / 1000;
            // 소수점 한자리까지 표시 (반올림)
            distanceStr = (Math.round(distance*10) / 10.0) + "Km";
        } else {
            // 소수점 버림
            distanceStr = (int) Math.floor(distance) + "m";
        }

        return distanceStr;
    }

    /* 칼로리 소모 */
    public static String getCalorieStr(double calorie) {
        String calorieStr;

        // 1Kcal 이상이면
        if (calorie > 1000) {
            calorie = calorie / 1000;
            // 소수점 한자리까지 표시 (반올림)
            calorieStr = (Math.round(calorie*10) / 10.0) + "Kcal";
        } else {
            // 소수점 버림
            calorieStr = (int) Math.floor(calorie) + "cal";
        }

        return calorieStr;
    }

    /* GPS 정보로 주소 얻기 */
    public static String getAddressFromGps(Context context, double lat, double lng) {
        // 지오코더... GPS 를 주소로 변환
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
        } catch (IllegalArgumentException illegalArgumentException) {
            return "잘못된 GPS 좌표";
        } catch (IOException ioException) {
            //네트워크 문제
            return "네트워크 오류";
        }

        if (addresses == null || addresses.size() == 0) {
            return "주소정보가 없습니다.";
        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0);
        }
    }

    /* 주소로 GPS (위도,경도) 얻기 */
    public static Point getGpsFromAddress(Context context, String address) {
        // 지오코더... 주소 를 GPS 로 변환
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocationName(address, 1);
        } catch (IllegalArgumentException illegalArgumentException) {
            return new Point(0 ,0);
        } catch (IOException ioException) {
            //네트워크 문제
            return new Point(0 ,0);
        }

        if (addresses == null || addresses.size() == 0) {
            return new Point(0 ,0);
        } else {
            // 위도 경도 넘김
            return new Point(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
        }
    }

}
