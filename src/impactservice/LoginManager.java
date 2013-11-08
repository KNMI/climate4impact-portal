package impactservice;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.ietf.jgss.Oid;

import tools.DebugConsole;
import tools.Tools;

/**
 * @author maartenplieger
 * 
 * Info:
 * http://www.globus.org/cog/distribution/1.2/api/org/globus/myproxy/MyProxy.html
 * Command line SLC retrieval:
 * myproxy-logon -s bvlpenes.knmi.nl -k https://pcmdi3.llnl.gov/esgcet/myopenid/<user>
 * 
 * To be able to login on ESGF openid providers, their trustroots need to be set. This can be done by adding the following arguments to ECLIPSE,
 * or to JAVA_OPTS in production environment:
 * 
 * (https://meteo.unican.es/trac/wiki/ESGF-Security)
 * 
 * wget https://rainbow.llnl.gov/dist/certs/esg-truststore.ts
 * 
 * In: Run --> Run Configurations --> Arguments --> VM Arguments, add:
 * -Djavax.net.ssl.trustStore="<pathto>esg-truststore.ts" -Djavax.net.ssl.trustStorePassword="changeit"
 * Or add these arguments to JAVA_OPTS in production environment
 * 
 * 
 * # Or Create a custom java trust store file with a shell script:
 * echo | openssl s_client -connect pcmdi9.llnl.gov:443  2>&1 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p'  > pcmdi9.llnl.gov
 * keytool -delete -alias pcmdi9.llnl.gov  -keystore openidcerts.ts -storepass changeit
 * keytool -import -v -trustcacerts -alias pcmdi9.llnl.gov -file pcmdi9.llnl.gov -keystore openidcerts.ts -storepass changeit -noprompt
 * 
 * 
 * 
 * 
 */

public class LoginManager {
  static Vector<User> users = new Vector<User>();
  
  /**
   * Retrieves a SLC (short lived credential) from the SLCS and stores it in the users home directory 
   * @param user User object with identifier and home directory set
   * @throws Exception
   */
  public synchronized static void getCredential(User user) throws Exception{
    
    if(Configuration.GlobalConfig.isInOfflineMode()==true){
      DebugConsole.println("offline mode");
      return;
    }
    MyProxy myProxy = new MyProxy(Configuration.LoginConfig.getMyProxyServerHost(),Configuration.LoginConfig.getMyProxyServerPort());
    //myProxy.setHost(Configuration.LoginConfig.getMyProxyServerHost());
    //myProxy.setPort(Configuration.LoginConfig.getMyProxyServerPort());
    
    DebugConsole.println("Setting proxy host:port as '"+Configuration.LoginConfig.getMyProxyServerHost()+":"+Configuration.LoginConfig.getMyProxyServerPort()+"'");
    //GSSCredential retcred = myProxy.get(null,user.id, Configuration.LoginConfig.getMyProxyDefaultPassword(), 60*60*24);
    try {
      String userName = Configuration.LoginConfig.getMyProxyDefaultUserName();
      if(userName == null){
        userName = user.id;
      }
      
      DebugConsole.println("Setting username to "+userName+":"+Configuration.LoginConfig.getMyProxyDefaultPassword());
      
      ExtendedGSSCredential cred= (ExtendedGSSCredential) myProxy.get(userName, Configuration.LoginConfig.getMyProxyDefaultPassword(), 60*60*24);
     
      try {
        DebugConsole.println(cred.getName().toString());
       // DebugConsole.println(""+cred.getRemainingLifetime());
        
        Oid [] mechs = cred.getMechs();
        if (mechs != null) {
                for (int i = 0; i < mechs.length; i++)
                        DebugConsole.println(mechs[i].toString());
        }
        
        //Export credential to file
        byte [] data = cred.export(ExtendedGSSCredential.IMPEXP_OPAQUE);
        FileOutputStream out = new FileOutputStream(user.certificateFile);
        out.write(data);
        out.close();
        // release system resources held by the credential
        cred.dispose();

      } catch (Exception e) {
        DebugConsole.printStackTrace(e);
        throw new Exception("LoginManager: Unable to write credential");
      }
    } catch (MyProxyException e) {
      String msg="Unable to get credential for "+user.id;
      DebugConsole.errprintln(msg);
      DebugConsole.printStackTrace(e);
      throw new Exception("LoginManager: "+msg);
    }
    DebugConsole.println("Credentials for user "+user.id+" retrieved");
  }
  
 /**
  * Get the user object based on the http session
  * @param request The httpservletrequest
  * @return The user object
  * @throws Exception
  */
  public static User getUser(HttpServletRequest request) throws Exception {
    /*try{
 
    X509Certificate certs[] = 
        (X509Certificate[])request.getAttribute("javax.servlet.request.X509Certificate");
    // TODO ... Test if non-null, non-empty.

    X509Certificate clientCert = certs[0];

    // Get the Subject DN's X500Principal
    X500Principal subjectDN = clientCert.getSubjectX500Principal();
    String dn = subjectDN.getName();
    DebugConsole.println("DN: "+dn);
    }catch(Exception e){
      e.printStackTrace();
    }*/
    HttpSession session = request.getSession();
    String id=(String) session.getAttribute("openid_identifier");
    if(Configuration.GlobalConfig.isInOfflineMode()==true){
      id=Configuration.GlobalConfig.getDefaultUser();
    }
    DebugConsole.println("Getting user from session with id "+id);
    if(id==null){throw new Exception("You are not logged in...");}
    User user = getUser(id);
    return user;
  }

  /**
   * Get user based on his/hers userId
   * @param userId The userID, equal to the OpenID identifier
   * @return The user object
   */
  public synchronized static User getUser(String userId){
    DebugConsole.println("Looking up user "+userId);
    //Lookup the user in the vector list
    if(userId==null)return null;
    for(int j=0;j<users.size();j++){
      if(users.get(j).id.equals(userId)){
        User user = users.get(j);
        DebugConsole.println("Found existing user "+userId);
        return user;
      }
    }
    //The user was not found, so create a new user
    DebugConsole.println("Creating new user object for "+userId);
    User user = new User();
    user.id=userId;
    users.add(user);
    try {checkLogin(userId);} catch (Exception e) { }
    
    return user;
  }
  
  /**
   * Called upon succesfull login, handles and checks user ID
   * @param session
   * @throws Exception 
   */
  public synchronized static void checkLogin(String openIdIdentifier) throws Exception{

    DebugConsole.println("checkLogin "+openIdIdentifier);
    if(openIdIdentifier==null){
      DebugConsole.errprintln("No openIdIdentifier given");
    }
    User user = getUser(openIdIdentifier);
    
    DebugConsole.println("Check login "+user.id);
    user.internalName = user.id.replace("http://", "");
    user.internalName = user.internalName.replace("https://", "");
    user.internalName = user.internalName.replaceAll("/", ".");
    DebugConsole.println("internalName = "+user.internalName);

    user.usersDir = Configuration.getImpactWorkspace()+user.internalName+"/";
    try {
      DebugConsole.println("Making dir "+user.usersDir);
      Tools.mkdir(user.usersDir);
      Tools.mkdir(user.usersDir+"certs");
      user.certificateFile = user.usersDir+"certs/"+"creds.pem";
    } catch (IOException e) {
      DebugConsole.errprintln(e.getMessage());
      user.credentialError=true;
      throw new Exception("Unable to create credential for user, server misconfiguration:"+user.id+"\n"+e.getMessage());
    }
    try{
    getCredential(user);
    }catch(Exception e){
      user.credentialError=true;
      throw new Exception("Unable to get credential for user "+user.id+"\n"+e.getMessage());
    }
    
    createNCResourceFile(user);
    //createFontConfigFile(user);
    user.configured = true;
  }
  
  /**
   * Create NetCDF .httprc or .dodsrc resource file and store it in the users home directory
   * @param user The user object
   * @throws IOException
   */
  public synchronized static void createNCResourceFile(User user) throws IOException{
    //DebugConsole.println("createNCResourceFile for user "+user.id);
    /*
      .httprc/.dodsrc file contents:
      HTTP.SSL.VALIDATE=0
      HTTP.COOKIEJAR=.dods_cookies
      HTTP.SSL.CERTIFICATE=<slc.pem>
      HTTP.SSL.KEY=<slc.pem>
      HTTP.SSL.CAPATH=<esg_trusted_certificates>
   */  

    String fileContents =
        "HTTP.SSL.VALIDATE=0\n"
        +"HTTP.COOKIEJAR="+user.usersDir+"/.dods_cookies\n"
        +"HTTP.SSL.CERTIFICATE="+user.certificateFile+"\n"
        +"HTTP.SSL.KEY="+user.certificateFile+"\n"
        +"HTTP.SSL.CAPATH="+ Configuration.getImpactWorkspace()+"/esg_trusted_certificates/";
    DebugConsole.println("createNCResourceFile for user "+user.id+":\n"+fileContents);
    Tools.writeFile(user.usersDir+"/.httprc", fileContents) ;
    Tools.writeFile(user.usersDir+"/.dodsrc", fileContents) ;
 }
 
}
