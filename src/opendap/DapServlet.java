package opendap;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import impactservice.AccessTokenStore;
import impactservice.ImpactUser;
import impactservice.LoginManager;
import impactservice.AccessTokenStore.AccessTokenHasExpired;
import impactservice.AccessTokenStore.AccessTokenIsNotYetValid;
import tools.Debug;

/**
 * Servlet implementation class Server
 */
public class DapServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  /**
   * @see HttpServlet#HttpServlet()
   */
  public DapServlet() {
    super();
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    /* Three ways of authentication are possible:
     * 1) - browser session based
     * 2) - X509, for command line
     * 3) - Access token, for commandline/browsers
     * 
     * When an access token is provided, it is part of the path. 
     */

    /*Get User ID from tokenstore*/ 
    JSONObject token = null;
    try {
      token = AccessTokenStore.checkIfTokenIsValid(request);
    } catch (AccessTokenIsNotYetValid e1) {
    } catch (AccessTokenHasExpired e1) {
    }


    String path = request.getPathInfo();
    if(path==null)return;
    

    /*Retrieve user ID from path*/
    boolean skipTokenFirst=false;
    if(token!=null){
      skipTokenFirst = true;/*token is the first piece of the part.*/
    }
    String userIdFromPath = "";
    String cleanPath = "";/*Complete string*/
    String[] pathParts = path.split("/");
    int pathPartsIndex = 0;
    while(pathPartsIndex<pathParts.length){
      String pathParth = pathParts[pathPartsIndex];
      if(pathParth.length()>0){
        if(skipTokenFirst){
          try {
            String tokenString = token.getString("token");
            if(pathParth.equals(tokenString)){
              skipTokenFirst = false;
            }
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }
        else{
          if(pathParth.length()>0){
            if(userIdFromPath.length()==0){
              userIdFromPath = pathParth;
            }
            cleanPath+="/"+pathParth;
          }
        }
      }
      pathPartsIndex++;
    }

    token = null;
    String baseNameWithOpenDapSuffixes = cleanPath.substring(cleanPath.lastIndexOf("/")+1);
    String baseName = null;
    if(baseNameWithOpenDapSuffixes.endsWith(".das")||
        baseNameWithOpenDapSuffixes.endsWith(".ddx")||
        baseNameWithOpenDapSuffixes.endsWith(".dds")||
        baseNameWithOpenDapSuffixes.endsWith(".dods")){
      baseName = baseNameWithOpenDapSuffixes.substring(0,baseNameWithOpenDapSuffixes.lastIndexOf("."));
    }else{
      baseName = baseNameWithOpenDapSuffixes;
    }
    String filePath = cleanPath.substring(userIdFromPath.length()+1);
    filePath = filePath.substring(0,filePath.lastIndexOf("/"));
    String localNetCDFFileName = null;
    try {
      ImpactUser user =  LoginManager.getUser(request);
      localNetCDFFileName = user.getDataDir()+"/"+filePath+"/"+baseName;
      if(!userIdFromPath.startsWith(user.getUserId())){
        Debug.println("Comparing "+user.getUserId() + "==" + userIdFromPath+ " UNEQUAL");
        Debug.errprintln("403, Unauthorized: "+userIdFromPath+"!="+user.getUserId());
        response.setStatus(403);
        response.getOutputStream().print("403 Forbidden (Wrong user id)");
        return;
      }
    } catch (Exception e) {
      String message = "401 No user information provided: "+e.getMessage();
      response.setStatus(401);
      Debug.errprintln(message);
      response.getOutputStream().print(message);
      return;
    }
    OpendapServer.handleOpenDapReqeuests(localNetCDFFileName,baseName,request,response);
  };

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet( request,  response);
  }

}
