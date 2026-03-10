package com.ride.mate.util;

import lombok.extern.slf4j.Slf4j;

/**
 * ConversionUtil
 * Utility class for type conversions and data transformations
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 02-03-2026    N/A          N/A          Tishan          Initial Development
 */
@Slf4j
public class ConversionUtil {

    /**
     * Converts a String to Long
     *
     * @param str the string to convert
     * @return Long value or null if conversion fails
     */
    public static Long stringToLong(String str) {
        if (str == null || str.trim().isEmpty()) {
            log.warn("Attempted to convert null or empty string to Long");
            return null;
        }
        try {
            return Long.parseLong(str.trim());
        } catch (NumberFormatException e) {
            log.error("Failed to convert string to Long: {}", str, e);
            return null;
        }
    }

    /**
     * Converts a String to Long with exception throwing
     *
     * @param str the string to convert
     * @return Long value
     * @throws NumberFormatException if conversion fails
     */
    public static Long stringToLongStrict(String str) throws NumberFormatException {
        if (str == null || str.trim().isEmpty()) {
            throw new NumberFormatException("Cannot convert null or empty string to Long");
        }
        return Long.parseLong(str.trim());
    }

    /**
     * Converts a String to Integer
     *
     * @param str the string to convert
     * @return Integer value or null if conversion fails
     */
    public static Integer stringToInteger(String str) {
        if (str == null || str.trim().isEmpty()) {
            log.warn("Attempted to convert null or empty string to Integer");
            return null;
        }
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            log.error("Failed to convert string to Integer: {}", str, e);
            return null;
        }
    }

    /**
     * Converts a String to Integer with exception throwing
     *
     * @param str the string to convert
     * @return Integer value
     * @throws NumberFormatException if conversion fails
     */
    public static Integer stringToIntegerStrict(String str) throws NumberFormatException {
        if (str == null || str.trim().isEmpty()) {
            throw new NumberFormatException("Cannot convert null or empty string to Integer");
        }
        return Integer.parseInt(str.trim());
    }

    /**
     * Converts a String to Double
     *
     * @param str the string to convert
     * @return Double value or null if conversion fails
     */
    public static Double stringToDouble(String str) {
        if (str == null || str.trim().isEmpty()) {
            log.warn("Attempted to convert null or empty string to Double");
            return null;
        }
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            log.error("Failed to convert string to Double: {}", str, e);
            return null;
        }
    }

    /**
     * Converts a String to Double with exception throwing
     *
     * @param str the string to convert
     * @return Double value
     * @throws NumberFormatException if conversion fails
     */
    public static Double stringToDoubleStrict(String str) throws NumberFormatException {
        if (str == null || str.trim().isEmpty()) {
            throw new NumberFormatException("Cannot convert null or empty string to Double");
        }
        return Double.parseDouble(str.trim());
    }

    /**
     * Converts a String to Boolean
     *
     * @param str the string to convert
     * @return Boolean value or null if string is null/empty
     */
    public static Boolean stringToBoolean(String str) {
        if (str == null || str.trim().isEmpty()) {
            log.warn("Attempted to convert null or empty string to Boolean");
            return null;
        }
        return Boolean.parseBoolean(str.trim());
    }

    /**
     * Safely converts an Object to String
     *
     * @param obj the object to convert
     * @return String representation or empty string if null
     */
    public static String objectToString(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    /**
     * Safely converts an Object to String with default value
     *
     * @param obj the object to convert
     * @param defaultValue the default value if object is null
     * @return String representation or default value if null
     */
    public static String objectToString(Object obj, String defaultValue) {
        return obj != null ? obj.toString() : defaultValue;
    }
}

