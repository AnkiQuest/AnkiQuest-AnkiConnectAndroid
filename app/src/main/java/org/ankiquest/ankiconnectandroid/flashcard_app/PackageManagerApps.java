// SPDX-License-Identifier: AGPL-3.0-or-later

package org.ankiquest.ankiconnectandroid.flashcard_app;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

/** InstalledApps backed by PackageManager. */
public class PackageManagerApps implements InstalledApps {
    private static final String DIGEST_ALGORITHM = "SHA-256";

    private final PackageManager packageManager;

    public PackageManagerApps(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    @Override
    public Set<String> signerDigests(String packageName) {
        Set<String> digests = new HashSet<>();
        Signature[] signatures = signatures(packageName);
        if (signatures == null) {
            return digests;
        }

        for (Signature signature : signatures) {
            digests.add(sha256(signature.toByteArray()));
        }
        return digests;
    }

    // The int-flags overload is the only one before API 33
    @SuppressWarnings("deprecation")
    @Override
    public String authorityHolder(String authority) {
        ProviderInfo info = packageManager.resolveContentProvider(authority, 0);
        return info == null ? null : info.packageName;
    }

    // GET_SIGNATURES is unsafe on API 28+ (it can report any one of several signers),
    // so newer systems use GET_SIGNING_CERTIFICATES.
    @SuppressWarnings("deprecation")
    private Signature[] signatures(String packageName) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                return packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures;
            }

            PackageInfo info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES);
            SigningInfo signingInfo = info.signingInfo;
            if (signingInfo == null) {
                return null;
            }
            if (signingInfo.hasMultipleSigners()) {
                return signingInfo.getApkContentsSigners();
            }
            return signingInfo.getSigningCertificateHistory();
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private static String sha256(byte[] data) {
        try {
            byte[] digest = MessageDigest.getInstance(DIGEST_ALGORITHM).digest(data);
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(DIGEST_ALGORITHM + " is not available", e);
        }
    }
}
