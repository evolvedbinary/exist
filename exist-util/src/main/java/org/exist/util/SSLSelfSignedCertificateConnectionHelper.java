/*
 * Copyright (C) 2014, Evolved Binary Ltd
 *
 * This file was originally ported from FusionDB to eXist-db by
 * Evolved Binary, for the benefit of the eXist-db Open Source community.
 * Only the ported code as it appears in this file, at the time that
 * it was contributed to eXist-db, was re-licensed under The GNU
 * Lesser General Public License v2.1 only for use in eXist-db.
 *
 * This license grant applies only to a snapshot of the code as it
 * appeared when ported, it does not offer or infer any rights to either
 * updates of this source code or access to the original source code.
 *
 * The GNU Lesser General Public License v2.1 only license follows.
 *
 * ---------------------------------------------------------------------
 *
 * Copyright (C) 2014, Evolved Binary Ltd
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.exist.util;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.annotation.Nullable;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Class that causes a {@link HttpsURLConnection} to
 * potentially work with any self-signed SSL certificates.
 *
 * @author <a href="mailto:adam@evolvedbinary.com">Adam Retter</a>
 */
public class SSLSelfSignedCertificateConnectionHelper {

  @Nullable private static SSLSelfSignedCertificateConnectionHelper instance = null;

  private final boolean sslAllowSelfSigned;
  private final boolean sslVerifyHostname;
  @Nullable private final TrustManager trustManager;
  @Nullable private final HostnameVerifier hostnameVerifier;

  private SSLSelfSignedCertificateConnectionHelper(final boolean sslAllowSelfSigned, final boolean sslVerifyHostname, @Nullable final TrustManager trustManager, @Nullable final HostnameVerifier hostnameVerifier) {
    // singleton
    this.sslAllowSelfSigned = sslAllowSelfSigned;
    this.sslVerifyHostname = sslVerifyHostname;
    this.trustManager = trustManager;
    this.hostnameVerifier = hostnameVerifier;
  }

  /**
   * Initialize an {@link HttpsURLConnection} with a non-validating SSL
   * trust manager and a hostname verifier that accepts all hostnames..
   *
   * Note that this makes the SSL connection less secure!
   *
   * @return the instance of this class.
   */
  public static synchronized SSLSelfSignedCertificateConnectionHelper getInstance() throws NoSuchAlgorithmException, KeyManagementException {
    return getInstance(true, false);
  }

  /**
   * Initialize an {@link HttpsURLConnection} with (optionally) a non-validating SSL
   * trust manager and (optionally) a hostname verifier that accepts all hostnames.
   *
   * Note that this may make the SSL connection less secure!
   *
   * @param sslAllowSelfSigned Set to true to allow self-signed certificates
   * @param sslVerifyHostname  Set to false for not verifying hostnames.
   *
   * @return the instance of this class.
   */
  public static synchronized SSLSelfSignedCertificateConnectionHelper getInstance(final boolean sslAllowSelfSigned, final boolean sslVerifyHostname) throws NoSuchAlgorithmException, KeyManagementException {
    if (instance == null) {
      instance = create(sslAllowSelfSigned, sslVerifyHostname);
    } else if (sslAllowSelfSigned != instance.sslAllowSelfSigned || sslVerifyHostname != instance.sslVerifyHostname) {
      throw new IllegalStateException("Instance was previously instantiated with different arguments");
    }
    return instance;
  }

  private static SSLSelfSignedCertificateConnectionHelper create(final boolean sslAllowSelfSigned, final boolean sslVerifyHostname) throws NoSuchAlgorithmException, KeyManagementException {
    TrustManager trustManager = null;
    if (sslAllowSelfSigned) {
      final SSLContext sc = SSLContext.getInstance("SSL");
      trustManager = X509AlwaysTrustManager.getInstance();
      sc.init(null, new TrustManager[]{trustManager}, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    HostnameVerifier hostnameVerifier = null;
    if (!sslVerifyHostname) {
      hostnameVerifier = AllHostnamesVerify.getInstance();
      HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
    }

    return new SSLSelfSignedCertificateConnectionHelper(sslAllowSelfSigned, sslVerifyHostname, trustManager, hostnameVerifier);
  }

  /**
   * A {@link HostnameVerifier} that simply accepts all hostnames.
   */
  public static class AllHostnamesVerify implements HostnameVerifier {

    @Nullable private static AllHostnamesVerify instance = null;

    private AllHostnamesVerify() {
      // singleton pattern
    }

    /**
     * Get an instance of this class.
     *
     * @return an instance of this class.
     */
    public static synchronized AllHostnamesVerify getInstance() {
      if (instance == null) {
        instance = new AllHostnamesVerify();
      }
      return instance;
    }

    @Override
    public boolean verify(final String hostname, final SSLSession session) {
      return true;
    }
  }

  /**
   * An {@link X509TrustManager} that simply trusts everything always.
   */
  public static class X509AlwaysTrustManager implements X509TrustManager {

    @Nullable private static X509AlwaysTrustManager instance = null;

    private X509AlwaysTrustManager() {
      // singleton pattern
    }

    /**
     * Get an instance of this class.
     *
     * @return an instance of this class.
     */
    public static synchronized X509AlwaysTrustManager getInstance() {
      if (instance == null) {
        instance = new X509AlwaysTrustManager();
      }
      return instance;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
      // Always trust
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
      // Always trust
    }
  }
}
