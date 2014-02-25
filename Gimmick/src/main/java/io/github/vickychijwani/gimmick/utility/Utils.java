package io.github.vickychijwani.gimmick.utility;

import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.vickychijwani.gimmick.R;

public class Utils {

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

}
