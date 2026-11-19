package com.kamwithk.ankiconnectandroid.flashcard_app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Decides which flashcard app to talk to.
 *
 * Rules:
 * - A candidate is a known app that is installed and actually holds its own authority,
 *   so an app squatting the authority is rejected.
 * - The stored choice is used only while its certificate still matches. If the certificate
 *   changed, the choice is dropped and the user asked again; if the app is gone, the
 *   choice is dropped and the default used.
 * - Without a valid choice, AnkiDroid is used and its certificate pinned on first use.
 *   Any other app needs an explicit choice: the app never switches silently.
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

    public FlashcardAppResolver(InstalledApps installedApps, PinStore pinStore) {
        this.installedApps = installedApps;
        this.pinStore = pinStore;
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

        // A stored choice wins while it is still installed and signed by the same certificate
        PinStore.Pin pin = pinStore.load();
        if (pin != null) {
            if (isPinValid(pin, candidates)) {
                return new Resolution(State.READY, pin.app, candidates);
            }
            pinStore.clear();

            // Still installed but signed by another certificate: ask instead of trusting it
            if (candidates.contains(pin.app)) {
                return new Resolution(State.NEEDS_CHOICE, null, candidates);
            }
        }

        if (candidates.isEmpty()) {
            return new Resolution(State.NONE_INSTALLED, null, candidates);
        }

        // AnkiDroid is the default: use it as before and pin its certificate on first use
        if (candidates.contains(FlashcardApp.DEFAULT) && pinNow(FlashcardApp.DEFAULT)) {
            return new Resolution(State.READY, FlashcardApp.DEFAULT, candidates);
        }

        return new Resolution(State.NEEDS_CHOICE, null, candidates);
    }

    /** Stores the user's choice with the app's current certificate. Returns false if it isn't a candidate. */
    public boolean choose(FlashcardApp app) {
        if (!isCandidate(app)) {
            return false;
        }
        return pinNow(app);
    }

    private boolean isCandidate(FlashcardApp app) {
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

    private boolean pinNow(FlashcardApp app) {
        Set<String> digests = installedApps.signerDigests(app.packageName);
        if (digests.isEmpty()) {
            return false;
        }

        // Pin the lexically first digest so the choice is stable across calls
        String digest = Collections.min(digests);
        pinStore.save(new PinStore.Pin(app, app.packageName, digest));
        return true;
    }
}
