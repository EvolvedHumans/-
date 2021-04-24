package tool;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Timestamp {
    /**
     * 时间函数集合
     * 1.获取当前时间戳
     * 2.将当前时间戳转成String
     */

    public static long timestamp(){
        return System.currentTimeMillis();
    }

    public static String timestamp_string() {
        return String.valueOf(timestamp());
    }

    public static String modernClock(){
        Calendar calendar = Calendar.getInstance();

        Integer year = calendar.get(Calendar.YEAR);

        return String.valueOf(calendar.get(Calendar.YEAR))
                +calendar.get(Calendar.MONTH)
                +calendar.get(Calendar.DAY_OF_MONTH)
                +calendar.get(Calendar.HOUR_OF_DAY)
                +calendar.get(Calendar.MINUTE)
                +calendar.get(Calendar.SECOND);

    }

}
