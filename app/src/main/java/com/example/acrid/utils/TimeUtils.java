package com.example.acrid.utils;


import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeUtils {
        public static String getTimeAgo(long timeInMillis) {
            long currentTime = System.currentTimeMillis();
            long diff = currentTime - timeInMillis;

            if (diff < TimeUnit.MINUTES.toMillis(1)) {
                return "Vừa xong";
            } else if (diff < TimeUnit.HOURS.toMillis(1)) {
                return (diff / TimeUnit.MINUTES.toMillis(1)) + " phút trước";
            } else if (diff < TimeUnit.DAYS.toMillis(1)) {
                return (diff / TimeUnit.HOURS.toMillis(1)) + " giờ trước";
            } else if (diff < TimeUnit.DAYS.toMillis(7)) {
                return (diff / TimeUnit.DAYS.toMillis(1)) + " ngày trước";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                return sdf.format(timeInMillis);
            }
        }
}
