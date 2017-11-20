package com.iquanwai.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class DateUtils {
    private static DateTimeFormatter format1 = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static DateTimeFormatter format2 = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static DateTimeFormatter format3 = DateTimeFormat.forPattern("yyyyMMddHHmmss");
    private static DateTimeFormatter format4 = DateTimeFormat.forPattern("yyyy.MM.dd");

    public static String parseDateToString(Date date) {
        return format1.print(new DateTime(date));
    }

    public static Date parseStringToDate(String strDate) {
        return format1.parseDateTime(strDate).toDate();
    }

    public static String parseDateTimeToString(Date date) {
        return format2.print(new DateTime(date));
    }

    public static Date parseStringToDateTime(String strDate) {
        return format2.parseDateTime(strDate).toDate();
    }

    public static int interval(Date date) {
        long now = System.currentTimeMillis();
        long thatTime = date.getTime();

        return Math.abs((int) ((now - thatTime) / 1000 / 60 / 60 / 24));
    }

    public static int interval(Date date1, Date date2) {
        long thisTime = date1.getTime();
        long thatTime = date2.getTime();

        return Math.abs((int) ((thisTime - thatTime) / 1000 / 60 / 60 / 24));
    }

    // 计算日期差值，向上取整
    public static int intervalCeil(Date date1, Date date2) {
        long thisTime = date1.getTime();
        long thatTime = date2.getTime();

        return Math.abs((int)(thisTime - thatTime)/1000)/60/60/24;
    }

    public static Date startOfDay(Date date) {
        return new DateTime(date).withTimeAtStartOfDay().toDate();
    }

    public static boolean isToday(Date date) {
        Date inputDate = new DateTime(date).withTimeAtStartOfDay().toDate();
        Date today = new DateTime(new Date()).withTimeAtStartOfDay().toDate();

        return inputDate.equals(today);
    }

    public static long currentTimestamp(){
        return System.currentTimeMillis()/1000;
    }

    public static String parseDateToString3(Date date) {
        return format3.print(new DateTime(date));
    }

    public static Date parseStringToDate3(String strDate) {
        return format3.parseDateTime(strDate).toDate();
    }

    public static Date afterMinutes(Date date, int increment){
        return new DateTime(date).plusMinutes(increment).toDate();
    }

    public static Date afterHours(Date date, int increment) {
        return new DateTime(date).plusHours(increment).toDate();
    }

    public static Date startDay(Date date){
        return new DateTime(date).withTimeAtStartOfDay().toDate();
    }

    public static Date afterMonths(Date date, int increment) {
        return new DateTime(date).plusMonths(increment).toDate();
    }

    public static Date afterYears(Date date, int increment){
        return new DateTime(date).plusYears(increment).toDate();
    }

    public static Date afterDays(Date date, int increment){
        return new DateTime(date).plusDays(increment).toDate();
    }

    public static Date beforeDays(Date date, int increment){
        return new DateTime(date).minusDays(increment).toDate();
    }

    public static Date startOfHour(Date date) {
        DateTime dateTime = new DateTime(date);
        int hour = dateTime.hourOfDay().get();
        return dateTime.withTime(hour, 0, 0, 0).toDate();
    }

    public static Date beforeHours(Date date, int increment){
        return new DateTime(date).minusHours(increment).toDate();
    }

    public static String parseDateToStringByCommon(Date date) {
        return format4.print(new DateTime(date));
    }

    public static Date getMonday(Date date) {
        return new DateTime(date.getTime()).withDayOfWeek(DateTimeConstants.MONDAY).toDate();
    }
}
