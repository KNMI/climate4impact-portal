package tokenapi;

import impactservice.AccessTokenStore;
import impactservice.ImpactUser;
import impactservice.LoginManager;
import impactservice.MessagePrinters;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import tools.Debug;
import tools.HTTPTools;
import tools.JSONResponse;

/**
 * Servlet implementation class CLIPCTokenAPI
 */
public class CLIPCTokenAPI extends HttpServlet {
  private static final long serialVersionUID = 1L;
  /*
   * Required steps for configuration of functioning clipc token service
   * Step 1: Generate a public/private key
   * openssl genrsa -des3 -out clipc.key 4096

   * 
   */

  /**
   * @see HttpServlet#HttpServlet()
   */
  public CLIPCTokenAPI() {
    super();
    // TODO Auto-generated constructor stub
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   * https://localhost/impactportal/clipctokenapi?service=account&request=generatetoken&client_id=ceda.ac.uk%2Fopenid%2FMaarten.Plieger"
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // TODO Auto-generated method stub
    JSONResponse jsonResponse = new JSONResponse(request);

    String expectedIssuerDN   = "CN=knmi_clipc_ca_tokenapi, OU=RDWDT, O=KNMICLIPCCA";
    String expectedSubjectDN  = "CN=clipctokenapiformaris_20160303, OU=MARIS, O=MARIS";
    String issuerDN=null;
    String subjectDN=null;
    // org.apache.catalina.authenticator.SSLAuthenticator
    X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
    if (null != certs && certs.length > 0) {
      X509Certificate cert = certs[0];
      issuerDN = cert.getIssuerDN().toString();
      subjectDN = cert.getSubjectDN().toString();
    }
    String errorMessage=null;
    boolean authorizationOK=false;
    boolean authenticationOK = false;
    if(issuerDN!=null && subjectDN!=null){
      Debug.println("CLIPCTokenAPI request received");
//      Debug.println("issuerDN : ["+issuerDN+"]");
//      Debug.println("subjectDN: ["+subjectDN+"]");
      authenticationOK = true;
      if(issuerDN.equals(expectedIssuerDN)){
        if(subjectDN.equals(expectedSubjectDN)){
          authorizationOK = true;
          try {
            String serviceStr=HTTPTools.getHTTPParam(request, "service");
            if(serviceStr.equals("account")){
              try {
                String requestStr=HTTPTools.getHTTPParam(request, "request");
                if(requestStr.equals("generatetoken")){
                  try {
                    String userID=HTTPTools.getHTTPParam(request, "client_id");
                    Debug.println("Found generatetoken request for user "+userID);
                    int status = getAccessToken(userID,request,jsonResponse);
                    if(status != 0){
                      errorMessage="Unable to find user id";
                    }
                  }catch (Exception e) {
                    errorMessage="parameter userID is missing or wrong";
                  }
                }else{
                  errorMessage="parameter request is or wrong";
                }
                  
              }catch (Exception e) {
                errorMessage="parameter request is missing or wrong";
              }
            }else{
              errorMessage="parameter service is wrong";
            }
          } catch (Exception e) {
            errorMessage="parameter service is missing or wrong";
          }
        }else{
          errorMessage="subjectDN Mismatch!";
        }
      }else{
        errorMessage="issuerDN Mismatch!";
      }
    }else{
      errorMessage="No client authentication certificate found.";
    }
    
    

   

    if(errorMessage!=null){
      jsonResponse.setErrorMessage(errorMessage, 400);
    }
    if(authenticationOK==false&&authorizationOK==false){
      jsonResponse.setErrorMessage(errorMessage, 401, null, null, null);
      MessagePrinters.emailFatalErrorMessage("clipctokenapi 401", errorMessage);
    }
    if(authenticationOK==true&&authorizationOK==false){
      jsonResponse.setErrorMessage(errorMessage, 403, null, null, null);  
      MessagePrinters.emailFatalErrorMessage("clipctokenapi 403", errorMessage);
    }
    
    


    try {
      jsonResponse.print(response);
    } catch (Exception e1) {

    }
  }

  /**
   * Obtains an access token from the tokenstore. A token will be re-used if the token is at least 8 hours valid. Otherwise a new token is returned.
   * @param userId
   * @param request
   * @param jsonResponse
   * @return
   */
  private int getAccessToken(String userId,HttpServletRequest request,JSONResponse jsonResponse) {
    ImpactUser user = null;
    user = LoginManager.getUser(userId, request);
    if(user == null){
      Debug.errprintln("Unable to find user in store ["+userId+"]");
      return -1; 
    }
    Debug.println("Found user "+user.getUserId());
    try {
      jsonResponse.setMessage(AccessTokenStore.getAccessToken(user,60*60*8).toString());
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return 0;
  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // TODO Auto-generated method stub
  }

}
