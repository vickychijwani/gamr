package io.github.vickychijwani.gimmick.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.vickychijwani.gimmick.R;

public class NetworkUtils {

    public static void loadImage(@Nullable String url, @NotNull ImageView imageView) {
        if (url != null) {
            imageView.setVisibility(View.VISIBLE);
            Glide.load(url)
                    .fitCenter()
                    .placeholder(R.color.image_placeholder)
                    .animate(android.R.anim.fade_in)
                    .into(imageView);
        }
    }

    /**
     * Whether there is any network with a usable connection.
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
