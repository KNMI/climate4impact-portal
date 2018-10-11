package impactservice;

import java.util.Vector;

import tools.Debug;
import tools.MyXMLParser.XMLElement;


public class Configuration {

  static String portalMode = "c4i";
  public static String getPortalMode(){
    
    readConfig();
    //portalMode = "c3s-magic";
    return portalMode;//"c3s-magic";//c4i
  }
  
  static long readConfigPolInterval = 0;;
  
  private static String impactWorkspace=null;//"/home/visadm/impactspace/";

  public static String getImpactWorkspace(){readConfig();return impactWorkspace+"/";}
  
  public static String getDownscalingPortalWorkspace(){return getImpactWorkspace()+"downscalingportal/";};
  
  public static String getHomePath(){
    return System.getProperty("user.home")+"/impactportal/";
  }
  
  public static String getHomeURLPrefix(){
    return "impactportal";
  }
  
  public static String _getHomeURLHTTP(){
    return GlobalConfig.getServerHTTPURL()+getHomeURLPrefix();
  }
  
  public static String getHomeURLHTTPS(){
    return GlobalConfig.getServerHTTPSURL()+getHomeURLPrefix();
  }

  
  private static String getConfigFile(){
    try{
      String configLocation = System.getenv("IMPACTPORTAL_CONFIG");
      if(configLocation!=null){
        if(configLocation.length()>0){
          return configLocation;
        }
      }
    }catch(Exception e){
    }
    
    return getHomePath()+"config.xml";
  }
  
  public static String getImpactServiceLocation(){
    return "/impactportal/ImpactService?";
  }
  
  
  public static synchronized void readConfig(){
    //Re-read the config file every 10 seconds.
    if(impactWorkspace != null && readConfigPolInterval != 0){
      if(System.currentTimeMillis()<readConfigPolInterval+10000)return;
    }
    readConfigPolInterval=System.currentTimeMillis(); 
    Debug.println("Reading configfile "+getConfigFile());
    XMLElement configReader = new XMLElement();
    try {
      configReader.parseFile(getConfigFile());
    } catch (Exception e) {
      Debug.println("Unable to read "+getConfigFile());
      configReader = null;
      return;
    }

    impactWorkspace=configReader.getNodeValue("impactportal.impactworkspace");
    String _portalMode = configReader.getNodeValue("impactportal.portalmode");
    if(_portalMode!=null){
      portalMode=_portalMode;
    }
    GlobalConfig.doConfig(configReader);
    DrupalConfig.doConfig(configReader);
    VercSearchConfig.doConfig(configReader);
    LoginConfig.doConfig(configReader);
    ExpertContact.doConfig(configReader);
    Admin.doConfig(configReader);
    ADAGUCServerConfig.doConfig(configReader);
    PyWPSServerConfig.doConfig(configReader);
    WPSServicesConfig.doConfig(configReader);
    DownloadScriptConfig.doConfig(configReader);
    Oauth2Config.doConfig(configReader);
    DownscalingConfig.doConfig(configReader);
    
    
    configReader = null; 
  }
  
  public static class Admin{
    static String[] addresses={};
    static String[] identifiers={};
    
    public static void doConfig(XMLElement  configReader){
      try{
        addresses = configReader.getNodeValue("impactportal.admin.mailaddresses").split(",");
      }catch(Exception e){
        Debug.errprintln("impactportal.admin.mailaddresses not set");
      }
      try{
        identifiers = configReader.getNodeValue("impactportal.admin.identifiers").split(",");
      }catch(Exception e){
        Debug.errprintln("impactportal.admin.identifiers not set");
      }
    }
    
    public static String[] getEmailAddresses(){
       readConfig();return  addresses;
    }

    public static String[] getIdentifiers(){
       readConfig();return  identifiers;
    }
  }
  
//  public static String getEmailToSendFatalErrorMessages(){
//     configReader.getNodeValue("impactportal.expertcontact.mailaddresses").split(",");
//    return "plieger@knmi.nl";
//  }
  
  public static class GlobalConfig{
    private static String serverURLHTTP="";
    private static String serverURLHTTPS="";
    
    public static String offlineMode;
    public static String defaultUserInOfflineMode;
    public static String statlogfile = "";
    
    //public static String getServerHomeURL(){readConfig();return serverURLHTTP;}
    public static void doConfig(XMLElement configReader) {
      serverURLHTTP = configReader.getNodeValue("impactportal.serverurl");
      serverURLHTTPS = configReader.getNodeValue("impactportal.serverurlhttps");
      offlineMode = configReader.getNodeValue("impactportal.offlinemode");
      defaultUserInOfflineMode= configReader.getNodeValue("impactportal.defaultuseropenid");
      statlogfile= configReader.getNodeValue("impactportal.statlogfile");
    }
    public static String getServerHTTPURL(){
      readConfig();
      return serverURLHTTP;
    }
    public static String getServerHTTPSURL(){
      readConfig();
      return serverURLHTTPS;
    }
    public static boolean isInOfflineMode() {
      readConfig();
      if("true".equalsIgnoreCase(offlineMode))return true;
      return false;
    }
    public static String getDefaultUser() {
      //Only used in case of offline mode
      readConfig();
      return defaultUserInOfflineMode;
    }
    public static String getStatLogfile() {
      readConfig();
      if(statlogfile==null)return null;
      if(statlogfile.length()==0)return null;
      return statlogfile;
    }
  }
  
  public static class DrupalConfig{
    private static String drupalUserName=null;
    private static String drupalPassword=null;
    private static String drupalHost="<drupalhost>";
    private static String drupalBaseURL="<drupalbaseurl>";
    private static String drupalDirectory="<drupaldirectory>";
    private static String portalFilesLocation="https://climate4impact.eu/files/";
    public static void doConfig(XMLElement  configReader){
      drupalHost=configReader.getNodeValue("impactportal.drupalconfig.drupalhost");
      drupalBaseURL=configReader.getNodeValue("impactportal.drupalconfig.drupalbaseurl");
      drupalDirectory=configReader.getNodeValue("impactportal.drupalconfig.drupaldirectory");
      
      try{drupalUserName=configReader.getNodeValue("impactportal.drupalconfig.username");}catch(Exception e){Debug.errprintln("impactportal.drupalconfig.username not set");}
      try{drupalPassword=configReader.getNodeValue("impactportal.drupalconfig.password");}catch(Exception e){Debug.errprintln("impactportal.drupalconfig.password not set");}
      

      //portalFilesLocation=configReader.getNodeValue("impactportal.drupalconfig.portalfileslocation");
    }
    public static String getDrupalHost(){readConfig();return drupalHost;}
    public static String getDrupalBaseURL(){readConfig();return drupalBaseURL;}
    public static String getDrupalDirectory(){readConfig();return drupalDirectory;}
    public static String getPortalFilesLocation() {readConfig();return portalFilesLocation;}
    public static String getDrupalUserName()  {readConfig();return drupalUserName;}
    public static String getDrupalPassword()  {readConfig();return drupalPassword;}
  }
  
  public static class Oauth2Config{
    
    public static class Oauth2Settings{
    //  For building the web page:
      public String description = null;
      public String logo = null;
      public String registerlink= null;
      
      //For server requests:
      public String OAuthAuthLoc = null;
      public String OAuthTokenLoc = null;
      public String OAuthClientId = null;
      public String OAuthClientScope = null;
      
      //Secret thing
      public String OAuthClientSecret = null;

      
      
      public String id = null;
      public String getConfig() {
        String config = "OAuthAuthLoc: "+OAuthAuthLoc+"\n";
        config += "OAuthTokenLoc: "+OAuthTokenLoc+"\n";
        config += "OAuthClientId: "+OAuthClientId+"\n";
        config += "OAuthClientScope: "+OAuthClientScope+"\n";
        return config;
      }
    }
    
    static Vector<Oauth2Settings> oauth2Providers = new Vector<Oauth2Settings>();
    
    private static Oauth2Settings _getOauthSetting(String id){
      for(int j=0;j<oauth2Providers.size();j++){
//        Debug.println("Iterating "+oauth2Providers.get(j).id);
        if(oauth2Providers.get(j).id.equals(id))return oauth2Providers.get(j);
      }
      return null;
    }
    
    private static Vector<String> _getProviders(){
      Vector<String> providers = new Vector<String>();
      for(int j=0;j<oauth2Providers.size();j++){
        providers.add(oauth2Providers.get(j).id);
      }
      return providers;
    }
    
   
    public static void doConfig(XMLElement  configReader){
      synchronized(oauth2Providers){
        oauth2Providers.clear();
        Vector<XMLElement> providers = null;
        try {
          providers = configReader.get("impactportal").get("oauth2").getList("provider");
        } catch (Exception e1) {
          e1.printStackTrace();
        }
        if(providers == null){
          Debug.errprintln("No Oauth2 providers configured");
          return;
        }
        for(int j=0;j<providers.size();j++){
          XMLElement provider = providers.get(j);
         
          try {
            Oauth2Settings oauthSetting = new Oauth2Settings();
            oauthSetting.id = provider.getAttrValue("name");
            oauthSetting.OAuthAuthLoc = provider.get("authloc").getValue();
            oauthSetting.OAuthTokenLoc = provider.get("tokenloc").getValue();
            oauthSetting.OAuthClientId = provider.get("clientid").getValue();
            oauthSetting.OAuthClientSecret = provider.get("clientsecret").getValue();
            oauthSetting.OAuthClientScope = provider.get("scope").getValue();
            
            try{oauthSetting.description = provider.get("description").getValue();}catch(Exception e){}
            try{oauthSetting.logo = provider.get("logo").getValue();}catch(Exception e){}
            try{oauthSetting.registerlink = provider.get("registerlink").getValue();}catch(Exception e){}
            
            oauth2Providers.add(oauthSetting);
            //Debug.println(j+") Found Oauth2 provider "+oauthSetting.id);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
    
    public static Oauth2Settings getOAuthSettings(String id) {
      readConfig();
      return _getOauthSetting(id);
    }
    
    public static Vector<String> getProviders() {
      readConfig();
      
      return _getProviders();
    }
  }
  
  public static class VercSearchConfig{
    private static String __deprecated_vercSearchURL="<deprecated>";
    private static String esgfSearchURL="<esgfSearchURL>";
    public static void doConfig(XMLElement  configReader){
      __deprecated_vercSearchURL=configReader.getNodeValue("impactportal.searchconfig.vercsearchurl");
      esgfSearchURL=configReader.getNodeValue("impactportal.searchconfig.esgfsearchurl");
    }
    public static String __getDeprecatedVercSearchURL(){readConfig();return __deprecated_vercSearchURL;}
    public static String getEsgfSearchURL(){readConfig();return esgfSearchURL;}
  }
  
  public static class LoginConfig{
    
    private static String myProxyServerHost = "<proxyhost>";
    private static String myProxyServerIdendityAuthorization = "/O=Grid/OU=Globus/OU=climate4impact.eu/CN=host/climate4impact.eu";
    private static int myProxyServerPort = 7512;
    private static String trustStoreFile = null;
    private static String trustStorePassword= null;
    private static String myProxyDefaultPassword = "<defaultpassword>";
    private static String trustRootsLocation = null;
    
    // myProxyDefaultUserName should be null, because in that case the openid identifier from the current user is used.
    // It can be set to override a custom username, e.g. in case of testing on a workstation.
    private static String myProxyDefaultUserName = null;
    public static void doConfig(XMLElement  configReader){
      myProxyServerHost=configReader.getNodeValue("impactportal.loginconfig.myproxyserverhost");
      myProxyServerIdendityAuthorization=configReader.getNodeValue("impactportal.loginconfig.myproxyserveridentityauthorization");
      myProxyServerPort=Integer.parseInt(configReader.getNodeValue("impactportal.loginconfig.myproxyserverport"));
      trustStoreFile=configReader.getNodeValue("impactportal.loginconfig.truststorefile");
      trustStorePassword=configReader.getNodeValue("impactportal.loginconfig.truststorepassword");
      myProxyDefaultUserName = configReader.getNodeValue("impactportal.loginconfig.myproxyserverusernameoverride");
      myProxyDefaultPassword = configReader.getNodeValue("impactportal.loginconfig.myproxyserverpassword");
      trustRootsLocation = configReader.getNodeValue("impactportal.loginconfig.trustrootslocation");
      
      Debug.println("Setting javax.net.ssl.trustStore to "+trustStoreFile);
      System.setProperty("javax.net.ssl.trustStore",trustStoreFile);
      System.setProperty("javax.net.ssl.truststorePassword",trustStorePassword);

    }
    
    public static String getMyProxyServerHost(){readConfig();return myProxyServerHost;}
    public static String getMyProxyServerIdendityAuthorization(){readConfig();return myProxyServerIdendityAuthorization;}
    public static int getMyProxyServerPort(){readConfig();return myProxyServerPort;}
    public static String getTrustStoreFile(){readConfig();return trustStoreFile;}
    public static String getTrustStorePassword(){readConfig();return trustStorePassword;}
    public static String getMyProxyDefaultPassword(){readConfig();return myProxyDefaultPassword;}
    public static String getMyProxyDefaultUserName(){readConfig();return myProxyDefaultUserName;}

    public static String getTrustRootsLocation() {
      return trustRootsLocation;
    }

    
  }
  
  
    

  public static class ExpertContact{
    static String[] addresses={};
    
    public static void doConfig(XMLElement  configReader){
      addresses = configReader.getNodeValue("impactportal.expertcontact.mailaddresses").split(",");
    }
    
    public static String[] getEmailAddresses(){
       readConfig();return  addresses;
    }
  }

  public static class ADAGUCServerConfig{
    
    private static String ADAGUCExecutable="";
    
    private static String[] environmentVariables = {
        };
    
    public static void doConfig(XMLElement  configReader){
      ADAGUCExecutable=configReader.getNodeValue("impactportal.adagucserverconfig.adagucexecutable");
      environmentVariables = configReader.getNodeValues("impactportal.adagucserverconfig.exportenvironment");
    }
    
    public static String[] getADAGUCExecutable() {
      readConfig();
      String commands[]={ADAGUCExecutable};
      return commands;
    }
    
    public static String[] getADAGUCEnvironment() {
      readConfig();
      return environmentVariables;
    }
  }
  
  public static class WPSServicesConfig{
    private static String[] wpsservices = { };
    public static void doConfig(XMLElement  configReader){
      wpsservices = configReader.getNodeValues("impactportal.wpsservices.url");
    }
    public static String[] getWPSServices() {
      readConfig();
      return wpsservices;
    }
  }
  
  public static class PyWPSServerConfig{
    
    private static String PyWPSExecutable="";
    
    private static String[] environmentVariables = { };
    
    public static void doConfig(XMLElement  configReader){
      PyWPSExecutable=configReader.getNodeValue("impactportal.pywpsconfig.pywpsexecutable");
      
      environmentVariables = configReader.getNodeValues("impactportal.pywpsconfig.exportenvironment");
    }
    
    public static String[] getPyWPSExecutable() {
      readConfig();
      String commands[]={PyWPSExecutable};
      return commands;
    }
    
    public static String[] getPyWPSEnvironment() {
      readConfig();
      return environmentVariables;
    }
  }
  public static class DownloadScriptConfig{
    private static String downloadScriptTemplate="";
    
    public static void doConfig(XMLElement configReader){
      downloadScriptTemplate=configReader.getNodeValue("impactportal.downloadscriptconfig.downloadscripttemplate");
    }
    
    public static String getDownloadScriptTemplate() {
      readConfig();
      return downloadScriptTemplate;
    }
  }
  
  public static class DownscalingConfig{
    private static String username;
    private static String password;
    private static String tokenPath;
    private static String tokenFileName;
    private static String dateFormat;
    private static String dpBaseUrl;
    private static String dpBaseRestUrl;
    private static String dpBaseSearchRestUrl;
    
    public static void doConfig(XMLElement  configReader){
      username = configReader.getNodeValue("impactportal.downscaling.credential.username");
      password = configReader.getNodeValue("impactportal.downscaling.credential.password");
      tokenPath = configReader.getNodeValue("impactportal.downscaling.token.path");
      tokenFileName = configReader.getNodeValue("impactportal.downscaling.token.filename");
      dateFormat = configReader.getNodeValue("impactportal.downscaling.token.dateformat");
      dpBaseUrl = configReader.getNodeValue("impactportal.downscaling.dpbaseurl");
      dpBaseRestUrl = configReader.getNodeValue("impactportal.downscaling.dprestbaseurl");
      dpBaseSearchRestUrl = configReader.getNodeValue("impactportal.downscaling.dpbasesearchresturl");
    }
    
    public static String getUsername(){
       readConfig();
       return username;
    }
    public static String getPassword(){
      readConfig();
      return password;
   }
    public static String getTokenPath(){
      readConfig();
      return tokenPath;
   }
    
    public static String getTokenFileName(){
      readConfig();
      return tokenFileName;
   }
    public static String getDateFormat(){
      readConfig();
      return dateFormat;
   }
    public static String getDpBase(){
      readConfig();
      return dpBaseUrl;
   }
    public static String getDpBaseRestUrl(){
      readConfig();
      return dpBaseRestUrl;
   }
    public static String getDpBaseSearchRestUrl() {
      readConfig();
      return dpBaseSearchRestUrl;
    }
  }

  public static String getStatLogfile() {
    return GlobalConfig.getStatLogfile();
  }
}
