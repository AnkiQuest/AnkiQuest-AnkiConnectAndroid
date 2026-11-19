package com.kamwithk.ankiconnectandroid.flashcard_app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class FlashcardAppResolverTest {
    private static final String ANKIDROID_CERT = AnkiDroidCheck.RELEASE_CERT_SHA256;
    private static final String ANKIQUEST_CERT = "bb";
    private static final String OTHER_CERT = "cc";
    private static final String SQUATTER = "com.example.squatter";

    private FakeInstalledApps apps;
    private FakePins pins;
    private FlashcardAppResolver resolver;

    @Before
    public void setUp() {
        apps = new FakeInstalledApps();
        pins = new FakePins();
        resolver = new FlashcardAppResolver(apps, pins, AppBuild.RELEASE);
    }

    @Test
    public void noneInstalled() {
        FlashcardAppResolver.Resolution resolution = resolver.resolve();

        assertEquals(FlashcardAppResolver.State.NONE_INSTALLED, resolution.state);
        assertTrue(resolution.candidates.isEmpty());
        assertNull(pins.pin);
    }

    @Test
    public void ankiDroidOnlyIsUsedWithoutPromptOrPin() {
        install(FlashcardApp.ANKIDROID, ANKIDROID_CERT);

        FlashcardAppResolver.Resolution resolution = resolver.resolve();

        assertEquals(FlashcardAppResolver.State.READY, resolution.state);
        assertEquals(FlashcardApp.ANKIDROID, resolution.app);
        assertNull(pins.pin);
    }

    @Test
    public void ankiDroidWithoutReleaseKeyIsNoCandidate() {
        install(FlashcardApp.ANKIDROID, OTHER_CERT);

        assertEquals(FlashcardAppResolver.State.NONE_INSTALLED, resolver.resolve().state);
        assertFalse(resolver.choose(FlashcardApp.ANKIDROID));
        assertNull(pins.pin);
    }

    @Test
    public void ankiDroidWithoutReleaseKeyIsUsedWhenDebuggable() {
        install(FlashcardApp.ANKIDROID, OTHER_CERT);
        resolver = new FlashcardAppResolver(apps, pins, AppBuild.DEBUGGABLE);

        assertEquals(FlashcardApp.ANKIDROID, resolver.resolve().app);
    }

    @Test
    public void ankiDroidWithoutReleaseKeyLeavesAnkiQuestToChoose() {
        install(FlashcardApp.ANKIDROID, OTHER_CERT);
        install(FlashcardApp.ANKIQUEST, ANKIQUEST_CERT);

        FlashcardAppResolver.Resolution resolution = resolver.resolve();

        assertEquals(FlashcardAppResolver.State.NEEDS_CHOICE, resolution.state);
        assertEquals(Collections.singletonList(FlashcardApp.ANKIQUEST), resolution.candidates);
    }

    @Test
    public void ankiQuestOnlyNeedsExplicitChoice() {
        install(FlashcardApp.ANKIQUEST, ANKIQUEST_CERT);

        FlashcardAppResolver.Resolution resolution = resolver.resolve();

        assertEquals(FlashcardAppResolver.State.NEEDS_CHOICE, resolution.state);
        assertEquals(Collections.singletonList(FlashcardApp.ANKIQUEST), resolution.candidates);
        assertNull(pins.pin);

        assertTrue(resolver.choose(FlashcardApp.ANKIQUEST));
        resolution = resolver.resolve();
        assertEquals(FlashcardAppResolver.State.READY, resolution.state);
        assertEquals(FlashcardApp.ANKIQUEST, resolution.app);
        assertEquals(ANKIQUEST_CERT, pins.pin.certDigest);
    }

    @Test
    public void bothInstalledDefaultsToAnkiDroid() {
        install(FlashcardApp.ANKIDROID, ANKIDROID_CERT);
        install(FlashcardApp.ANKIQUEST, ANKIQUEST_CERT);

        FlashcardAppResolver.Resolution resolution = resolver.resolve();

        assertEquals(FlashcardAppResolver.State.READY, resolution.state);
        assertEquals(FlashcardApp.ANKIDROID, resolution.app);
        assertEquals(Arrays.asList(FlashcardApp.ANKIDROID, FlashcardApp.ANKIQUEST), resolution.candidates);
    }

    @Test
    public void bothInstalledChoiceIsKept() {
        install(FlashcardApp.ANKIDROID, ANKIDROID_CERT);
        install(FlashcardApp.ANKIQUEST, ANKIQUEST_CERT);

        assertTrue(resolver.choose(FlashcardApp.ANKIQUEST));

        assertEquals(FlashcardApp.ANKIQUEST, resolver.resolve().app);
    }

    @Test
    public void choosingAnkiDroidDropsThePin() {
        install(FlashcardApp.ANKIDROID, ANKIDROID_CERT);
        install(FlashcardApp.ANKIQUEST, ANKIQUEST_CERT);
        resolver.choose(FlashcardApp.ANKIQUEST);

        assertTrue(resolver.choose(FlashcardApp.ANKIDROID));

        assertNull(pins.pin);
        assertEquals(FlashcardApp.ANKIDROID, resolver.resolve().app);
    }

    @Test
    public void chosenAppUninstalledFallsBackToAnkiDroid() {
        install(FlashcardApp.ANKIDROID, ANKIDROID_CERT);
        install(FlashcardApp.ANKIQUEST, ANKIQUEST_CERT);
        resolver.choose(FlashcardApp.ANKIQUEST);

        uninstall(FlashcardApp.ANKIQUEST);
        FlashcardAppResolver.Resolution resolution = resolver.resolve();

        assertEquals(FlashcardAppResolver.State.READY, resolution.state);
        assertEquals(FlashcardApp.ANKIDROID, resolution.app);
        assertNull(pins.pin);
    }

    @Test
    public void chosenAppUninstalledWithoutAnkiDroidIsNoneInstalled() {
        install(FlashcardApp.ANKIQUEST, ANKIQUEST_CERT);
        resolver.choose(FlashcardApp.ANKIQUEST);

        uninstall(FlashcardApp.ANKIQUEST);

        assertEquals(FlashcardAppResolver.State.NONE_INSTALLED, resolver.resolve().state);
        assertNull(pins.pin);
    }

    @Test
    public void certificateChangeFallsBackToAnkiDroid() {
        install(FlashcardApp.ANKIDROID, ANKIDROID_CERT);
        install(FlashcardApp.ANKIQUEST, ANKIQUEST_CERT);
        resolver.choose(FlashcardApp.ANKIQUEST);

        install(FlashcardApp.ANKIQUEST, OTHER_CERT);
        FlashcardAppResolver.Resolution resolution = resolver.resolve();

        assertEquals(FlashcardAppResolver.State.READY, resolution.state);
        assertEquals(FlashcardApp.ANKIDROID, resolution.app);
        assertNull(pins.pin);
    }

    @Test
    public void certificateChangeWithoutAnkiDroidAsksAgain() {
        install(FlashcardApp.ANKIQUEST, ANKIQUEST_CERT);
        resolver.choose(FlashcardApp.ANKIQUEST);

        install(FlashcardApp.ANKIQUEST, OTHER_CERT);
        FlashcardAppResolver.Resolution resolution = resolver.resolve();

        assertEquals(FlashcardAppResolver.State.NEEDS_CHOICE, resolution.state);
        assertNull(pins.pin);

        // Picking it again trusts the new certificate
        assertTrue(resolver.choose(FlashcardApp.ANKIQUEST));
        assertEquals(OTHER_CERT, pins.pin.certDigest);
    }

    @Test
    public void certificateChangeWithUntrustedAnkiDroidAsksAgain() {
        install(FlashcardApp.ANKIDROID, OTHER_CERT);
        install(FlashcardApp.ANKIQUEST, ANKIQUEST_CERT);
        resolver.choose(FlashcardApp.ANKIQUEST);

        install(FlashcardApp.ANKIQUEST, OTHER_CERT);

        assertEquals(FlashcardAppResolver.State.NEEDS_CHOICE, resolver.resolve().state);
        assertNull(pins.pin);
    }

    @Test
    public void authorityHeldByUnexpectedPackageIsRejected() {
        install(FlashcardApp.ANKIDROID, ANKIDROID_CERT);
        install(FlashcardApp.ANKIQUEST, ANKIQUEST_CERT);
        apps.squat(FlashcardApp.ANKIQUEST.authority, SQUATTER);

        FlashcardAppResolver.Resolution resolution = resolver.resolve();

        assertEquals(Collections.singletonList(FlashcardApp.ANKIDROID), resolution.candidates);
        assertFalse(resolver.choose(FlashcardApp.ANKIQUEST));
    }

    @Test
    public void authoritySquattedWhileAnkiDroidMissingIsRejected() {
        apps.squat(FlashcardApp.ANKIDROID.authority, SQUATTER);

        assertEquals(FlashcardAppResolver.State.NONE_INSTALLED, resolver.resolve().state);
        assertFalse(resolver.choose(FlashcardApp.ANKIDROID));
        assertNull(pins.pin);
    }

    @Test
    public void unknownStoredAppIsDropped() {
        install(FlashcardApp.ANKIDROID, ANKIDROID_CERT);
        pins.pin = new PinStore.Pin(null, "com.example.removed", OTHER_CERT);

        assertEquals(FlashcardApp.ANKIDROID, resolver.resolve().app);
        assertNull(pins.pin);
    }

    private void install(FlashcardApp app, String cert) {
        apps.install(app.packageName, app.authority, cert);
    }

    private void uninstall(FlashcardApp app) {
        apps.uninstall(app.packageName, app.authority);
    }

    private static final class FakePins implements PinStore {
        Pin pin;

        @Override
        public Pin load() {
            return pin;
        }

        @Override
        public void save(Pin pin) {
            this.pin = pin;
        }

        @Override
        public void clear() {
            pin = null;
        }
    }
}
