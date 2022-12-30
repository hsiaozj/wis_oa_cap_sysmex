package com.seeyon.apps.bjev.util;


import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

public class IdUtil {
    public static void main(String[] args) {
        Date date = new Date();
        String strDateFormat = "yyyyMMddHHmmss";

        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        String result =  localDateTime.format(DateTimeFormatter.ofPattern(strDateFormat));
        System.out.println("DateTimeFormatter:"+result);
    }
}
