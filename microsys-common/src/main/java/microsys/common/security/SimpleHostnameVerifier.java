package microsys.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 *
 */
public class SimpleHostnameVerifier implements HostnameVerifier {
    private final static Logger LOG = LoggerFactory.getLogger(SimpleHostnameVerifier.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean verify(@Nonnull final String hostname, @Nonnull final SSLSession sslSession) {
        try {
            LOG.info("Verifying hostname {}", hostname);
            LOG.info("  SSLSession Local Principal:    {}", sslSession.getLocalPrincipal());
            if (sslSession.getLocalCertificates() != null) {
                LOG.info("  SSLSession Local Certificates: {}", sslSession.getLocalCertificates().length);
            }
            if (sslSession.getPeerCertificates() != null) {
                LOG.info("  SSLSession Peer Certificates:  {}", sslSession.getPeerCertificates().length);
            }
            LOG.info("  SSLSession Peer Host:          {}", sslSession.getPeerHost());
            LOG.info("  SSLSession Peer Port:          {}", sslSession.getPeerPort());
            LOG.info("  SSLSession Peer Principal:     {}", sslSession.getPeerPrincipal());
            LOG.info("  SSLSession Protocol:           {}", sslSession.getProtocol());
            LOG.info("  SSLSession Cipher Suite:       {}", sslSession.getCipherSuite());
        } catch (final Throwable error) {
            LOG.error("Failed to verify hostname", error);
        }

        return true;
    }
}
