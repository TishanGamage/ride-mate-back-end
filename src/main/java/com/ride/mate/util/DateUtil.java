package com.ride.mate.util;

import java.sql.Timestamp;
import java.util.Calendar;

public class DateUtil {

    public static Timestamp getDate() {
        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();
        return new Timestamp(now.getTime());
    }
}
