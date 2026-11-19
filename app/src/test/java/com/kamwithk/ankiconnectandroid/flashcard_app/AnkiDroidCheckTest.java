package com.kamwithk.ankiconnectandroid.flashcard_app;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class AnkiDroidCheckTest {
    private static final String OTHER_CERT = "cc";
    private static final String ROTATED_CERT = "dd";
    private static final String SQUATTER = "com.example.squatter";

    private FakeInstalledApps apps;

    @Before
    public void setUp() {
        apps = new FakeInstalledApps();
    }

    @Test
    public void genuineAnkiDroidIsTrusted() {
        installAnkiDroid(AnkiDroidCheck.RELEASE_CERT_SHA256);

        assertTrue(AnkiDroidCheck.isTrusted(apps, AppBuild.RELEASE));
    }

    @Test
    public void otherPackageHoldingAuthorityIsRejected() {
        installAnkiDroid(AnkiDroidCheck.RELEASE_CERT_SHA256);
        apps.squat(AnkiDroidCheck.AUTHORITY, SQUATTER);

        assertFalse(AnkiDroidCheck.isTrusted(apps, AppBuild.RELEASE));
        assertFalse(AnkiDroidCheck.isTrusted(apps, AppBuild.DEBUGGABLE));
    }

    @Test
    public void unknownCertificateIsRejected() {
        installAnkiDroid(OTHER_CERT);

        assertFalse(AnkiDroidCheck.isTrusted(apps, AppBuild.RELEASE));
    }

    @Test
    public void unknownCertificateIsAcceptedWhenDebuggable() {
        installAnkiDroid(OTHER_CERT);

        assertTrue(AnkiDroidCheck.isTrusted(apps, AppBuild.DEBUGGABLE));
    }

    @Test
    public void rotatedKeyWithReleaseKeyInHistoryIsTrusted() {
        installAnkiDroid(AnkiDroidCheck.RELEASE_CERT_SHA256, ROTATED_CERT);

        assertTrue(AnkiDroidCheck.isTrusted(apps, AppBuild.RELEASE));
    }

    @Test
    public void notInstalledIsRejected() {
        assertFalse(AnkiDroidCheck.isTrusted(apps, AppBuild.RELEASE));
        assertFalse(AnkiDroidCheck.isTrusted(apps, AppBuild.DEBUGGABLE));
    }

    @Test
    public void squatterWhileAnkiDroidMissingIsRejected() {
        apps.squat(AnkiDroidCheck.AUTHORITY, SQUATTER);

        assertFalse(AnkiDroidCheck.isTrusted(apps, AppBuild.DEBUGGABLE));
    }

    private void installAnkiDroid(String... certs) {
        apps.install(AnkiDroidCheck.PACKAGE_NAME, AnkiDroidCheck.AUTHORITY, certs);
    }
}
