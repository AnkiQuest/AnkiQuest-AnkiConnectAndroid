package com.kamwithk.ankiconnectandroid.flashcard_app;

import android.content.Context;

/** Entry point for the rest of the app: which flashcard app to talk to right now. */
public final class FlashcardApps {
    private FlashcardApps() {
    }

    public static FlashcardAppResolver resolver(Context context) {
        return new FlashcardAppResolver(new PackageManagerApps(context.getPackageManager()), new PreferencePinStore(context));
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
                throw new IllegalStateException("AnkiDroid is not installed");
        }
    }
}
