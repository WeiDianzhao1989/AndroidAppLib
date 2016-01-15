
package com.koudai.net.kernal;

import com.koudai.net.kernal.internal.Util;

import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

/**
 * A record of a TLS handshake. For HTTPS clients, the client is <i>local</i> and the remote server
 * is its <i>peer</i>.
 *
 * <p>This value object describes a completed handshake. Use {@link ConnectionSpec} to set policy
 * for new handshakes.
 */
public final class Handshake {
  private final TlsVersion tlsVersion;
  private final CipherSuite cipherSuite;
  private final List<Certificate> peerCertificates;
  private final List<Certificate> localCertificates;

  private Handshake(TlsVersion tlsVersion, CipherSuite cipherSuite,
                    List<Certificate> peerCertificates, List<Certificate> localCertificates) {
    this.tlsVersion = tlsVersion;
    this.cipherSuite = cipherSuite;
    this.peerCertificates = peerCertificates;
    this.localCertificates = localCertificates;
  }

  public static Handshake get(SSLSession session) {
    String cipherSuiteString = session.getCipherSuite();
    if (cipherSuiteString == null) throw new IllegalStateException("cipherSuite == null");
    CipherSuite cipherSuite = CipherSuite.forJavaName(cipherSuiteString);

    String tlsVersionString = session.getProtocol();
    if (tlsVersionString == null) throw new IllegalStateException("tlsVersion == null");
    TlsVersion tlsVersion = TlsVersion.forJavaName(tlsVersionString);

    Certificate[] peerCertificates;
    try {
      peerCertificates = session.getPeerCertificates();
    } catch (SSLPeerUnverifiedException ignored) {
      peerCertificates = null;
    }
    List<Certificate> peerCertificatesList = peerCertificates != null
            ? Util.immutableList(peerCertificates)
            : Collections.<Certificate>emptyList();

    Certificate[] localCertificates = session.getLocalCertificates();
    List<Certificate> localCertificatesList = localCertificates != null
            ? Util.immutableList(localCertificates)
            : Collections.<Certificate>emptyList();

    return new Handshake(tlsVersion, cipherSuite, peerCertificatesList, localCertificatesList);
  }

  public static Handshake get(TlsVersion tlsVersion, CipherSuite cipherSuite,
                              List<Certificate> peerCertificates, List<Certificate> localCertificates) {
    if (cipherSuite == null) throw new IllegalArgumentException("cipherSuite == null");
    return new Handshake(tlsVersion, cipherSuite, Util.immutableList(peerCertificates),
            Util.immutableList(localCertificates));
  }

  /**
   * Returns the TLS version used for this connection. May return null if the response was cached
   * with a version of OkHttp prior to 3.0.
   */
  public TlsVersion tlsVersion() {
    return tlsVersion;
  }

  /** Returns the cipher suite used for the connection. */
  public CipherSuite cipherSuite() {
    return cipherSuite;
  }

  /** Returns a possibly-empty list of certificates that identify the remote peer. */
  public List<Certificate> peerCertificates() {
    return peerCertificates;
  }

  /** Returns the remote peer's principle, or null if that peer is anonymous. */
  public Principal peerPrincipal() {
    return !peerCertificates.isEmpty()
            ? ((X509Certificate) peerCertificates.get(0)).getSubjectX500Principal()
            : null;
  }

  /** Returns a possibly-empty list of certificates that identify this peer. */
  public List<Certificate> localCertificates() {
    return localCertificates;
  }

  /** Returns the local principle, or null if this peer is anonymous. */
  public Principal localPrincipal() {
    return !localCertificates.isEmpty()
            ? ((X509Certificate) localCertificates.get(0)).getSubjectX500Principal()
            : null;
  }

  @Override public boolean equals(Object other) {
    if (!(other instanceof Handshake)) return false;
    Handshake that = (Handshake) other;
    return Util.equal(cipherSuite, that.cipherSuite)
            && cipherSuite.equals(that.cipherSuite)
            && peerCertificates.equals(that.peerCertificates)
            && localCertificates.equals(that.localCertificates);
  }

  @Override public int hashCode() {
    int result = 17;
    result = 31 * result + (tlsVersion != null ? tlsVersion.hashCode() : 0);
    result = 31 * result + cipherSuite.hashCode();
    result = 31 * result + peerCertificates.hashCode();
    result = 31 * result + localCertificates.hashCode();
    return result;
  }
}