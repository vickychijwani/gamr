package io.github.vickychijwani.gimmick.view;

import android.app.ActionBar;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.squareup.otto.Subscribe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.pref.AppState;
import io.github.vickychijwani.gimmick.task.FirstRunTask;
import io.github.vickychijwani.gimmick.utility.event.FirstRunTaskDoneEvent;

public abstract class LauncherActivity extends BaseActivity {

    FirstRunDialog mFirstRunDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkFirstRun();
    }

    @Override
    protected void setupActionBar(@NotNull ActionBar actionBar) {
        // nothing to do
    }

    private void checkFirstRun() {
        if (AppState.getInstance().getBoolean(AppState.Key.FIRST_RUN)) {
            mFirstRunDialog = new FirstRunDialog();
            mFirstRunDialog.show(getSupportFragmentManager(), null);
            new FirstRunTask().execute();
        }
    }

    private class FirstRunDialog extends DialogFragment {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_first_run, container, false);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            setCancelable(false);
            return dialog;
        }

    }

    @Override
    protected boolean usesEventBus() {
        return true;
    }

    @NotNull
    @Override
    protected Object getEventHandler() {
        return new Object() {
            @Subscribe public void onFirstRunTaskDone(FirstRunTaskDoneEvent event) {
                // TODO handle success / failure
                mFirstRunDialog.dismiss();
            }
        };
    }
}
