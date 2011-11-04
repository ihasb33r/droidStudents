package com.unipistudentgrades.mygrades;
import org.apache.http.conn.scheme.*;
import android.util.Log;
import org.apache.http.impl.conn.*;
import org.apache.http.conn.*;
import java.io.*;
import java.security.KeyStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.conn.ssl.*;
import android.content.Context;



public class MyHttpClient extends DefaultHttpClient {

        final Context context;

        public MyHttpClient(Context context) {
            this.context = context;
        }

    @Override
        protected ClientConnectionManager createClientConnectionManager() {
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            // Register for port 443 our SSLSocketFactory with our keystore
            // to the ConnectionManager
            registry.register(new Scheme("https", newSslSocketFactory(), 443));
            return new SingleClientConnManager(getParams(), registry);
        }

    private SSLSocketFactory newSslSocketFactory() {
        try {
            // Get an instance of the Bouncy Castle KeyStore format
            KeyStore trusted = KeyStore.getInstance("BKS");
            // Get the raw resource, which contains the keystore with
            // your trusted certificates (root and any intermediate certs)
                        InputStream in = context.getResources().openRawResource(R.raw.mykeystore);
//            InputStream in = new FileInputStream("myKeystore");
            try {
                // Initialize the keystore with the provided trusted certificates
                // Also provide the password of the keystore
                trusted.load(in, "screwyouguys".toCharArray());
            } finally {
                in.close();
            }
            // Pass the keystore to the SSLSocketFactory. The factory is responsible
            // for the verification of the server certificate.
            SSLSocketFactory sf = new SSLSocketFactory(trusted);
            // Hostname verification from certificate
            // http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e506
            sf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
            return sf;
        } catch (Exception e) {
            Log.i("MYHTTPCLIENT", "\n\n\nFAILED TO CREATE CLIENT: " + e.getMessage()+"\n\n\n");
        }
        return null;
    }
}
