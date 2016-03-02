package tokenapi;

import impactservice.AccessTokenStore;
import impactservice.ImpactUser;
import impactservice.LoginManager;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import tools.Debug;
import tools.HTTPTools;
import tools.JSONMessageDecorator;
import tools.JSONResponse;

/**
 * Servlet implementation class TokenAPI
 */
public class TokenAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TokenAPI() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	  String serviceStr = null;
    JSONMessageDecorator errorResponder = new JSONMessageDecorator (response);
	  serviceStr=request.getParameter("service");
	  if(serviceStr==null){
	    serviceStr=request.getParameter("SERVICE");
	  }
	  if(serviceStr!=null){serviceStr=URLDecoder.decode(serviceStr,"UTF-8");}else{errorResponder.printexception("serviceStr="+serviceStr);return;}
	  if(serviceStr.equals("account")){
	    handleAccountRequests(request,response);
	  }
    
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	
	 /**
   * Handles account service requests, like generating/revoking access_tokens.
   * Service needs to be "account"
   * Possible requests are: 
   *  - "generatetoken": generates a new token
   *  - "listtokens": provides a list with all published accesstokens for this user
   * @param request
   * @param response
   */
  private void handleAccountRequests(HttpServletRequest request,
      HttpServletResponse response) {

    Debug.println("Service \"account\"");
    JSONResponse jsonResponse = new JSONResponse(request);
    try {
      String requestStr=HTTPTools.getHTTPParam(request, "request");
      ImpactUser user = null;
      user = LoginManager.getUser(request,response);
      if(requestStr.equals("generatetoken")){
        jsonResponse.setMessage(AccessTokenStore.generateAccessToken(user).toString());
      }
      if(requestStr.equals("listtokens")){
        
        JSONArray a = new JSONArray();
        Vector<String> accessTokens = AccessTokenStore.listtokens(user);
        for(int j=0;j<accessTokens.size();j++){
          a.put((JSONObject) new JSONTokener(accessTokens.get(j)).nextValue());
        }
        jsonResponse.setMessage(a.toString());
        a = null;
      }
    } catch(Exception e){
      e.printStackTrace();
      jsonResponse.setException("Service account failed",e);
    }
    
    try {
      jsonResponse.print(response);
    } catch (Exception e1) {
    
    }

    
  }
}
