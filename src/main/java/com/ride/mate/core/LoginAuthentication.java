package com.ride.mate.core;

/**
 * LoginAuthentication
 * Thread-local storage for authenticated user information
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-02-2026    N/A          N/A          Tishan          Initial Development
 * 2 02-03-2026    N/A          N/A          Tishan          Added clear method
 */
public class LoginAuthentication {
    private static final ThreadLocal<String> USER = new ThreadLocal<>();

    /**
     * Get the current authenticated user's username
     *
     * @return username or null if not authenticated
     */
    public static String getUserName() {
        return USER.get();
    }

    /**
     * Set the current authenticated user's username
     *
     * @param userName the username to set
     */
    public static void setUserName(String userName) {
        USER.set(userName);
    }

    /**
     * Clear the current authentication context
     */
    public static void clear() {
        USER.remove();
    }
}
