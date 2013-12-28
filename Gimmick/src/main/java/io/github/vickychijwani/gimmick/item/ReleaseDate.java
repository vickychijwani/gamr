package io.github.vickychijwani.gimmick.item;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReleaseDate implements Comparable<ReleaseDate> {

    // mMonth and mQuarter cannot be set simultaneously!
    private final byte mDay;
    private final byte mMonth;
    private final byte mQuarter;
    private final short mYear;

    private static final String INPUT_DATE_FORMAT = "yyyy-MM-dd HH:mm";
    private static final DateFormat FORMATTER = new SimpleDateFormat(INPUT_DATE_FORMAT);

    public static final ReleaseDate INVALID = new ReleaseDate();

    public static final byte DAY_INVALID = 0;
    public static final byte MONTH_INVALID = 0;
    public static final byte QUARTER_INVALID = 0;
    public static final short YEAR_INVALID = 0;

    public static final byte DAY_MIN = 1;
    public static final byte DAY_MAX = 31;
    public static final byte MONTH_MIN = 1;
    public static final byte MONTH_MAX = 12;
    public static final byte QUARTER_MIN = 1;
    public static final byte QUARTER_MAX = 4;
    public static final short YEAR_MIN = 1980;

    /**
     * @param day       Any value in the range [{@value #DAY_MIN}, {@value #DAY_MAX}], inclusive
     * @param month     Any value in the range [{@value #MONTH_MIN}, {@value #MONTH_MAX}], inclusive
     * @param quarter   Any value in the range [{@value #QUARTER_MIN}, {@value #QUARTER_MAX}], inclusive
     * @param year      Any value >= {@value #YEAR_MIN}
     * @throws IllegalArgumentException
     */
    public ReleaseDate(byte day, byte month, byte quarter, short year) throws IllegalArgumentException {
        if (year == YEAR_INVALID || year < YEAR_MIN)
            throw new IllegalArgumentException("Invalid year: " + year);

        if (day != DAY_INVALID && (day < DAY_MIN || day > DAY_MAX))
            throw new IllegalArgumentException("Invalid day of month: " + day);
        mDay = day;

        if (month != MONTH_INVALID) {
            if (month < MONTH_MIN || month > MONTH_MAX)
                throw new IllegalArgumentException("Invalid month: " + month);

            mMonth = month;
            mQuarter = QUARTER_INVALID;
        } else if (quarter != QUARTER_INVALID) {
            if (quarter < QUARTER_MIN || quarter > QUARTER_MAX)
                throw new IllegalArgumentException("Invalid quarter: " + quarter);

            mMonth = MONTH_INVALID;
            mQuarter = quarter;
        } else {
            mMonth = MONTH_INVALID;
            mQuarter = QUARTER_INVALID;
        }

        mYear = year;
    }

    public ReleaseDate(String dateStr) throws ParseException {
        Date date = FORMATTER.parse(dateStr);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        mDay = (byte) calendar.get(Calendar.DAY_OF_MONTH);
        mMonth = (byte) (calendar.get(Calendar.MONTH) + 1);
        mYear = (short) calendar.get(Calendar.YEAR);
        mQuarter = QUARTER_INVALID;
    }

    private ReleaseDate() {
        mDay = DAY_INVALID;
        mMonth = MONTH_INVALID;
        mQuarter = QUARTER_INVALID;
        mYear = YEAR_INVALID;
    }

    public byte getDay() { return mDay; }
    public byte getMonth() { return mMonth; }
    public byte getQuarter() { return mQuarter; }
    public short getYear() { return mYear; }

    @Override
    public int compareTo(ReleaseDate another) {
        if (this == another)    return 0;
        if (this == INVALID)    return -1;
        if (another == INVALID) return 1;

        if (this.mYear < another.mYear) return -1;
        if (this.mYear > another.mYear) return 1;

        int comparison;

        comparison = compareMonths(this.mMonth, another.mMonth);
        if (comparison != 0)    return comparison;

        comparison = compareQuarters(this.mQuarter, another.mQuarter);
        if (comparison != 0)    return comparison;

        comparison = compareMonthToQuarter(this.mMonth, another.mQuarter);
        if (comparison != 0)    return comparison;

        comparison = compareQuarterToMonth(this.mQuarter, another.mMonth);
        if (comparison != 0)    return comparison;

        comparison = compareDays(this.mDay, another.mDay);
        if (comparison != 0)    return comparison;

        return 0;
    }

    @Override
    public String toString() {
        if (this == INVALID)
            return "N/A";

        StringBuilder builder = new StringBuilder();

        if (mDay != DAY_INVALID && mMonth != MONTH_INVALID) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, mMonth - 1);       // month argument is zero-based!
            builder.append(mDay).append(' ');
            builder.append(calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US));
            builder.append(' ');
        } else if (mQuarter != QUARTER_INVALID) {
            builder.append('Q').append(mQuarter).append(' ');
        }

        builder.append(mYear);

        return builder.toString();
    }

    private static int compareDays(int lhs, int rhs) {
        if (lhs == rhs) return 0;
        if (lhs != DAY_INVALID && rhs != DAY_INVALID) {
            if (lhs < rhs)  return -1;
            if (lhs > rhs)  return 1;
        }
        if (lhs == DAY_INVALID)   return -1;
        if (rhs == DAY_INVALID)   return 1;
        return 0;
    }

    private static int compareMonths(int lhs, int rhs) {
        if (lhs == rhs) return 0;
        if (lhs != MONTH_INVALID && rhs != MONTH_INVALID) {
            if (lhs < rhs)  return -1;
            if (lhs > rhs)  return 1;
        }
        if (lhs == MONTH_INVALID)   return -1;
        if (rhs == MONTH_INVALID)   return 1;
        return 0;
    }

    private static int compareQuarters(int lhs, int rhs) {
        if (lhs == rhs) return 0;
        if (lhs != QUARTER_INVALID && rhs != QUARTER_INVALID) {
            if (lhs < rhs)  return -1;
            if (lhs > rhs)  return 1;
        }
        if (lhs == QUARTER_INVALID)   return -1;
        if (rhs == QUARTER_INVALID)   return 1;
        return 0;
    }

    private static int compareMonthToQuarter(int month, int quarter) {
        if (month != MONTH_INVALID && quarter != QUARTER_INVALID) {
            int quarterUpperBound = quarter * 3;    // Q1 => Mar, Q2 => Jun, Q3 => Sep, Q4 => Dec
            int comparison = compareMonths(month, quarterUpperBound);
            return (comparison != 0) ? comparison : -1;   // month < quarter if month == quarterUpperBound
        }
        return 0;
    }

    private static int compareQuarterToMonth(int quarter, int month) {
        return -1 * compareMonthToQuarter(month, quarter);
    }

}
