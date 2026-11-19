package com.kamwithk.ankiconnectandroid.flashcard_app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FlashcardAppResolverTest {
    private static final String ANKIDROID_CERT = AnkiDroidCheck.RELEASE_CERT_SHA256;
    private static final String ANKIQUEST_CERT = "bb";
    private static final String OTHER_CERT = "cc";
    private static final String SQUATTER = "com.example.squatter";

    private FakeApps apps;
    private FakePins pins;
    private FlashcardAppResolver resolver;

    @Before
    public void setUp() {
        apps = new FakeApps();
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
    public void ankiDroidOnlyIsUsedWithoutPromptAndPinned() {
        apps.install(FlashcardApp.ANKIDROID, ANKIDROID_CERT);

        FlashcardAppResolver.Resolution resolution = resolver.resolve();

        assertEquals(FlashcardAppResolver.State.READY, resolution.state);
        assertEquals(FlashcardApp.ANKIDROID, resolution.app);
        assertEquals(FlashcardApp.ANKIDROID, pins.pin.app);
        assertEquals(ANKIDROID_CERT, pins.pin.certDigest);
    }

    @Test
    public void ankiQuestOnlyNeedsExplicitChoice() {
        apps.install(FlashcardApp.ANKIQUEST, ANKIQUEST_CERT);

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
        apps.install(FlashcardApp.ANKIDROID, ANKIDROID_CERT);
        apps.install(FlashcardApp.ANKIQUEST, ANKIQUEST_CERT);

        FlashcardAppResolver.Resolution resolution = resolver.resolve();

        assertEquals(FlashcardAppResolver.State.READY, resolution.state);
        assertEquals(FlashcardApp.ANKIDROID, resolution.app);
        assertEquals(Arrays.asList(FlashcardApp.ANKIDROID, FlashcardApp.ANKIQUEST), resolution.candidates);
    }

    @Test
    public void bothInstalledChoiceIsKept() {
        apps.install(FlashcardApp.ANKIDROID, ANKIDROID_CERT);
        apps.install(FlashcardApp.ANKIQUEST, ANKIQUEST_CERT);

        assertTrue(resolver.choose(FlashcardApp.ANKIQUEST));

        assertEquals(FlashcardApp.ANKIQUEST, resolver.resolve().app);
    }

    @Test
    public void chosenAppUninstalledFallsBackToDefault() {
        apps.install(FlashcardApp.ANKIDROID, ANKIDROID_CERT);
        apps.install(FlashcardApp.ANKIQUEST, ANKIQUEST_CERT);
        resolver.choose(FlashcardApp.ANKIQUEST);

        apps.uninstall(FlashcardApp.ANKIQUEST);
        FlashcardAppResolver.Resolution resolution = resolver.resolve();

        assertEquals(FlashcardAppResolver.State.READY, resolution.state);
        assertEquals(FlashcardApp.ANKIDROID, resolution.app);
        assertEquals(FlashcardApp.ANKIDROID, pins.pin.app);
    }

    @Test
    public void chosenAppUninstalledWithoutDefaultIsNoneInstalled() {
        apps.install(FlashcardApp.ANKIQUEST, ANKIQUEST_CERT);
        resolver.choose(FlashcardApp.ANKIQUEST);

        apps.uninstall(FlashcardApp.ANKIQUEST);

        assertEquals(FlashcardAppResolver.State.NONE_INSTALLED, resolver.resolve().state);
        assertNull(pins.pin);
    }

    @Test
    public void certificateChangeResetsChoice() {
        apps.install(FlashcardApp.ANKIDROID, ANKIDROID_CERT);
        apps.install(FlashcardApp.ANKIQUEST, ANKIQUEST_CERT);
        resolver.choose(FlashcardApp.ANKIQUEST);

        apps.install(FlashcardApp.ANKIQUEST, OTHER_CERT);
        FlashcardAppResolver.Resolution resolution = resolver.resolve();

        assertEquals(FlashcardAppResolver.State.NEEDS_CHOICE, resolution.state);
        assertNull(pins.pin);
    }

    @Test
    public void ankiDroidWithoutReleaseKeyIsNoCandidate() {
        apps.install(FlashcardApp.ANKIDROID, OTHER_CERT);

        assertEquals(FlashcardAppResolver.State.NONE_INSTALLED, resolver.resolve().state);
        assertFalse(resolver.choose(FlashcardApp.ANKIDROID));
        assertNull(pins.pin);
    }

    @Test
    public void authorityHeldByUnexpectedPackageIsRejected() {
        apps.install(FlashcardApp.ANKIDROID, ANKIDROID_CERT);
        apps.installed.put(FlashcardApp.ANKIQUEST.packageName, Collections.singleton(ANKIQUEST_CERT));
        apps.authorities.put(FlashcardApp.ANKIQUEST.authority, SQUATTER);

        FlashcardAppResolver.Resolution resolution = resolver.resolve();

        assertEquals(Collections.singletonList(FlashcardApp.ANKIDROID), resolution.candidates);
        assertFalse(resolver.choose(FlashcardApp.ANKIQUEST));
    }

    @Test
    public void authoritySquattedWhileAnkiDroidMissingIsRejected() {
        apps.authorities.put(FlashcardApp.ANKIDROID.authority, SQUATTER);

        assertEquals(FlashcardAppResolver.State.NONE_INSTALLED, resolver.resolve().state);
        assertFalse(resolver.choose(FlashcardApp.ANKIDROID));
        assertNull(pins.pin);
    }

    @Test
    public void unknownStoredAppIsDropped() {
        apps.install(FlashcardApp.ANKIDROID, ANKIDROID_CERT);
        pins.pin = new PinStore.Pin(null, "com.example.removed", OTHER_CERT);

        assertEquals(FlashcardApp.ANKIDROID, resolver.resolve().app);
        assertEquals(FlashcardApp.ANKIDROID, pins.pin.app);
    }

    private static final class FakeApps implements InstalledApps {
        final Map<String, Set<String>> installed = new HashMap<>();
        final Map<String, String> authorities = new HashMap<>();

        void install(FlashcardApp app, String cert) {
            installed.put(app.packageName, new HashSet<>(Collections.singleton(cert)));
            authorities.put(app.authority, app.packageName);
        }

        void uninstall(FlashcardApp app) {
            installed.remove(app.packageName);
            authorities.remove(app.authority);
        }

        @Override
        public Set<String> signerDigests(String packageName) {
            return installed.getOrDefault(packageName, Collections.emptySet());
        }

        @Override
        public String authorityHolder(String authority) {
            return authorities.get(authority);
        }
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
