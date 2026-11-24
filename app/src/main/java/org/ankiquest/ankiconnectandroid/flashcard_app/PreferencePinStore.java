// SPDX-License-Identifier: AGPL-3.0-or-later

package org.ankiquest.ankiconnectandroid.flashcard_app;

import android.content.Context;
import android.content.SharedPreferences;

/** PinStore backed by its own SharedPreferences file. */
public class PreferencePinStore implements PinStore {
    private static final String FILE_NAME = "flashcard_app";
    private static final String KEY_APP = "app";
    private static final String KEY_PACKAGE = "package";
    private static final String KEY_CERT = "cert_sha256";

    private final SharedPreferences preferences;

    public PreferencePinStore(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public Pin load() {
        String packageName = preferences.getString(KEY_PACKAGE, null);
        String certDigest = preferences.getString(KEY_CERT, null);
        if (packageName == null || certDigest == null) {
            return null;
        }
        return new Pin(FlashcardApp.fromName(preferences.getString(KEY_APP, null)), packageName, certDigest);
    }

    @Override
    public void save(Pin pin) {
        preferences.edit()
                .putString(KEY_APP, pin.app.name())
                .putString(KEY_PACKAGE, pin.packageName)
                .putString(KEY_CERT, pin.certDigest)
                .apply();
    }

    @Override
    public void clear() {
        preferences.edit().clear().apply();
    }
}
