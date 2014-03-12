package tools;

import impactservice.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

/**
 * Servlet implementation class DoHTTPRequest
 */
public class HTTPTools extends HttpServlet {
  private static final long serialVersionUID = 1L;

  /**
   * @see HttpServlet#HttpServlet()
   */
  public HTTPTools() {
    super();
    // TODO Auto-generated constructor stub
  }

  static byte[] validTokens = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
      'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
      'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
      'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '-', '|', '&',
      '.', ',', '~', ' ','/',':','?','_','#','=' };
  
  public static String validateInputTokens(String input) throws Exception {
    if(input == null)return null;
    
    byte[] str = input.getBytes();
    for (int c = 0; c < str.length; c++) {
      boolean found = false;
      for (int v = 0; v < validTokens.length; v++) {
        if (validTokens[v] == str[c]) {
          found = true;
          break;
        }
      }
      if (found == false) {
        
        String message = "Invalid token given: '"
            + Character.toString((char) str[c]) + "', code (" + str[c] + ").";
        DebugConsole.errprintln("Invalid string given: " + message + " in string "+input);
        throw new InvalidHTTPKeyValueTokensException(message);
      }
    }
    return input;
  }

  /*
   * This code is public domain: you are free to use, link and/or modify it in
   * any way you want, for all purposes including commercial applications.
   */
  public static class WebClientDevWrapper {

    public static HttpClient wrapClient(HttpClient base) {
      try {
        SSLContext ctx = SSLContext.getInstance("TLS");
        X509TrustManager tm = new X509TrustManager() {

          public X509Certificate[] getAcceptedIssuers() {
            return null;
          }

          @Override
          public void checkClientTrusted(X509Certificate[] arg0, String arg1)
              throws CertificateException {
            // TODO Auto-generated method stub

          }

          @Override
          public void checkServerTrusted(X509Certificate[] arg0, String arg1)
              throws CertificateException {
            // TODO Auto-generated method stub

          }
        };
        ctx.init(null, new TrustManager[] { tm }, null);
        SSLSocketFactory ssf = new SSLSocketFactory(ctx);
        ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        ClientConnectionManager ccm = base.getConnectionManager();
        SchemeRegistry sr = ccm.getSchemeRegistry();
        sr.register(new Scheme("https", ssf, 443));
        return new DefaultHttpClient(ccm, base.getParams());
      } catch (Exception ex) {
        ex.printStackTrace();
        return null;
      }
    }
  }

  public static class WebRequestBadStatusException extends Exception {
    private static final long serialVersionUID = 1L;
    int statusCode = 0;
    String result = null;

    public WebRequestBadStatusException(int statusCode, String result) {
      this.statusCode = statusCode;
      this.result = result;
    }

    public WebRequestBadStatusException(int statusCode) {
      this.statusCode = statusCode;
      this.result = "";
    }

    public int getStatusCode() {
      return statusCode;
    }

    public String getResult() {
      return result;
    }

    public String getMessage() {
      return "HTTP status code " + statusCode;
    }
  }

  public static class InvalidHTTPKeyValueTokensException extends Exception {
    private static final long serialVersionUID = 1L;
    String message = null;

    public InvalidHTTPKeyValueTokensException(String result) {
      this.message= result;
    }

    public String getMessage() {
      return message;
    }
  }

  
  public static String makeHTTPGetRequest(String url)
      throws WebRequestBadStatusException, IOException {
    return makeHTTPGetRequest(url, null, null, null);
  }

  public static String makeHTTPGetRequest(String url, String pemFile,
      String trustRootsFile, String trustRootsPassword)
      throws WebRequestBadStatusException, IOException {
    String connectToURL = makeCleanURL(url);
    DebugConsole.println("  Making GET: " + connectToURL);
    if (pemFile != null) {
      DebugConsole.println("  Using private key: " + pemFile);
      DebugConsole.println("  Using trustRootsFile: " + trustRootsFile);
      DebugConsole.println("  Using trustRootsPassword: " + trustRootsPassword);

    }
    if (Configuration.GlobalConfig.isInOfflineMode() == true) {
      DebugConsole.println("Offline mode");
      return null;
    }
    long startTimeInMillis = Calendar.getInstance().getTimeInMillis();

    String result = null;
    String redirectLocation = null;
    try {
      DefaultHttpClient httpclient = null;

      if (pemFile == null || trustRootsFile == null
          || trustRootsPassword == null) {
        httpclient = new DefaultHttpClient();
        httpclient = (DefaultHttpClient) WebClientDevWrapper
            .wrapClient(httpclient);
      } else {
        httpclient = createHTTPClientFromGSSCredential(pemFile, trustRootsFile,
            trustRootsPassword);
      }

      HttpGet httpget = new HttpGet(connectToURL);

      HttpResponse response = null;

      response = httpclient.execute(httpget);

      HttpEntity entity = response.getEntity();
      // DebugConsole.println("Result status  : " + response.getStatusLine());
      if (response.getStatusLine().getStatusCode() < 200
          || response.getStatusLine().getStatusCode() > 300) {
        if (entity != null) {
          result = EntityUtils.toString(entity); // DebugConsole.println("Content:\n"+EntityUtils.toString(entity);
        }
        try {
          entity.getContent().close();
        } catch (Exception e) {
        }
        // DebugConsole.errprintln("HTTPCode "+response.getStatusLine().getStatusCode()+" for '"+connectToURL+"'"+" "+result);

        throw new WebRequestBadStatusException(response.getStatusLine()
            .getStatusCode(), result);

      }
      // DebugConsole.println("Result mimetype: " +
      // response.getFirstHeader("content-type").getValue());
      if (entity != null) {
        result = EntityUtils.toString(entity); // DebugConsole.println("Content:\n"+EntityUtils.toString(entity);
      }
      org.apache.http.Header locationHeader = response
          .getFirstHeader("location");
      if (locationHeader != null) {
        redirectLocation = locationHeader.getValue();
      }
      // EntityUtils.consume(entity);
      try {
        entity.getContent().close();
      } catch (Exception e) {
      }
    } catch (UnknownHostException unknownHostException) {
      throw unknownHostException;
    } catch (IOException e) {
      DebugConsole.printStackTrace(e);
      throw new WebRequestBadStatusException(403, "IOException: Forbidden");
    } catch (UnrecoverableKeyException e) {
      DebugConsole.printStackTrace(e);
      throw new WebRequestBadStatusException(403);
    } catch (KeyManagementException e) {
      DebugConsole.printStackTrace(e);
      throw new WebRequestBadStatusException(403);
    } catch (KeyStoreException e) {
      DebugConsole.printStackTrace(e);
      throw new WebRequestBadStatusException(403);
    } catch (NoSuchAlgorithmException e) {
      DebugConsole.printStackTrace(e);
      throw new WebRequestBadStatusException(403);
    } catch (CertificateException e) {
      DebugConsole.printStackTrace(e);
      throw new WebRequestBadStatusException(403);
    } catch (GSSException e) {
      DebugConsole.printStackTrace(e);
      throw new WebRequestBadStatusException(403);
    } catch (WebRequestBadStatusException e) {
      DebugConsole.printStackTrace(e);
      throw e;
    } catch (Exception e) {
      DebugConsole.printStackTrace(e);
      DebugConsole.println("HTTP Exception");
      throw new WebRequestBadStatusException(403);
    }
    if (redirectLocation != null) {
      DebugConsole.println("redirectLocation =" + redirectLocation);
    }
    long stopTimeInMillis = Calendar.getInstance().getTimeInMillis();
    DebugConsole.println("Finished GET: " + connectToURL + " ("
        + (stopTimeInMillis - startTimeInMillis) + " ms)");
    // DebugConsole.println("Retrieved "+result.length()+" bytes");
    return result;
  }

  public static void main(String[] argks) {
    // String
    // r=makeHTTPJSONGetRequest("http://verc.enes.org/myapp/cmip5/ws/rest/search?facet=id,dataset_name&realm=atmos&frequency=6hr");

  }

  /**
   * Returns the value of a key, but does checking on valid tokens for XSS attacks and decodes the URL.
   * @param request The HTTPServlet containing the KVP's
   * @param name Name of the key
   * @return The value of the key
   * @throws Exception (UnsupportedEncoding and InvalidHTTPKeyValueTokensException)
   */
  public static String getHTTPParam(HttpServletRequest request, String name)
      throws Exception {
    String param = request.getParameter(name);
    if (param != null) {
      param = URLDecoder.decode(param, "UTF-8");
      param = validateInputTokens(param);
    } else {
      throw new Exception("UnableFindParam " + name);
    }
    return param;
  }

  /**
   * Get values for a multiple keys with the same name in a URL, 
   * e.g. ?variable=psl&variable=tas means: key="variable" value="psl,tas" (as list) 
   * @param url The URL containging the KVP encoded data
   * @param key The key we want to search for
   * @return value, null if not found.
   * @throws Exception 
   */
  static public List<String> getKVPList(String url, String key) throws Exception {

    String urlParts[] = url.split("\\?");
    String queryString = urlParts[urlParts.length - 1];
    List<String> values = new ArrayList<String>();
    // System.out.println("*********QU"+queryString);
    String[] kvpparts = queryString.split("&");
    for (int j = 0; j < kvpparts.length; j++) {
      // System.out.println("*********KV"+kvpparts[j]);
      String kvp[] = kvpparts[j].split("=");

      if (kvp.length == 2) {

        if (kvp[0].equalsIgnoreCase(key)){
          String valueChecked = validateInputTokens(kvp[1]);
          values.add(valueChecked);
        }
      }
    }
    return values;

  }

  static public String makeCleanURL(String url) {
    // DebugConsole.println("oldURL="+url);
    if (url.length() == 0)
      return url;
    // Remove double && signs
    String newURL = "";
    String urlParts[] = url.split("\\?");
    if (urlParts.length == 2) {
      newURL = urlParts[0] + "?";
    }
    boolean requireAmp = false;
    String queryString = urlParts[urlParts.length - 1];
    // System.out.println("*********QU"+queryString);
    String[] kvpparts = queryString.split("&");
    for (int j = 0; j < kvpparts.length; j++) {
      // System.out.println("*********KV"+kvpparts[j]);
      String kvp[] = kvpparts[j].split("=");

      if (kvp.length == 2) {
        if (requireAmp)
          newURL += "&";
        newURL += kvp[0] + "=" + kvp[1];
        requireAmp = true;
      }
      if (kvp.length == 1) {
        if (kvp[0].length() != 0) {
          newURL += kvp[0];
          if (urlParts.length == 1 && j == 0) {
            newURL += "?";
          }
        }
      }
    }
    // return newURL;

    try {
      // DebugConsole.println("+newURL: "+newURL);
      String rootCatalog = new URL(newURL).toString();
      String path = new URL(rootCatalog).getFile();
      String hostPath = rootCatalog.substring(0,
          rootCatalog.length() - path.length());

      // DebugConsole.println("Catalog: "+rootCatalog);
      // DebugConsole.println("hostPath: "+hostPath);
      path = path.replace("//", "/");

      newURL = hostPath + path;
      // DebugConsole.println("newURL: "+newURL);
      // DebugConsole.println("/newURL: "+newURL);
      return newURL;
    } catch (MalformedURLException e) {
      return newURL;
    }

    // TODO THIS does not work, sometimes http:// changes to http:/
    // Remove double //
    /*
     * boolean slashAtStart = false; if(newURL.charAt(0)=='/')slashAtStart=true;
     * 
     * urlParts = newURL.split("/"); newURL="";
     * 
     * for(int j=0;j<urlParts.length;j++){
     * 
     * if(urlParts[j].length()>0){
     * if(newURL.length()>0||slashAtStart)newURL+="/";
     * //DebugConsole.println("URLPARTS"+urlParts[j]); newURL+=urlParts[j];
     * //if(j==0) {
     * if(urlParts[j].indexOf("http")==0||urlParts[j].indexOf("dods")==0){
     * newURL+="/"; } } }
     * 
     * 
     * } //DebugConsole.println("newURL="+newURL); return newURL;
     */
  }

  public static List<String> getKVPListDecoded(String url, String key) throws Exception {
    List<String> a = getKVPList(url, key);
    if (a == null)
      return null;
    try {
      for (int j = 0; j < a.size(); j++) {
        a.set(j, validateInputTokens(URLDecoder.decode(a.get(j), "UTF-8")));
      }
    } catch (UnsupportedEncodingException e) {
      return null;
    }
    return a;
  }

  public static DefaultHttpClient createHTTPClientFromGSSCredential(
      String pemFile, String trustRootsFile, String trustRootsPassword)
      throws GSSException, KeyStoreException, NoSuchAlgorithmException,
      CertificateException, IOException, UnrecoverableKeyException,
      KeyManagementException {

    ExtendedGSSManager m = (ExtendedGSSManager) ExtendedGSSManager
        .getInstance();
    GlobusGSSCredentialImpl cred = (GlobusGSSCredentialImpl) m
        .createCredential(Tools.readBytes(pemFile),
            ExtendedGSSCredential.IMPEXP_OPAQUE,
            GSSCredential.DEFAULT_LIFETIME, null, GSSCredential.ACCEPT_ONLY);

    PrivateKey privateKey = cred.getPrivateKey();// KeyFactory.getInstance("RSA").
                                                 // generatePrivate(new
                                                 // PKCS8EncodedKeySpec(derFile.privateKeyDER));
    Certificate certificate = cred.getCertificateChain()[0];

    // Create a new keyStore to load TrustRoots, Certificates and Keys in.
    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

    // Load trustroots
    FileInputStream trustStoreStream = new FileInputStream(new File(
        trustRootsFile));
    try {
      keyStore.load(trustStoreStream, "changeit".toCharArray());
    } finally {
      try {
        trustStoreStream.close();
      } catch (Exception ignore) {
      }
    }
    trustStoreStream.close();

    // Set key and certificate
    keyStore.setKeyEntry("privateKeyAlias", privateKey,
        trustRootsPassword.toCharArray(), new Certificate[] { certificate });

    // Create a trustmanager
    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
    tmf.init(keyStore);

    // Create a keymanager
    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    kmf.init(keyStore, trustRootsPassword.toCharArray());

    // Setup SSL context
    SSLContext sslcontext = SSLContext.getInstance("SSL");
    sslcontext.init(kmf.getKeyManagers(), tmf.getTrustManagers(),
        new java.security.SecureRandom());
    SSLSocketFactory socketFactory = new SSLSocketFactory(sslcontext);

    Scheme sch = new Scheme("https", socketFactory, 443);

    DefaultHttpClient httpclient = new DefaultHttpClient();
    httpclient.getConnectionManager().getSchemeRegistry().register(sch);

    return httpclient;
  }

  public static String getKVPItem(String queryString, String string) throws Exception {
    List<String> items = getKVPList(queryString, string);
    if (items.size() == 0)
      return null;
    return items.get(0);
  }

  public static String getKVPItemDecoded(String queryString, String string) throws Exception {
    List<String> items = getKVPListDecoded(queryString, string);
    if (items.size() == 0)
      return null;
    return items.get(0);
  }

}
