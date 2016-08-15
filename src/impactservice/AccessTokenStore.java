package impactservice;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

import impactservice.GenericCart.DataLocator;
import tools.Debug;

public class AccessTokenStore {
  static boolean debug=false;
  /**
   * Map of access_tokens, key is the access_token, value is the stringified JSONObject.
   */
  private static Map<String, String> access_tokens = Collections.synchronizedMap(new TreeMap<String,String>());

  /**
   * Determines wether the store is already loaded from a file.
   */
  private static long isTokenStoreLoaded = -1;//Time at which store was loaded

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

  public static synchronized void loadAccessTokens() throws  JSONException{
    long currentDateMillis = tools.DateFunctions.getCurrentDateInMillis();

    if((currentDateMillis-isTokenStoreLoaded)/1000<10 && isTokenStoreLoaded!=-1){
      //Debug.println("Skipping loadAccessTokens, mssec: "+(currentDateMillis-isTokenStoreLoaded));
      return;
    }
    isTokenStoreLoaded = currentDateMillis;
    String fileName = Configuration.getImpactWorkspace()+"tokens.json";
    Debug.println("loadAccessTokens:"+fileName);
    String accessTokenData = null;
    try{
      accessTokenData = tools.Tools.readFile(fileName);
    }catch(IOException e){
      return;
    }

    JSONObject a = (JSONObject) new JSONTokener(accessTokenData).nextValue();
    @SuppressWarnings("unchecked")
    Iterator<String> b = a.keys();
    while( b.hasNext()){
      String c = (String) b.next();
      //Debug.println(c);
      //Debug.println(a.get(c).toString());
      JSONObject d = a.getJSONObject(c);
      access_tokens.put(c, d.toString());
    }


  }

  public static synchronized void saveAccessTokens() throws JSONException, IOException{
    if(access_tokens.size()==0)return;
    String fileName = Configuration.getImpactWorkspace()+"tokens.json";
    Debug.print("saveAccessTokens:"+fileName+" found nr of tokens: "+access_tokens.size());
    JSONObject a = new JSONObject();
    for (Entry<String, String> param : access_tokens.entrySet()) {
      a.put(param.getKey(),(JSONObject) new JSONTokener(param.getValue()).nextValue());
    }
    tools.Tools.writeFile(fileName, a.toString());
    a = null;
  }

  public static JSONObject getAccessToken(ImpactUser user,long ageValidInSeconds) throws JSONException, IOException{
    Debug.println("GetAccessToken");
    Vector<String> tokens = listtokens(user); 
    for(int j=0;j<tokens.size();j++){
      JSONObject a = (JSONObject) new JSONTokener(tokens.get(j)).nextValue();
      String notafter = a.getString("notafter");
      try {
        long notAfterMillis = tools.DateFunctions.getMillisFromISO8601Date(notafter);
        long currentMillis = tools.DateFunctions.getCurrentDateInMillis();
        Debug.println("notAfterMillis-currentMillis:["+(notAfterMillis-currentMillis)+"]");
        Debug.println("ageValidInSeconds           :["+ageValidInSeconds*1000+"]");
        if(notAfterMillis-currentMillis>ageValidInSeconds*1000){
          return a;
        }
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return generateAccessToken(user);
  }

  public static JSONObject generateAccessToken(ImpactUser user){
    UUID idOne = UUID.randomUUID();
    Debug.println("UUID :"+idOne);
    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put("token", ""+idOne);
      jsonObject.put("userid", ""+user.getUserId());
      if(user.getOpenId()!=null){
        jsonObject.put("openid", ""+user.getOpenId());
      }
      if(user.getEmailAddress()!=null){
        jsonObject.put("email", ""+user.getEmailAddress());
      }
      String currentDate = tools.DateFunctions.getCurrentDateInISO8601();
      jsonObject.put("creationdate", currentDate);
      jsonObject.put("notbefore", currentDate);
      try {
        //jsonObject.put("notafter", tools.DateFunctions.dateAddStepInStringFormat(currentDate, "week"));
        jsonObject.put("notafter", tools.DateFunctions.dateAddStepInStringFormat(currentDate, "hour",10));
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

  public static Vector<String> listtokens(ImpactUser user) throws JSONException, IOException{
    loadAccessTokens();
    class TokenObject{
      public TokenObject(JSONObject jsonTokenizedToken) throws JSONException {
        token = jsonTokenizedToken.toString();
        creationdate = jsonTokenizedToken.getString("creationdate");
      }
      String token;
      String creationdate;
    }
    Vector<TokenObject> tokenObjectListUnsorted = new Vector<TokenObject>();
    Vector<String> tokensToRemove = new Vector<String>();
    for (Entry<String, String> param : access_tokens.entrySet()) {
      JSONObject jsonTokenizedToken = (JSONObject) new JSONTokener(param.getValue()).nextValue();
      try{

        String token = null;

        token = _checkIfTokenIsValid(param.getKey());

        if(token!=null){
          String userid = jsonTokenizedToken.getString("userid");
          if(userid.equals(user.getUserId())){
            TokenObject tokenObj = new TokenObject(jsonTokenizedToken);
            tokenObjectListUnsorted.add(tokenObj);
          }
        }else{
          tokensToRemove.add(param.getKey());
        }
      }catch(JSONException e){
        try {
          MessagePrinters.emailFatalErrorException("Access token store is corrupt", e);
        } catch (IOException e1) {
          e1.printStackTrace();
        }
        e.printStackTrace();
      }
    }
    //Remove tokens:
    for(int j=0;j<tokensToRemove.size();j++){
      Debug.println("Removing token "+tokensToRemove.get(j)+" for user "+user.getUserId());
      access_tokens.remove(tokensToRemove.get(j));
    }
    if(tokensToRemove.size()>0){
      saveAccessTokens();
    }
    
    TokenObject[] tokenObjectArray = (TokenObject[]) tokenObjectListUnsorted.toArray(new TokenObject[tokenObjectListUnsorted.size()]);
    
    Arrays.sort(tokenObjectArray, new Comparator<TokenObject>(){
      public int compare(TokenObject o1, TokenObject o2) {
        return o2.creationdate.compareTo(o1.creationdate);
      }
    });

    
    Vector<String> tokenListSorted = new Vector<String>();
    for(int j=0;j<tokenObjectArray.length;j++){
      tokenListSorted.add(tokenObjectArray[j].token);
    }
    return tokenListSorted;
  }

  private static String _checkIfTokenIsValid(String token){
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
        //Debug.println(v);
        a = (JSONObject) new JSONTokener(v).nextValue();
        //Debug.println(a.toString());
        String notbefore = a.getString("notbefore");
        String notafter = a.getString("notafter");
        long notbeforeMillis = tools.DateFunctions.getMillisFromISO8601Date(notbefore);
        long notafterMillis = tools.DateFunctions.getMillisFromISO8601Date(notafter);
        long currentMillis = tools.DateFunctions.getCurrentDateInMillis();
        if(notbeforeMillis>currentMillis){
          throw new AccessTokenIsNotYetValid();
        }
        if(notafterMillis<currentMillis){
          throw new AccessTokenHasExpired();
        }
        //
        //        Debug.println("Token is valid, now check cert");
        //        ImpactUser user = LoginManager.getUser(a.getString("userid"), null);
        //        LoginManager.checkLogin(user);

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
    //Debug.println("Check if token is Valid");
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
      //Debug.println("getPathInfo: "+path);
      if(path.indexOf(".")!=-1){
        return null;
      }


      try {
        String a = AccessTokenStore._checkIfTokenIsValid(path);
        if(a==null){
          Debug.println("[NOPE] Token is not Valid");
          return null;
        }

        JSONObject props = (JSONObject) new JSONTokener(a).nextValue();
        String userID = props.getString("userid");
        if(debug)Debug.println("[OK] Token is Valid for user "+userID);
        ImpactUser user = LoginManager.getUser(userID, null);
        LoginManager.checkLogin(user);
        return props;
      } catch (Exception e) {
        Debug.println("[NOPE:EXECPTION] Token is not Valid");
      }

    }
    return null;
  }

  public static JSONObject revokeAccessToken(ImpactUser user, String token) throws IOException {
    JSONObject jsonObject = new JSONObject();
    try{
      try {
        loadAccessTokens();
      } catch (JSONException e1) {
        jsonObject.put("message", "unable to load access tokens");
        jsonObject.put("status", "failed");
        return jsonObject;
      }

      String v = access_tokens.get(token);
      if(v==null){
        jsonObject.put("message", "token not found.");
        jsonObject.put("status", "failed");
      }else{
        JSONObject props = null;
        props = (JSONObject) new JSONTokener(v).nextValue();
        String userID = null;
        try {
          userID = props.getString("userid");
        } catch (JSONException e1) {
        }

        if(user.getUserId().equals(userID)==false){
          jsonObject.put("message", "Token user ID does not match your user ID.");
          jsonObject.put("status", "failed");
        }else{
          access_tokens.remove(token);
          saveAccessTokens();
          jsonObject.put("message", "Removed token for user ["+userID+"].");
          jsonObject.put("status", "ok");
        }
      } 
    } catch (JSONException e) {
    }
    return jsonObject;
  }
}
