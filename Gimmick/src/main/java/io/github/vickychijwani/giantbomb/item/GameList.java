package io.github.vickychijwani.giantbomb.item;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GameList extends ArrayList<Game> {

    private static Comparator<Game> mLatestFirstComparator = null;
    private static Comparator<Game> mEarliestFirstComparator = null;

    /**
     * Construct an empty {@link GameList}.
     */
    public GameList() {
        super();
    }

    /**
     * Construct a {@link GameList} having the specified capacity.
     *
     * @param capacity  the capacity of this list
     */
    public GameList(int capacity) {
        super(capacity);
    }

    /**
     * Construct a {@link GameList} from a {@link Cursor} object. The cursor must contain rows suitable for
     * constructing individual {@link Game} objects.
     *
     * @param cursor    the cursor from which to construct the list
     * @see Game#Game(Cursor)
     */
    public GameList(Cursor cursor) {
        super(cursor.getCount());
        cursor.moveToPosition(-1);  // move to beginning
        while (cursor.moveToNext()) {
            add(new Game(cursor));
        }
    }

    /**
     * Convenience method for call to {@link #sort}({@link SortOrder#LATEST_FIRST}).
     *
     * @see #sort(GameList.SortOrder)
     */
    public void sortByLatestFirst() {
        sort(SortOrder.LATEST_FIRST);
    }

    /**
     * Convenience method for call to {@link #sort}({@link SortOrder#EARLIEST_FIRST}).
     *
     * @see #sort(GameList.SortOrder)
     */
    public void sortByEarliestFirst() {
        sort(SortOrder.EARLIEST_FIRST);
    }

    /**
     * Sort the list as per the given ordering.
     *
     * @param sortOrder the order in which to sort
     */
    public void sort(SortOrder sortOrder) {
        switch (sortOrder) {
            case EARLIEST_FIRST:
                Collections.sort(this, getEarliestFirstComparator());
                break;
            case LATEST_FIRST:
                Collections.sort(this, getLatestFirstComparator());
                break;
            default:
                throw new IllegalArgumentException("invalid sort order");
        }
    }

    public static enum SortOrder {
        /**
         * Games released (or to be released) earlier appear on top
         */
        EARLIEST_FIRST,

        /**
         * Games released (or to be released) later appear on top
         */
        LATEST_FIRST,
    }

    private Comparator<Game> getLatestFirstComparator() {
        if (mLatestFirstComparator == null) {
            mLatestFirstComparator = new Comparator<Game>() {
                @Override
                public int compare(Game lhs, Game rhs) {
                    return -lhs.releaseDate.compareTo(rhs.releaseDate);
                }
            };
        }
        return mLatestFirstComparator;
    }

    private Comparator<Game> getEarliestFirstComparator() {
        if (mEarliestFirstComparator == null) {
            mEarliestFirstComparator = new Comparator<Game>() {
                @Override
                public int compare(Game lhs, Game rhs) {
                    return lhs.releaseDate.compareTo(rhs.releaseDate);
                }
            };
        }
        return mEarliestFirstComparator;
    }

}
