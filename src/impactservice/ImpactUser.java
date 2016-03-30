package impactservice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import tools.Debug;
import tools.HTTPTools;
import tools.Tools;




public class ImpactUser {

  private String id = null; // The unique ID of the user object
  public String internalName = null;
  private String usersDir = null;
  private String loginInfo = null;
  
  public String certificateFile = null;
  public boolean credentialError = false;
  public boolean configured = false;
  private GenericCart shoppingCart = null;
  
  private String emailAddress;
  public String userMyProxyService = null;
  private String openid = null;
  
  public ImpactUser(String userId) {
    id = userId;
  }
  public String getUserMyProxyService() {
    return userMyProxyService;
  }
  public String getWorkspace(){
    return usersDir;
  }
  public void setWorkspace(String _usersDir){
    usersDir = _usersDir;
  }
  
  public String getLoginInfo(){
    return loginInfo;
  }


  synchronized public GenericCart getProcessingJobList(){
    GenericCart processingJobsList = null;
    Debug.println("getProcessingJobList for "+this.getInternalName());
    try{
      processingJobsList = new GenericCart("processingJobsList",this);
      processingJobsList.loadFromStore();
    }catch(Throwable e){
      try {
        MessagePrinters.emailFatalErrorException("getProcessingJobList",e);
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
    return processingJobsList;  
  }
  synchronized public GenericCart getShoppingCart() {
    Debug.println("getShoppingCart");
    try{
      if(shoppingCart==null){
        shoppingCart = new GenericCart("shoppingCart",this);
      }
      shoppingCart.loadFromStore();
    }catch(Throwable e){
      try {
        MessagePrinters.emailFatalErrorException("getShoppingCart",e);
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }

    return shoppingCart;
  }

  public String getDataDir()  {
    String dataDir = Tools.makeCleanPath(getWorkspace()+"/data");
    try {
      Tools.mkdir(dataDir);
    } catch (IOException e) {
      try {
        MessagePrinters.emailFatalErrorMessage("Unable to create directory", dataDir);
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
    return dataDir;
  }
  
  
  public String getInternalName() {
    return internalName;
  }
  public void _setInternalName(String internalName) {
    this.internalName = internalName;
  }
  public String getId(){
    return id;
  }
  public String getEmailAddress() {
    return emailAddress;
  }
  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }
  public String getOpenId(){
    return this.openid ;
  }
  public String getOpenIdAsString(){
    if(this.openid==null){
      return "Not available from this identity provider.";
    }
    return this.openid ;
  }
  public void setOpenId(String openid) {
    this.openid = openid;
  }
  public String getUserName() {
    if(openid!=null){
      return openid;
    }
    return getEmailAddress();
  }
  public String getDataURL() {
    String url = HTTPTools.makeCleanURL(Configuration.getHomeURLHTTPS()+"/DAP/"+internalName);
    url = url.replace("?","/");
    return url;
  }
  /**
   * Checks if user has given role
   * @param string
   * @return
   */
  public boolean hasRole(String string) {
    if(string.equals("admin")){
      String adminUsers[] = Configuration.Admin.getIdentifiers();
      
      String id = getId();
      Debug.println("HasRole ID "+id);
      for(int j=0;j<adminUsers.length;j++){
        //Debug.println(adminUsers[j]+" -- "+id);
        if(id.equals(adminUsers[j])){
          //Debug.println("ISADMIN: "+id+"\n");
          return true;
        }
      }
    }
    return false;
  }

  
  class UserSessionInfo{
    String creationTime;
    String accessTime;
  }

  Map<Integer,UserSessionInfo> sessions = java.util.Collections.synchronizedMap(new HashMap<Integer, UserSessionInfo>());
  
  private void _addSessionId(int id, long creationTime,long accessTime) {
    UserSessionInfo i = sessions.get(id);
    if(i == null){i=new UserSessionInfo();sessions.put(id, i);}
    
    i.creationTime = ""+tools.DateFunctions.getTimeStampInMillisToISO8601(creationTime);
    i.accessTime = ""+tools.DateFunctions.getTimeStampInMillisToISO8601(accessTime);
  }
  public void removeSessionId(int id) {
    sessions.remove(id);
  }

  Map<Integer, UserSessionInfo> getSessionIds(){
    return sessions;
  }
  
  
  public void setSessionInfo(HttpServletRequest request){
    try{
      _addSessionId(request.getSession().hashCode(),request.getSession().getCreationTime(),request.getSession().getLastAccessedTime());
    }catch(Exception e){
      e.printStackTrace();
    }
  }
  public void setLoginInfo(String loginMethod) {
    this.loginInfo = loginMethod;
  }
  
  
  
 
}
