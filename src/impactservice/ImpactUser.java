package impactservice;

import java.io.IOException;

import tools.Debug;
import tools.Tools;




public class ImpactUser {

  private String id = null; // The unique ID of the user object
  public String internalName = null;
  private String usersDir = null;
  
  public String certificateFile = null;
  public boolean credentialError = false;
  public boolean configured = false;
  private GenericCart shoppingCart = null;
  private GenericCart processingJobsList = null;
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
  


  synchronized public GenericCart getProcessingJobList(){
    Debug.println("getProcessingJobList");
    try{
      if(processingJobsList==null){
        processingJobsList = new GenericCart("processingJobsList",this);
      }
      Debug.println("Loading processing list from store");
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

  public String getDataDir() throws IOException {
    String dataDir = Tools.makeCleanPath(getWorkspace()+"/data");
    Tools.mkdir(dataDir);
    return dataDir;
  }
  
  
  public String getInternalName() {
    return internalName;
  }
  public void setInternalName(String internalName) {
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
  public void setOpenId(String openid) {
    this.openid = openid;
  }
  public String getUserName() {
    if(openid!=null){
      return openid;
    }
    return getEmailAddress();
  }

 
}
