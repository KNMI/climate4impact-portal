package impactservice;

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
import java.util.Vector;
import java.util.Map.Entry;

import javax.net.ssl.SSLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import oauth2handling.OAuth2Handler;
import stats.StatLogger;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.ietf.jgss.Oid;
import org.json.JSONObject;

import impactservice.ImpactUser.UserSessionInfo;
import tools.Debug;
import tools.HTTPTools;
import tools.HTTPTools.WebRequestBadStatusException;
import tools.JSONResponse;
import tools.LazyCaller;
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
  static boolean debug=false;
  private static Vector<ImpactUser> users = new Vector<ImpactUser>();
  public static String impactportal_userid_cookie="C4I_ID";
  public static int impactportal_userid_cookie_durationsec = 3600*24;

  public static void getUserProxyService(ImpactUser user)
      throws MalformedURLException, WebRequestBadStatusException, Exception {
    // Test met openID URL

    user.userMyProxyService = null;
    // String data2 = HTTPTools.makeHTTPGetRequest(user.id);
    XMLElement xmlParser = new XMLElement();
    if (user.getUserId().startsWith("http") == true) {
      xmlParser.parse(new URL(user.getUserId()));
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
  private synchronized static void _getCredential(ImpactUser user) throws Exception {
    Debug.println("GetCredential for user "+user.getUserId());
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
          userName = user.getUserId();
        }
      }
      if (userName == null) {
        Debug.errprintln("No openid set for " + user.getUserId());
        throw new Exception("LoginManager: No openid set");
      }

      Debug.println("Setting username to " + userName + ":"
          + Configuration.LoginConfig.getMyProxyDefaultPassword());

      
      
      String identAuth = Configuration.LoginConfig.getMyProxyServerIdendityAuthorization();
      if(identAuth != null){
        Debug.println("Setting myproxy identity authorization to " + identAuth);
        myProxy.setAuthorization(new org.globus.gsi.gssapi.auth.IdentityAuthorization(identAuth));
      }

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
    Debug.println("Credentials for user " + user.getUserId() + " retrieved");
  }
  
  public static String getUserFromCookie(HttpServletRequest request){
    long currentTime = tools.DateFunctions.getCurrentDateInMillis();
    String foundUserId = null;
    Debug.println("getUserFromCookie..");
    String userIdFromCookie = HTTPTools.getCookieValue(request, impactportal_userid_cookie); 
    if(userIdFromCookie == null)return null;
    String sessionUUID = userIdFromCookie;
    for (int j = 0; j < users.size(); j++) {
      Iterator<Map.Entry<String, UserSessionInfo>> iter = users.get(j).getSessionIds().entrySet().iterator();
      while (iter.hasNext()) {
        Entry<String, UserSessionInfo> entry = iter.next();
        Debug.println(""+(currentTime-entry.getValue().accessTime));
        
        if((currentTime-entry.getValue().accessTime)>impactportal_userid_cookie_durationsec*1000){
          Debug.println("Removing session, because too old: "+(currentTime-entry.getValue().accessTime));
          iter.remove();
        }else{
          Debug.println("Comparing "+sessionUUID+" with "+entry.getValue().uniqueId.toString());
          if(sessionUUID.equals(entry.getValue().uniqueId)){
            Debug.println("found UserFromCookie");
            foundUserId = users.get(j).getUserId();
          }
        }
      }
    }
    return foundUserId;
  }

  /**
   * Get the user object based on the http session, based on x509 cert, or access token
   * 
   * @param request The httpservletrequest
   * @return The user object or null when a redirect is requested.
   * @throws Exception
   */
  public static ImpactUser getUserAndRegisterCookie(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    
    if(response!=null){
      Debug.println("getUserAndRegisterCookie");
    }
    
    String id = null;
    String accessToken = null;

    /* For development purposes and no idp is available, a user can configured bluntly by setting the id in the config */
    if (Configuration.GlobalConfig.isInOfflineMode() == true) {
      id = Configuration.GlobalConfig.getDefaultUser();
    }
    
    /*Get user from access token provided in the request, usually commandline access*/
    if (id == null ) {
      try{
        try{
          JSONObject token = AccessTokenStore.checkIfTokenIsValid(request);
          accessToken= token.getString("token");
          if(token!=null){
            id = token.getString("userid");
          }
        }catch(Exception e){
        }
      }catch(Exception e){
      }
    }
    
    /*Simply get user from browser session*/
    if(id == null){
      HttpSession session = request.getSession();
      id = (String) session.getAttribute("user_identifier");
    }
    
    /*Get user from OAuth2*/
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

    
    /*Trying to get user info from X509 cert*/
    String CertOpenIdIdentifier = null;
    if (id == null) {
      Debug.println("Trying to retrieve username from cert");
      String uniqueId = null;
      
      // org.apache.catalina.authenticator.SSLAuthenticator
      X509Certificate[] certs = (X509Certificate[]) request
          .getAttribute("javax.servlet.request.X509Certificate");
      if (null != certs && certs.length > 0) {
        X509Certificate cert = certs[0];
        
        uniqueId = "x509_"+cert.getSerialNumber();
        String subjectDN = cert.getSubjectDN().toString();
        //Debug.println("getSubjectDN: " + subjectDN);
        String[] dnItems = subjectDN.split(", ");
        for (int j = 0; j < dnItems.length; j++) {
          int CNIndex = dnItems[j].indexOf("CN");
          if (CNIndex != -1) {
            CertOpenIdIdentifier = dnItems[j].substring("CN=".length()
                + CNIndex);
          }
        }
      }else{
        Debug.println("No cert provided");
      }

      
      if (CertOpenIdIdentifier != null) {
        id = CertOpenIdIdentifier;
        try{
          
          accessToken = uniqueId+"_"+CertOpenIdIdentifier;
          //Debug.println("Unique id = ["+accessToken+"]");
          request.getSession().setAttribute("openid_identifier",id);
        }catch(Exception e){
          Debug.printStackTrace(e);
        }
      }
    }
    
    /* Get user from climate4impact cookie */
    if(id == null){
      String userIdFromCookie = getUserFromCookie(request);
      if(userIdFromCookie!=null){
        id = userIdFromCookie;
        Debug.println("Retrieved user_identifier from climate4impact cookie");
        request.getSession().setAttribute("user_identifier",id);
      }
      Debug.errprintln("Get User from cooke: "+id);
    }
    
    /* Still no user found... */
    if (id == null) {
      throw new WebRequestBadStatusException(401,
          "Unauthorized user, you are not logged in.");
    }

    ImpactUser user = getUser(id);

    
    user.setAttributesFromHTTPRequestSession(request,response,accessToken);
    
    try {
      checkLogin(user);
    } catch (Exception e) {
    }
    
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
    return getUserAndRegisterCookie(request, null);
  }

  
  
  public static String makeUserIdString(String userId){
    if (userId == null)
      return null;

    userId = userId.replace("http://", "");
    userId = userId.replace("https://", "");
    userId = userId.replaceAll("/", ".");
    return userId;
  }

  
  public static ImpactUser doesUserExist(String userId) {
    if (userId == null)return null;
    userId = makeUserIdString(userId);
    for (int j = 0; j < users.size(); j++) {
      if (users.get(j).getUserId().equals(userId)) {
        ImpactUser user = users.get(j);
        return user;
      }
    }
    return null;
  }
  
  /**
   * Get user based on his/hers userId
   * 
   * @param userId
   *          The userID, equal to the OpenID identifier
   * @return The user object
   */
  public synchronized static ImpactUser getUser(String userId) {
    userId = makeUserIdString(userId);
    ImpactUser user = doesUserExist(userId);
    if(user!=null){
      return user;
    }
    // The user was not found, so create a new user
    Debug.println("Creating new user object for " + userId);
    user = new ImpactUser(userId);

    users.add(user);

    
    /* NOTE CHECKLOGIN SHOULD NOT YET BE CALLED, AS USER ATTRIBUTES FROM SESSION OR NOT YET RETRIEVED. E.G. OpenID is not yet known */
    /* We do this for google accounts, to allow x509 cert creation for CLIPC */
    if(user.getUserId().startsWith("google")){
      Debug.println("************** IS GOOGLE ***************");
      try {
        checkLogin(user);
      } catch (Exception e) {
      }
    }

    return user;
  }

  /** Get available users */
  public synchronized static Vector<ImpactUser> getUsers() {
    return users;
  }

  /**
   * Called upon successful login, handles and checks user ID, reads settings into the user object from the users file.
   * 
   * @param session
   * @throws Exception
   */
  private synchronized static void _directCheckLogin(ImpactUser user) throws Exception {

    if(debug)Debug.println("Check login " + user.getUserId());

    if(debug)Debug.println("internalName = " + user.getUserId());
    String workspace = Configuration.getImpactWorkspace();
    if(debug)Debug.println("Base workspace = " + workspace);
    user.setWorkspace(workspace + user.getUserId() + "/");
    if(debug)Debug.println("User workspace = " + user.getWorkspace());
    try {
      if(debug)Debug.println("Making dir " + user.getWorkspace());
      Tools.mkdir(user.getWorkspace());
      Tools.mkdir(user.getWorkspace() + "certs");
      user.certificateFile = user.getWorkspace() + "certs/" + "creds.pem";
    } catch (IOException e) {
      Debug.errprintln(e.getMessage());
      user.credentialError = true;
      throw new Exception(
          "Unable to create credential for user, server misconfiguration:"
              + user.getUserId() + "\n" + e.getMessage());
    }

    user.loadProperties();

    String certificate = user.getCertificate();
    String loginMethod = user.getLoginMethod();

    if (certificate == null) {
      /* Certificate is not set in user object: obtain from local proxy server */
      boolean certNeedsRefresh = true;

      try{
        _checkCertificate(user);
        if(user.certificateValidityNotAfter!=-1){
          if(debug)Debug.println("GetCredential: checking validity notafter: "+user.certificateValidityNotAfter);
          long currentMillis = tools.DateFunctions.getCurrentDateInMillis();
          long minValidityPeriodAfterMillis = 8*60*60*1000;
          //          Debug.println("currentMillis                     :["+currentMillis+"]");
          //          Debug.println("certificateValidityNotAfter       :["+user.certificateValidityNotAfter+"]");
          //          Debug.println("certificateValidityNotAfter min 8 :["+(user.certificateValidityNotAfter-(minValidityPeriodAfterMillis))+"]");
          if(debug)Debug.println("remaining H                       :["+(((user.certificateValidityNotAfter-minValidityPeriodAfterMillis)-currentMillis)/(1000*60*60))+"]");
          if(user.certificateValidityNotAfter-minValidityPeriodAfterMillis>currentMillis){
            if(debug)Debug.println("Certificate is still valid");
            certNeedsRefresh = false;
          }
        }
      }catch(Exception e){

      }

      user.setLoginInfo("Using " + loginMethod + ", credential retrieved via impactportal MyProxy.");
      if(certNeedsRefresh){
        Debug.println("Certificate not set, retrieving from MyProxy");
        try {
          _getCredential(user);

        } catch (Exception e) {
          user.credentialError = true;
          e.printStackTrace();
          user.setLoginInfo("Using " + loginMethod + ", unable to retrieve credential via impactportal MyProxy");
          throw new Exception("Unable to get credential for user " + user.getUserId() + "\n" + e.getMessage());
        }
      }
    } else {
      /* Certificate file is set in user object, probably by OAuth2 login session: write it to the right place */
      Debug.println("Certificate set, writing to " + user.certificateFile);
      FileOutputStream out = new FileOutputStream(user.certificateFile);
      out.write(certificate.getBytes());
      out.close();
      user.setLoginInfo("Using " + loginMethod + ", credential retrieved via remote SLCS.");
    }

    // Check certificate and report CN and validity period
    _checkCertificate(user);

    createNCResourceFile(user);
    user._saveProperties();
    // createFontConfigFile(user);
    user.configured = true;
  }

  private static void _checkCertificate(ImpactUser user) throws Exception {
    String SLCSX509Certificate = tools.Tools.readFile(user.certificateFile);

    X509Certificate cert = null;
    try {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(
          SLCSX509Certificate.getBytes(StandardCharsets.UTF_8)));
      Date date = cert.getNotAfter();
      user.setCertInfo("Valid till "+ date.toString() + ".\n - " + cert.getSubjectDN().toString());
      user.certificateValidityNotAfter = date.getTime();
    } catch (CertificateException e) {
      throw new Exception("Unable to validate credential for user "
          + user.getUserId() + "\n" + e.getMessage());
    }
    
    /* Check if OpenID is same as ID from certificate */
    String CertOpenIdIdentifier = null;
    String[] dnItems = cert.getSubjectDN().toString().split(", ");
    for (int j = 0; j < dnItems.length; j++) {
      int CNIndex = dnItems[j].indexOf("CN");
      if (CNIndex != -1) {
        CertOpenIdIdentifier = dnItems[j].substring("CN=".length()
            + CNIndex);
      }
    }
    
    if(user.getOpenId()!=null && CertOpenIdIdentifier!=null){
      if(!user.getOpenId().equals(CertOpenIdIdentifier)){
        Debug.errprintln("Certificate ID is different from OpenID:");
        Debug.errprintln("  OpenID:              "+user.getOpenId());
        Debug.errprintln("  CertOpenIdIdentifier:"+CertOpenIdIdentifier);
        String myproxyserverusernameoverride = Configuration.LoginConfig.getMyProxyDefaultUserName();
        if(myproxyserverusernameoverride != null){
          Debug.errprintln("--- Please NOTE myproxyserverusernameoverride is set in config.xml, but is different from login ID ---");
        }
      }
    }
    
  
    
  }

  /**
   * Create NetCDF .httprc or .dodsrc resource file and store it in the users
   * home directory
   * 
   * @param user
   *          The user object
   * @throws IOException
   */
  private synchronized static void createNCResourceFile(ImpactUser user)
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
      HttpServletRequest request)
          throws WebRequestBadStatusException, IOException {
    JSONResponse jsonResponse = new JSONResponse(request);
    ImpactUser user = null;
    try {
      user = getUser(request);
      jsonResponse.setUserId(user.getUserId());
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
          Configuration.LoginConfig.getTrustStorePassword(),0);
      // } catch (IOException e) {
      // throw e;
      // }

    } catch (javax.net.ssl.SSLPeerUnverifiedException e) {

      msg = "The peer is unverified (SSL unverified): " + e.getMessage();
      Debug.errprintln(msg);
      jsonResponse.setErrorMessage(msg, 500);
      return jsonResponse;
    } catch (SSLException e) {
      msg = "SSLException: " + e.getMessage();
      Debug.errprintln(msg);
      jsonResponse.setErrorMessage(msg, 500);
      return jsonResponse;
    }catch (UnknownHostException e) {
      msg = "The host is unknown: '" + e.getMessage() + "'\n";
      Debug.errprintln(msg);
      jsonResponse.setErrorMessage(msg, 500);
      return jsonResponse;
    } catch (ConnectTimeoutException e) {
      msg = "The connection timed out: '" + e.getMessage() + "'\n";
      Debug.errprintln(msg);
      jsonResponse.setErrorMessage(msg, 500);
      return jsonResponse;
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
          String u = user.getOpenId();
          if(u==null){
            u = user.getUserId();
          }
          msg += "\nYou are signed in as " + u + "\n";
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

  static public void logout(HttpServletRequest request,HttpServletResponse response) {
    Debug.println("--- LOGOUT --- "
        + request.getSession().getAttribute("user_identifier"));
    try {
      ImpactUser user;
      user = getUser(request);
      if (user != null) {
        user.logoutAndRemoveSessionId(request,response);
        tools.LazyCaller.getInstance().markDirty("lazyCheckLogin"+user.getUserId());
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
    request.getSession().invalidate();
    //request.getSession().setAttribute("message", null);
    Debug.println("--- LOGOUT DONE --- ");
  }

  public static void checkLogin(ImpactUser user) throws Exception {
    if(user == null)return;
    if(LazyCaller.getInstance().isCallable("lazyCheckLogin"+user.getUserId(),2000)){
      Debug.println("lazyCheckLogin_"+user.getUserId());
      _directCheckLogin(user);     
    }    
  }

}
