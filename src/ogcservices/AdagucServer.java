package ogcservices;

import impactservice.Configuration;
import impactservice.ImpactUser;
import impactservice.LoginManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tools.CGIRunner;
import tools.Debug;
import tools.HTTPTools;
import tools.JSONResponse;
import tools.Tools;


public class AdagucServer extends HttpServlet{

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Runs the ADAGUC WMS server as executable on the system. 
   * Emulates the behavior of scripts in a traditional cgi-bin directory of apache http server.
   * @param response Can be null, when given the content-type for the response will be set. 
   * Results are not sent to this stream, this is done by outputStream parameter
   * @param queryString The querystring for the CGI script
   * @param outputStream A standard byte output stream in which the data of stdout is captured. 
   * Can for example be response.getOutputStream().
   * @throws Exception
   */
  public static void runADAGUCWMS(HttpServletRequest request,HttpServletResponse response,String queryString,OutputStream outputStream) throws Exception{
    Debug.println("runADAGUCWMS");
    String[] environmentVariables = Configuration.ADAGUCServerConfig.getADAGUCEnvironment();
    
    //Try to get homedir, ignore if failed
    String userHomeDir="/tmp/";
    try{
      ImpactUser user = LoginManager.getUser(request);
      if(user == null)return;
      userHomeDir=user.getWorkspace();
      Debug.println("WMS for user: "+user.getId());
    }catch(Exception e){    
//        /* This checks when a resource needs authentication and there is no user is signed in, 
//         * the user is redirected to the login screen.
//         */
//   
//        String source = HTTPTools.getKVPItemDecoded(URLDecoder.decode(queryString,"UTF-8"), "source");
//        if(source != null){
//          Debug.println("Checking reason for "+source);
//          JSONResponse reason = LoginManager.identifyWhyGetRequestFailed(source, request);
//          if(reason.getStatusCode()==401){
//            Debug.println("Redirecting to login page");
//            LoginManager.redirectToLoginPage(request,response);
//            return;
//          }
//        }
    }
    

    environmentVariables=Tools.appendString( environmentVariables,"HOME="+userHomeDir);
    environmentVariables=Tools.appendString( environmentVariables,"QUERY_STRING="+queryString);
    environmentVariables=Tools.appendString( environmentVariables,"ADAGUC_ONLINERESOURCE="+Configuration.getHomeURLHTTPS()+"/adagucserver?");
    environmentVariables=Tools.appendString( environmentVariables,"ADAGUC_TMP="+userHomeDir+"/tmp/");
    
    String commands[] = Configuration.ADAGUCServerConfig.getADAGUCExecutable();
    //Debug.println("ADAGUCExec"+Configuration.ADAGUCServerConfig.getADAGUCExecutable()[0]);
    CGIRunner.runCGIProgram(commands,environmentVariables,userHomeDir,response,outputStream,null);
  }
  
  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request,response);
  }
  
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    Debug.println("Handle ADAGUC WMS requests");
    OutputStream out1 = null;
    //response.setContentType("application/json");
    try {
      out1 = response.getOutputStream();
    } catch (IOException e) {
      Debug.errprint(e.getMessage());
      return;
    }
  
    try {
      AdagucServer.runADAGUCWMS(request,response,request.getQueryString(),out1);

    } catch (Exception e) {
      response.setStatus(401);
      try {
        out1.write(e.getMessage().getBytes());
      } catch (IOException e1) {
        Debug.errprintln("Unable to write to stream");
        //Debug.printStackTrace(e);
      }
    }    
  }


}
