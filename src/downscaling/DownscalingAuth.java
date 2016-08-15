package downscaling;

import impactservice.Configuration;
import impactservice.ImpactUser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import tools.Debug;


/**
 * Servlet implementation class ImpactService
 */
public class DownscalingAuth{
  public static final String CONTENT_TYPE_JSON = "application/json";
  public static enum responseStatus {OK, CREATED, BAD_REQUEST, NOT_FOUND};
  /**
   * @see HttpServlet#HttpServlet()
   */
  public DownscalingAuth() {
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
    urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf8");
    urlConn.setRequestProperty("Accept", "application/json");
    urlConn.setRequestProperty("token", getToken());
    urlConn.setDoOutput(true);
    return urlConn;
  }
  
  protected static InputStream getConnectionInputStream(String URI, String type) throws IOException{
    return prepareQuery(URI, type).getInputStream();
  }
  
  protected static HttpsURLConnection prepareSecureQuery(String URI, String type) throws IOException{
    Debug.println("Subscribe user -- Building statement for URI: " + URI);
    URL url;
    HttpsURLConnection urlConn;
    url = new URL(URI);
    urlConn = (HttpsURLConnection)url.openConnection();
    urlConn.setRequestMethod(type);
    urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf8");
    urlConn.setRequestProperty("Accept", "application/json");
    urlConn.setRequestProperty("token", getToken());
    urlConn.setDoOutput(true);
    return urlConn;
  }
  
  protected static HttpURLConnection prepareSimpleQuery(String URI, String type) throws IOException{
    Debug.println("Building statement for URI: " + URI);
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
      File file = new File(Configuration.DownscalingConfig.getTokenPath()+"/"+Configuration.DownscalingConfig.getTokenFileName());
      if(!file.exists())
        authenticate();
      scanner = new Scanner(file);
      while(scanner.hasNextLine() && !isValid){
        String line = scanner.nextLine();
        String[] parts = line.split("=");
        String key = parts[0];
        if(key.equals("expiration")){
          SimpleDateFormat formatter = new SimpleDateFormat(Configuration.DownscalingConfig.getDateFormat());
          try {
            expiration = formatter.parse(parts[1]);
            Date now = new Date();
            if(expiration.compareTo(now)<0) //now before expiration: valid
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
      url = new URL(Configuration.DownscalingConfig.getDpBaseRestUrl() + "/authenticate");
      urlConn = (HttpURLConnection)url.openConnection();
      urlConn.setRequestMethod("GET");
      String urlParameters = "username="+Configuration.DownscalingConfig.getUsername()+"&password="+Configuration.DownscalingConfig.getPassword();
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
      File directory = new File(Configuration.DownscalingConfig.getTokenPath());
      if(!directory.exists())
        directory.mkdirs();
      String tokenFilePath = Configuration.DownscalingConfig.getTokenPath() + "/" + Configuration.DownscalingConfig.getTokenFileName();
      
      File tokenFile = new File(tokenFilePath);
      if(!tokenFile.exists()){
        Debug.println("Creating token file "+tokenFilePath);
        tokenFile.createNewFile();
      }
      file = new FileWriter(tokenFilePath);
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
    if(getUser(user, user.getUserId()) != null)
      return true;
    return false;
          
  }
  
  protected static ImpactUser getUser(ImpactUser user, String username) throws ServletException, IOException{
    HttpURLConnection urlConn = prepareQuery(Configuration.DownscalingConfig.getDpBaseRestUrl() + "/users/" + username, "GET");
    if(urlConn.getResponseCode() == HttpStatus.SC_SERVICE_UNAVAILABLE)
      throw new ServletException("[CODE " + HttpStatus.SC_SERVICE_UNAVAILABLE + "] Downscaling Portal Service temporarily unavailable");
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
      JSONObject jsonResponse = new JSONObject(response.toString());
      String internalName;
      if(jsonResponse.getString("responseStatus").equals(responseStatus.OK.toString()) && jsonResponse.optJSONObject("value") != null){
        internalName = jsonResponse.getJSONObject("value").getString("username");
        if(internalName.equals(user.getUserId())){
          Debug.println("User found in DP: " + internalName);
          return user;
        }
      } 
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return null;
  }

}

