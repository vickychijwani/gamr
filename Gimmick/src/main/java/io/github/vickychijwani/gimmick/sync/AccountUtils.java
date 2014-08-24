package io.github.vickychijwani.gimmick.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.vickychijwani.gimmick.GamrApplication;

public class AccountUtils {

    public static final String TAG = "AccountUtils";

    private static final int SYNC_FREQUENCY = 24 * 60 * 60; // 1 day (in seconds)
    private static final String ACCOUNT_TYPE = "io.github.vickychijwani.gimmick";
    private static final String ACCOUNT_NAME = "Gamr Sync";

    public static void createAccount(@NotNull Context context) {
        Log.i(TAG, "Setting up sync account...");

        AccountManager manager = AccountManager.get(context);
        Account account = new Account(ACCOUNT_NAME, ACCOUNT_TYPE);

        boolean isNewAccountAdded;
        try {
            isNewAccountAdded = (manager != null) && manager.addAccountExplicitly(account, null, null);
        } catch (SecurityException e) {
            Log.e(TAG, "FAILED to set up sync account");
            return;
        }

        if (isNewAccountAdded) {
            final String contentAuthority = GamrApplication.CONTENT_AUTHORITY;
            // inform the system that this account supports sync
            ContentResolver.setIsSyncable(account, contentAuthority, 1);

            // inform the system that this account is eligible for auto sync when the network is up
            ContentResolver.setSyncAutomatically(account, contentAuthority, true);

            // recommend a schedule for automatic synchronization
            // the system may modify this based on other scheduled syncs and network utilization
            ContentResolver.addPeriodicSync(account, contentAuthority, new Bundle(), SYNC_FREQUENCY);
        }
    }

    @Nullable
    public static Account getAccount(Context context) {
        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE);

        // return first available account
        return (accounts.length > 0) ? accounts[0] : null;
    }

    public static boolean isAccountExists(@NotNull Context context) {
        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE);
        return accounts.length > 0;
    }

}
