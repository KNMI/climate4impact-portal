package impactservice;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;




public class User {

  public String internalName = null;
  public String usersDir = null;
  public String id = null;
  public String certificateFile = null;
  public boolean credentialError = false;
  public boolean configured = false;
  private GenericCart shoppingCart = null;
  private GenericCart processingJobsList = null;
  public String getWorkspace(){
    return usersDir;
  }
 
  static public String getUserId(HttpServletRequest request){
    try{
      return  LoginManager.getUser(request).id;
    }catch(Exception e){
      return "";
    }
  }
  synchronized public GenericCart getProcessingJobList(){
    try{
      if(processingJobsList==null){
        processingJobsList = new GenericCart("processingJobsList",this);
        processingJobsList.loadFromStore();
      }
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
    try{
      if(shoppingCart==null){
        shoppingCart = new GenericCart("shoppingCart",this);
        shoppingCart.loadFromStore();
      }
    }catch(Throwable e){
      try {
        MessagePrinters.emailFatalErrorException("getShoppingCart",e);
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }

    return shoppingCart;
  }

  public static User getUser(HttpServletRequest request) throws Exception {
    return  LoginManager.getUser(request);
  }

 
}
