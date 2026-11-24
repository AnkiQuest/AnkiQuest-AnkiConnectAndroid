// SPDX-License-Identifier: AGPL-3.0-or-later

package org.ankiquest.ankiconnectandroid.flashcard_app;

/** Stores the chosen flashcard app together with the certificate it was signed with. */
public interface PinStore {
    final class Pin {
        public final FlashcardApp app;
        public final String packageName;
        public final String certDigest;

        public Pin(FlashcardApp app, String packageName, String certDigest) {
            this.app = app;
            this.packageName = packageName;
            this.certDigest = certDigest;
        }
    }

    /** Returns the stored pin, or null if there is none. */
    Pin load();

    void save(Pin pin);

    void clear();
}
