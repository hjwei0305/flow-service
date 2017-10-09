package com.ecmp.flow.common.util;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.cert.X509Certificate;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/9/27 10:25      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class MyCustomClientHttpRequestFactory extends SimpleClientHttpRequestFactory {

    private final HostnameVerifier hostNameVerifier;
//    private final ServerInfo serverInfo;

    public MyCustomClientHttpRequestFactory(final HostnameVerifier hostNameVerifier) {
        this.hostNameVerifier = hostNameVerifier;
//        this.serverInfo = serverInfo;
    }

    @Override
    protected void prepareConnection(final HttpURLConnection connection, final String httpMethod)
            throws IOException {
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setHostnameVerifier(hostNameVerifier);
            ((HttpsURLConnection) connection).setSSLSocketFactory(initSSLContext()
                    .getSocketFactory());
        }
        super.prepareConnection(connection, httpMethod);
    }

    private SSLContext initSSLContext() {
        try {
            //  System.setProperty("https.protocols", "TLSv1");

            // Set ssl trust manager. Verify against our server thumbprint
            final SSLContext ctx = SSLContext.getInstance("TLS");
//            final SslThumbprintVerifier verifier = new SslThumbprintVerifier(serverInfo);
//            final ThumbprintTrustManager thumbPrintTrustManager =
//                    new ThumbprintTrustManager(null, verifier);
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};
            ctx.init(null, trustAllCerts, null);
            return ctx;
        } catch (final Exception ex) {
            return null;
        }
    }
}
