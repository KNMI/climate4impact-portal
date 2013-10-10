
package client;
//import java.net.*;
import java.io.*;
import javax.net.ssl.*;
//import javax.security.cert.X509Certificate;
import java.security.KeyStore;

/*
 * This example shows how to set up a key manager to do client
 * authentication if required by server.
 *
 * This program assumes that the client is not inside a firewall.
 * The application can be modified to connect to a server outside
 * the firewall by following SSLSocketClientWithTunneling.java.
 */
public class SSLSocketClientWithClientAuth {

        public static void main(String[] args) throws Exception {
                String host = "https://cmip-dn.badc.rl.ac.uk/";
                int port = 443;
                String path = "thredds/dodsC/cmip5.output1.MOHC.HadCM3.decadal1960.day.atmos.day.r4i2p1.sfcWind.20110627.aggregation";
                
                for (int i = 0; i < args.length; i++)
                        System.out.println(args[i]);

               /* if (args.length < 3) {
                        System.out.println("USAGE: java SSLSocketClientWithClientAuth "
                                        + "host port requestedfilepath");
                        System.exit(-1);
                }

                try {
                        host = args[0];
                        port = Integer.parseInt(args[1]);
                        path = args[2];
                } catch (IllegalArgumentException e) {
                        System.out.println("USAGE: java SSLSocketClientWithClientAuth "
                                        + "host port requestedfilepath");
                        System.exit(-1);
                }*/

                try {

                        /*
                         * Set up a key manager for client authentication if asked by the
                         * server. Use the implementation's default TrustStore and
                         * secureRandom routines.
                         */
                        SSLSocketFactory factory = null;
                        try {
                                SSLContext ctx;
                                KeyManagerFactory kmf;
                                KeyStore ks;
                                char[] passphrase = "password".toCharArray();

                                ctx = SSLContext.getInstance("TLS");
                                kmf = KeyManagerFactory.getInstance("SunX509");
                                ks = KeyStore.getInstance("JKS");

                                ks.load(new FileInputStream("/home/plieger/software/source/expts/myproxy/cred-2010-06-30.pem"), passphrase);

                                kmf.init(ks, passphrase);
                                ctx.init(kmf.getKeyManagers(), null, null);

                                factory = ctx.getSocketFactory();
                        } catch (Exception e) {
                                throw new IOException(e.getMessage());
                        }

                        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);

                        /*
                         * send http request
                         * 
                         * See SSLSocketClient.java for more information about why there is
                         * a forced handshake here when using PrintWriters.
                         */
                        socket.startHandshake();

                        PrintWriter out = new PrintWriter(new BufferedWriter(
                                        new OutputStreamWriter(socket.getOutputStream())));
                        out.println("GET " + path + " HTTP/1.0");
                        out.println();
                        out.flush();

                        /*
                         * Make sure there were no surprises
                         */
                        if (out.checkError())
                                System.out
                                                .println("SSLSocketClient: java.io.PrintWriter error");

                        /* read response */
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket
                                        .getInputStream()));

                        String inputLine;

                        while ((inputLine = in.readLine()) != null)
                                System.out.println(inputLine);

                        in.close();
                        out.close();
                        socket.close();

                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
}
