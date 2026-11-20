// SPDX-License-Identifier: AGPL-3.0-or-later

package com.kamwithk.ankiconnectandroid.flashcard_app;

/**
 * The closed list of apps whose flashcard provider Ankiconnect Android may talk to.
 * Both use AnkiDroid's provider code, so only the names differ.
 */
public enum FlashcardApp {
    ANKIDROID(AnkiDroidCheck.PACKAGE_NAME),
    ANKIQUEST("org.ankiquest.anki");

    public final String packageName;
    public final String authority;
    public final String permission;

    FlashcardApp(String packageName) {
        this.packageName = packageName;
        this.authority = packageName + ".flashcards";
        this.permission = packageName + ".permission.READ_WRITE_DATABASE";
    }

    /** Returns the app with this enum name, or null for an unknown or missing name. */
    public static FlashcardApp fromName(String name) {
        for (FlashcardApp app : values()) {
            if (app.name().equals(name)) {
                return app;
            }
        }
        return null;
    }
}
