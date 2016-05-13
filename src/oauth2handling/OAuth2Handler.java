/*
  Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php 

  Copyright (C) 2015 by Royal Netherlands Meteorological Institute (KNMI)

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
 */
/*
 Authors: Maarten Plieger (plieger at knmi.nl) and Ernst de Vreede, KNMI
 */

package oauth2handling;

import impactservice.Configuration;
import impactservice.Configuration.Oauth2Config.Oauth2Settings;
import impactservice.LoginManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.oltu.commons.encodedtoken.TokenDecoder;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import tools.Debug;
import tools.HTTPTools;
import tools.HTTPTools.WebRequestBadStatusException;
import tools.JSONResponse;
import tools.KVPKey;

/**
 * Class which helps handling OAuth requests. Uses APACHE oltu, bouncycastle and
 * java security.
 * 
 * @author Maarten Plieger and Ernst de Vreede, KNMI
 * 
 * If you use parts of this code, please let us know :).
 *
 */
public class OAuth2Handler {

 /* Documentation: 
  *  
  *  === First of all: ===
  *    !!! Remember to add accounts.google ssl certificate to truststore !!!
  *    And add other SSL certificates from configured Oauth2 providers like CEDA 
  *  
  *  === Adding an SSL cert to the truststore can be done like: ===
  *  echo | openssl s_client -connect accounts.google.com:443 2>&1 | sed -ne  '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > accounts.google.com
  *  echo | openssl s_client -connect github.com:443 2>&1 | sed -ne '/-BEGIN
  *  CERTIFICATE-/,/-END CERTIFICATE-/p' > github.com
  *  keytool -import -v -trustcacerts -alias accounts.google.com -file
  *  accounts.google.com -keystore esg-truststore2.ts
  *  keytool -import -v -trustcacerts -alias github.com -file github.com
  *  -keystore esg-truststore2.ts 
  *  
echo | openssl s_client -connect slcs.ceda.ac.uk:443 2>&1 | sed -ne  '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > slcs.ceda.ac.uk
keytool -import -v -trustcacerts -alias slcs.ceda.ac.uk -file  slcs.ceda.ac.uk -keystore /usr/people/plieger/impactportal/esg-truststore2.ts

  *
  * 
  * === Test URLs to check which are restricted ===
  *  /impactportal/ImpactService?&source=http://vesg.ipsl.fr/thredds/dodsC/esg_dataroot/CMIP5/output1/IPSL/IPSL-CM5A-LR/1pctCO2/day/atmos/cfDay/r1i1p1/v20110427/albisccp/albisccp_cfDay_IPSL-CM5A-LR_1pctCO2_r1i1p1_19700101-19891231.nc&SERVICE=WMS&&SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&LAYERS=albisccp&WIDTH=1635&HEIGHT=955&CRS=EPSG:4326&BBOX=-105.13761467889908,-180,105.13761467889908,180&STYLES=auto/nearest&FORMAT=image/png&TRANSPARENT=TRUE&&time=1989-11-27T12:00:00Z
  *
  * === wget example to climate4impact with an OAuth2 access_token used as bearer in the headers ===
  * "http://climate4impact.eu/impactportal/ImpactService?&service=basket&request=getoverview&_dc=1424696174221&node=root"
  *  --header="Authorization: Bearer <access token>"
  *  -O info.txt --no-check-certificate
  *   
  * === wget example with a JWT ID Token to climate4impact ===
  * wget "http://climate4impact.eu/impactportal/ImpactService?&service=basket&request=getoverview&_dc=1424696174221&node=root"
  * --header="Authorization: JWT <jwt id token>"
  * 
  * === wget example with an access_token to Google OpenID connect services ===
  *  wget "https://www.googleapis.com/plus/v1/people/me/openIdConnect?"
  *  --header="Authorization: Bearer <access token>"
  *  -O info.txt --no-check-certificate
  *
  * === Useful links: === 
  * - http://self-issued.info/docs/draft-ietf-oauth-v2-bearer.html#authz-header
  *
  * - http://self-issued.info/docs/draft-jones-json-web-token-01.html#DefiningRSA
  * - https://www.googleapis.com/oauth2/v2/certs
  * - https://console.developers.google.com/project
  * 
  */
  
  static Map<String,StateObject> states = new ConcurrentHashMap<String,StateObject>();//Remembered states
  
  public static class StateObject{
    StateObject(String redirectURL){
      this.redirectURL = redirectURL;
      creationTimeMillies = tools.DateFunctions.getCurrentDateInMillis();
    }
    public String redirectURL ="";
    long creationTimeMillies;
  }
  
  private static void cleanStateObjects(){
    long currentTimeMillis = tools.DateFunctions.getCurrentDateInMillis();
    for (Map.Entry<String, StateObject> entry : states.entrySet()){
      StateObject stateObject = entry.getValue();
      if(currentTimeMillis-stateObject.creationTimeMillies>1000*60){
        Debug.println("Removing unused state with key" +entry.getKey());
        states.remove(entry.getKey());
      }
    }
  }

  static String oAuthCallbackURL = "/oauth"; // The external Servlet location

  /**
   * UserInfo object used to share multiple userinfo attributes over functions.
   * 
   * @author plieger
   *
   */
  public static class UserInfo {
    public String user_openid = null;
    public String user_identifier = null;
    public String user_email = null;
    public String certificate;
    public String access_token;
    public String certificate_notafter;
  }

  /**
   * Endpoint which should directly be called by the servlet.
   * 
   * @param request
   * @param response
   */
  public static void doGet(HttpServletRequest request,
      HttpServletResponse response) {
    
    // Check if we are dealing with getting JSON request for building up the
    // login form
    String makeform = null;
    try {
      makeform = tools.HTTPTools.getHTTPParam(request, "makeform");
    } catch (Exception e) {
    }
    if (makeform != null) {
      makeForm(request, response);
      return;
    }

    // Check if we are dealing with step1 or step2 in the OAuth process.
    String code = null;
    try {
      code = tools.HTTPTools.getHTTPParam(request, "code");
    } catch (Exception e) {
    }

    if (code == null) {
      // Step 1
      Debug.println("Step 1: start GetCode request for "
          + request.getQueryString());
      try {
        getCode(request, response);
      } catch (OAuthSystemException e1) {
        e1.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      // Step 2
      Debug.println("Step 2: start makeOauthzResponse for "
          + request.getQueryString());
      makeOauthzResponse(request, response);

    }
  };

  /**
   * Step 1: Starts Oauth2 authentication request. It retrieves a one time
   * usable code which can be used to retrieve an access token or id token
   * 
   * @param httpRequest
   * @return
   * @throws OAuthSystemException
   * @throws IOException
   */
  static void getCode(HttpServletRequest httpRequest,
      HttpServletResponse response) throws OAuthSystemException, IOException {
    LoginManager.logout(httpRequest);
  
    
    Debug.println("getQueryString:"+httpRequest.getQueryString());
    
    String c4i_redir = "";
    try {
      c4i_redir=HTTPTools.getHTTPParam(httpRequest, "c4i_redir");
    } catch (Exception e1) {
      Debug.println("Note: No redir URL given");
    }
    
    cleanStateObjects();
    
    String stateID = UUID.randomUUID().toString();
    
    states.put(stateID, new StateObject(c4i_redir));
    
    String provider = null;
    try {
      provider = tools.HTTPTools.getHTTPParam(httpRequest, "provider");
    } catch (Exception e) {
    }
    Debug.println("  OAuth2 Step 1 getCode: Provider is " + provider);

    Configuration.Oauth2Config.Oauth2Settings settings = Configuration.Oauth2Config
        .getOAuthSettings(provider);
    if (settings == null) {
      Debug.errprintln("  OAuth2 Step 1 getCode: No Oauth settings set");
      return;
    }
    Debug.println("  OAuth2 Step 1 getCode: Using " + settings.id);
    
    JSONObject state = new JSONObject();
    try {
      state.put("provider", provider);
      state.put("state_id", stateID);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }


    OAuthClientRequest oauth2ClientRequest = OAuthClientRequest
        .authorizationLocation(settings.OAuthAuthLoc)
        .setClientId(settings.OAuthClientId)
        .setRedirectURI(Configuration.getHomeURLHTTPS() + oAuthCallbackURL)
        .setScope(settings.OAuthClientScope).setResponseType("code")
        .setState(state.toString()).buildQueryMessage();

    Debug.println("  OAuth2 Step 1 getCode: locationuri = "
        + oauth2ClientRequest.getLocationUri());
    response.sendRedirect(oauth2ClientRequest.getLocationUri());
  }

  /**
   * Step 2: Get authorization response. Here the access_tokens and possibly
   * id_tokens are retrieved with the previously retrieved code.
   * 
   * @param request
   * @param response
   */
  public static void makeOauthzResponse(HttpServletRequest request,
      HttpServletResponse response) {
    try {
      OAuthAuthzResponse oar = OAuthAuthzResponse
          .oauthCodeAuthzResponse(request);

      String stateResponseAsString = oar.getState();
      if (stateResponseAsString == null) {
        stateResponseAsString = "";
      }
      if (stateResponseAsString.equals("")) {
        Debug.errprintln("  OAuth2 Step 2 OAuthz:  FAILED");
        return;
      }

      Debug
      .println("  OAuth2 Step 2 OAuthz:  State is "
          + stateResponseAsString);

      JSONObject stateResponseAsJSONObject = (JSONObject) new JSONTokener(stateResponseAsString)
          .nextValue();
      
      String stateID= stateResponseAsJSONObject.getString("state_id");
      
      Debug.println("  OAuth2 Step 2 OAuthz: stateID="+stateID);
      
      if (request.getParameter("r") != null) {
        Debug
        .println("  OAuth2 Step 2 OAuthz:  Token request already done, stopping");
        return;
      }
      
      String currentProvider = stateResponseAsJSONObject.getString("provider");
      Debug.println("  OAuth2 Step 2 OAuthz: Provider="+currentProvider);

      Debug.println("  OAuth2 Step 2 OAuthz:  Starting token request");

 
    

      Configuration.Oauth2Config.Oauth2Settings settings = Configuration.Oauth2Config
          .getOAuthSettings(currentProvider);
      Debug.println("  OAuth2 Step 2 OAuthz:  Using " + settings.id);
      OAuthClientRequest tokenRequest = OAuthClientRequest
          .tokenLocation(settings.OAuthTokenLoc)
          .setGrantType(GrantType.AUTHORIZATION_CODE)
          .setClientId(settings.OAuthClientId).setCode(oar.getCode())
          .setScope(settings.OAuthClientScope)
          .setClientSecret(settings.OAuthClientSecret)
          .setRedirectURI(Configuration.getHomeURLHTTPS() + oAuthCallbackURL)
          .buildBodyMessage();

      OAuthClient oauthclient = new OAuthClient(new URLConnectionClient());
      OAuthAccessTokenResponse oauth2Response = oauthclient
          .accessToken(tokenRequest);

      Debug.println("  OAuth2 Step 2 OAuthz:  Token request succeeded");

      Debug.println("  OAuth2 Step 2 OAuthz:  oauth2Response.getBody():"
          + oauth2Response.getBody());

      Debug.println("  OAuth2 Step 2 OAuthz:  ACCESS TOKEN:"
          + oauth2Response.getAccessToken());
      
    
      
      StateObject stateObject = states.get(stateID);
      
      if(stateObject == null){
        throw new Exception("  OAuth2 Step 2 OAuthz:  Given STATE parameter is not matching, incorrect!!!");
      }else{
        Debug.println("  OAuth2 Step 2 OAuthz:  Found state object with key "+stateID);
      }
      
      handleSpecificProviderCharacteristics(request, settings, oauth2Response);


      
      String c4i_redir = stateObject.redirectURL;
      if(c4i_redir.equals("")==false){
        Debug.println(" sendRedirect("+"/impactportal/account/login_embed.jsp?c4i_redir="+c4i_redir);
        response.sendRedirect("/impactportal/account/login_embed.jsp?c4i_redir="+c4i_redir);
      }else{
        response.sendRedirect("/impactportal/account/login.jsp");
      }

    } catch (Exception e) {
      request.getSession().setAttribute("message", "Error in OAuth2 service:\n"+e.getMessage());
      try {
        impactservice.MessagePrinters.emailFatalErrorException("Error in OAuth2 service", e);
        response.sendRedirect("/impactportal/exception.jsp");
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      e.printStackTrace();
    }
  };

  /**
   * All providers are handled a bit different. One of them is CEDA, which
   * offers a certificate issuing service for ESGF.
   * 
   * @param request
   * @param settings
   * @param oauth2Response
   * @throws Exception 
   */
  private static void handleSpecificProviderCharacteristics(
      HttpServletRequest request, Oauth2Settings settings,
      OAuthAccessTokenResponse oauth2Response) throws Exception {
    if (settings.id.equals("ceda")) {

      UserInfo userInfo = makeSLCSCertificateRequest(settings.id,
          oauth2Response.getAccessToken());
      setSessionInfo(request, userInfo);
    }

    if (settings.id.equals("google")) {
      try {
        /* Google */
        String id_token = oauth2Response.getParam("id_token");

        if (id_token == null) {
          Debug.errprintln("ID TOKEN == NULL!");
        }
        if (id_token != null) {
          if (id_token.indexOf(".") != -1) {
            UserInfo userInfo = getIdentifierFromJWTPayload(TokenDecoder
                .base64Decode(id_token.split("\\.")[1]));
            
//            userInfo.user_openid =userInfo.user_identifier;
//            userInfo.user_openid =  userInfo.user_openid.replaceAll("\\/", "_");
//            userInfo.user_openid =  userInfo.user_openid.replaceAll("\\.", "_");
//            userInfo.user_openid = "https://climate4impact.eu/"+userInfo.user_openid ;
//            Debug.println("Setting openid to ["+ userInfo.user_openid+"]");
            if(userInfo == null){
              impactservice.MessagePrinters.emailFatalErrorMessage("Error in OAuth2 service", "userInfo == null, getIdentifierFromJWTPayload failed. Check logs!!!");
              return ;
            }
            setSessionInfo(request, userInfo);

            String accessToken = oauth2Response.getAccessToken();
            Debug.println("ACCESS TOKEN:" + accessToken);
            Debug.println("EXPIRESIN:" + oauth2Response.getExpiresIn());

          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  };

  /**
   * Step 3 - Make SLCS certificate request to external OAuth2 service A -
   * generate keypair B - generate certificate signing request (CSR) C - Request
   * certificate from CEDA service using CSR and access_token D - Retrieve user
   * identifier from retrieved SLCS
   * 
   * @param currentProvider
   * @param accessToken
   * @return
   * @throws Exception 
   */
  private static UserInfo makeSLCSCertificateRequest(String currentProvider,
      String accessToken) throws Exception {
    Debug
    .println("Step 3 - Make SLCS certificate request to external OAuth2 service");
    UserInfo userInfo = new UserInfo();
    userInfo.user_identifier = null;//retrieved from slc x509 CN
    Security.addProvider(new BouncyCastleProvider());

    PublicKey publicKey = null;
    PrivateKey privateKey = null;
    KeyPairGenerator keyGen = null;

    // Generate KeyPair
    Debug.println("  Step 3.1 - Generate KeyPair");
    try {
      keyGen = KeyPairGenerator.getInstance("RSA");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return null;
    }
    keyGen.initialize(2048, new SecureRandom());
    KeyPair keypair = keyGen.generateKeyPair();
    publicKey = keypair.getPublic();
    privateKey = keypair.getPrivate();

    // Generate Certificate Signing Request
    Debug.println("  Step 3.2 - Generate CSR");
    String CSRinPEMFormat = null;
    try {

      PKCS10CertificationRequest a = new PKCS10CertificationRequest(
          "SHA256withRSA", new X509Name("CN=Requested Test Certificate"),
          publicKey, null, privateKey);
      StringWriter str = new StringWriter();
      PEMWriter pemWriter = new PEMWriter(str);
      pemWriter.writeObject(a);
      pemWriter.close();
      str.close();

      CSRinPEMFormat = str.toString();
      Debug.println("  CSR Seems OK");
    } catch (InvalidKeyException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NoSuchProviderException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (SignatureException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    Debug.println("  Step 3.3 - Use SLCS service with CSR and OAuth2 access_token");

    KVPKey key = new KVPKey();
    key.addKVP("Authorization", "Bearer " + accessToken);
    Debug.println("Starting request");

    String postData = null;
    try {
      postData = "certificate_request=" + URLEncoder.encode(CSRinPEMFormat, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

//    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//    
//    trustManagerFactory.init((KeyStore)null);
//     
//    System.out.println("JVM Default Trust Managers:");
//    for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
//        System.out.println(trustManager);
//        
//        if (trustManager instanceof X509TrustManager) {
//            X509TrustManager x509TrustManager = (X509TrustManager)trustManager;
//            
//            System.out.println("\tAccepted issuers count : " + x509TrustManager.getAcceptedIssuers().length);
//            for(int j=0;j< x509TrustManager.getAcceptedIssuers().length;j++){
//              X509Certificate issuer = x509TrustManager.getAcceptedIssuers()[j];
//              Debug.println("getIssuerDN"+issuer.getIssuerDN().getName().toString());
//            }
//        }
//    }
    
    //TODO hard coded slcs service
    String SLCSX509Certificate = null;
    try{
      SLCSX509Certificate = HTTPTools.makeHTTPostRequestWithHeaders(
          "https://slcs.ceda.ac.uk/oauth/certificate/", key, postData);
    }catch(SSLPeerUnverifiedException e){
      Debug.printStackTrace(e);
      throw new Exception("SSLPeerUnverifiedException: Unable to retrieve SLC from SLCS for https://slcs.ceda.ac.uk/");
    }

    if (SLCSX509Certificate != null) {
      Debug.println("Succesfully retrieved an SLCS\n");
    }else{
      throw new Exception("Unable to retrieve SLC from SLCS");
    }

    String privateKeyInPemFormat = null;
    try {
      StringWriter str = new StringWriter();
      PEMWriter pemWriter = new PEMWriter(str);
      pemWriter.writeObject(privateKey);
      pemWriter.close();
      str.close();
      privateKeyInPemFormat = str.toString();
    } catch (Exception e) {

    }

    Debug.println("Finished request");

    String CertOpenIdIdentifier = null;

    X509Certificate cert = null;
    try {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(
          SLCSX509Certificate.getBytes(StandardCharsets.UTF_8)));
    } catch (CertificateException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    String subjectDN = cert.getSubjectDN().toString();
    Debug.println("getSubjectDN: " + subjectDN);
    String[] dnItems = subjectDN.split(", ");
    for (int j = 0; j < dnItems.length; j++) {
      int CNIndex = dnItems[j].indexOf("CN");
      if (CNIndex != -1) {
        CertOpenIdIdentifier = dnItems[j].substring("CN=".length() + CNIndex);
      }
    }
    userInfo.user_identifier = CertOpenIdIdentifier;
    userInfo.user_openid = CertOpenIdIdentifier;

    userInfo.certificate = SLCSX509Certificate + privateKeyInPemFormat;
    userInfo.access_token = accessToken;

    return userInfo;
  };

  /**
   * Sets session parameters for the impactportal
   * 
   * @param request
   * @param userInfo
   */
  public static void setSessionInfo(HttpServletRequest request,
      UserInfo userInfo) {
    request.getSession()
    .setAttribute("openid_identifier", userInfo.user_openid);
    request.getSession().setAttribute("user_identifier",
        userInfo.user_identifier);
    request.getSession().setAttribute("emailaddress", userInfo.user_email);
    request.getSession().setAttribute("certificate", userInfo.certificate);
    request.getSession().setAttribute("access_token", userInfo.access_token);
    request.getSession().setAttribute("login_method", "oauth2");
  };

  /**
   * Verifies a signed JWT Id_token with RSA SHA-256
   * 
   * @param id_token
   * @return true if verified
   * @throws JSONException
   * @throws WebRequestBadStatusException
   * @throws IOException
   * @throws SignatureException
   * @throws InvalidKeyException
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   */
  @SuppressWarnings("unused")
  private static boolean verify_JWT_IdToken(String id_token)
      throws JSONException, WebRequestBadStatusException, IOException,
      SignatureException, InvalidKeyException, NoSuchAlgorithmException,
      InvalidKeySpecException {
    // http://self-issued.info/docs/draft-jones-json-web-token-01.html#DefiningRSA
    // The JWT Signing Input is always the concatenation of a JWT Header
    // Segment, a period ('.') character, and the JWT Payload Segment
    // RSASSA-PKCS1-V1_5-VERIFY

    // 8.2. Signing a JWT with RSA SHA-256
    //
    // This section defines the use of the RSASSA-PKCS1-v1_5 signature algorithm
    // as defined in RFC 3447 [RFC3447], Section 8.2 (commonly known as PKCS#1),
    // using SHA-256 as the hash function. Note that the use of the
    // RSASSA-PKCS1-v1_5 algorithm is described in FIPS 186-3 [FIPS.186‑3],
    // Section 5.5, as is the SHA-256 cryptographic hash function, which is
    // defined in FIPS 180-3 [FIPS.180‑3]. The reserved "alg" header parameter
    // value "RS256" is used in the JWT Header Segment to indicate that the JWT
    // Crypto Segment contains an RSA SHA-256 signature.
    //
    // A 2048-bit or longer key length MUST be used with this algorithm.
    //
    // The RSA SHA-256 signature is generated as follows:
    //
    // Let K be the signer's RSA private key and let M be the UTF-8
    // representation of the JWT Signing Input.
    // Compute the octet string S = RSASSA-PKCS1-V1_5-SIGN (K, M) using SHA-256
    // as the hash function.
    // Base64url encode the octet string S, as defined in this document.
    //
    // The output is placed in the JWT Crypto Segment for that JWT.
    //
    // The RSA SHA-256 signature on a JWT is validated as follows:
    //
    // Take the JWT Crypto Segment and base64url decode it into an octet string
    // S. If decoding fails, then the token MUST be rejected.
    // Let M be the UTF-8 representation of the JWT Signing Input and let (n, e)
    // be the public key corresponding to the private key used by the signer.
    // Validate the signature with RSASSA-PKCS1-V1_5-VERIFY ((n, e), M, S) using
    // SHA-256 as the hash function.
    // If the validation fails, the token MUST be rejected.
    //
    // Signing with the RSA SHA-384 and RSA SHA-512 algorithms is performed
    // identically to the procedure for RSA SHA-256 - just with correspondingly
    // longer key and result values.

    Debug.println("Starting verification of id_token");
    Debug.println("[" + id_token + "]");
    String JWTHeader = TokenDecoder.base64Decode(id_token.split("\\.")[0]);
    String JWTPayload = TokenDecoder.base64Decode(id_token.split("\\.")[1]);
    String JWTSigningInput = id_token.split("\\.")[0] + "."
        + id_token.split("\\.")[1];
    String JWTSignature = id_token.split("\\.")[2];

    Debug.println("Decoded JWT IDToken Header:" + JWTHeader);
    Debug.println("Decoded JWT IDToken Payload:" + JWTPayload);

    // Find the discovery page
    JSONObject JWTPayLoadObject = (JSONObject) new JSONTokener(JWTPayload)
    .nextValue();
    String iss = JWTPayLoadObject.getString("iss");
    Debug.println("iss=" + iss);

    // Load the OpenId discovery page
    String discoveryURL = "https://" + iss
        + "/.well-known/openid-configuration";
    JSONObject openid_configuration = (JSONObject) new JSONTokener(
        HTTPTools.makeHTTPGetRequest(discoveryURL)).nextValue();
    String jwks_uri = openid_configuration.getString("jwks_uri");
    Debug.println("jwks_uri:" + jwks_uri);

    // Load the jwks uri
    JSONObject certs = (JSONObject) new JSONTokener(
        HTTPTools.makeHTTPGetRequest(jwks_uri)).nextValue();
    JSONArray jwks_keys = certs.getJSONArray("keys");
    Debug.println("jwks_keys:" + jwks_keys.length());

    JSONObject JWTHeaderObject = (JSONObject) new JSONTokener(JWTHeader)
    .nextValue();
    String kid = JWTHeaderObject.getString("kid");
    Debug.println("kid=" + kid);

    String modulus = null;
    String exponent = null;

    for (int j = 0; j < jwks_keys.length(); j++) {
      if (jwks_keys.getJSONObject(j).getString("kid").equals(kid)) {
        Debug.println("Found kid in jwks");
        modulus = jwks_keys.getJSONObject(j).getString("n");
        exponent = jwks_keys.getJSONObject(j).getString("e");
        break;
      }
    }
    return RSASSA_PKCS1_V1_5_VERIFY(modulus, exponent, JWTSigningInput,
        JWTSignature);
  };

  /**
   * RSASSA-PKCS1-V1_5-VERIFY ((n, e), M, S) using SHA-256
   * 
   * @param modulus_n
   * @param exponent_e
   * @param signinInput_M
   * @param signature_S
   * @return
   * @throws SignatureException
   * @throws InvalidKeyException
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   */
  static boolean RSASSA_PKCS1_V1_5_VERIFY(String modulus_n, String exponent_e,
      String signinInput_M, String signature_S) throws SignatureException,
      InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
    Debug.println("Starting verification");
    /* RSA SHA-256 RSASSA-PKCS1-V1_5-VERIFY */
    // Modulus (n from https://www.googleapis.com/oauth2/v2/certs)
    String n = modulus_n;
    // Exponent (e from https://www.googleapis.com/oauth2/v2/certs)
    String e = exponent_e;
    // The JWT Signing Input (JWT Header and JWT Payload concatenated with ".")
    byte[] M = signinInput_M.getBytes();
    // Signature (JWT Crypto)
    byte[] S = Base64.decodeBase64(signature_S);

    byte[] modulusBytes = Base64.decodeBase64(n);
    byte[] exponentBytes = Base64.decodeBase64(e);
    BigInteger modulusInteger = new BigInteger(1, modulusBytes);
    BigInteger exponentInteger = new BigInteger(1, exponentBytes);

    RSAPublicKeySpec rsaPubKey = new RSAPublicKeySpec(modulusInteger,
        exponentInteger);
    KeyFactory fact = KeyFactory.getInstance("RSA");
    PublicKey pubKey = fact.generatePublic(rsaPubKey);
    Signature signature = Signature.getInstance("SHA256withRSA");
    signature.initVerify(pubKey);
    signature.update(M);
    boolean isVerified = signature.verify(S);
    Debug.println("Verify result [" + isVerified + "]");
    return isVerified;
  }


  /**
   * Returns unique user identifier from id_token (JWTPayload). The JWT token is
   * *NOT* verified. Several impact portal session attributes are set: -
   * user_identifier - emailaddress
   * 
   * @param request
   * @param JWT
   * @return
   */
  private static UserInfo getIdentifierFromJWTPayload(String JWT) {
    JSONObject id_token_json = null;
    try {
      id_token_json = (JSONObject) new JSONTokener(JWT).nextValue();
    } catch (JSONException e1) {
      Debug.errprintln("Unable to convert JWT Token to JSON");
      return null;
    }

    String email = "null";
    String userSubject = null;
    String aud = "";
    try {
      email = id_token_json.get("email").toString();
    } catch (JSONException e) {
    }
    try {
      userSubject = id_token_json.get("sub").toString();
    } catch (JSONException e) {
    }

    try {
      aud = id_token_json.get("aud").toString();
    } catch (JSONException e) {
    }

    if (aud == null){
      Debug.errprintln("Error: aud == null");
      return null;
    }
    if (userSubject == null){
      Debug.errprintln("Error: userSubject == null");
      return null;
    }
    
    //Get ID based on aud (client id)
    String clientId = null;
    
    Vector<String> providernames = Configuration.Oauth2Config.getProviders();

    for (int j = 0; j < providernames.size(); j++) {
      Configuration.Oauth2Config.Oauth2Settings settings = Configuration.Oauth2Config
          .getOAuthSettings(providernames.get(j));
      if(settings.OAuthClientId.equals(aud)){
        clientId = settings.id;
      }
    }
    
    if(clientId == null){
      Debug.errprintln("Error: could not match OAuthClientId to aud");
      return null;
      
    }
    
    String user_identifier = clientId + "/" + userSubject;
    String user_openid = null;
    UserInfo userInfo = new UserInfo();
    userInfo.user_identifier = user_identifier;
    userInfo.user_openid = user_openid;
    userInfo.user_email = email;

    Debug.println("getIdentifierFromJWTPayload (id_token): Found unique ID "
        + user_identifier);

    return userInfo;

  }

  /**
   * Makes a JSON object and sends it to response with information needed for
   * building the OAuth2 login form.
   * 
   * @param request
   * @param response
   */
  private static void makeForm(HttpServletRequest request,
      HttpServletResponse response) {
    JSONResponse jsonResponse = new JSONResponse(request);

    JSONObject form = new JSONObject();
    try {

      JSONArray providers = new JSONArray();
      form.put("providers", providers);
      Vector<String> providernames = Configuration.Oauth2Config.getProviders();

      for (int j = 0; j < providernames.size(); j++) {
        Configuration.Oauth2Config.Oauth2Settings settings = Configuration.Oauth2Config
            .getOAuthSettings(providernames.get(j));
        JSONObject provider = new JSONObject();
        provider.put("id", providernames.get(j));
        provider.put("description", settings.description);
        provider.put("logo", settings.logo);
        provider.put("registerlink", settings.registerlink);
        providers.put(provider);

      }
    } catch (JSONException e) {
    }
    jsonResponse.setMessage(form);

    try {
      jsonResponse.print(response);
    } catch (Exception e1) {

    }

  };

   /* Check if an access token was provided in the HttpServletRequest object
     and return a user identifier on success.
     *
     * It returns the unique user identifier. It does this by calling the
     userinfo_endpoint using the access_token.
     * All endpoints are discovered by reading the open-id Discovery service.
     * This is one of the OpenId-Connect extensions on OAuth2
     *
     * @param request
     * @return
     * @throws JSONException
     * @throws WebRequestBadStatusException
     * @throws IOException
     */
   public static UserInfo verifyAndReturnUserIdentifier(HttpServletRequest request)
     throws JSONException, WebRequestBadStatusException, IOException {
    
     //1) Find the Authorization header containing the access_token
     String access_token = request.getHeader("Authorization");
     if (access_token == null){
     //No access token, probably not an OAuth2 request, skip.
     return null;
     }
     Debug.println("Authorization    : " + access_token);
    
     //2) Find the Discovery service, it might have been passed in the request  headers:
     String discoveryURL = request.getHeader("Discovery");
     if (discoveryURL == null) {
     discoveryURL =
     "https://accounts.google.com/.well-known/openid-configuration";
     }
     Debug.println("Discovery        : " + discoveryURL);
    
     //3 Retrieve the Discovery service, so we get all service endpoints
     String discoveryData = HTTPTools.makeHTTPGetRequest(discoveryURL);
     JSONObject jsonObject = (JSONObject) new JSONTokener(discoveryData)
     .nextValue();
    
     //4) Retrieve userinfo endpoint
     String userInfoEndpoint = jsonObject.getString("userinfo_endpoint");
     Debug.println("userInfoEndpoint:" + userInfoEndpoint);
    
     //5) Make a get request with Authorization headers set, the access_token is used here as Bearer.
     KVPKey key = new KVPKey();
     key.addKVP("Authorization", access_token);
     Debug.println("Starting request");
     String id_token = HTTPTools.makeHTTPGetRequestWithHeaders(userInfoEndpoint,
     key);// ,"Authorization: Bearer "+access_token);
     Debug.println("Finished request");
    
     //6) The ID token is retrieved, now return the identifier from this token.
     Debug.println("Retrieved id_token=" + id_token);
     return getIdentifierFromJWTPayload(id_token);
   };

}
