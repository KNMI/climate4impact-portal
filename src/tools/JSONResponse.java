package tools;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import org.json.JSONArray;

/**
 *  @author Maarten Plieger - KNMI
 *  
 *  Helper class for creating json and jsonp responses
 *  
 *  Example usages:
   
    JSONResponse jsonResponse = new JSONResponse(request);

    try{
      jsonResponse.setMessage("bla");
    } catch(Exception e){
      jsonResponse.setException("nice error message",e);
    }
    
    try {
      jsonResponse.print(response);
    } catch (Exception e1) {
    
    }

 *
 */
public class JSONResponse {
  
  public JSONResponse(HttpServletRequest request,String userId){
    setJSONP(request);
    this.userId = userId;
    
  }  
  
  public JSONResponse(HttpServletRequest request){
    setJSONP(request);
    this.userId = null;    
  }
  
  
  public JSONResponse(){
  }
  
  public void setJSONP(HttpServletRequest request) {
    try{
      jsonp=HTTPTools.getHTTPParam(request,"jsonp");
    }catch (Exception e) {
      try{
        jsonp=HTTPTools.getHTTPParam(request,"callback");
      }catch (Exception e1) {
      }
    }
  }
  public String getMimeType() {
    return mimetype;
  }
  public void setJSONP(String jsonp) {
    this.jsonp = jsonp;
    
  }
  public void disableJSONP() {
    this.jsonp = null;
    
  }

  private String userId = null;
  private String message = "";
  private String mimetype = "application/javascript";
  private String jsonp = null;
  private int statusCode = 200;
  private String redirectURL = null;
  private String errorMessage = null;
  boolean hasError = false; 
  public String getMessage(){
    if(message.length()==0){
      setErrorMessage("JSONResponse: no message set",200);
    }
    if(jsonp!=null){
      if(jsonp.length()>0){
        return jsonp+"("+message+");";
      }
    }
    return message;
  }
  public void setMessage(String message){
    if(this.hasError){
      Debug.errprintln("Message can not be set because error has occured");
    }else{
      this.message = message;
    }
  }

  public void setException(String string, Exception e2) {
    setException(string,e2,null);
  }
  public void setException(String string, Exception e2, String url) {
    if(e2.getClass() == tools.HTTPTools.WebRequestBadStatusException.class){
      this.statusCode = ((tools.HTTPTools.WebRequestBadStatusException)e2).getStatusCode();
    }
    JSONObject error = new JSONObject();
    try {
      error.put("error", string);
      error.put("statuscode", this.statusCode);
      error.put("exception", e2.getMessage());
      if(redirectURL!=null){error.put("redirect", this.redirectURL);}
      if(this.userId!=null){error.put("userid", this.userId);}  
      if(url!=null){
        error.put("url", url);
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
    this.message = error.toString();
    this.hasError = true;
  }
  
  public void setErrorMessage(String errorMessage, int statusCode){
    setErrorMessage(errorMessage, statusCode,null,null,null);
  }
  public void setErrorMessage(String errorMessage, int statusCode,String redirectURL,String currentPage,String resource){
    JSONObject error = new JSONObject();
    this.errorMessage = errorMessage;
    this.statusCode = statusCode;
    this.redirectURL = redirectURL;
    try {
      error.put("error", this.errorMessage);
      error.put("statuscode", this.statusCode);
      if(redirectURL!=null){error.put("redirect", this.redirectURL);}
      if(currentPage!=null){error.put("current", currentPage);}
      if(resource!=null){error.put("resource", resource);}
      if(this.userId!=null){error.put("userid", this.userId);}      
    } catch (JSONException e) {
      e.printStackTrace();
    }
    this.message = error.toString();
    this.hasError = true;
  }
  public int getStatusCode(){
    return statusCode;
  }

  public void setMessage(JSONObject json) {
    setMessage(json.toString());    
  }
  public void setMessage(JSONArray json) {
    setMessage(json.toString());    
  }
  public boolean hasError(){
    return hasError;
  }
  
  public void print(HttpServletResponse response) throws IOException{
    response.setContentType(getMimeType());
    byte[] msg = getMessage().getBytes();
    response.setHeader("Content-Length", String.valueOf(msg.length));
    response.getOutputStream().write(msg);
    response.flushBuffer();
  }
  public void printNE(HttpServletResponse response) throws IOException{
    try {
      print(response);
    } catch (Exception e1) {
    
    }
  }
  
  public static class JSONResponseException extends Exception {
    private static final long serialVersionUID = 1L;

    String result = null;

    public JSONResponseException(JSONResponse r) {

      this.result = r.getMessage();
    }

 



    public JSONResponseException(String errorMessage,int statusCode) {
      JSONResponse r = new JSONResponse();
      r.setErrorMessage(errorMessage, statusCode);
      result = r.getMessage();
    }

    public String getResult() {
      return result;
    }

    public String getMessage() {
      return result;
    }
  }

  public void setException(JSONResponseException e) {
    this.hasError = true;
    this.message = e.getMessage();    
  }

  public void setUserId(String id) {
    this.userId = id;    
  }
}
