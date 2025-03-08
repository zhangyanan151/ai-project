//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cn.techwolf.server.utils;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HttpUtils {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
    private static int socketTimeout = 2000;
    private static int connectTimeout = 2000;
    private static int connectionRequestTimeout = 2000;
    private static int maxConnTotal = 500;
    private static int maxConnPerRoute = 100;

    public HttpUtils() {
    }

    public static CloseableHttpClient createSSLClientDefault() {
        return createSSLClient(maxConnTotal, maxConnPerRoute);
    }

    public static CloseableHttpClient createSSLClient(int maxConnTotal, int maxConnPerRoute) {
        try {
            SSLContext sslContext = (new SSLContextBuilder()).loadTrustMaterial((KeyStore)null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            return HttpClients.custom().setMaxConnPerRoute(maxConnPerRoute).setMaxConnTotal(maxConnTotal).setSSLSocketFactory(socketFactory).build();
        } catch (KeyManagementException var4) {
            KeyManagementException e = var4;
            logger.error("HttpUtils.createSSLClientDefault()", e);
        } catch (NoSuchAlgorithmException var5) {
            NoSuchAlgorithmException e = var5;
            logger.error("HttpUtils.createSSLClientDefault()", e);
        } catch (KeyStoreException var6) {
            KeyStoreException e = var6;
            logger.error("HttpUtils.createSSLClientDefault()", e);
        }

        return HttpClients.createDefault();
    }

    public static CloseableHttpClient createDefaultHttpClient() {
        return createHttpClient(socketTimeout, connectTimeout, connectionRequestTimeout, maxConnTotal, maxConnPerRoute);
    }

    public static CloseableHttpClient createHttpClient(int maxConnTotal, int maxConnPerRoute) {
        return createHttpClient(socketTimeout, connectTimeout, connectionRequestTimeout, maxConnTotal, maxConnPerRoute);
    }

    public static CloseableHttpClient createHttpClient(int socketTimeout, int connectTimeout, int connectionRequestTimeout, int maxConnTotal, int maxConnPerRoute) {
        RequestConfig config = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout).setConnectionRequestTimeout(connectionRequestTimeout).build();
        return HttpClients.custom().setDefaultRequestConfig(config).setMaxConnTotal(maxConnTotal).setMaxConnPerRoute(maxConnPerRoute).build();
    }

    public static void closeQuietly(CloseableHttpResponse response) {
        try {
            if (response != null) {
                response.close();
            }
        } catch (Exception var2) {
        }

    }
}
