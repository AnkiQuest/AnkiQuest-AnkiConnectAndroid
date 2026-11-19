package com.kamwithk.ankiconnectandroid.flashcard_app;

import android.content.Context;
import android.content.pm.ApplicationInfo;

/** Entry point for the rest of the app: which flashcard app to talk to right now. */
public final class FlashcardApps {
    private static final String NOT_TRUSTED =
            "AnkiDroid is not installed, or the installed app is not signed with AnkiDroid's release key";

    private FlashcardApps() {
    }

    public static FlashcardAppResolver resolver(Context context) {
        return new FlashcardAppResolver(new PackageManagerApps(context.getPackageManager()), new PreferencePinStore(context), appBuild(context));
    }

    /**
     * The app to use for this call, checked again each time so a reinstalled or
     * re-signed app is never used unnoticed.
     */
    public static FlashcardApp require(Context context) {
        FlashcardAppResolver.Resolution resolution = resolver(context).resolve();
        switch (resolution.state) {
            case READY:
                return resolution.app;
            case NEEDS_CHOICE:
                throw new IllegalStateException("Choose a flashcard app in Ankiconnect Android's settings");
            default:
                throw new IllegalStateException(NOT_TRUSTED);
        }
    }

    public static AppBuild appBuild(Context context) {
        if ((context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            return AppBuild.DEBUGGABLE;
        }
        return AppBuild.RELEASE;
    }
}
