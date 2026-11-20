// SPDX-License-Identifier: AGPL-3.0-or-later

package com.kamwithk.ankiconnectandroid.flashcard_app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Decides which flashcard app to talk to.
 *
 * Rules:
 * - A candidate is a known app that is installed and actually holds its own authority,
 *   so an app squatting the authority is rejected. AnkiDroid must also pass AnkiDroidCheck
 *   (its release key, or any key in a debuggable build of this app).
 * - AnkiDroid is trusted by its release key and never pinned. It is used whenever it is a
 *   candidate and no other app was chosen.
 * - Any other app needs an explicit choice, which pins its certificate. The choice is used
 *   only while that certificate still matches. If the app is gone or re-signed, the choice
 *   is dropped: AnkiDroid is used if it is a candidate, otherwise the user is asked again.
 */
public class FlashcardAppResolver {
    public enum State { READY, NEEDS_CHOICE, NONE_INSTALLED }

    public static final class Resolution {
        public final State state;
        /** The app to use; set only when READY. */
        public final FlashcardApp app;
        /** The apps the user may choose from; empty when NONE_INSTALLED. */
        public final List<FlashcardApp> candidates;

        private Resolution(State state, FlashcardApp app, List<FlashcardApp> candidates) {
            this.state = state;
            this.app = app;
            this.candidates = Collections.unmodifiableList(candidates);
        }
    }

    private final InstalledApps installedApps;
    private final PinStore pinStore;
    private final AppBuild appBuild;

    public FlashcardAppResolver(InstalledApps installedApps, PinStore pinStore, AppBuild appBuild) {
        this.installedApps = installedApps;
        this.pinStore = pinStore;
        this.appBuild = appBuild;
    }

    /** Known apps that are installed and hold their own authority, in declaration order. */
    public List<FlashcardApp> candidates() {
        List<FlashcardApp> candidates = new ArrayList<>();
        for (FlashcardApp app : FlashcardApp.values()) {
            if (isCandidate(app)) {
                candidates.add(app);
            }
        }
        return candidates;
    }

    public Resolution resolve() {
        List<FlashcardApp> candidates = candidates();

        // A stored choice wins while it is still installed and signed by the pinned certificate
        PinStore.Pin pin = pinStore.load();
        if (pin != null) {
            if (isPinValid(pin, candidates)) {
                return new Resolution(State.READY, pin.app, candidates);
            }

            // Gone, re-signed or unknown: drop it and decide as if nothing had been chosen
            pinStore.clear();
        }

        // AnkiDroid's release key is built in, so it needs no choice
        if (candidates.contains(FlashcardApp.ANKIDROID)) {
            return new Resolution(State.READY, FlashcardApp.ANKIDROID, candidates);
        }

        if (candidates.isEmpty()) {
            return new Resolution(State.NONE_INSTALLED, null, candidates);
        }
        return new Resolution(State.NEEDS_CHOICE, null, candidates);
    }

    /** Stores the user's choice. Returns false if it isn't a candidate. */
    public boolean choose(FlashcardApp app) {
        if (!isCandidate(app)) {
            return false;
        }

        // AnkiDroid is used by default whenever nothing else is chosen
        if (app == FlashcardApp.ANKIDROID) {
            pinStore.clear();
            return true;
        }

        pin(app);
        return true;
    }

    private boolean isCandidate(FlashcardApp app) {
        if (app == FlashcardApp.ANKIDROID) {
            return AnkiDroidCheck.isTrusted(installedApps, appBuild);
        }
        if (installedApps.signerDigests(app.packageName).isEmpty()) {
            return false;
        }
        return app.packageName.equals(installedApps.authorityHolder(app.authority));
    }

    private boolean isPinValid(PinStore.Pin pin, List<FlashcardApp> candidates) {
        if (pin.app == null || !candidates.contains(pin.app)) {
            return false;
        }
        if (!pin.app.packageName.equals(pin.packageName)) {
            return false;
        }
        return installedApps.signerDigests(pin.app.packageName).contains(pin.certDigest);
    }

    private void pin(FlashcardApp app) {
        // Pin the lexically first digest so the choice is stable across calls
        String digest = Collections.min(installedApps.signerDigests(app.packageName));
        pinStore.save(new PinStore.Pin(app, app.packageName, digest));
    }
}
