package com.ride.mate.util;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;

public class DateUtil {

    public static Timestamp getDate() {
        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();
        return new Timestamp(now.getTime());
    }

    public static Timestamp stringToTimeStamp(String date) {
        return Timestamp.valueOf(date);
    }

    public static boolean isPastDate(Timestamp date) {
        Timestamp currentTimestamp = getDate();
        return date.before(currentTimestamp);
    }

     public static boolean isFutureDate(Timestamp date) {
        Timestamp currentTimestamp = getDate();
        return date.after(currentTimestamp);
    }

    public static LocalDate stringToLocalDate(String date) {
        return LocalDate.parse(date);
    }

    public static boolean isPastLocalDateTime(LocalDate dateTime) {
        LocalDate currentDateTime = LocalDate.now();
        return dateTime.isBefore(currentDateTime);
    }

     public static boolean isFutureLocalDateTime(java.time.LocalDate dateTime) {
         LocalDate currentDateTime = LocalDate.now();
        return dateTime.isAfter(currentDateTime);
    }
}
