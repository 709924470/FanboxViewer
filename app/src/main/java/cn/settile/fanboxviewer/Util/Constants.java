package cn.settile.fanboxviewer.Util;

public class Constants {
    public static String StroagePath = null;
    public static String Cookie = "";
    public static boolean DEBUG = true;
    public static int MAX_HOME_LOAD_PAGE = 10;
    public static int MAX_PLAN_LOAD_PAGE = 10;

    public final class requestCodes {
        public static final int LOGIN = 0;
        public static final int EXIT = 1;
    }

    public final class loginResultCodes {
        public static final int GUEST = 0;
        public static final int USER = 1;
    }
}