package impactservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.Predictand;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Servlet implementation class ImpactService
 */
public class DownscalingServiceDeprecated extends HttpServlet {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * @see HttpServlet#HttpServlet()
   */
  public DownscalingServiceDeprecated() {
      super();
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
    String pathInfo = request.getPathInfo();
    if(pathInfo.matches("/users/.*/predictands")){
      HttpURLConnection urlConn = DownscalingAuthDeprecated.prepareQuery(DownscalingAuthDeprecated.BASE_DP_REST_URL + pathInfo + "?" + request.getQueryString(), "GET");
      BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
      StringBuilder sb = new StringBuilder();
      String inputLine;
      while ((inputLine = in.readLine()) != null) 
        sb.append(inputLine);
      in.close();
      response.setContentType("application/json");
      // Get the printwriter object from response to write the required json object to the output stream      
      PrintWriter out = response.getWriter();
      // Assuming your json object is **jsonObject**, perform the following, it will return your json object  
      out.print(sb);
      out.flush();
    }else if(pathInfo.matches("/users/.*/zone/.*/predictors/.*/predictands/.*")){
      HttpURLConnection urlConn = DownscalingAuthDeprecated.prepareQuery(DownscalingAuthDeprecated.BASE_DP_REST_URL + pathInfo, "GET");
      BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
      StringBuilder sb = new StringBuilder();
      String inputLine;
      while ((inputLine = in.readLine()) != null) 
        sb.append(inputLine);
      in.close();
      response.setContentType("application/json");
      // Get the printwriter object from response to write the required json object to the output stream      
      PrintWriter out = response.getWriter();
      // Assuming your json object is **jsonObject**, perform the following, it will return your json object  
      out.print(sb);
      out.flush();
    }else if(pathInfo.matches("/users/.*/zone/.*/predictands/.*/downscalingMethods")){
      HttpURLConnection urlConn = DownscalingAuthDeprecated.prepareQuery(DownscalingAuthDeprecated.BASE_DP_REST_URL + pathInfo, "GET");
      BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
      StringBuilder sb = new StringBuilder();
      String inputLine;
      while ((inputLine = in.readLine()) != null) 
        sb.append(inputLine);
      in.close();
      response.setContentType("application/json");
      // Get the printwriter object from response to write the required json object to the output stream      
      PrintWriter out = response.getWriter();
      // Assuming your json object is **jsonObject**, perform the following, it will return your json object  
      out.print(sb);
      out.flush();
    }else if(pathInfo.matches("/variables")){
      HttpURLConnection urlConn = DownscalingAuthDeprecated.prepareQuery(DownscalingAuthDeprecated.BASE_DP_REST_URL + pathInfo + "?variableType=" + request.getParameter("variableType"), "GET");
      BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
      StringBuilder sb = new StringBuilder();
      String inputLine;
      while ((inputLine = in.readLine()) != null) 
        sb.append(inputLine);
      in.close();
      response.setContentType("application/json");
      // Get the printwriter object from response to write the required json object to the output stream      
      PrintWriter out = response.getWriter();
      // Assuming your json object is **jsonObject**, perform the following, it will return your json object  
      out.print(sb);
      out.flush();
    }else if(pathInfo.matches("/validation")){
      HttpURLConnection urlConn = DownscalingAuthDeprecated.prepareSimpleQuery(DownscalingAuthDeprecated.BASE_DP_REST_URL + pathInfo + "?idZone=" + 
              request.getParameter("idZone") +"&predictandName="+request.getParameter("predictandName")+"&downscalingMethod="+request.getParameter("downscalingMethod"), "GET");
      OutputStream out = response.getOutputStream();
      InputStream in = urlConn.getInputStream();
      byte[] buffer = new byte[4096];
      int length;
      while ((length = in.read(buffer)) > 0){
          out.write(buffer, 0, length);
      }
      in.close();
      out.flush();
      response.setContentType("application/pdf");
      response.setHeader("Content-Disposition", "attachment; filename=output.pdf");
    }
  }
    
  public static List<Predictand> getUserPredictands(String username)throws ServletException, IOException, JSONException{
    HttpURLConnection urlConn = DownscalingAuthDeprecated.prepareQuery(DownscalingAuthDeprecated.BASE_DP_REST_URL + "/users/" + username + "/predictands", "GET");
    if(urlConn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
      return null;
    StringBuffer response = new StringBuffer();
    BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
    String inputLine;
    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine);
    }
    in.close();
    JSONObject myObject = new JSONObject(response.toString());
    JSONArray jsonPredictands = myObject.getJSONArray("values");
    List<Predictand> predictands = new ArrayList<Predictand>();
    for(int i=0; i< jsonPredictands.length(); i++){
      Predictand p = new Predictand.PredictandBuilder(jsonPredictands.getJSONObject(i)).build();
      predictands.add(p);
    }
    return predictands;
  }
  
  public static List<String> getVariableTypes()throws ServletException, IOException, JSONException{
    HttpURLConnection urlConn = DownscalingAuthDeprecated.prepareQuery(DownscalingAuthDeprecated.BASE_DP_REST_URL + "/variables/types", "GET");
    if(urlConn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
      return null;
    StringBuffer response = new StringBuffer();
    BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
    String inputLine;
    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine);
    }
    in.close();
    JSONObject myObject = new JSONObject(response.toString());
    JSONArray jsonVariableTypes = myObject.getJSONArray("values");
    List<String> variableTypes = new ArrayList<String>();
    for(int i=0; i< jsonVariableTypes.length(); i++){
      variableTypes.add(jsonVariableTypes.getString(i));
    }
    return variableTypes;
  }


  /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	  String pathInfo = request.getPathInfo();
//	  ImpactUser loggedInUser = LoginManager.getUser(request);
    if(pathInfo.equals("/subscribe")){//Configuration.getImpactWorkspace()
      HttpURLConnection urlConn = DownscalingAuthDeprecated.prepareQuery(DownscalingAuthDeprecated.BASE_DP_REST_URL + "/users", "POST");
      JSONObject user = new JSONObject();
      try {
        user.put("username", request.getParameter("username"));
        user.put("password", "");
        user.put("email", request.getParameter("email"));
        user.put("openID", "");
      } catch (JSONException e) {
        e.printStackTrace();
      }
      OutputStreamWriter wr= new OutputStreamWriter(urlConn.getOutputStream());
      wr.write(user.toString());
      wr.flush();
      wr.close();
      if(urlConn.getResponseCode() ==HttpURLConnection.HTTP_CREATED){
          System.out.println("201 - User created");  
      }else{
          System.out.println(urlConn.getResponseMessage());  
      }  
    }
	}
}

