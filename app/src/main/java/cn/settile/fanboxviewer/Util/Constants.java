package cn.settile.fanboxviewer.Util;

public class Constants {
    public static String Domain="https://fanbox.cc/";
    public static String StroagePath = null;
    public static String Cookie = "";
    public static boolean DEBUG = true;
    public static boolean NOTIFICATION_SETUP = false;

    public static int MAX_HOME_LOAD_PAGE = 10;
    public static int MAX_PLAN_LOAD_PAGE = 10;

    public static int MAX_DOWNLOAD_THREADS = 9;

    public static String DOWNLOAD_PATH = null;

    public static final class requestCodes {
        public static final int LOGIN = 0;
        public static final int EXIT = 1;
    }

    public static final class loginResultCodes {
        public static final int GUEST = 0;
        public static final int USER = 1;
    }
    public enum CheckItemState {
        UNKNOW ,
        SUCCESS,
        FAIL ,
    }
}