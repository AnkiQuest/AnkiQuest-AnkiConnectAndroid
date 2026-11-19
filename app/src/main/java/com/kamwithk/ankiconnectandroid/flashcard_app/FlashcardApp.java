package com.kamwithk.ankiconnectandroid.flashcard_app;

/**
 * The closed list of apps whose flashcard provider Ankiconnect Android may talk to.
 * Both use AnkiDroid's provider code, so only the names differ.
 */
public enum FlashcardApp {
    ANKIDROID("com.ichi2.anki"),
    ANKIQUEST("org.ankiquest.anki");

    public static final FlashcardApp DEFAULT = ANKIDROID;

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
