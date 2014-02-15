package io.github.vickychijwani.gimmick.adapter;

import android.content.Context;
import android.view.View;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.background.TaskManager;
import io.github.vickychijwani.gimmick.item.SearchResult;

public class AddGamesAdapter extends GameListAdapter {

    private static final int LAYOUT = R.layout.component_game_add;

    public AddGamesAdapter(Context context, List<SearchResult> objects,
                           @Nullable View.OnClickListener detailsButtonListener) {
        super(context, LAYOUT, objects, detailsButtonListener);
    }

    @Override
    protected void onGetViewFinished(final SearchResultViewHolder viewHolder, final SearchResult item) {
        // hide add button if already added that show
        viewHolder.addbutton.setVisibility(item.isAdded ? View.INVISIBLE : View.VISIBLE);
        viewHolder.addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskManager.getInstance(getContext()).performAddTask(item);
                item.isAdded = true;
                v.setVisibility(View.INVISIBLE);
            }
        });
    }

}
