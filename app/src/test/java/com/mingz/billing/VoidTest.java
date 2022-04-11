package com.mingz.billing;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class VoidTest {
    @Test
    public void test() {
        Calendar now = Calendar.getInstance();
        printCalendar(now);
        now.set(1970, 0, 1, 8, 0);
        printCalendar(now);
    }

    private void printCalendar(Calendar calendar) {
        System.out.println(calendar.get(Calendar.YEAR) + "年" +
                (calendar.get(Calendar.MONTH) + 1) + "月" +
                calendar.get(Calendar.DAY_OF_MONTH) + "日  " +
                calendar.get(Calendar.HOUR_OF_DAY) + "时" +
                calendar.get(Calendar.MINUTE) + "分");
    }
}
