package com.juanjob.app.helpers;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateHelper {
    public String getDateNow() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat date_format = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        String current_date_time = date_format.format(cal.getTime());
        return current_date_time;
    }
}
