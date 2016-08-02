package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
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
import java.util.SortedSet;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

import impactservice.Configuration;





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
  }

  static byte[] validTokens = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
      'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
      'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
      'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '-', '|', '&',
      '.', ',', '~', ' ','/',':','?','_','#','=' ,'(',')',';','%'};
  /**
   * Validates input for valid tokens, preventing XSS attacks. Throws Exception when invalid tokens are encountered.
   * @param input The string as input
   * @return returns the same string
   * @throws Exception when invalid tokens are encountered
   */
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
        Debug.errprintln("Invalid string given: " + message + " in string "+input);
        throw new InvalidHTTPKeyValueTokensException(message);
      }
    }
    return input;
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
      
      //if(statusCode == 401){
        return statusCode+": "+ org.apache.commons.httpclient.HttpStatus.getStatusText(statusCode); 
      //}
      //return ""+statusCode;
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

  
  public static String makeHTTPGetRequest(String url, int timeOutMs)
      throws WebRequestBadStatusException, IOException {
    return _makeHTTPGetWithHeaderRequest(url,null,null,null,null,null,null,timeOutMs);
  }
  
  public static String makeHTTPGetRequestBasicAuth(String url,String basicAuthUserName,String basicAuthPassword, int timeOutMs)
      throws WebRequestBadStatusException, IOException {
    return _makeHTTPGetWithHeaderRequest(url,null,null,null,basicAuthUserName,basicAuthPassword,null,timeOutMs);
  }

  
  public static String makeHTTPGetRequestWithHeaders(String url,KVPKey key, int timeOutMs)throws WebRequestBadStatusException, IOException{
    return _makeHTTPGetWithHeaderRequest(url,null,null,null,null,null,key,timeOutMs);
  }
  
//  public static String makeHTTPGetRequestWithHeadersX509(String url, String pemFile,
//      String trustRootsFile, String trustRootsPassword,KVPKey headers)throws WebRequestBadStatusException, IOException{
//    return _makeHTTPGetWithHeaderRequest(url,pemFile,trustRootsFile,trustRootsPassword,null,null,headers);
//  }
  
  public static String makeHTTPGetRequestX509ClientAuthentication(String url, String pemFile,
      String trustRootsFile, String trustRootsPassword, int timeOutMs)throws WebRequestBadStatusException, IOException, SSLPeerUnverifiedException{
    return _makeHTTPGetWithHeaderRequest(url,pemFile,trustRootsFile,trustRootsPassword,null,null,null, timeOutMs);
  }
  private static String _makeHTTPGetWithHeaderRequest(String url, String pemFile,
      String trustRootsFile, String trustRootsPassword,String basicAuthUserName,String basicAuthPassword, KVPKey headers, int timeOutMs)
      throws WebRequestBadStatusException, IOException, SSLPeerUnverifiedException, SSLException {
    Debug.println("createHTTPClientFromGSSCredential, trustrootsfile:"+trustRootsFile);
    Debug.println("createHTTPClientFromGSSCredential, pemFile:"+pemFile);
    String connectToURL = makeCleanURL(url);
 //   Debug.println("  Making GET: " + connectToURL);
    if (pemFile != null) {
      Debug.println("  Using private key: " + pemFile);
      Debug.println("  Using trustRootsFile: " + trustRootsFile);
      Debug.println("  Using trustRootsPassword: " + trustRootsPassword);

    }
    if (Configuration.GlobalConfig.isInOfflineMode() == true) {
      Debug.println("Offline mode");
      return null;
    }
    long startTimeInMillis = Calendar.getInstance().getTimeInMillis();

    String result = null;
    String redirectLocation = null;
    try {
      DefaultHttpClient httpclient = null;

      if (pemFile == null || trustRootsFile == null || trustRootsPassword == null) {
        
        httpclient = new DefaultHttpClient();
        
        if(basicAuthUserName!=null&&basicAuthPassword!=null){
        
          CredentialsProvider provider = httpclient.getCredentialsProvider();
          UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(basicAuthUserName, basicAuthPassword);
          
          provider.setCredentials(AuthScope.ANY, credentials);
        }
        
        
        
        //httpclient = (DefaultHttpClient) WebClientDevWrapper.wrapClient(httpclient);

        
      } else {
        httpclient = createHTTPClientFromGSSCredential(pemFile, trustRootsFile,
            trustRootsPassword);
      }

      HttpGet httpget = new HttpGet(connectToURL);

      HttpResponse response = null;

      if(headers!=null){
        SortedSet<String> a = headers.getKeys();
        for(String b : a){
          System.out.println("Adding header "+b+"="+headers.getValue(b).firstElement());
          httpget.addHeader(b,headers.getValue(b).firstElement());
        }
        
      }
      
      if(timeOutMs <=0){
        timeOutMs = 15000;
      }
      HttpParams httpParams = httpclient.getParams();
      HttpConnectionParams.setConnectionTimeout(httpParams, timeOutMs); // http.connection.timeout
      HttpConnectionParams.setSoTimeout(httpParams, timeOutMs); // http.socket.timeout
      
      
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
        Debug.errprintln("HTTPCode "+response.getStatusLine().getStatusCode()+" for '"+connectToURL);

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
    } catch (SSLPeerUnverifiedException sSLPeerUnverifiedException){
      throw sSLPeerUnverifiedException;
    } catch (SSLException sslException){
      throw sslException;
    } catch (UnknownHostException unknownHostException) {
      throw unknownHostException;
    } catch(SocketTimeoutException e){
      //Debug.printStackTrace(e);
      throw new WebRequestBadStatusException(408, "Request time out");
    }catch (IOException e) {
      Debug.printStackTrace(e);
      throw new WebRequestBadStatusException(400, "IOException");
    } catch (UnrecoverableKeyException e) {
      Debug.printStackTrace(e);
      throw new WebRequestBadStatusException(400,"UnrecoverableKeyException");
    } catch (KeyManagementException e) {
      Debug.printStackTrace(e);
      throw new WebRequestBadStatusException(400,"KeyManagementException");
    } catch (KeyStoreException e) {
      Debug.printStackTrace(e);
      throw new WebRequestBadStatusException(400,"KeyStoreException");
    } catch (NoSuchAlgorithmException e) {
      Debug.printStackTrace(e);
      throw new WebRequestBadStatusException(400,"NoSuchAlgorithmException");
    } catch (CertificateException e) {
      Debug.printStackTrace(e);
      throw new WebRequestBadStatusException(400,"CertificateException");
    } catch (GSSException e) {
      Debug.printStackTrace(e);
      throw new WebRequestBadStatusException(400,"GSSException");
    } catch (WebRequestBadStatusException e) {
      //Debug.printStackTrace(e);
      throw e;
    } catch (Exception e) {
      Debug.printStackTrace(e);
      Debug.println("HTTP Exception");
      throw new WebRequestBadStatusException(400,e.getMessage());
    }
    if (redirectLocation != null) {
      Debug.println("redirectLocation =" + redirectLocation);
    }
    long stopTimeInMillis = Calendar.getInstance().getTimeInMillis();
    //System.out.println(result)
 //   Debug.println("Finished GET: " + connectToURL + " ("
 //      + (stopTimeInMillis - startTimeInMillis) + " ms)");
    //Debug.println("Retrieved "+result.length()+" bytes");
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
      int firstEqualsSign = kvpparts[j].indexOf("=");
      if(firstEqualsSign>=0){
        String foundKey = kvpparts[j].substring(0, firstEqualsSign);
        String foundValue = kvpparts[j].substring(firstEqualsSign+1);
        if (foundKey.equalsIgnoreCase(key)){
          String valueChecked = validateInputTokens(foundValue);
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

    Debug.println("createHTTPClientFromGSSCredential, trustrootsfile:"+trustRootsFile);
    Debug.println("createHTTPClientFromGSSCredential, pemFile:"+pemFile);
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
    Debug.println("x509 http client has been created");
    return httpclient;
  }

  /**
   * Finds a KVP from the querystring. Returns null if not found.
   * @param queryString
   * @param string
   * @return
   * @throws Exception
   */
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

  public static String makeHTTPGetRequest(URL url,int timeOutMs) throws WebRequestBadStatusException, IOException {
    return makeHTTPGetRequest(url.toString(),timeOutMs);
    
  }

  public static KVPKey parseQueryString(String url) {
    KVPKey kvpKey = new KVPKey();
    String urlParts[] = url.split("\\?");
    String queryString = urlParts[urlParts.length - 1];
    String[] kvpparts = queryString.split("&");
    for (int j = 0; j < kvpparts.length; j++) {
      int equalIndex = kvpparts[j].indexOf("=");
      if(equalIndex > 0){
        String key = kvpparts[j].substring(0,equalIndex);
        String value = kvpparts[j].substring(equalIndex+1);
        String valueChecked;
        try {
          valueChecked = validateInputTokens(value);
          kvpKey.addKVP(key,valueChecked);
        } catch (Exception e) {
          kvpKey.addKVP(key,e.getMessage());
        }
      }
    }
    return kvpKey;
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

          }

          @Override
          public void checkServerTrusted(X509Certificate[] arg0, String arg1)
              throws CertificateException {

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

  
  public static String makeHTTPostRequestWithHeaders(String connectToURL, KVPKey headers,String postData) throws Exception {
//    String trustRootsFile = "/nobackup/users/plieger/c4i_dev/certs/esg-truststore.ts";
//    String trustRootsPassword="changeit";
//    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
//
//    // Load trustroots
//    FileInputStream trustStoreStream = new FileInputStream(new File(
//        trustRootsFile));
//    try {
//      keyStore.load(trustStoreStream, "changeit".toCharArray());
//    } finally {
//      try {
//        trustStoreStream.close();
//      } catch (Exception ignore) {
//      }
//    }
//    trustStoreStream.close();
//
//
//    // Create a trustmanager
//    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
//    tmf.init(keyStore);
//
//    // Create a keymanager
//    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
//    kmf.init(keyStore, trustRootsPassword.toCharArray());
//
//    // Setup SSL context
//    SSLContext sslcontext = SSLContext.getInstance("SSL");
//    sslcontext.init(kmf.getKeyManagers(), tmf.getTrustManagers(),
//        new java.security.SecureRandom());
//    SSLSocketFactory socketFactory = new SSLSocketFactory(sslcontext);
//
//    Scheme sch = new Scheme("https", socketFactory, 443);
//
//    
    DefaultHttpClient httpclient =new DefaultHttpClient();
//    httpclient.getConnectionManager().getSchemeRegistry().register(sch);
    
    HttpPost httpPost=new HttpPost(connectToURL);
    if(headers!=null){
      SortedSet<String> a = headers.getKeys();
      for(String b : a){
        //System.out.println("Adding header "+b+"="+headers.getValue(b).firstElement());
        httpPost.addHeader(b,headers.getValue(b).firstElement());
        Debug.println("addHeader ["+b+","+headers.getValue(b).firstElement()+"]");
      }
    }
    httpPost.addHeader("Content-Type","application/x-www-form-urlencoded ");
    httpPost.setEntity(new StringEntity(postData));
    Debug.println("connectToURL ["+connectToURL+"]");
    Debug.println("Posting ["+postData+"]");
    HttpResponse httpResponse=httpclient.execute(httpPost);
    return EntityUtils.toString(httpResponse.getEntity(),"utf-8");
  }
  
  /*
   * Gets the posted data from the request
   */
  public static String getPostBody(HttpServletRequest request) throws IOException {

    String body = null;
    StringBuilder stringBuilder = new StringBuilder();
    BufferedReader bufferedReader = null;

    try {
        InputStream inputStream = request.getInputStream();
        if (inputStream != null) {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            char[] charBuffer = new char[128];
            int bytesRead = -1;
            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                stringBuilder.append(charBuffer, 0, bytesRead);
            }
        } else {
            stringBuilder.append("");
        }
    } catch (IOException ex) {
        throw ex;
    } finally {
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (IOException ex) {
                throw ex;
            }
        }
    }

    body = stringBuilder.toString();
    return body;
  }
  
}
