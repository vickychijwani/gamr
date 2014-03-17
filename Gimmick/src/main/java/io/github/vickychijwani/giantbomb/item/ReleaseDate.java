package io.github.vickychijwani.giantbomb.item;

import android.database.Cursor;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.github.vickychijwani.gimmick.database.DatabaseContract;

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
     * @param day       Any value in the range [{@link #DAY_MIN}, {@link #DAY_MAX}], inclusive,
     *                  or {@link #DAY_INVALID}
     * @param month     Any value in the range [{@link #MONTH_MIN}, {@link #MONTH_MAX}], inclusive,
     *                  or {@link #MONTH_INVALID}
     * @param quarter   Any value in the range [{@link #QUARTER_MIN}, {@link #QUARTER_MAX}], inclusive,
     *                  or {@link #QUARTER_INVALID}
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

    public ReleaseDate(String dateStr)
            throws ParseException {
        this(FORMATTER.parse(dateStr));
    }

    public ReleaseDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        mDay = (byte) calendar.get(Calendar.DAY_OF_MONTH);
        mMonth = (byte) (calendar.get(Calendar.MONTH) + 1);
        mYear = (short) calendar.get(Calendar.YEAR);
        mQuarter = QUARTER_INVALID;
    }

    public ReleaseDate(Cursor cursor) {
        this((byte) cursor.getShort(cursor.getColumnIndexOrThrow(DatabaseContract.GameTable.COL_RELEASE_DAY)),
                (byte) cursor.getShort(cursor.getColumnIndexOrThrow(DatabaseContract.GameTable.COL_RELEASE_MONTH)),
                (byte) cursor.getShort(cursor.getColumnIndexOrThrow(DatabaseContract.GameTable.COL_RELEASE_QUARTER)),
                cursor.getShort(cursor.getColumnIndexOrThrow(DatabaseContract.GameTable.COL_RELEASE_YEAR)));
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

    /**
     * Compare this {@link ReleaseDate} to another.
     * <p/>
     * <code>
     *     31st Dec 2014
     *     < Dec 2014
     *     < Q4 2014
     *     < 2014
     *     < {@link ReleaseDate#INVALID}
     *     == {@link ReleaseDate#INVALID}
     * </code>
     *
     * @param another    the {@link ReleaseDate} to compare against
     * @return  -1, 0, or 1 depending on whether this {@link ReleaseDate} occurs before, with, or after
     *          {@code another}
     */
    @Override
    public int compareTo(@NotNull ReleaseDate another) {
        if (this.equals(another))    return 0;
        if (INVALID.equals(this))    return 1;
        if (INVALID.equals(another)) return -1;

        if (this.mYear < another.mYear) return -1;
        if (this.mYear > another.mYear) return 1;

        int comparison;

        if (this.mMonth != MONTH_INVALID && another.mMonth != MONTH_INVALID) {
            comparison = compareMonths(this.mMonth, another.mMonth);
            if (comparison != 0)
                return comparison;
        }

        if (this.mQuarter != QUARTER_INVALID && another.mQuarter != QUARTER_INVALID) {
            comparison = compareQuarters(this.mQuarter, another.mQuarter);
            if (comparison != 0)
                return comparison;
        }

        if (this.mMonth != MONTH_INVALID && another.mQuarter != QUARTER_INVALID) {
            comparison = compareMonthToQuarter(this.mMonth, another.mQuarter);
            if (comparison != 0)
                return comparison;
        }

        if (this.mQuarter != QUARTER_INVALID && another.mMonth != MONTH_INVALID) {
            comparison = compareQuarterToMonth(this.mQuarter, another.mMonth);
            if (comparison != 0)
                return comparison;
        }

        comparison = compareDays(this.mDay, another.mDay);
        if (comparison != 0)    return comparison;

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (! (o instanceof ReleaseDate))
            throw new IllegalArgumentException("only ReleaseDate objects can be checked for equality");
        ReleaseDate other = (ReleaseDate) o;
        return mDay == other.mDay &&
                mMonth == other.mMonth &&
                mQuarter == other.mQuarter &&
                mYear == other.mYear;
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
        } else if (mMonth != MONTH_INVALID) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, mMonth - 1);       // month argument is zero-based!
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
        if (lhs == DAY_INVALID)   return 1;
        if (rhs == DAY_INVALID)   return -1;
        return 0;
    }

    private static int compareMonths(int lhs, int rhs) {
        if (lhs == rhs) return 0;
        if (lhs != MONTH_INVALID && rhs != MONTH_INVALID) {
            if (lhs < rhs)  return -1;
            if (lhs > rhs)  return 1;
        }
        if (lhs == MONTH_INVALID)   return 1;
        if (rhs == MONTH_INVALID)   return -1;
        return 0;
    }

    private static int compareQuarters(int lhs, int rhs) {
        if (lhs == rhs) return 0;
        if (lhs != QUARTER_INVALID && rhs != QUARTER_INVALID) {
            if (lhs < rhs)  return -1;
            if (lhs > rhs)  return 1;
        }
        if (lhs == QUARTER_INVALID)   return 1;
        if (rhs == QUARTER_INVALID)   return -1;
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
