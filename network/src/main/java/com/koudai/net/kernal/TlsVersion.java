
package com.koudai.net.kernal;

import javax.net.ssl.SSLSocket;

/**
 * Versions of TLS that can be offered when negotiating a secure socket. See
 * {@link SSLSocket#setEnabledProtocols}.
 */
public enum TlsVersion {
    TLS_1_2("TLSv1.2"), // 2008.
    TLS_1_1("TLSv1.1"), // 2006.
    TLS_1_0("TLSv1"),   // 1999.
    SSL_3_0("SSLv3"),   // 1996.
    ;

    final String javaName;

    TlsVersion(String javaName) {
        this.javaName = javaName;
    }

    public static TlsVersion forJavaName(String javaName) {

        if (TLS_1_2.javaName().equals(javaName)) {
            return TLS_1_2;
        } else if (TLS_1_1.javaName().equals(javaName)) {
            return TLS_1_1;
        } else if (TLS_1_0.javaName().equals(javaName)) {
            return TLS_1_0;
        } else if (SSL_3_0.javaName().equals(javaName)) {
            return SSL_3_0;
        }

        throw new IllegalArgumentException("Unexpected TLS version: " + javaName);
    }

    public String javaName() {
        return javaName;
    }
}
