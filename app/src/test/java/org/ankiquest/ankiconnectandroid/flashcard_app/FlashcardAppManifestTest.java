// SPDX-License-Identifier: AGPL-3.0-or-later

package org.ankiquest.ankiconnectandroid.flashcard_app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FlashcardAppManifestTest {
    /** Without these, Android hides the app (package visibility) or never shows its permission dialog */
    @Test
    public void manifestDeclaresEveryKnownApp() throws Exception {
        // Gradle runs unit tests from the module directory
        String manifest = new String(Files.readAllBytes(new File("src/main/AndroidManifest.xml").toPath()), StandardCharsets.UTF_8);

        for (FlashcardApp app : FlashcardApp.values()) {
            assertTrue(app.permission, manifest.contains("<uses-permission android:name=\"" + app.permission + "\""));
            assertTrue(app.packageName, manifest.contains("<package android:name=\"" + app.packageName + "\""));
            assertTrue(app.authority, manifest.contains("<provider android:authorities=\"" + app.authority + "\""));
        }
    }

    /** Every AnkiQuest build installs as org.ankiquest (AnkiQuest-Android#461) */
    @Test
    public void ankiQuestIsOrgAnkiquest() {
        FlashcardApp app = FlashcardApp.ANKIQUEST;

        assertEquals("org.ankiquest", app.packageName);
        assertEquals("org.ankiquest.flashcards", app.authority);
        assertEquals("org.ankiquest.permission.READ_WRITE_DATABASE", app.permission);
    }
}
