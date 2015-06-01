package ogcservices;

import impactservice.Configuration;
import impactservice.ImpactUser;
import impactservice.LoginManager;

import java.io.OutputStream;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tools.CGIRunner;
import tools.Debug;
import tools.HTTPTools;
import tools.JSONResponse;
import tools.Tools;


public class AdagucServer {

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
    String[] environmentVariables = Configuration.ADAGUCServerConfig.getADAGUCEnvironment();
    
    //Try to get homedir, ignore if failed
    String userHomeDir="";
    try{
      ImpactUser user = LoginManager.getUser(request,response);
      if(user == null)return;
      userHomeDir=user.getWorkspace();
      Debug.println("WMS for user: "+user.getId());
    }catch(Exception e){    
      Debug.println("Warning: Anonymous user: '"+e.getMessage()+"'");
   
        String source = HTTPTools.getKVPItemDecoded(URLDecoder.decode(queryString,"UTF-8"), "source");
        if(source != null){
          Debug.println("Checking reason for "+source);
          JSONResponse reason = LoginManager.identifyWhyGetRequestFailed(source, request,response);
          if(reason.getStatusCode()==401){
            Debug.println("Redirecting to login page");
            LoginManager.redirectToLoginPage(request,response);
            return;
          }
        }
    }
    

    environmentVariables=Tools.appendString( environmentVariables,"HOME="+userHomeDir);
    environmentVariables=Tools.appendString( environmentVariables,"QUERY_STRING="+queryString);
    
    String commands[] = Configuration.ADAGUCServerConfig.getADAGUCExecutable();
    //Debug.println("ADAGUCExec"+Configuration.ADAGUCServerConfig.getADAGUCExecutable()[0]);
    CGIRunner.runCGIProgram(commands,environmentVariables,userHomeDir,response,outputStream,null);
  }

}
