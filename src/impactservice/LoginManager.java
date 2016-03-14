package impactservice;

import impactservice.AccessTokenStore.AccessTokenHasExpired;
import impactservice.AccessTokenStore.AccessTokenIsNotYetValid;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import oauth2handling.OAuth2Handler;

import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.ietf.jgss.Oid;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import tools.Debug;
import tools.HTTPTools;
import tools.HTTPTools.WebRequestBadStatusException;
import tools.JSONResponse;
import tools.MyXMLParser.XMLElement;
import tools.Tools;

/**
 * @author maartenplieger
 * 
 *         Info:
 *         http://www.globus.org/cog/distribution/1.2/api/org/globus/myproxy
 *         /MyProxy.html Command line SLC retrieval: myproxy-logon -s
 *         bvlpenes.knmi.nl -k https://pcmdi3.llnl.gov/esgcet/myopenid/<user>
 * 
 *         To be able to login on ESGF openid providers, their trustroots need
 *         to be set. This can be done by adding the following arguments to
 *         ECLIPSE, or to JAVA_OPTS in production environment:
 * 
 *         (https://meteo.unican.es/trac/wiki/ESGF-Security)
 * 
 *         wget https://rainbow.llnl.gov/dist/certs/esg-truststore.ts
 * 
 *         In: Run --> Run Configurations --> Arguments --> VM Arguments, add:
 *         -Djavax.net.ssl.trustStore="<pathto>esg-truststore.ts"
 *         -Djavax.net.ssl.trustStorePassword="changeit" Or add these arguments
 *         to JAVA_OPTS in production environment
 * 
 * 
 *         # Or Create a custom java trust store file with a shell script: echo
 *         | openssl s_client -connect pcmdi9.llnl.gov:443 2>&1 | sed -ne
 *         '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > pcmdi9.llnl.gov
 *         keytool -delete -alias pcmdi9.llnl.gov -keystore openidcerts.ts
 *         -storepass changeit keytool -import -v -trustcacerts -alias
 *         pcmdi9.llnl.gov -file pcmdi9.llnl.gov -keystore openidcerts.ts
 *         -storepass changeit -noprompt
 * 
 * 
 * 
 * 
 */

public class LoginManager {

  private static Vector<ImpactUser> users = new Vector<ImpactUser>();
  
  
  public static void getUserProxyService(ImpactUser user)
      throws MalformedURLException, WebRequestBadStatusException, Exception {
    // Test met openID URL

    user.userMyProxyService = null;
    // String data2 = HTTPTools.makeHTTPGetRequest(user.id);
    XMLElement xmlParser = new XMLElement();
    if (user.getId().startsWith("http") == true) {
      xmlParser.parse(new URL(user.getId()));
      // Debug.println(xmlParser.toString());
      Vector<XMLElement> services = xmlParser.getFirst().get("XRD")
          .getList("Service");
      for (XMLElement service : services) {
        if (service.get("Type").getValue().indexOf("myproxy") != -1) {
          Debug.println(service.get("URI").getValue());
          user.userMyProxyService = service.get("URI").getValue();
        }
      }
    }

  }

  /**
   * Retrieves a SLC (short lived credential) from the SLCS and stores it in the
   * users home directory
   * 
   * @param user
   *          User object with identifier and home directory set
   * @throws Exception
   */
  synchronized static void getCredential(ImpactUser user)
      throws Exception {

    if (Configuration.GlobalConfig.isInOfflineMode() == true) {
      Debug.println("offline mode");
      return;
    }

    // Retrieve myproxy service info from openID URL
    user.userMyProxyService = null;
    if (user.getOpenId() != null) {
      // String data2 = HTTPTools.makeHTTPGetRequest(user.id);
      XMLElement xmlParser = new XMLElement();
      xmlParser.parse(new URL(user.getOpenId()));
      Vector<XMLElement> services = xmlParser.getFirst().get("XRD")
          .getList("Service");
      for (XMLElement service : services) {
        if (service.get("Type").getValue().indexOf("myproxy") != -1) {
          Debug.println("myproxyservice retrieved from OpenId: "
              + service.get("URI").getValue());
          user.userMyProxyService = service.get("URI").getValue();
        }
      }
    }

    MyProxy myProxy = new MyProxy(
        Configuration.LoginConfig.getMyProxyServerHost(),
        Configuration.LoginConfig.getMyProxyServerPort());


    Debug.println("Setting proxy host:port as '"
        + Configuration.LoginConfig.getMyProxyServerHost() + ":"
        + Configuration.LoginConfig.getMyProxyServerPort() + "'");
    // GSSCredential retcred = myProxy.get(null,"testyser","bbbbbghfhfh",10);
    try {
      String userName = Configuration.LoginConfig.getMyProxyDefaultUserName();
      if (userName == null) {
        userName = user.getOpenId();
        if(userName == null){
          userName = user.getInternalName();
        }
      }
      if (userName == null) {
        Debug.errprintln("No openid set for " + user.getId());
        throw new Exception("LoginManager: No openid set");
      }

      Debug.println("Setting username to " + userName + ":"
          + Configuration.LoginConfig.getMyProxyDefaultPassword());

      ExtendedGSSCredential cred = (ExtendedGSSCredential) myProxy.get(
          userName, Configuration.LoginConfig.getMyProxyDefaultPassword(),
          60 * 60 * 24 * 7);

      try {
        Debug.println(cred.getName().toString());
        // DebugConsole.println(""+cred.getRemainingLifetime());

        Oid[] mechs = cred.getMechs();
        if (mechs != null) {
          for (int i = 0; i < mechs.length; i++)
            Debug.println(mechs[i].toString());
        }

        // Export credential to file
        byte[] data = cred.export(ExtendedGSSCredential.IMPEXP_OPAQUE);
        FileOutputStream out = new FileOutputStream(user.certificateFile);
        out.write(data);
        out.close();
        // release system resources held by the credential
        cred.dispose();

      } catch (Exception e) {
        Debug.printStackTrace(e);
        throw new Exception("LoginManager: Unable to write credential");
      }
    } catch (MyProxyException e) {
      String msg = "Unable to get credential for " + user.getOpenId();
      Debug.errprintln(msg);
      Debug.printStackTrace(e);
      throw new Exception("LoginManager: " + msg);
    }
    Debug.println("Credentials for user " + user.getId() + " retrieved");
  }

  /**
   * Get the user object based on the http session, based on x509 cert, or access token
   * 
   * @param request The httpservletrequest
   * @return The user object or null when a redirect is requested.
   * @throws Exception
   */
  public static ImpactUser getUser(HttpServletRequest request,
      HttpServletResponse response) throws Exception {

//    String doLogOut = tools.HTTPTools.getHTTPParam(request,"c4i_dologout");
//    if(doLogOut.equals("true")==true){
//      logout(request);
//      return null;
//    }
    
    //Get user from session
    HttpSession session = request.getSession();
    String id = (String) session.getAttribute("user_identifier");
    if (Configuration.GlobalConfig.isInOfflineMode() == true) {
      id = Configuration.GlobalConfig.getDefaultUser();
    }

    //Get user from access token provided in the request
    if (id == null ) {
      try{
       
        try{
          JSONObject token = AccessTokenStore.checkIfTokenIsValid(request);
          if(token!=null){
            //Debug.println("Valid token "+token.toString()+" obtained");
            id = token.getString("userid");
          }
        }catch(Exception e){
        }
      }catch(Exception e){
        //e.printStackTrace();
      }
   
    }
    if (id == null) {
      try {
        OAuth2Handler.UserInfo userInfo = OAuth2Handler
            .verifyAndReturnUserIdentifier(request);
        String userId = null;
        if (userInfo != null) {
          OAuth2Handler.setSessionInfo(request, userInfo);
          userId = userInfo.user_identifier;
        }

        if (userId != null) {
          id = userId;
          Debug.println("User ID from OAuth2 " + userId);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    


    //"Trying to get user info from X509 cert"
    if (id == null && response != null) {
      String CertOpenIdIdentifier = null;
      // org.apache.catalina.authenticator.SSLAuthenticator
      X509Certificate[] certs = (X509Certificate[]) request
          .getAttribute("javax.servlet.request.X509Certificate");
      if (null != certs && certs.length > 0) {
        X509Certificate cert = certs[0];

        String subjectDN = cert.getSubjectDN().toString();
        Debug.println("getSubjectDN: " + subjectDN);
        String[] dnItems = subjectDN.split(", ");
        for (int j = 0; j < dnItems.length; j++) {
          int CNIndex = dnItems[j].indexOf("CN");
          if (CNIndex != -1) {
            CertOpenIdIdentifier = dnItems[j].substring("CN=".length()
                + CNIndex);
          }
        }

      } else {

        String message = "No user information available from either session, oauth2 or x509\n";
        Debug.errprintln(message);
        // response.setStatus(403);
        /*
         * response.getOutputStream().println(message); throw new
         * Exception("You are not logged in...");
         */
      }

      if (CertOpenIdIdentifier != null) {
        id = CertOpenIdIdentifier;
        try{
          request.getSession().setAttribute("openid_identifier",id);
        }catch(Exception e){
          Debug.printStackTrace(e);
        }
      }
    }
    // }
    // }

    // Debug.println("id == "+id);
    if (id == null) {
      throw new WebRequestBadStatusException(401,
          "Unauthorized user, you are not logged in.");
    }
    ImpactUser user = getUser(id, request);
    return user;
  }

  public static String getLoginPage() {
    return "/impactportal/account/login.jsp";
  }

  public static void redirectToLoginPage(HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    String queryString = "";
    if (request.getQueryString() != null) {
      queryString = "?" + request.getQueryString();
    }
    String redirURL = request.getRequestURL().toString() + queryString;
    Debug.println("401:" + redirURL);
    String redirURLEncoded = getLoginPage();
    try {
      redirURLEncoded = URLEncoder.encode(redirURL, "utf-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      response.sendRedirect(getLoginPage());
    }

    response.sendRedirect(getLoginPage() + "?redirect=" + redirURLEncoded);
  }

  public static ImpactUser getUser(HttpServletRequest request) throws Exception {
    return getUser(request, null);
  }

  /**
   * Get user based on his/hers userId
   * 
   * @param userId
   *          The userID, equal to the OpenID identifier
   * @return The user object
   */
  public synchronized static ImpactUser getUser(String userId,
      HttpServletRequest request) {
    // Debug.println("Looking up user "+userId);
    // Lookup the user in the vector list
    if (userId == null)
      return null;
    
    userId = userId.replace("http://", "");
    userId = userId.replace("https://", "");
    userId = userId.replaceAll("/", ".");
 
    
    for (int j = 0; j < users.size(); j++) {
      if (users.get(j).getId().equals(userId)) {
        ImpactUser user = users.get(j);
        user.setSessionInfo(request);
        // Debug.println("Found existing user "+userId);
        return user;
      }
    }
    // The user was not found, so create a new user
    Debug.println("Creating new user object for " + userId);
    ImpactUser user = new ImpactUser(userId);
    if(user.getOpenId()==null){
      user.setOpenId((String) request.getSession().getAttribute(
          "openid_identifier"));
    }
    users.add(user);
    user.setSessionInfo(request);
    try {
      checkLogin(userId, request);
    } catch (Exception e) {
    }

    return user;
  }

  /** Get available users */
  public synchronized static Vector<ImpactUser> getUsers() {
    return users;
  }

  /**
   * Called upon succesfull login, handles and checks user ID
   * 
   * @param session
   * @throws Exception
   */
  public synchronized static void checkLogin(String userId,
      HttpServletRequest request) throws Exception {

    Debug.println("checkLogin " + userId);
    if (userId == null) {
      Debug.errprintln("No openIdIdentifier given");
    }
    ImpactUser user = getUser(userId, request);

    Debug.println("Check login " + user.getId());
    user.internalName = user.getId().replace("http://", "");
    user.internalName = user.internalName.replace("https://", "");
    user.internalName = user.internalName.replaceAll("/", ".");
    Debug.println("internalName = " + user.internalName);
    String workspace = Configuration.getImpactWorkspace();
    Debug.println("Base workspace = " + workspace);
    user.setWorkspace(workspace + user.internalName + "/");
    Debug.println("User workspace = " + user.getWorkspace());
    try {
      Debug.println("Making dir " + user.getWorkspace());
      Tools.mkdir(user.getWorkspace());
      Tools.mkdir(user.getWorkspace() + "certs");
      user.certificateFile = user.getWorkspace() + "certs/" + "creds.pem";
    } catch (IOException e) {
      Debug.errprintln(e.getMessage());
      user.credentialError = true;
      throw new Exception(
          "Unable to create credential for user, server misconfiguration:"
              + user.getId() + "\n" + e.getMessage());
    }

    // Get email
    try {
      String emailAddress = (String) request.getSession().getAttribute(
          "emailaddress");
      boolean foundEmail = false;
      if (emailAddress != null) {
        if (emailAddress.length() > 0) {
          user.setEmailAddress(emailAddress);
          foundEmail = true;
          Debug.println("Email: " + emailAddress);
        }
      }
//      if (!foundEmail) {
//        MessagePrinters.emailFatalErrorMessage("User email is not found",
//            "User email is not found for " + user.getId());
//      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Get Certificate
    String certificate = (String) request.getSession().getAttribute(
        "certificate");
    String loginMethod = (String) request.getSession().getAttribute(
        "login_method");
    if (certificate == null) {

      // Obtain from local proxy server
      Debug.println("Certificate not set, retrieving from MyProxy");
      try {
        getCredential(user);
        user.setLoginInfo("Using " + loginMethod
            + ", credential retrieved via impactportal MyProxy.");
      } catch (Exception e) {
        user.credentialError = true;
        // e.printStackTrace();
        user.setLoginInfo("Using " + loginMethod
            + ", unable to retrieve credential via impactportal MyProxy");
        throw new Exception("Unable to get credential for user " + user.getId()
            + "\n" + e.getMessage());
      }
    } else {
      Debug.println("Certificate set, writing to " + user.certificateFile);
      FileOutputStream out = new FileOutputStream(user.certificateFile);
      out.write(certificate.getBytes());
      out.close();
      user.setLoginInfo("Using " + loginMethod
          + ", credential retrieved via remote SLCS.");
    }

    // Check certificate
    String SLCSX509Certificate = tools.Tools.readFile(user.certificateFile);

    X509Certificate cert = null;
    try {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(
          SLCSX509Certificate.getBytes(StandardCharsets.UTF_8)));
      Date date = cert.getNotAfter();
      user.setLoginInfo(" - " + user.getLoginInfo() + "\n - Valid till "
          + date.toString() + ".\n - " + cert.getSubjectDN().toString());

    } catch (CertificateException e) {
      throw new Exception("Unable to validate credential for user "
          + user.getId() + "\n" + e.getMessage());
    }

    createNCResourceFile(user);

    // createFontConfigFile(user);
    user.configured = true;
  }

  /**
   * Create NetCDF .httprc or .dodsrc resource file and store it in the users
   * home directory
   * 
   * @param user
   *          The user object
   * @throws IOException
   */
  public synchronized static void createNCResourceFile(ImpactUser user)
      throws IOException {
    // DebugConsole.println("createNCResourceFile for user "+user.id);
    /*
     * .httprc/.dodsrc file contents: HTTP.SSL.VALIDATE=0
     * HTTP.COOKIEJAR=.dods_cookies HTTP.SSL.CERTIFICATE=<slc.pem>
     * HTTP.SSL.KEY=<slc.pem> HTTP.SSL.CAPATH=<esg_trusted_certificates>
     */

    String fileContents = "HTTP.SSL.VALIDATE=0\n" + "HTTP.COOKIEJAR="
        + user.getWorkspace() + "/.dods_cookies\n" + "HTTP.SSL.CERTIFICATE="
        + user.certificateFile + "\n" + "HTTP.SSL.KEY="
        + user.certificateFile
        + "\n"
        // +"HTTP.SSL.SSLv3="+user.certificateFile+"\n"
        // +"HTTP.SSL.CAPATH="+
        // Configuration.getImpactWorkspace()+"/esg_trusted_certificates/";
        + "HTTP.SSL.CAPATH="
        + Configuration.LoginConfig.getTrustRootsLocation();// +"/esg_trusted_certificates/";
    // Debug.println("createNCResourceFile for user "+user.getId()+":\n"+fileContents);
    Tools.writeFile(user.getWorkspace() + "/.httprc", fileContents);
    Tools.writeFile(user.getWorkspace() + "/.dodsrc", fileContents);
  }

  /**
   * This function can be used when e.g. ncdump or ADAGUC is unable to access a
   * resource. This function make a call with the standard java libraries to
   * identify the real problem. A JSONResponse object is returned with detailed
   * error information. This object can be sent back to the browser directly.
   * 
   * @param requestStr
   * @param request
   * @param response
   * @return
   * @throws WebRequestBadStatusException
   * @throws IOException
   */
  public static JSONResponse identifyWhyGetRequestFailed(String requestStr,
      HttpServletRequest request, HttpServletResponse response)
      throws WebRequestBadStatusException, IOException {
    JSONResponse jsonResponse = new JSONResponse(request);
    ImpactUser user = null;
    try {
      user = getUser(request);
    } catch (Exception e1) {
    }
    String msg = "";
    String redirectURL = null;
    String currentURL = null;
    try {
      String certificateLocation = null;
      if (user != null) {
        if (user.certificateFile != null) {
          certificateLocation = user.certificateFile;
        }
      }

      // try {
      HTTPTools.makeHTTPGetRequestX509ClientAuthentication(requestStr,
          certificateLocation, Configuration.LoginConfig.getTrustStoreFile(),
          Configuration.LoginConfig.getTrustStorePassword());
      // } catch (IOException e) {
      // throw e;
      // }

    } catch (javax.net.ssl.SSLPeerUnverifiedException e) {

      msg = "The peer is unverified (SSL unverified): " + e.getMessage();
      Debug.errprintln(msg);
      jsonResponse.setErrorMessage(msg, 500);
      return jsonResponse;
    } catch (UnknownHostException e) {
      msg = "The host is unknown: '" + e.getMessage() + "'\n";
      Debug.errprintln(msg);
      jsonResponse.setErrorMessage(msg, 500);
      return jsonResponse;
//    } catch (ConnectTimeoutException e) {
//      msg = "The connection timed out: '" + e.getMessage() + "'\n";
//      Debug.errprintln(msg);
//      jsonResponse.setErrorMessage(msg, 500);
//      return jsonResponse;
    } catch (WebRequestBadStatusException e) {
      Debug.println("WebRequestBadStatusException: " +":"
          + e.getStatusCode());
      if (e.getStatusCode() == 400) {
        msg += "HTTP status code " + e.getStatusCode() + ": Bad request\n";
        if (user == null) {
          msg += "\nNote: you are not logged in.\n";
        }
      } else if (e.getStatusCode() == 401) {
        msg += "Unauthorized (401)\n";
        if (user == null) {
          msg += "\nNote: you are not logged in.\n";
          String queryString = "";
          if (request.getQueryString() != null) {
            queryString = "?" + request.getQueryString();
          }
          currentURL = request.getRequestURL().toString() + queryString;
          Debug.println("401:" + currentURL);
          String redirURLEncoded = URLEncoder.encode(currentURL, "utf-8");

          redirectURL = LoginManager.getLoginPage() + "?redirect="
              + redirURLEncoded;
          // Debug.println("msg: throwing WebRequestBadStatusException");
          // throw e;
        }
      } else if (e.getStatusCode() == 403) {
        msg += "Forbidden (403)\n";
        if (user != null) {
          msg += "\nYou are not authorized to view this resource, you are logged in as "
              + user.getOpenId() + "\n";
        }
      } else if (e.getStatusCode() == 404) {
        msg += "File not found (404)\n";
        if (user != null) {
          msg += "\nYou are logged in as " + user.getOpenId() + "\n";
        }
      } else {
        msg = "WebRequestBadStatusException: " + e.getStatusCode() + "\n";
      }

      Debug.errprintln(msg);
      msg = msg.replaceAll("\n", "<br/>");
      jsonResponse.setErrorMessage(msg, e.getStatusCode(), redirectURL,
          currentURL, null);
    }
    return jsonResponse;

  }

  static public void logout(HttpServletRequest request) {
    Debug.println("--- LOGOUT --- "
        + request.getSession().getAttribute("user_identifier"));
    try {
      ImpactUser user;
      user = getUser(request);
      if (user != null) {

        user.removeSessionId(request.getSession().hashCode());
      }
    } catch (Exception e) {
    }

    request.getSession().setAttribute("access_token", null);
    request.getSession().setAttribute("openid_identifier", null);
    request.getSession().setAttribute("user_identifier", null);
    request.getSession().setAttribute("email", null);
    request.getSession().setAttribute("emailaddress", null);
    request.getSession().setAttribute("certificate", null);
    request.getSession().setAttribute("access_token", null);
    request.getSession().setAttribute("login_method", null);
    request.getSession().setAttribute("message", null);
    Debug.println("--- LOGOUT DONE --- ");
  }

}
