package oauth2handling;

import impactservice.Configuration;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import tools.Debug;
import tools.HTTPTools;
import tools.HTTPTools.WebRequestBadStatusException;
import tools.KVPKey;


public class OAuth2Handler {
  /* Add accounts.google ssl certificate to truststore */
  //echo | openssl s_client -connect accounts.google.com:443 2>&1 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p'  > accounts.google.com
  //echo | openssl s_client -connect github.com:443 2>&1 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p'  > github.com
  //keytool -import -v -trustcacerts -alias accounts.google.com -file accounts.google.com -keystore esg-truststore2.ts 
  //keytool -import -v -trustcacerts -alias github.com -file github.com -keystore esg-truststore2.ts
  
  /* Restricted URL to check */
  // /impactportal/ImpactService?&source=http://vesg.ipsl.fr/thredds/dodsC/esg_dataroot/CMIP5/output1/IPSL/IPSL-CM5A-LR/1pctCO2/day/atmos/cfDay/r1i1p1/v20110427/albisccp/albisccp_cfDay_IPSL-CM5A-LR_1pctCO2_r1i1p1_19700101-19891231.nc&SERVICE=WMS&&SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&LAYERS=albisccp&WIDTH=1635&HEIGHT=955&CRS=EPSG:4326&BBOX=-105.13761467889908,-180,105.13761467889908,180&STYLES=auto/nearest&FORMAT=image/png&TRANSPARENT=TRUE&&time=1989-11-27T12:00:00Z
  
  //E.g. wget "http://bhw485.knmi.nl:8280/impactportal/ImpactService?&service=basket&request=getoverview&_dc=1424696174221&node=root" --header="Authorization: Bearer ya29.KwFhgkkEdgQBKh_5eRo_ODoN3h8uvdscC3gbhjcCB46wAWZpSsQg2CjFw8vm5LlygtqYRKQ6esLvuw" -O info.txt --no-check-certificate
  
  //wget "http://bhw485.knmi.nl:8280/impactportal/ImpactService?&service=basket&request=getoverview&_dc=1424696174221&node=root" --header="Authorization: JWT eyJhbGciOiJSUzI1NiIsImtpZCI6IjlhODEzMzhlMmFmOGVlZjA0ODE5OTA2MzgwZDBkOTZmNjBmNzI4ZjYifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwic3ViIjoiMTA4NjY0NzQxMjU3NTMxMzI3MjU1IiwiYXpwIjoiMjMxOTMzNTM3NDU2LW1zcjlwOG9zb3VpdTQwMGludmMwbWY4NTdhMTd2NGNxLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiZW1haWwiOiJtYWFydGVucGxpZWdlckBnbWFpbC5jb20iLCJhdF9oYXNoIjoiaV9nVzEzc3VpNTRWLWJiTzhHRTlFdyIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJhdWQiOiIyMzE5MzM1Mzc0NTYtbXNyOXA4b3NvdWl1NDAwaW52YzBtZjg1N2ExN3Y0Y3EuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJpYXQiOjE0MjUzMTI3OTEsImV4cCI6MTQyNTMxNjY5MX0.cTmkv5ym0ef6KtLIYQ5DqlD3TSzpbURtrm7qmQAbKcdBtMKxhtuqeXOTd3_pNqRoaoo0vQ5yUv6TLBBlLQTe0MMj0kd5wZqfnjHzeGO0lCu2B8BijDdhYFto1pqzJqWhtluvXuBm0Ws4zZJs5NpkBnXNWMWOW1M04F6hoAfyfao"
  //wget "https://www.googleapis.com/plus/v1/people/me/openIdConnect?" --header="Authorization: Bearer ya29.IwF2DNugbZI1KFo0EUyRMe2o_tfpgAoytDZkT4F0d98azSuGV3N9sTt4JN9zUnLXg3SQykxCz5BOzQ" -O info.txt --no-check-certificate
  //wget "http://bhw485.knmi.nl:8280/impactportal/ImpactService?&service=basket&request=getoverview&_dc=1424696174221&node=root" --header="Authorization: Bearer ya29.IwF2DNugbZI1KFo0EUyRMe2o_tfpgAoytDZkT4F0d98azSuGV3N9sTt4JN9zUnLXg3SQykxCz5BOzQ" --header="Discovery: https://accounts.google.com/.well-known/openid-configuration" -O info.txt --no-check-certificate
  
  //http://self-issued.info/docs/draft-ietf-oauth-v2-bearer.html#authz-header
  
  // http://self-issued.info/docs/draft-jones-json-web-token-01.html#DefiningRSA
  // https://www.googleapis.com/oauth2/v2/certs
  // https://console.developers.google.com/project
  
  static String oAuthCallbackURL = "/oauth";

  /**
   * Starts Oauth2 authentication request
   * @param httpRequest
   * @return
   * @throws OAuthSystemException 
   * @throws IOException 
   */
  static void getCode(HttpServletRequest httpRequest,HttpServletResponse response) throws OAuthSystemException, IOException{
    Configuration.Oauth2Config.Oauth2Settings settings = Configuration.Oauth2Config.getOAuthSettings("google");
    if(settings == null){
      Debug.errprintln("No Oauth settings set");
    }
    
    Debug.println(settings.toString());
    
    OAuthClientRequest oauth2ClientRequest = OAuthClientRequest
        .authorizationLocation(settings.OAuthAuthLoc)
        .setClientId(settings.OAuthClientId)
        .setRedirectURI(Configuration.getHomeURLHTTPS()+oAuthCallbackURL)
        .setScope(settings.OAuthClientScope)
        .setResponseType("code")
        .setState("oK")
        .buildQueryMessage();
    response.sendRedirect(oauth2ClientRequest.getLocationUri());
  }

  /**
   * Get authorization response
   * @param request
   * @param response
   */
  public static void makeOauthzResponse(HttpServletRequest request,HttpServletResponse response) {
    try {
      OAuthAuthzResponse oar = OAuthAuthzResponse.oauthCodeAuthzResponse(request);
      
      String stateResponse = oar.getState();
      if(stateResponse == null){stateResponse = "";}
      if(stateResponse.equals("")){
        Debug.errprintln("FAILED");
        return;
      }

      if(request.getParameter("r")!=null){
        Debug.println("Token request already done, stopping");
        return;
      }
      
      Debug.println("Starting token request");
      Configuration.Oauth2Config.Oauth2Settings settings = Configuration.Oauth2Config.getOAuthSettings("google");
      OAuthClientRequest tokenRequest = OAuthClientRequest
          .tokenLocation(settings.OAuthTokenLoc)
          .setGrantType(GrantType.AUTHORIZATION_CODE)
          .setClientId(settings.OAuthClientId)
          .setCode(oar.getCode())
          .setScope(settings.OAuthClientScope)
          .setClientSecret(settings.OAuthClientSecret)
          .setRedirectURI(Configuration.getHomeURLHTTPS()+oAuthCallbackURL)
          .buildBodyMessage();
      

      OAuthClient oauthclient = new OAuthClient(new URLConnectionClient());
      OAuthAccessTokenResponse oauth2Response = oauthclient.accessToken(tokenRequest);
      try{

        /*Google*/
        String id_token = oauth2Response.getParam("id_token");
       
        
        /*Microsoft*/
        //String token = oauth2Response.getParam("authentication_token");
        //String token = oauth2Response.getParam("user_id");
        
        if(id_token == null){
          Debug.errprintln("TOKEN == NULL!");
        }
        if(id_token != null){
          if(id_token.indexOf(".")!=-1){
            getIdentifierFromJWTPayload(request,TokenDecoder.base64Decode(id_token.split("\\.")[1]));

            String accessToken = oauth2Response.getAccessToken();
            Debug.println("ACCESS TOKEN:"+accessToken);
            request.getSession().setAttribute("access_token",accessToken);
            Debug.println("EXPIRESIN:"+oauth2Response.getExpiresIn());
    
          }
        }
      }catch(Exception e){
        e.printStackTrace();
      }
      response.sendRedirect("/impactportal/account/login.jsp");
    
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } 
    
  }    

  
  /**
   * Verifies a signed JWT Id_token with RSA SHA-256
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
  private static boolean verify_JWT_IdToken(String id_token) throws JSONException, WebRequestBadStatusException, IOException, SignatureException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
    //  http://self-issued.info/docs/draft-jones-json-web-token-01.html#DefiningRSA
    //  The JWT Signing Input is always the concatenation of a JWT Header Segment, a period ('.') character, and the JWT Payload Segment
    //  RSASSA-PKCS1-V1_5-VERIFY
      
    //  8.2.  Signing a JWT with RSA SHA-256
    //
    //  This section defines the use of the RSASSA-PKCS1-v1_5 signature algorithm as defined in RFC 3447 [RFC3447], Section 8.2 (commonly known as PKCS#1), using SHA-256 as the hash function. Note that the use of the RSASSA-PKCS1-v1_5 algorithm is described in FIPS 186-3 [FIPS.186‑3], Section 5.5, as is the SHA-256 cryptographic hash function, which is defined in FIPS 180-3 [FIPS.180‑3]. The reserved "alg" header parameter value "RS256" is used in the JWT Header Segment to indicate that the JWT Crypto Segment contains an RSA SHA-256 signature.
    //
    //  A 2048-bit or longer key length MUST be used with this algorithm.
    //
    //  The RSA SHA-256 signature is generated as follows:
    //
    //      Let K be the signer's RSA private key and let M be the UTF-8 representation of the JWT Signing Input.
    //      Compute the octet string S = RSASSA-PKCS1-V1_5-SIGN (K, M) using SHA-256 as the hash function.
    //      Base64url encode the octet string S, as defined in this document.
    //
    //  The output is placed in the JWT Crypto Segment for that JWT.
    //
    //  The RSA SHA-256 signature on a JWT is validated as follows:
    //
    //      Take the JWT Crypto Segment and base64url decode it into an octet string S. If decoding fails, then the token MUST be rejected.
    //      Let M be the UTF-8 representation of the JWT Signing Input and let (n, e) be the public key corresponding to the private key used by the signer.
    //      Validate the signature with RSASSA-PKCS1-V1_5-VERIFY ((n, e), M, S) using SHA-256 as the hash function.
    //      If the validation fails, the token MUST be rejected.
    //
    //  Signing with the RSA SHA-384 and RSA SHA-512 algorithms is performed identically to the procedure for RSA SHA-256 - just with correspondingly longer key and result values.

    Debug.println("Starting verification of id_token");
    Debug.println("["+id_token+"]");
    String JWTHeader = TokenDecoder.base64Decode(id_token.split("\\.")[0]);
    String JWTPayload = TokenDecoder.base64Decode(id_token.split("\\.")[1]);
    String JWTSigningInput = id_token.split("\\.")[0]+"."+id_token.split("\\.")[1];
    String JWTSignature = id_token.split("\\.")[2];
    
    Debug.println("Decoded JWT IDToken Header:"+JWTHeader);
    Debug.println("Decoded JWT IDToken Payload:"+JWTPayload);
    

    //Find the discovery page
    JSONObject JWTPayLoadObject = (JSONObject) new JSONTokener(JWTPayload).nextValue();
    String iss = JWTPayLoadObject.getString("iss");
    Debug.println("iss="+iss);
    
    //Load the OpenId discovery page
    String discoveryURL = "https://"+iss+"/.well-known/openid-configuration"; 
    JSONObject openid_configuration = (JSONObject) new JSONTokener(HTTPTools.makeHTTPGetRequest(discoveryURL)).nextValue();
    String jwks_uri = openid_configuration.getString("jwks_uri");
    Debug.println("jwks_uri:"+jwks_uri);
    
    //Load the jwks uri
    JSONObject certs = (JSONObject) new JSONTokener(HTTPTools.makeHTTPGetRequest(jwks_uri)).nextValue();
    JSONArray jwks_keys = certs.getJSONArray("keys");
    Debug.println("jwks_keys:"+jwks_keys.length());
    
    
    JSONObject JWTHeaderObject = (JSONObject) new JSONTokener(JWTHeader).nextValue();
    String kid = JWTHeaderObject.getString("kid");
    Debug.println("kid="+kid);
    
    String modulus = null;
    String exponent = null;
    
    for(int j=0;j<jwks_keys.length();j++){
      if(jwks_keys.getJSONObject(j).getString("kid").equals(kid)){
        Debug.println("Found kid in jwks");
        modulus = jwks_keys.getJSONObject(j).getString("n");
        exponent = jwks_keys.getJSONObject(j).getString("e");
        break;
      }
    }
    return RSASSA_PKCS1_V1_5_VERIFY(modulus,exponent, JWTSigningInput,JWTSignature);
  }
  
  /**
   * RSASSA-PKCS1-V1_5-VERIFY ((n, e), M, S) using SHA-256
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
  static boolean RSASSA_PKCS1_V1_5_VERIFY(String modulus_n, String exponent_e, String signinInput_M,String signature_S) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException{
    Debug.println("Starting verification");
    /* RSA SHA-256 RSASSA-PKCS1-V1_5-VERIFY */
    //Modulus (n from https://www.googleapis.com/oauth2/v2/certs)
    String n = modulus_n;
    //Exponent (e from https://www.googleapis.com/oauth2/v2/certs)
    String e = exponent_e;
    // The JWT Signing Input (JWT Header and JWT Payload concatenated with ".")
    byte[] M = signinInput_M.getBytes();
    //Signature (JWT Crypto)
    byte[] S = Base64.decodeBase64(signature_S);
    
    byte[] modulusBytes = Base64.decodeBase64(n);
    byte[] exponentBytes = Base64.decodeBase64(e);
    BigInteger modulusInteger = new BigInteger(1, modulusBytes );               
    BigInteger exponentInteger = new BigInteger(1, exponentBytes);
    
    RSAPublicKeySpec rsaPubKey = new RSAPublicKeySpec(modulusInteger,exponentInteger);
    KeyFactory fact = KeyFactory.getInstance("RSA");
    PublicKey pubKey = fact.generatePublic(rsaPubKey);
    Signature signature = Signature.getInstance("SHA256withRSA");
    signature.initVerify(pubKey);
    signature.update(M);
    boolean isVerified=signature.verify(S);
    Debug.println("Verify result ["+isVerified+"]");
    return isVerified;
  }

///**
//* Used to check if an access token was provided, if so verifies the token and returns the unique user identifier.
//* @param request
//* @return
//* @throws JSONException
//* @throws WebRequestBadStatusException
//* @throws IOException
//*/
//public static String verifyAndReturnUserIdentifier(HttpServletRequest request) throws JSONException, WebRequestBadStatusException, IOException {
//  String authorizationHeader = request.getHeader("Authorization");
//  if(authorizationHeader == null)return null;
//  Debug.println("Authorization    : ["+authorizationHeader+"]");
//  if(authorizationHeader.startsWith("JWT")==true){
//    Debug.println("OK");
//    String id_token= authorizationHeader.substring(4);
//    Debug.println("ID_TOKEN: "+id_token);
//    try {
//      if(verify_JWT_IdToken(id_token)){
//        return getIdentifierFromJWT(request,TokenDecoder.base64Decode(id_token.split("\\.")[1]));
//      }
//    } catch (InvalidKeyException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (SignatureException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (NoSuchAlgorithmException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (InvalidKeySpecException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
//  }
//  
//  return null;
//}


/**
 * Used to check if an access token was provided, if so verifies the token and returns the unique user identifier.
 * @param request
 * @return
 * @throws JSONException
 * @throws WebRequestBadStatusException
 * @throws IOException
 */
public static String verifyAndReturnUserIdentifier(HttpServletRequest request) throws JSONException, WebRequestBadStatusException, IOException {
  //Try to get user info from OpenID Connect

  String discoveryURL = request.getHeader("Discovery");
  if(discoveryURL == null){
    discoveryURL = "https://accounts.google.com/.well-known/openid-configuration"; 
  }
  String access_token = request.getHeader("Authorization");
  if(access_token == null)return null;
  Debug.println("Authorization    : "+access_token);
  Debug.println("Discovery        : "+discoveryURL);
  
  String discoveryData= HTTPTools.makeHTTPGetRequest(discoveryURL);
  JSONObject jsonObject = (JSONObject) new JSONTokener(discoveryData).nextValue();
  String userInfoEndpoint = jsonObject.getString("userinfo_endpoint");
  Debug.println("userInfoEndpoint:"+userInfoEndpoint);
  
  KVPKey key = new KVPKey();
  key.addKVP("Authorization", access_token);
  Debug.println("Starting request");
  String id_token = HTTPTools.makeHTTPGetRequest(userInfoEndpoint,key);//,"Authorization: Bearer "+access_token);
  Debug.println("Finished request");
  
  Debug.println("Retrieved id_token="+id_token);
  return getIdentifierFromJWTPayload(request,id_token);
}

  private static String getIdentifierFromJWTPayload(HttpServletRequest request, String JWT){
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
    
    if(aud == null)return null;
    if(userSubject == null)return null;

    String user_identifier = aud+"/"+userSubject;
    String user_openid = null;
    request.getSession().setAttribute("openid_identifier",user_openid);
    request.getSession().setAttribute("user_identifier",user_identifier);
    
    request.getSession().setAttribute("emailaddress",email);
    
    Debug.println("Found unique ID"+user_identifier);
    
    
    return user_identifier;
    
  }
  
}
