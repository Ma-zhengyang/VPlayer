package com.android.mazhengyang.vplayer.utils;

/**
 * Created by mazhengyang on 19-2-22.
 */

public class Util {

    public static String formatTime(long timeMs) {
        int totalSeconds = (int)(timeMs / 1000);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
                .toString();
    }

}
