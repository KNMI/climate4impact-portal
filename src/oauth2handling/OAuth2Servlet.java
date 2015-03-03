package oauth2handling;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import tools.Debug;

/**
 * Servlet implementation class Oauth2Servlet
 */
public class OAuth2Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public OAuth2Servlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	  Debug.println(request.getQueryString());
	  if(request.getQueryString()==null){
	    try {
        OAuth2Handler.getCode(request,response);
      } catch (OAuthSystemException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
	    return;
	    
	  }else{
	  
	    
	    Debug.println("makeOauthzResponse for "+request.getQueryString());
	    OAuth2Handler.makeOauthzResponse(request,response);
	  }
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
