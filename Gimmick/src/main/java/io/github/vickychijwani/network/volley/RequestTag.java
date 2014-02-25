package io.github.vickychijwani.network.volley;

public final class RequestTag {

    /** default tag for network requests */
    static final RequestTag DEFAULT = new RequestTag(0);

    private static int sLastId = DEFAULT.mId;

    private int mId;    // unique identifier representing this tag

    private RequestTag(int id) { mId = id; }

    /**
     * Generates a new, unique {@link RequestTag} that can be used to identify a set of requests.
     *
     * @return a unique {@link RequestTag}, guaranteed to never be generated again, within
     * reasonable limits, i.e., a maximum of {@link Integer#MAX_VALUE} unique tags can be generated,
     * after which the uniqueness guarantee breaks down.
     */
    public static RequestTag generate() {
        return new RequestTag(++sLastId);
    }

}
