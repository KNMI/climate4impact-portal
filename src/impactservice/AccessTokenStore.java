package impactservice;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import tools.Debug;

public class AccessTokenStore {
  /**
   * Map of access_tokens, key is the access_token, value is the stringified JSONObject.
   */
  private static Map<String,String> access_tokens= new TreeMap<String,String>();
  /**
   * Determines wether the store is already loaded from a file.
   */
  private static boolean isTokenStoreLoaded = false;
  
  public static class AccessTokenHasExpired extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
  
  }
  
  public static class AccessTokenIsNotYetValid extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
  
  }
  
  public static void loadAccessTokens() throws  JSONException{
    if(isTokenStoreLoaded == true)return;
    String fileName = Configuration.getImpactWorkspace()+"tokens.json";
    Debug.print("loadAccessTokens:"+fileName);
    String accessTokenData = null;
    try{
      accessTokenData = tools.Tools.readFile(fileName);
    }catch(IOException e){
      return;
    }
    
    JSONObject a = (JSONObject) new JSONTokener(accessTokenData).nextValue();
    Iterator<String> b = a.keys();
    while( b.hasNext()){
      String c = (String) b.next();
      Debug.println(c);
      Debug.println(a.get(c).toString());
      JSONObject d = a.getJSONObject(c);
      access_tokens.put(c, d.toString());
    }
    //isTokenStoreLoaded = true;
  }
  
  public static void saveAccessTokens() throws JSONException, IOException{
    loadAccessTokens();
    String fileName = Configuration.getImpactWorkspace()+"tokens.json";
    Debug.print("saveAccessTokens:"+fileName);
      JSONObject a = new JSONObject();
      for (Entry<String, String> param : access_tokens.entrySet()) {
        a.put(param.getKey(),(JSONObject) new JSONTokener(param.getValue()).nextValue());
      }
      tools.Tools.writeFile(fileName, a.toString());
      a = null;
  }
  
  public static JSONObject generateAccessToken(ImpactUser user){
    UUID idOne = UUID.randomUUID();
    Debug.println("UUID :"+idOne);
    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put("token", ""+idOne);
      jsonObject.put("userid", ""+user.getId());
      String currentDate = tools.DateFunctions.getCurrentDateInISO8601();
      jsonObject.put("creationdate", currentDate);
      jsonObject.put("notbefore", currentDate);
      try {
        jsonObject.put("notafter", tools.DateFunctions.dateAddStepInStringFormat(currentDate, "week"));
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
    } catch (JSONException e) {
    }
    String token = jsonObject.toString();

    access_tokens.put(""+idOne, token);
    try {
      saveAccessTokens();
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return jsonObject;
  }

  public static Vector<String> listtokens(ImpactUser user) throws JSONException, IOException, AccessTokenIsNotYetValid, AccessTokenHasExpired {
    loadAccessTokens();
    Vector<String> tokenList = new Vector<String>();
    for (Entry<String, String> param : access_tokens.entrySet()) {
      JSONObject o = (JSONObject) new JSONTokener(param.getValue()).nextValue();
      try{
        String userid = o.getString("userid");
        if(userid.equals(user.getId())){
          if(checkIfTokenIsValid(param.getKey())!=null){
            tokenList.add(o.toString());
          }else{
            Debug.println("Should remove this token!");
          }
        }
      }catch(JSONException e){
        MessagePrinters.emailFatalErrorException("Access token store is corrupt", e);
        e.printStackTrace();
      }
    }
    return tokenList;
  }
  
  public static String checkIfTokenIsValid(String token) throws AccessTokenIsNotYetValid, AccessTokenHasExpired{
    try {
      loadAccessTokens();
    } catch (JSONException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    String v = access_tokens.get(token);
    if(v!=null){
      JSONObject a;
      try {
        Debug.println(v);
        a = (JSONObject) new JSONTokener(v).nextValue();
        Debug.println(a.toString());
        String notbefore = a.getString("notbefore");
        String notafter = a.getString("notafter");
        long notbeforeMillis = tools.DateFunctions.getMillisFromISO8601Date(notbefore);
        long notafterMillis = tools.DateFunctions.getMillisFromISO8601Date(notafter);
        long currentMillis = tools.DateFunctions.getCurrentDateInMillis();
        Debug.println("notbeforeMillis: "+notbeforeMillis);
        Debug.println("currentMillis: "+currentMillis);
        Debug.println("notafterMillis: "+notafterMillis);
        if(notbeforeMillis>currentMillis){
          throw new AccessTokenIsNotYetValid();
        }
        if(notafterMillis<currentMillis){
          throw new AccessTokenHasExpired();
        }
        return v;
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      
      a = null;
    }
    
    return null;
  }

  public static JSONObject checkIfTokenIsValid(HttpServletRequest request) throws AccessTokenIsNotYetValid, AccessTokenHasExpired {
    String pathInfo = request.getPathInfo();
    if(pathInfo!=null){
      String[] paths = pathInfo.split("/");
      String path  = null;
     
      //Strip access code from path and build a new path
      for(int j=0;j<paths.length&&j<2;j++){
        if(path==null){
          if(paths[j].length()>1){
            path = paths[j];
          }
        }
      }
      path = path.replaceAll("/", "");
      Debug.println("getPathInfo: "+path);
      try{
        return (JSONObject) new JSONTokener(AccessTokenStore.checkIfTokenIsValid(path)).nextValue();
      }catch(AccessTokenIsNotYetValid e){
        throw e;
      }catch(AccessTokenHasExpired e){
        throw e;
      }catch(Exception e){
        //e.printStackTrace();
      }
    }
    return null;
  }
}
