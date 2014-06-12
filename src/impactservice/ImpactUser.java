package impactservice;

import java.io.IOException;

import tools.DebugConsole;
import tools.Tools;




public class ImpactUser {

  public String internalName = null;
  private String usersDir = null;
  public String id = null;
  public String certificateFile = null;
  public boolean credentialError = false;
  public boolean configured = false;
  private GenericCart shoppingCart = null;
  private GenericCart processingJobsList = null;
  private String emailAddress;
  public String getWorkspace(){
    return usersDir;
  }
  public void setWorkspace(String _usersDir){
    usersDir = _usersDir;
  }

  synchronized public GenericCart getProcessingJobList(){
    DebugConsole.println("getProcessingJobList");
    try{
      if(processingJobsList==null){
        processingJobsList = new GenericCart("processingJobsList",this);
      }
      DebugConsole.println("Loading processing list from store");
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
    DebugConsole.println("getShoppingCart");
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
  public String getEmailAddress() {
    return emailAddress;
  }
  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }
  
  /**
   * Checks a internal file ID and returns the absolute system path to the file.
   * @param internalFileID The internal id, <impactspacepath>/<userhomepath>/data is prepended
   * @return The absolute path to the file
   * @throws FileAccessForbiddenException
   * @throws IOException
   */
  /*public String checkFileAndGetAbsolutePath(String internalFileID) throws FileAccessForbiddenException, IOException {
    DebugConsole.println("checkFileAndGetAbsolutePath: "+internalFileID);
    
    String service = tools.HTTPTools.getKVPItem(internalFileID, "service");
    String fileId = tools.HTTPTools.getKVPItem(internalFileID, "file");
    
    if(service!=null && fileId !=null){
      if("data".equalsIgnoreCase(service)){
        internalFileID = fileId;
        DebugConsole.println("FileID: "+internalFileID);
      }
    }
    
    try {
      Tools.checkValidCharsForFile(internalFileID);
    } catch (Exception e) {
      throw new FileAccessForbiddenException("Forbidden tokens");
    }
    String internalFileLocation = getDataDir()+"/"+internalFileID;
    File file = new File(internalFileLocation);
    if(file.exists() == false){
      DebugConsole.errprintln("File not found: '"+internalFileLocation+"'");
      throw new FileNotFoundException(internalFileID);
    }
    String absoluteFile = file.getCanonicalPath();
    DebugConsole.println("Absolute path: =  "+absoluteFile);
    if(absoluteFile.indexOf(getDataDir())!=0){
      throw new FileAccessForbiddenException("Forbidden path");
    }
    return absoluteFile;
  }*/

 
}
