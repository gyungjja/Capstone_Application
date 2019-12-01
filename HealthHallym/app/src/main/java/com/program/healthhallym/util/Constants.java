package com.program.healthhallym.util;

public class Constants {

    // SharedPreferences 캐쉬 이름
    public static final String CACHE_NAME = "Health";

    /* SharedPreferences 관련 상수 */
    public static class SharedPreferencesName {
        // 위도
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
    }

    /* Activity 요청 코드 */
    public static class RequestCode {
        public static final int JOIN = 0;
        public static final int SETTING = 1;
        public static final int EDIT = 2;
        public static final int MAP = 3;

        public static final int GPS_ENABLE = 100;
    }

    /* 액티비티에서 프레그먼트에 요청할 작업 종류 */
    public static class FragmentTaskKind {
        // 확인
        public static final int OK = 0;
        // 설정
        public static final int SETTING = 1;
        // 걷기 저장
        public static final int WALK_SAVE = 2;
    }

    /* Fire store Collection 이름 */
    public static class FirestoreCollectionName {
        // 사용자
        public static final String USER = "users";
        // 산책정보
        public static final String WALK = "walks";
    }

    /* 성별 */
    public static class Gender {
        public static final String MALE = "M";
        public static final String FEMALE = "F";
    }

    /* 로딩 딜레이 */
    public static class LoadingDelay {
        public static final int SHORT = 300;
        public static final int LONG = 1000;
    }
}
