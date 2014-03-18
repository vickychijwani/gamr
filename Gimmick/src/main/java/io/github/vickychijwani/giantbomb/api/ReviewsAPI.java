package io.github.vickychijwani.giantbomb.api;

import android.net.Uri;
import android.util.Log;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.github.vickychijwani.giantbomb.item.Game;
import io.github.vickychijwani.giantbomb.item.ResourceType;
import io.github.vickychijwani.giantbomb.item.Review;
import io.github.vickychijwani.network.volley.VolleyRequestQueue;
import io.github.vickychijwani.utility.DateTimeUtils;

@Singleton
public class ReviewsAPI extends BaseAPI<Review> {

    @Inject
    public ReviewsAPI(VolleyRequestQueue requestQueue, URLFactory urlFactory) {
        super(ResourceType.REVIEW, requestQueue, urlFactory);
    }

    /**
     * Fetch a game's reviews in a <i>synchronous</i> manner.
     * <p/>
     * NOTE: never call this from the UI thread!
     *
     * @param game  the {@link Game} for which to fetch reviews. It must have {@link Review}s with a
     *              valid GiantBomb review ID on each of them.
     * @return      the {@link Game} that was passed in, augmented with the requested review data
     */
    @NotNull
    public Game fetchAllForGame(@NotNull Game game)
            throws ExecutionException, InterruptedException, JSONException {
        Iterator<Review> reviewIterator = game.getReviews();

        while (reviewIterator.hasNext()) {
            Review review = reviewIterator.next();
            Uri uri = newDetailResourceURL(review.getGiantBombId())
                    .setFieldList(ID, REVIEWER, DECK, SCORE, PUBLISH_DATE, SITE_DETAIL_URL)
                    .build();
            assert uri != null;

            Log.i(TAG, "Fetching review from " + uri);

            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            JsonObjectRequest req = new JsonObjectRequest(uri.toString(), null, future, future);
            enqueueRequest(req);

            try {
                JSONObject reviewJson = future.get().getJSONObject(RESULTS);  // block until request completes
                if (reviewJson != null) {
                    itemFromJson(reviewJson, review);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, Log.getStackTraceString(e));
                reviewIterator.remove();
                throw e;
            } catch (ExecutionException e) {
                Log.e(TAG, Log.getStackTraceString(e));
                reviewIterator.remove();
                throw e;
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
                reviewIterator.remove();
                throw e;
            }
        }

        return game;
    }

    @Override
    @NotNull
    Review itemFromJson(@NotNull JSONObject reviewJson, @NotNull Review review) {
        // game id is set automatically when calling Game#addReview()

        // Weirdly enough, review resources don't have an ID in the returned JSON! Aaaargh!
        // But it doesn't matter since the ID should already be correctly set (because it was needed
        // for fetching the review in the first place)
        // review.setGiantBombId(reviewJson.getInt(ID));
        review.setReviewer(reviewJson.optString(REVIEWER));
        review.setTitle(reviewJson.optString(DECK));
        review.setScore(reviewJson.optDouble(SCORE));
        review.setSiteUrl(reviewJson.optString(SITE_DETAIL_URL));

        // publish date
        try {
            review.setPublishDate(DateTimeUtils.isoDateStringToDate(reviewJson.optString(PUBLISH_DATE, DateTimeUtils.getEarliestDateString())));
        } catch (ParseException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return review;
    }

}
