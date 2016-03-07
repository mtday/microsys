package microsys.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;
import javax.net.ssl.X509TrustManager;

/**
 * A simple trust manager implementation.
 */
public class SimpleTrustManager implements X509TrustManager {
    private final static Logger LOG = LoggerFactory.getLogger(SimpleTrustManager.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkClientTrusted(@Nonnull final X509Certificate[] x509Certificates, @Nonnull final String s)
            throws CertificateException {
        LOG.info("Checking if client is trusted");
        LOG.info("  S:            {}", s);
        LOG.info("  Certificates: {}", x509Certificates.length);

        for (final X509Certificate cert : x509Certificates) {
            LOG.info("    Subject: {}", cert.getSubjectDN());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkServerTrusted(@Nonnull final X509Certificate[] x509Certificates, @Nonnull final String s)
            throws CertificateException {
        LOG.info("Checking if server is trusted");
        LOG.info("  S:            {}", s);
        LOG.info("  Certificates: {}", x509Certificates.length);

        for (final X509Certificate cert : x509Certificates) {
            LOG.info("    Subject: {}", cert.getSubjectDN());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public X509Certificate[] getAcceptedIssuers() {
        LOG.info("No accepted issuers being returned");
        return new X509Certificate[0];
    }
}
