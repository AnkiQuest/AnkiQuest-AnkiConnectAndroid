package com.kamwithk.ankiconnectandroid.flashcard_app;

import android.content.Context;
import android.content.pm.ApplicationInfo;

/** Entry point for the rest of the app: whether AnkiDroid's provider may be used right now. */
public final class FlashcardApps {
    private static final String NOT_TRUSTED =
            "AnkiDroid is not installed, or the installed app is not signed with a release key of AnkiDroid (Google Play, F-Droid or GitHub)";

    private FlashcardApps() {
    }

    /** Throws unless AnkiDroid is genuine; checked on every call, so a replaced app is never used unnoticed. */
    public static void requireAnkiDroid(Context context) {
        InstalledApps apps = new PackageManagerApps(context.getPackageManager());
        if (!AnkiDroidCheck.isTrusted(apps, appBuild(context))) {
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
