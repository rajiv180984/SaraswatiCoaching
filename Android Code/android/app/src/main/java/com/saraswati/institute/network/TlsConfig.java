package com.saraswati.institute.network;

import android.content.Context;

import androidx.annotation.NonNull;

import com.saraswati.institute.R;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

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
 * TLS material for talking to the saraswati-auth dev server.
 *
 * <p>The server presents a self-signed PKCS12 certificate ({@code CN=Saraswati Coaching},
 * no Subject Alternative Names) and only negotiates <b>TLS 1.3</b>. This class:
 * <ul>
 *   <li>builds an {@link X509TrustManager} that trusts <em>only</em> that bundled
 *       certificate ({@code res/raw/saraswati_auth.pem}) — not a blanket trust-all;</li>
 *   <li>exposes a {@link ConnectionSpec} restricted to {@link TlsVersion#TLS_1_3};</li>
 *   <li>exposes a {@link HostnameVerifier} that accepts the dev host despite the missing
 *       SAN (needed only because the dev cert is self-signed / SAN-less).</li>
 * </ul>
 *
 * <p>All of this is scoped to the {@link ApiClient#BASE_URL} host and is intended for the
 * local development server. A production server with a CA-issued certificate needs none
 * of the hostname-verifier relaxation.
 */
final class TlsConfig {

    /** TLS 1.3 only, with the modern cipher suites OkHttp allows for it. */
    static final ConnectionSpec TLS_1_3_ONLY = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_3)
            .cipherSuites(
                    CipherSuite.TLS_AES_128_GCM_SHA256,
                    CipherSuite.TLS_AES_256_GCM_SHA384,
                    CipherSuite.TLS_CHACHA20_POLY1305_SHA256)
            .build();

    private final SSLContext sslContext;
    private final X509TrustManager trustManager;

    private TlsConfig(SSLContext sslContext, X509TrustManager trustManager) {
        this.sslContext = sslContext;
        this.trustManager = trustManager;
    }

    SSLContext sslContext()          { return sslContext; }
    X509TrustManager trustManager()  { return trustManager; }

    /** Accepts the configured dev host only; everything else falls back to strict verification. */
    HostnameVerifier hostnameVerifier(final String allowedHost) {
        final HostnameVerifier defaultVerifier = javax.net.ssl.HttpsURLConnection.getDefaultHostnameVerifier();
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession sslSession) {
                if (allowedHost.equalsIgnoreCase(hostname)) {
                    return true;
                }
                return defaultVerifier.verify(hostname, sslSession);
            }
        };
    }

    /**
     * Loads {@code res/raw/saraswati_auth.pem} into a trust manager that trusts that
     * certificate only.
     */
    @NonNull
    static TlsConfig create(@NonNull Context context) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate serverCert;
            try (InputStream in = context.getResources().openRawResource(R.raw.saraswati_auth)) {
                serverCert = cf.generateCertificate(in);
            }

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("saraswati-auth", serverCert);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            TrustManager[] trustManagers = tmf.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException(
                        "Unexpected default trust managers: " + Arrays.toString(trustManagers));
            }
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager}, null);

            return new TlsConfig(sslContext, trustManager);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to build TLS config for saraswati-auth", e);
        }
    }

    /** Convenience for logging which cert is pinned. */
    static String describe(@NonNull Context context) {
        try (InputStream in = context.getResources().openRawResource(R.raw.saraswati_auth)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(in);
            return cert.getSubjectX500Principal().getName() + " (expires " + cert.getNotAfter() + ")";
        } catch (Exception e) {
            return "unknown";
        }
    }
}
