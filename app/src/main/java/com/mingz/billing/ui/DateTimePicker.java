package com.mingz.billing.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import androidx.annotation.IntRange;
import androidx.annotation.Nullable;

import java.util.Calendar;

public class DateTimePicker extends LinearLayout {
    private final NumberPicker yearPicker;
    private final NumberPicker monthPicker;
    private final NumberPicker dayPicker;
    private final NumberPicker hourPicker;
    private final NumberPicker minutePicker;

    private OnDateTimeChangeListener listener = null;

    public DateTimePicker(Context context) {
        this(context, null);
    }

    public DateTimePicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        yearPicker = new NumberPicker(context, attrs);
        monthPicker = new NumberPicker(context, attrs);
        dayPicker = new NumberPicker(context, attrs);
        hourPicker = new NumberPicker(context, attrs);
        minutePicker = new NumberPicker(context, attrs);
        // 年选择器
        yearPicker.setWrapSelectorWheel(false);
        settingYearRange(1970);
        yearPicker.setValue(1970);
        yearPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (monthPicker.getValue() == 2) {
                dayPicker.setMaxValue(getDayMaxValueOnFeb(newVal));
            }
            if (listener != null) {
                listener.onDate(newVal, monthPicker.getValue(), dayPicker.getValue());
                listener.onDateTime(newVal, monthPicker.getValue(), dayPicker.getValue(),
                        hourPicker.getValue(), minutePicker.getValue());
            }
        });
        // 月选择器
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            dayPicker.setMaxValue(getDayMaxValue(yearPicker.getValue(), newVal));
            if (listener != null) {
                listener.onDate(yearPicker.getValue(), newVal, dayPicker.getValue());
                listener.onDateTime(yearPicker.getValue(), newVal, dayPicker.getValue(),
                        hourPicker.getValue(), minutePicker.getValue());
            }
        });
        // 日选择器
        dayPicker.setMinValue(1);
        dayPicker.setMaxValue(31);
        dayPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (listener != null) {
                listener.onDate(yearPicker.getValue(), monthPicker.getValue(), newVal);
                listener.onDateTime(yearPicker.getValue(), monthPicker.getValue(), newVal,
                        hourPicker.getValue(), minutePicker.getValue());
            }
        });
        // 时选择器
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);
        hourPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (listener != null) {
                listener.onTime(newVal, minutePicker.getValue());
                listener.onDateTime(yearPicker.getValue(), monthPicker.getValue(), dayPicker.getValue(),
                        newVal, minutePicker.getValue());
            }
        });
        // 分选择器
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (listener != null) {
                listener.onTime(hourPicker.getValue(), newVal);
                listener.onDateTime(yearPicker.getValue(), monthPicker.getValue(), dayPicker.getValue(),
                        hourPicker.getValue(), newVal);
            }
        });
        // 计算参数
        final int timeMarginStart = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                20.0f, context.getResources().getDisplayMetrics()));
        // 设置父组件参数
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        // 添加子组件
        yearPicker.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));
        addView(yearPicker);

        TextView dateSplit1 = new TextView(context, attrs);
        dateSplit1.setText(" - ");
        addView(dateSplit1);

        monthPicker.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));
        addView(monthPicker);

        TextView dateSplit2 = new TextView(context, attrs);
        dateSplit2.setText(" - ");
        addView(dateSplit2);

        dayPicker.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));
        addView(dayPicker);

        LayoutParams hourParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);
        hourParams.setMarginStart(timeMarginStart);
        hourPicker.setLayoutParams(hourParams);
        addView(hourPicker);

        TextView timeSplit = new TextView(context, attrs);
        timeSplit.setText(" : ");
        addView(timeSplit);

        minutePicker.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));
        addView(minutePicker);
    }

    private void settingYearRange(int year) {
        int lowLimit = year / 10 * 10 - 100;
        yearPicker.setMinValue(lowLimit);
        yearPicker.setMaxValue(lowLimit + 200);
    }

    private int getDayMaxValueOnFeb(int year) {
        if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) {
            return 29;
        } else {
            return 28;
        }
    }

    private int getDayMaxValue(int year, int month) {
        switch (month) {
            case 2:
                return getDayMaxValueOnFeb(year);
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31;
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
        }
        throw new IllegalArgumentException("月份应该在1到12之间");
    }

    public void updateToNowTime() {
        Calendar now = Calendar.getInstance();
        setDateTime(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1,
                now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE));
    }

    public void setDateTime(@IntRange(from = 100) int year,
                             @IntRange(from = 1, to = 12) int month,
                             @IntRange(from = 1, to = 31) int day,
                             @IntRange(from = 0, to = 23) int hour,
                             @IntRange(from = 0, to = 59) int minute) {
        settingYearRange(year);
        yearPicker.setValue(year);
        monthPicker.setValue(month);
        int dayMaxValue = getDayMaxValue(year, month);
        if (day > dayMaxValue) {
            day = dayMaxValue;
        }
        dayPicker.setMaxValue(dayMaxValue);
        dayPicker.setValue(day);
        hourPicker.setValue(hour);
        minutePicker.setValue(minute);
    }

    public int getYear() {
        return yearPicker.getValue();
    }

    public int getMonth() {
        return monthPicker.getValue();
    }

    public int getDay() {
        return dayPicker.getValue();
    }

    public int getHour() {
        return hourPicker.getValue();
    }

    public int getMinute() {
        return minutePicker.getValue();
    }

    public long getTimeInMillis() {
        Calendar current = Calendar.getInstance();
        current.set(yearPicker.getValue(), monthPicker.getValue() - 1, dayPicker.getValue(),
                hourPicker.getValue(), minutePicker.getValue());
        return current.getTimeInMillis();
    }

    public void setOnDateTimeChangeListener(OnDateTimeChangeListener listener) {
        this.listener = listener;
    }

    public interface OnDateTimeChangeListener {
        default void onDate(int year, int month, int day) {}

        default void onTime(int hour, int minute) {}

        default void onDateTime(int year, int month, int day, int hour, int minute) {}
    }
}
