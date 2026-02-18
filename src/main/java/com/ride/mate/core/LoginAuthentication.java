package com.ride.mate.core;

public class LoginAuthentication {
    private static final ThreadLocal<String> USER = new ThreadLocal<>();

    public static String getUserName() {
        return USER.get();
    }

    public static void setUserName(String userName) {
        USER.set(userName);
    }
}
