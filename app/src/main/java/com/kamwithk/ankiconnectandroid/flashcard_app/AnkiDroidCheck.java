package com.kamwithk.ankiconnectandroid.flashcard_app;

import java.util.Set;

/**
 * Decides whether AnkiDroid's flashcard provider really belongs to AnkiDroid.
 *
 * Any app can declare AnkiDroid's authority or package name while AnkiDroid is not
 * installed, and would then receive every note this app sends. So AnkiDroid is trusted
 * only if its own package holds the authority and is signed with AnkiDroid's release key.
 */
public final class AnkiDroidCheck {
    public static final String PACKAGE_NAME = "com.ichi2.anki";
    public static final String AUTHORITY = PACKAGE_NAME + ".flashcards";

    /** SHA-256 of AnkiDroid's release signing certificate (Play and GitHub releases, CN=Nicolas Raoul). */
    public static final String RELEASE_CERT_SHA256 = "2071534f0f4b5e54ae952dd275d70da6e3459ee69909d2ab1b4843c4c5b21a45";

    private AnkiDroidCheck() {
    }

    public static boolean isTrusted(InstalledApps apps, AppBuild build) {
        // Another package holding the authority is squatting it
        if (!PACKAGE_NAME.equals(apps.authorityHolder(AUTHORITY))) {
            return false;
        }

        Set<String> digests = apps.signerDigests(PACKAGE_NAME);
        if (digests.isEmpty()) {
            return false;
        }

        // The digests include past keys, so a rotated release key still matches
        if (digests.contains(RELEASE_CERT_SHA256)) {
            return true;
        }

        // Developers run their own AnkiDroid builds, signed with their own key
        return build == AppBuild.DEBUGGABLE;
    }
}
