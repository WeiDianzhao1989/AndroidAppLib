package com.koudai.net.kernal.internal.tls;

import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.X509TrustManager;

/**
 * Computes the effective certificate chain from the raw array returned by Java's built in TLS APIs.
 * Cleaning a chain returns a list of certificates where the first element is {@code chain[0]}, each
 * certificate is signed by the certificate that follows, and the last certificate is a trusted CA
 * certificate.
 *
 * <p>Use of the chain cleaner is necessary to omit unexpected certificates that aren't relevant to
 * the TLS handshake and to extract the trusted CA certificate for the benefit of certificate
 * pinning.
 */
public abstract class CertificateChainCleaner {
    public abstract List<Certificate> clean(List<Certificate> chain)
            throws SSLPeerUnverifiedException;

    public static CertificateChainCleaner get(X509TrustManager trustManager) {
        return new BasicCertificateChainCleaner(TrustRootIndex.get(trustManager));
    }

    public static CertificateChainCleaner get(X509Certificate... caCerts) {
        return new BasicCertificateChainCleaner(TrustRootIndex.get(caCerts));
    }

    /**
     * A certificate chain cleaner that uses a set of trusted root certificates to build the trusted
     * chain.
     *
     * <p>This class includes code from <a href="https://conscrypt.org/">Conscrypt's</a> {@code
     * TrustManagerImpl} and {@code TrustedCertificateIndex}.
     */
    static final class BasicCertificateChainCleaner extends CertificateChainCleaner {
        /** The maximum number of signers in a chain. We use 9 for consistency with OpenSSL. */
        private static final int MAX_SIGNERS = 9;

        private final TrustRootIndex trustRootIndex;

        public BasicCertificateChainCleaner(TrustRootIndex trustRootIndex) {
            this.trustRootIndex = trustRootIndex;
        }

        /**
         * Returns a cleaned chain for {@code chain}.
         *
         * <p>This method throws if the complete chain to a trusted CA certificate cannot be
         * constructed. This is unexpected unless the trust root index in this class has a different
         * trust manager than what was used to establish {@code chain}.
         */
        @Override public List<Certificate> clean(List<Certificate> chain)
                throws SSLPeerUnverifiedException {
            Deque<Certificate> queue = new ArrayDeque<Certificate>(chain);
            List<Certificate> result = new ArrayList<Certificate>();
            result.add(queue.removeFirst());
            boolean foundTrustedCertificate = false;

            followIssuerChain:
            for (int c = 0; c < MAX_SIGNERS; c++) {
                X509Certificate toVerify = (X509Certificate) result.get(result.size() - 1);

                // If this cert has been signed by a trusted cert, use that. Add the trusted certificate to
                // the end of the chain unless it's already present. (That would happen if the first
                // certificate in the chain is itself a self-signed and trusted CA certificate.)
                X509Certificate trustedCert = trustRootIndex.findByIssuerAndSignature(toVerify);
                if (trustedCert != null) {
                    if (result.size() > 1 || !toVerify.equals(trustedCert)) {
                        result.add(trustedCert);
                    }
                    if (verifySignature(trustedCert, trustedCert)) {
                        return result; // The self-signed cert is a root CA. We're done.
                    }
                    foundTrustedCertificate = true;
                    continue;
                }

                // Search for the certificate in the chain that signed this certificate. This is typically
                // the next element in the chain, but it could be any element.
                for (Iterator<Certificate> i = queue.iterator(); i.hasNext(); ) {
                    X509Certificate signingCert = (X509Certificate) i.next();
                    if (verifySignature(toVerify, signingCert)) {
                        i.remove();
                        result.add(signingCert);
                        continue followIssuerChain;
                    }
                }

                // We've reached the end of the chain. If any cert in the chain is trusted, we're done.
                if (foundTrustedCertificate) {
                    return result;
                }

                // The last link isn't trusted. Fail.
                throw new SSLPeerUnverifiedException(
                        "Failed to find a trusted cert that signed " + toVerify);
            }

            throw new SSLPeerUnverifiedException("Certificate chain too long: " + result);
        }

        /** Returns true if {@code toVerify} was signed by {@code signingCert}'s public key. */
        private boolean verifySignature(X509Certificate toVerify, X509Certificate signingCert) {
            if (!toVerify.getIssuerDN().equals(signingCert.getSubjectDN())) return false;
            try {
                toVerify.verify(signingCert.getPublicKey());
                return true;
            } catch (GeneralSecurityException verifyFailed) {
                return false;
            }
        }
    }
}


