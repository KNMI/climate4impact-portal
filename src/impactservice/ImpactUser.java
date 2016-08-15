package impactservice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import tools.DateFunctions;
import tools.Debug;
import tools.HTTPTools;
import tools.Tools;




public class ImpactUser {
  static boolean debug=false;
  private String id = null; // The unique ID of the user object
  private String _internalName = null;
  private String usersDir = null;
  private String loginInfo = null; /* String composed by checklogin function based on login params */
  private String certInfo = null;
  private String oauth2certificate = null;/* String is retrieved from a session set during OAuth2 login */
  private String loginMethod = null;/* Set by the login method in the session */
  public long certificateValidityNotAfter = -1;
  public String certificateFile = null;/*Pointer to the users credential */
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
  
  public String getLoginAndCredentialInfo(){
    if(loginInfo != null){
      return loginInfo+"\n"+certInfo;
    }else{
      return "Cert info:"+"\n"+certInfo;
    }
  }


  synchronized public GenericCart getProcessingJobList(){
    GenericCart processingJobsList = null;
    Debug.println("getProcessingJobList for "+this.getUserId());
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
  
  

//  public void _setInternalName(String internalName) {
//    this.internalName = internalName;
//  }
  public String getUserId(){
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
    String url = HTTPTools.makeCleanURL(Configuration.getHomeURLHTTPS()+"/DAP/"+getUserId());
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
      
      String id = getUserId();
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
  
  
//  public void setSessionInfo(HttpServletRequest request){
//    try{
//      _addSessionId(request.getSession().hashCode(),request.getSession().getCreationTime(),request.getSession().getLastAccessedTime());
//    }catch(Exception e){
//      e.printStackTrace();
//    }
//  }
  public void setLoginInfo(String loginMethod) {
    this.loginInfo = loginMethod;
  }
  public void setCertInfo(String certInfo) {
    this.certInfo = certInfo;
    
  }
  
  public void setAttributesFromHTTPRequestSession(HttpServletRequest request) {
    if(request == null){
      return;
    }
    if(request.getSession()==null){
      return;
    }
    if(getOpenId()==null){
      String openid = (String) request.getSession().getAttribute("openid_identifier");
      if(openid != null){
        setOpenId(openid);
      }
    }
  
    oauth2certificate = (String) request.getSession().getAttribute("certificate");
    loginMethod = (String) request.getSession().getAttribute("login_method");
    
    try {
      String emailAddress = (String) request.getSession().getAttribute(
          "emailaddress");
      
      if (emailAddress != null) {
        if (emailAddress.length() > 0) {
          setEmailAddress(emailAddress);
      
          Debug.println("Email: " + emailAddress);
        }
      }
  
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /* Returns The X509 credential is stored as string, not as file location */
  public String getCertificate() {
    return oauth2certificate;
  } 
  public String getLoginMethod() {
    return loginMethod;
  }
  


  /*Properties in a json file on disk can be loaded*/
  public void loadProperties() {
    //Debug.println("loadProperties");
    String userPropertiesFile = this.getWorkspace()+"/userprops.json";
    try {
      String data = tools.LazyCaller.getInstance().readFile(userPropertiesFile);
      JSONObject searchResults =  (JSONObject) new JSONTokener(data).nextValue();
      try {
        this.openid = searchResults.getString("openid");
      } catch (JSONException e) {
      }
      try {
        this.emailAddress = searchResults.getString("email");
      } catch (JSONException e) {
      }
      try {
        this.certificateValidityNotAfter = searchResults.getLong("certificateValidityNotAfter");
      } catch (JSONException e) {
      }
      
      try {
        this.userMyProxyService = searchResults.getString("userMyProxyService");
      } catch (JSONException e) {
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
  }  
  
  /* Properties currently set in the user object can be written to a JSON file on disk 
   * It saves currently:
   * - email
   * - openid
   * - certificateValidityNotAfter
   * */
  
  public void _saveProperties() {
    //Debug.println("saveProperties");
    String userPropertiesFile = this.getWorkspace()+"/userprops.json";
    JSONObject userProps = new JSONObject();
    
    
    try {
      userProps.put("openid", this.openid);
      userProps.put("email", this.emailAddress);
      userProps.put("certificateValidityNotAfter", this.certificateValidityNotAfter);
      userProps.put("userMyProxyService", this.userMyProxyService);
    } catch (JSONException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    
    try {
      //Debug.println("Writing properties to " +userPropertiesFile);
      tools.LazyCaller.getInstance().writeFile(userPropertiesFile, userProps.toString());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  
  
 
}
