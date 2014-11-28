package impactservice;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.json.JSONException;
import org.json.JSONObject;

import tools.Debug;


/**
 * Servlet implementation class ImpactService
 */
public class DownscalingAuthDeprecated{
  public static final String CONTENT_TYPE_JSON = "application/json";
  public static final String C4I_USER = "climate4impact";
  public static final String C4I_PASSWORD = "climate4impact";
  public static final String BASE_DP__URL = "http://10.0.2.2:8080/dp/";
  public static final String BASE_DP_REST_URL = "http://10.0.2.2:8080/dp/rest";
  public static final String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss z";
  public static final String C4I_CREDENTIAL_PATH = Configuration.getDownscalingPortalWorkspace()+"downscaling.cred";
  /**
   * @see HttpServlet#HttpServlet()
   */
  public DownscalingAuthDeprecated() {
  }

  /*
   * Fill necessary headers to request DP
   */
  protected static HttpURLConnection prepareQuery(String URI, String type) throws IOException{
    Debug.println("Subscribe user -- Building statement for URI: " + URI);
    URL url;
    HttpURLConnection urlConn;
    url = new URL(URI);
    urlConn = (HttpURLConnection)url.openConnection();
    urlConn.setRequestMethod(type);
    urlConn.setRequestProperty("Content-Type", "application/json; charset=utf8");
    urlConn.setRequestProperty("Accept", "application/json");
    urlConn.setRequestProperty("token", getToken());
    urlConn.setDoOutput(true);
    return urlConn;
  }
  
  protected static HttpURLConnection prepareSimpleQuery(String URI, String type) throws IOException{
    Debug.println("Subscribe user -- Building statement for URI: " + URI);
    URL url;
    HttpURLConnection urlConn;
    url = new URL(URI);
    urlConn = (HttpURLConnection)url.openConnection();
    urlConn.setRequestMethod(type);
    urlConn.setRequestProperty("Accept", "application/pdf");
    urlConn.setRequestProperty("token", getToken());
    urlConn.setDoOutput(true);
    return urlConn;
  }
  
  /*
   * Returns a valid Token
   */
  protected static String getToken(){
    Date expiration;
    boolean isValid = false;
    String token = "";
    Scanner scanner = null;
    try {
      scanner = new Scanner(new File(C4I_CREDENTIAL_PATH));
      while(scanner.hasNextLine() && !isValid){
        String line = scanner.nextLine();
        String[] parts = line.split("=");
        String key = parts[0];
        if(key.equals("expiration")){
          SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
          try {
            expiration = formatter.parse(parts[1]);
            Date now = new Date();
            if(now.compareTo(expiration)<0) //now before expiration: valid
              isValid=true;
          } catch (ParseException e) {
            isValid = false;
          }
        }else if(key.equals("token")){
          token = parts[1];
        }
      }
    } catch (FileNotFoundException e) {
      token = authenticate();
    }finally{
      scanner.close();
    }
    if(!isValid)
      token = authenticate();
    return token;
  }

  /*
   * Returns a new valid token
   */

  protected static String authenticate(){
    //send POST
    URL url;
    HttpURLConnection urlConn;
    String token = null;
    try{
      url = new URL(BASE_DP_REST_URL + "/authenticate");
      urlConn = (HttpURLConnection)url.openConnection();
      urlConn.setRequestMethod("GET");
      String urlParameters = "username="+C4I_USER+"&password="+C4I_PASSWORD;
      urlConn.setDoOutput(true);
      DataOutputStream wr = new DataOutputStream(urlConn.getOutputStream());
      wr.writeBytes(urlParameters);
      wr.flush();
      wr.close();
      //int responseCode = urlConn.getResponseCode();
      token = urlConn.getHeaderField("token");
      String expiration =  urlConn.getHeaderField("expiration");
      storeCredential("token="+token+"\nexpiration="+expiration);
    }catch(IOException e){
      return null;
    }
    return token;
  }
    
  protected static void storeCredential(String content){
    FileWriter file = null;
    PrintWriter pw = null;
    try
    {
      file = new FileWriter(C4I_CREDENTIAL_PATH);
      pw = new PrintWriter(file);
      pw.println(content);
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
       try {
         if (null != file)
            file.close();
         } catch (Exception e2) {
            e2.printStackTrace();
         }
    }
  }
  
  public static boolean isUserSubscribed(ImpactUser user)throws ServletException, IOException{
    if(getUser(user, user.getInternalName()) != null)
      return true;
    return false;
          
  }
  
  protected static ImpactUser getUser(ImpactUser user, String username) throws ServletException, IOException{
    HttpURLConnection urlConn = prepareQuery(DownscalingAuthDeprecated.BASE_DP_REST_URL + "/users/" + username, "GET");
    if(urlConn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
        return null;
    try {
      StringBuffer response = new StringBuffer();
      BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();
      JSONObject myObject = new JSONObject(response.toString());
      String internalName = myObject.getJSONObject("value").getString("username");
      //if(!myObject.getJSONObject("responseCode").toString().equals("OK"))
        
      if(internalName.equals(user.getInternalName()))
        return user;
      //myObject.getJSONObject("responseCode");
      
      //LoginManager.get TODO
      //myObject.getJSONObject("value").getString("openID");
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

}

