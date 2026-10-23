package com.saraswati.institute.network;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import com.saraswati.institute.R;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.TlsVersion;

/**
 * Transport security for the auth-service connection.
 *
 * <p><b>Protocol floor.</b> There is no TLS 2.0 — TLS 1.3 (RFC 8446) is the current top of
 * the protocol line and TLS 1.2 (RFC 5246) is the lowest version still considered safe.
 * This class therefore offers <em>only</em> TLS 1.3 and TLS 1.2, prefers 1.3, and restricts
 * TLS 1.2 to AEAD cipher suites with forward secrecy (ECDHE). SSLv3 and TLS 1.0/1.1 are
 * never offered, and {@code cleartextTrafficPermitted="false"} in
 * {@code res/xml/network_security_config.xml} keeps the rest of the platform in step.
 * On API 24-28 the platform providers cannot negotiate TLS 1.3, so {@code SaraswatiApp}
 * installs Conscrypt as the first security provider to make 1.3 available there as well.
 *
 * <p><b>Trust.</b> The deployment presents a self-signed certificate, so the certificates
 * bundled in {@code res/raw} are trusted <em>in addition to</em> the system CAs — never a
 * trust-all manager. A chain that validates against either store is accepted, which means a
 * later migration to a CA-issued certificate needs no app change.
 *
 * <p><b>Hostname.</b> The bundled certificates carry {@code CN=localhost} /
 * {@code CN=Saraswati Coaching} rather than the deployment's IP address, so standard
 * hostname verification cannot pass. The verifier below accepts {@link ApiConfig#HOST}
 * only when the peer's leaf certificate is byte-for-byte one of the pinned certificates;
 * every other host, and any unpinned certificate, goes through the strict verifier.
 */
final class TlsConfig {

    private static final String TAG = "TlsConfig";

    /** Certificates bundled with the app; whichever one the server presents is accepted. */
    @RawRes
    private static final int[] PINNED_CERTIFICATES = {
            R.raw.auth_service,      // auth-service keystore.p12 (CN=localhost, SAN localhost/127.0.0.1)
            R.raw.saraswati_auth,    // previous deployment certificate (CN=Saraswati Coaching)
    };

    /**
     * TLS 1.3 preferred, TLS 1.2 as the floor. AEAD suites only, and every TLS 1.2 suite is
     * ECDHE so each session has forward secrecy.
     */
    static final ConnectionSpec MODERN_TLS_ONLY = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2)
            .cipherSuites(
                    // TLS 1.3
                    CipherSuite.TLS_AES_128_GCM_SHA256,
                    CipherSuite.TLS_AES_256_GCM_SHA384,
                    CipherSuite.TLS_CHACHA20_POLY1305_SHA256,
                    // TLS 1.2 - ECDHE + AEAD only
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
                    CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256)
            .supportsTlsExtensions(true)
            .build();

    private final SSLContext sslContext;
    private final X509TrustManager trustManager;
    private final Set<X509Certificate> pinnedCertificates;

    private TlsConfig(SSLContext sslContext, X509TrustManager trustManager,
                      Set<X509Certificate> pinnedCertificates) {
        this.sslContext = sslContext;
        this.trustManager = trustManager;
        this.pinnedCertificates = pinnedCertificates;
    }

    SSLContext sslContext() {
        return sslContext;
    }

    X509TrustManager trustManager() {
        return trustManager;
    }

    /**
     * Accepts {@code allowedHost} when the server presents one of the pinned certificates;
     * anything else is verified normally.
     */
    HostnameVerifier hostnameVerifier(final String allowedHost) {
        final HostnameVerifier strict = javax.net.ssl.HttpsURLConnection.getDefaultHostnameVerifier();
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                if (allowedHost.equalsIgnoreCase(hostname) && presentsPinnedCertificate(session)) {
                    return true;
                }
                return strict.verify(hostname, session);
            }
        };
    }

    private boolean presentsPinnedCertificate(SSLSession session) {
        try {
            java.security.cert.Certificate[] chain = session.getPeerCertificates();
            return chain.length > 0 && pinnedCertificates.contains(chain[0]);
        } catch (Exception e) {
            Log.w(TAG, "Could not read the peer certificate chain", e);
            return false;
        }
    }

    /** Builds the TLS material: the bundled certificates plus the system CAs as trust anchors. */
    @NonNull
    static TlsConfig create(@NonNull Context context) {
        try {
            Set<X509Certificate> pinned = loadPinnedCertificates(context);

            KeyStore pinnedStore = KeyStore.getInstance(KeyStore.getDefaultType());
            pinnedStore.load(null, null);
            int index = 0;
            for (X509Certificate certificate : pinned) {
                pinnedStore.setCertificateEntry("auth-service-" + index++, certificate);
            }

            X509TrustManager trustManager = new CompositeTrustManager(
                    x509TrustManagerFor(pinnedStore),   // bundled certificates
                    x509TrustManagerFor(null));         // system CA store

            // "TLS" resolves to the highest protocol the first provider supports: Conscrypt
            // when SaraswatiApp installed it, otherwise the platform provider.
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager}, null);

            return new TlsConfig(sslContext, trustManager, pinned);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to build the TLS configuration for auth-service", e);
        }
    }

    private static Set<X509Certificate> loadPinnedCertificates(Context context) throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        Set<X509Certificate> certificates = new LinkedHashSet<>();
        for (int rawResource : PINNED_CERTIFICATES) {
            try (InputStream in = context.getResources().openRawResource(rawResource)) {
                certificates.add((X509Certificate) factory.generateCertificate(in));
            } catch (Exception e) {
                Log.w(TAG, "Skipping unreadable bundled certificate " + rawResource, e);
            }
        }
        if (certificates.isEmpty()) {
            throw new IllegalStateException("No usable certificate found in res/raw");
        }
        return Collections.unmodifiableSet(certificates);
    }

    private static X509TrustManager x509TrustManagerFor(KeyStore store) throws Exception {
        TrustManagerFactory factory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(store); // null = the platform CA store
        for (TrustManager candidate : factory.getTrustManagers()) {
            if (candidate instanceof X509TrustManager) {
                return (X509TrustManager) candidate;
            }
        }
        throw new IllegalStateException(
                "No X509TrustManager among " + Arrays.toString(factory.getTrustManagers()));
    }

    /** Human-readable summary of what the app will trust, for the startup log. */
    static String describe(@NonNull Context context) {
        StringBuilder out = new StringBuilder();
        try {
            for (X509Certificate certificate : loadPinnedCertificates(context)) {
                if (out.length() > 0) {
                    out.append("; ");
                }
                out.append(certificate.getSubjectX500Principal().getName())
                        .append(" (expires ").append(certificate.getNotAfter()).append(')');
            }
        } catch (Exception e) {
            return "unknown";
        }
        return out.toString();
    }

    /**
     * Trusts a chain if <em>any</em> delegate trusts it, so the bundled self-signed
     * certificate and ordinary CA-issued certificates both work.
     *
     * <p>Lint flags every hand-written {@code X509TrustManager}, because most of them end up
     * accepting anything. This one validates nothing itself: it only asks two real
     * {@link TrustManagerFactory}-built managers in turn and rethrows the last
     * {@link CertificateException} when neither accepts the chain. An empty
     * {@code checkServerTrusted} body is what the warning is really about, and there is none
     * here.
     */
    @android.annotation.SuppressLint("CustomX509TrustManager")
    private static final class CompositeTrustManager implements X509TrustManager {

        private final List<X509TrustManager> delegates;

        CompositeTrustManager(X509TrustManager... delegates) {
            this.delegates = Arrays.asList(delegates);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            check(chain, authType, true);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            check(chain, authType, false);
        }

        private void check(X509Certificate[] chain, String authType, boolean client)
                throws CertificateException {
            CertificateException last = null;
            for (X509TrustManager delegate : delegates) {
                try {
                    if (client) {
                        delegate.checkClientTrusted(chain, authType);
                    } else {
                        delegate.checkServerTrusted(chain, authType);
                    }
                    return;
                } catch (CertificateException e) {
                    last = e;
                }
            }
            throw last != null ? last : new CertificateException("No trust manager accepted the chain");
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            List<X509Certificate> issuers = new ArrayList<>();
            for (X509TrustManager delegate : delegates) {
                issuers.addAll(Arrays.asList(delegate.getAcceptedIssuers()));
            }
            return issuers.toArray(new X509Certificate[0]);
        }
    }
}
