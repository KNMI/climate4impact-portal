package impactservice;


import impactservice.ImpactUser.UserSessionInfo;

import java.lang.reflect.AccessibleObject;
import java.text.MessageFormat.Field;
import java.util.Map.Entry;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.session.StandardSessionFacade;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tools.DateFunctions;
import tools.Debug;
import tools.JSONResponse;

public class HandleAdminRequests {
  public static void handleAdminRequests(HttpServletRequest request, HttpServletResponse response) throws Exception {
    JSONResponse jsonResponse = new JSONResponse(request);

    ImpactUser user = null;
    try {
      user = LoginManager.getUser(request);
    } catch (Exception e) {
      jsonResponse.setErrorMessage("No user authenticated, no user information available.", 401);
      try {
        jsonResponse.print(response);
      } catch (Exception e1) {
        e1.printStackTrace();
      }
      return;
    }
    
    
    if(user != null){
      Debug.println("User: "+user.getUserId());
      if(user.hasRole("admin")==false){
        jsonResponse.setErrorMessage("Unauthorized, user is not an admin.", 403);
        try {
          jsonResponse.print(response);
        } catch (Exception e1) {
          e1.printStackTrace();
        }
        return;
      }
    }

    int numSessions = 0;
    if(jsonResponse.hasError() == false){
      String requestParam = null;
      try {
        requestParam = tools.HTTPTools.getHTTPParam(request, "request");
      } catch (Exception e) {
        jsonResponse.setException("No Request parameter", e);
      }
      try {
        if(requestParam.equalsIgnoreCase("getusers")){
          JSONObject json = new JSONObject();
          JSONArray jsonUserList = new JSONArray();
          
          Vector<ImpactUser>  userList = LoginManager.getUsers();
          for(int j=0;j<userList.size();j++){
            JSONObject jsonUser = new JSONObject();
            jsonUser.put("id", userList.get(j).getUserId());
            jsonUser.put("openid", userList.get(j).getOpenId());
            jsonUser.put("email", userList.get(j).getEmailAddress());
            jsonUser.put("dataurl", userList.get(j).getDataURL());
            jsonUser.put("workspace", userList.get(j).getWorkspace());
            jsonUser.put("datadir", userList.get(j).getDataDir());
            JSONArray sessionIds = new JSONArray();



            for (Entry<String, UserSessionInfo> entry : userList.get(j).getSessionIds().entrySet()){
                JSONObject sessionInfo = new JSONObject();
                //sessionInfo.put("id",entry.getKey());
                sessionInfo.put("created",DateFunctions.getTimeStampInMillisToISO8601(entry.getValue().creationTime));
                sessionInfo.put("accessed",DateFunctions.getTimeStampInMillisToISO8601(entry.getValue().accessTime));
                sessionInfo.put("host",entry.getValue().host);
                sessionInfo.put("useragent",entry.getValue().userAgent);
                sessionInfo.put("sessiontype",entry.getValue().sessionType);
                sessionInfo.put("hits",entry.getValue().hits);
                sessionInfo.put("token",entry.getValue().token);
                numSessions++;
                sessionIds.put(sessionInfo);
            }
            
            jsonUser.put("sessions", sessionIds);
            ImpactUser infoUser = userList.get(j);
            
            jsonUser.put("processingjobsize", infoUser.getProcessingJobList().getNumJobs());
            jsonUser.put("basketsize", infoUser.getShoppingCart().getNumFiles());
            jsonUserList.put(jsonUser);
          }
          json.put("users", jsonUserList);
          json.put("numusers", jsonUserList.length());
          json.put("numsessions", numSessions);

          HttpSession session = request.getSession();
          java.lang.reflect.Field facadeSessionField;
          try {
            facadeSessionField = StandardSessionFacade.class.getDeclaredField("session");
            facadeSessionField.setAccessible(true);
            StandardSession stdSession = (StandardSession) facadeSessionField.get(session);
            Manager manager = stdSession.getManager();
            
            int a = manager.getActiveSessions();
            json.put("numactivetomcatsessions", a);
          } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
      
          
          
          jsonResponse.setMessage(json);
        }//End of getusers
        
        if(requestParam.equalsIgnoreCase("login")){
          String client_id = "";
          try {
            client_id = tools.HTTPTools.getHTTPParam(request, "client_id");
          } catch (Exception e) {
            jsonResponse.setException("No Request parameter", e);
          }
          if(jsonResponse.hasError()==false){
            Debug.println("client_id = "+client_id);
            

            //
            
            
            try {
              LoginManager.logout(request, response);
              user.logoutAndRemoveSessionId(request, response);
              ImpactUser newUser = LoginManager.getUser(client_id);
              newUser.setAttributesFromHTTPRequestSession(request,response,null);
              try {
                LoginManager.checkLogin(newUser);
              } catch (Exception e) {
              }
              
              jsonResponse.setMessage("{\"status\":\"ok\"}");
            } catch (Exception e) {
              jsonResponse.setException("unable to change user", e);
            }
            
          }
        }
        
      
      } catch (JSONException e) {
        jsonResponse.setException("JSON Error",e);
      }
    }
    
    try {
      jsonResponse.print(response);
    } catch (Exception e1) {
      e1.printStackTrace();
    }
    
  }
}
