package com.kamwithk.ankiconnectandroid.flashcard_app;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.kamwithk.ankiconnectandroid.R;

import java.util.List;
import java.util.function.Consumer;

/** "Flashcard app" dialog: lists the installed known apps by name and icon and pins the one picked. */
public final class FlashcardAppPicker {
    private static final int ICON_SIZE_DP = 40;
    private static final int ICON_PADDING_DP = 16;

    private FlashcardAppPicker() {
    }

    public static void show(Activity activity, Consumer<FlashcardApp> onChosen) {
        FlashcardAppResolver resolver = FlashcardApps.resolver(activity);
        List<FlashcardApp> candidates = resolver.candidates();
        if (candidates.isEmpty()) {
            Toast.makeText(activity, R.string.flashcard_app_none, Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(activity)
                .setTitle(R.string.flashcard_app_title)
                .setAdapter(new AppAdapter(activity, candidates), (dialog, which) -> {
                    FlashcardApp app = candidates.get(which);
                    if (resolver.choose(app)) {
                        onChosen.accept(app);
                    }
                })
                .show();
    }

    /** The app's name as the system shows it, falling back to its package name. */
    public static CharSequence label(PackageManager packageManager, FlashcardApp app) {
        try {
            return packageManager.getApplicationLabel(packageManager.getApplicationInfo(app.packageName, 0));
        } catch (PackageManager.NameNotFoundException e) {
            return app.packageName;
        }
    }

    private static final class AppAdapter extends ArrayAdapter<FlashcardApp> {
        private final PackageManager packageManager;
        private final int iconSize;
        private final int iconPadding;

        AppAdapter(Activity activity, List<FlashcardApp> apps) {
            super(activity, android.R.layout.select_dialog_item, android.R.id.text1, apps);
            packageManager = activity.getPackageManager();
            float density = activity.getResources().getDisplayMetrics().density;
            iconSize = Math.round(ICON_SIZE_DP * density);
            iconPadding = Math.round(ICON_PADDING_DP * density);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            FlashcardApp app = getItem(position);
            view.setText(label(packageManager, app));

            // Show the icon the system has for the installed app, so the user sees which app it is
            try {
                Drawable icon = packageManager.getApplicationIcon(app.packageName);
                icon.setBounds(0, 0, iconSize, iconSize);
                view.setCompoundDrawablesRelative(icon, null, null, null);
                view.setCompoundDrawablePadding(iconPadding);
            } catch (PackageManager.NameNotFoundException e) {
                view.setCompoundDrawablesRelative(null, null, null, null);
            }
            return view;
        }
    }
}
