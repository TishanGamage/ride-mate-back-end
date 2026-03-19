package com.ride.mate.util;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;

public class DateUtil {

    public static Timestamp getDate() {
        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();
        return new Timestamp(now.getTime());
    }

    public static Timestamp stringToTimeStamp(String date) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }
        String trimmed = date.trim();

        // Try SQL format first: yyyy-MM-dd HH:mm:ss[.fffffffff]
        try {
            return Timestamp.valueOf(trimmed);
        } catch (IllegalArgumentException ignored) {
            // fall through to other formats
        }

        // Try ISO 8601 with T separator: yyyy-MM-ddTHH:mm:ss or yyyy-MM-ddTHH:mm:ss.SSS
        try {
            LocalDateTime ldt = LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return Timestamp.valueOf(ldt);
        } catch (DateTimeParseException ignored) {
            // fall through
        }

        // Try date-only format: yyyy-MM-dd
        try {
            LocalDate ld = LocalDate.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE);
            return Timestamp.valueOf(ld.atStartOfDay());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Cannot parse timestamp from value: '" + date +
                    "'. Supported formats: 'yyyy-MM-dd HH:mm:ss', 'yyyy-MM-ddTHH:mm:ss', 'yyyy-MM-dd'", e);
        }
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
