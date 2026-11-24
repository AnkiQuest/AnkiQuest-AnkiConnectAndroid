// SPDX-License-Identifier: AGPL-3.0-or-later

package org.ankiquest.ankiconnectandroid.flashcard_app;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** InstalledApps for tests: packages with their signer digests, and who holds which authority. */
final class FakeInstalledApps implements InstalledApps {
    private final Map<String, Set<String>> digests = new HashMap<>();
    private final Map<String, String> authorities = new HashMap<>();

    /** Installs the package, signed by the given certificates, holding its authority. */
    void install(String packageName, String authority, String... certs) {
        digests.put(packageName, new HashSet<>(Arrays.asList(certs)));
        authorities.put(authority, packageName);
    }

    void uninstall(String packageName, String authority) {
        digests.remove(packageName);
        authorities.remove(authority);
    }

    /** Lets another package hold the authority. */
    void squat(String authority, String packageName) {
        authorities.put(authority, packageName);
    }

    @Override
    public Set<String> signerDigests(String packageName) {
        return digests.getOrDefault(packageName, Collections.emptySet());
    }

    @Override
    public String authorityHolder(String authority) {
        return authorities.get(authority);
    }
}
