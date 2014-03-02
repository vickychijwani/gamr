package io.github.vickychijwani.gimmick.view.adapter;

import android.content.Context;
import android.view.View;

import org.jetbrains.annotations.Nullable;

import io.github.vickychijwani.giantbomb.item.Game;
import io.github.vickychijwani.giantbomb.item.GameList;
import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.task.TaskManager;

public class AddGamesAdapter extends GameListAdapter {

    private static final int LAYOUT = R.layout.component_game_add;

    public AddGamesAdapter(Context context, GameList objects,
                           @Nullable View.OnClickListener detailsButtonListener) {
        super(context, LAYOUT, objects, detailsButtonListener);
    }

    @Override
    protected void onGetViewFinished(final GameViewHolder viewHolder, final Game item) {
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
