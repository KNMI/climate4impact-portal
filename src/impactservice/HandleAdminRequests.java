package impactservice;


import impactservice.ImpactUser.UserSessionInfo;

import java.util.Map.Entry;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tools.Debug;
import tools.JSONResponse;

public class HandleAdminRequests {
  public static void handleAdminRequests(HttpServletRequest request, HttpServletResponse response) {
    JSONResponse jsonResponse = new JSONResponse();

    ImpactUser user = null;
    try {
      user = LoginManager.getUser(request,response);
    } catch (Exception e) {
      jsonResponse.setErrorMessage("No user information", 401);
      e.printStackTrace();
    }
    
    if(user != null){
      Debug.println("User: "+user.internalName);
      if(user.hasRole("admin")==false){
        jsonResponse.setErrorMessage("Unauthorized", 403);
      }
    }

   
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
            jsonUser.put("id", userList.get(j).getId());
            jsonUser.put("internalname", userList.get(j).getInternalName());
            jsonUser.put("openid", userList.get(j).getOpenId());
            jsonUser.put("email", userList.get(j).getEmailAddress());
            jsonUser.put("dataurl", userList.get(j).getDataURL());
            jsonUser.put("workspace", userList.get(j).getWorkspace());
            jsonUser.put("datadir", userList.get(j).getDataDir());
            JSONArray sessionIds = new JSONArray();



            for (Entry<Integer, UserSessionInfo> entry : userList.get(j).getSessionIds().entrySet()){
                JSONObject sessionInfo = new JSONObject();
                sessionInfo.put("id",entry.getKey());
                sessionInfo.put("created",entry.getValue().creationTime);
                sessionInfo.put("accessed",entry.getValue().accessTime);
                sessionIds.put(sessionInfo);
            }
            
            jsonUser.put("sessions", sessionIds);
            ImpactUser infoUser = userList.get(j);
            jsonUser.put("basketsize", infoUser.getShoppingCart().getNumProducts());
            jsonUser.put("processingjobsize", infoUser.getProcessingJobList().getNumProducts());
            
            jsonUserList.put(jsonUser);
          }
          json.put("users", jsonUserList);
          
          jsonResponse.setMessage(json);
        }
      
      } catch (JSONException e) {
        jsonResponse.setException("JSON Error",e);
      }
    }
    
    try {
      jsonResponse.setJSONP(request);
      response.setContentType(jsonResponse.getMimeType());
      response.getOutputStream().print(jsonResponse.getMessage());
    } catch (Exception e1) {
      e1.printStackTrace();
    }
    
  }
}
