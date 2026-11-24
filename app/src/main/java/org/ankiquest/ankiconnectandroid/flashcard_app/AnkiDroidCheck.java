// SPDX-License-Identifier: AGPL-3.0-or-later

package org.ankiquest.ankiconnectandroid.flashcard_app;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Decides whether AnkiDroid's flashcard provider really belongs to AnkiDroid.
 *
 * Any app can declare AnkiDroid's authority or package name while AnkiDroid is not
 * installed, and would then receive every note this app sends. So AnkiDroid is trusted
 * only if its own package holds the authority and is signed with a release key of AnkiDroid's
 * (its own, or F-Droid's for the F-Droid build).
 */
public final class AnkiDroidCheck {
    public static final String PACKAGE_NAME = "com.ichi2.anki";
    public static final String AUTHORITY = PACKAGE_NAME + ".flashcards";

    /** SHA-256 of AnkiDroid's release signing certificate (Play and GitHub releases, CN=Nicolas Raoul). */
    public static final String RELEASE_CERT_SHA256 = "2071534f0f4b5e54ae952dd275d70da6e3459ee69909d2ab1b4843c4c5b21a45";

    /** SHA-256 of the certificate F-Droid signs its own AnkiDroid builds with (com.ichi2.anki_225033004.apk). */
    public static final String FDROID_CERT_SHA256 = "24a3d4756c62dd69c86df69aa7fa327b2a6bf55f55c4a1749f99bd1da75906bb";

    /** Every key a released AnkiDroid is signed with. */
    public static final Set<String> RELEASE_CERTS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(RELEASE_CERT_SHA256, FDROID_CERT_SHA256)));

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
        if (!Collections.disjoint(digests, RELEASE_CERTS)) {
            return true;
        }

        // Developers run their own AnkiDroid builds, signed with their own key
        return build == AppBuild.DEBUGGABLE;
    }
}
