package com.example.acrid.utils;

import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeUtils {
    public static long getUTCMilis(){
        return ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli();
    }
    public static String getTimeAgo(long timeInMillis) {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - timeInMillis;

        if (diff < TimeUnit.MINUTES.toMillis(1)) {
            return "Vừa xong";
        }

        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        if (minutes < 60) {
            return minutes + " phút trước";
        }

        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        if (hours < 24) {
            return hours + " giờ trước";
        }

        long days = TimeUnit.MILLISECONDS.toDays(diff);
        if (days < 7) {
            return days + " ngày trước";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(timeInMillis);
    }
}
