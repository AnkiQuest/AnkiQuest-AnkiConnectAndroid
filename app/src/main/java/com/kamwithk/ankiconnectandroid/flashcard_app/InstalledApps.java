// SPDX-License-Identifier: AGPL-3.0-or-later

package com.kamwithk.ankiconnectandroid.flashcard_app;

import java.util.Set;

/** What the system knows about installed apps. Keeps PackageManager out of the selection logic. */
public interface InstalledApps {
    /** SHA-256 digests (lowercase hex) of the package's signing certificates; empty if not installed. */
    Set<String> signerDigests(String packageName);

    /** Package that holds the content provider authority, or null if none does. */
    String authorityHolder(String authority);
}
